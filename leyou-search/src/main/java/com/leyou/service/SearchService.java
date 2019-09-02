package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.bo.SearchRequest;
import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecClient;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.pojo.*;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import com.leyou.vo.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Elasticsearch的读和写
 */
@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecClient specClient;

    private ObjectMapper mapper = new ObjectMapper();

    //----------------------------- 写数据到Elasticsearch中 -----------------------------------

    /**
     * 创建需要从msyql数据库写入到Elasticsearch中的数据
     * @param spu
     * @return
     * @throws IOException
     */
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        //1.查询商品分类名称
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3())).getBody();
        //2.查询sku
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());
        //3.查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());

        //4.处理sku,仅封装id，价格、标题、图片、并获得价格集合
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuLists = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            //取第一张图片
            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuLists.add(skuMap);
        });
        //提取公共属性
        List<Map<String,Object>> genericSpecs = mapper.readValue(spuDetail.getSpecifications(), new TypeReference<List<Map<String,Object>>>(){});

        //过滤规格模板，把所有可搜索的信息保存到Map中
        Map<String,Object> specMap = new HashMap<>();

        String searchable = "searchable";
        String v = "v";
        String k = "k";
        String options = "options";

        genericSpecs.forEach(m -> {
            List<Map<String, Object>> params = (List<Map<String, Object>>) m.get("params");
            params.forEach(spe ->{
                if ((boolean)spe.get(searchable)){
                    if (spe.get(v) != null){
                        specMap.put(spe.get(k).toString(), spe.get(v));
                    }else if (spe.get(options) != null){
                        specMap.put(spe.get(k).toString(), spe.get(options));
                    }
                }
            });
        });
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        goods.setSkus(mapper.writeValueAsString(skuLists));
        goods.setSpecs(specMap);

        return goods;
    }

    //------------------------------------------ 查询 -----------------------------------------

    /**
     * 自定义查询
     * @param searchRequest
     * @return
     */
    public SearchResult<Goods> search(SearchRequest searchRequest) {

        String key = searchRequest.getKey();

        //判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if(StringUtils.isBlank(key)){
            return null;
        }

        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //1.对关键字key创建全文检索查询条件。使用match查询，分词之间使用and
        //queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));

        QueryBuilder basicQuery = buildBasicQueryWithFilter(searchRequest);
        queryBuilder.withQuery(basicQuery);

        //2.通过sourceFilter设置返回的结果字段，只需要id,skus,subTitle。即使用_source:["id","skus","subTitle"]
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //3、创建排序(单独写一个函数)
        searchWithPageAndSort(queryBuilder, searchRequest);

        //4、聚合
        //商品分类聚合名称
        String categoryAggName = "category";
        //品牌聚合名称
        String brandAggName = "brand";

        //4.1、对商品分类进行聚合。桶类型为terms，按cid3字段分组。
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //4.2、对品牌进行聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //4.3、查询、获取结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        //5、解析查询结果
        //5.1、分页信息
        Long total = pageInfo.getTotalElements();  //总记录数
        int totalPage = pageInfo.getTotalPages();  //总页数

        //5.2、商品分类的聚合结果
        List<Category> categories = getCategoryAggResult(pageInfo.getAggregation(categoryAggName));
        //5.3 品牌的聚合结果
        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandAggName));
        //5.4 spec筛选条件
        List<Map<String,Object>> specs = null;
        if(categories.size() == 1){
            specs = getSpec(categories.get(0).getId(), basicQuery);
        }

        //6、封装结果，返回
        return new SearchResult(total, totalPage,
                pageInfo.getContent(), categories, brands, specs );
    }

    /**
     * 构建带过滤条件的基本查询
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        //过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //整理过滤条件
        Map<String,String> filter = searchRequest.getFilter();
        for (Map.Entry<String,String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";
            if (StringUtils.isNotBlank(key)) {
                if (value.matches(regex)) {
                    Double[] nums = NumberUtils.searchNumber(value, regex);
                    //数值类型进行范围查询   lt:小于  gte:大于等于
                    filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
                } else {
                    //商品分类和品牌要特殊处理
                    if (key != "cid3" && key != "brandId") {
                        key = "specs." + key + ".keyword";
                    }
                    //字符串类型，进行term查询
                    filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
                }
            } else {
                break;
            }
        }
        //添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    /**
     * 构建排序和分页条件
     * @param queryBuilder
     * @param searchRequest
     */
    private void searchWithPageAndSort(NativeSearchQueryBuilder queryBuilder, SearchRequest searchRequest){

        //1、分页信息：当前页和每页大小
        int page = searchRequest.getPage();
        int size = searchRequest.getDefaultSize();

        //elasticsearch分页从0开始
        queryBuilder.withPageable(PageRequest.of(page - 1,size));

        //排序
        String sortBy = searchRequest.getSortBy();
        Boolean desc = searchRequest.getDescending();

        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
    }

    /**
     * 对商品分类聚合结果解析
     * @param aggregation
     * @return
     */
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        //根据不同数据类型将桶聚合进行向上转型
        LongTerms brandAgg = (LongTerms) aggregation;
        List<Long> cids = new ArrayList<>();
        //获取桶并遍历
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()){
            cids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据id查询分类名称
        return this.categoryClient.queryCategoryByIds(cids).getBody();
    }

    /**
     * 解析品牌聚合结果
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms brandAgg = (LongTerms) aggregation;
        List<Long> bids = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandAgg.getBuckets()){
            bids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据品牌id查询品牌
        return this.brandClient.queryBrandByIds(bids);
    }

    /**
     * 聚合规格参数
     * @param id
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getSpec(Long id, QueryBuilder basicQuery) {
        //不管是全局参数还是sku参数，只要是搜索参数，都根据分类id查询出来
        String specsJSONStr = this.specClient.querySpecificationByCategoryId(id).getBody();
        System.out.println("json" + specsJSONStr);

        //1、将spec的json数据反序列化成list集合
        List<Map<String,Object>> specs = null;
        specs = JsonUtils.nativeRead(specsJSONStr, new TypeReference<List<Map<String, Object>>>() {});

        Set<String> strSpec = new HashSet<>();
        Map<String, String> numericalUnits = new HashMap<>();

        String searchable = "searchable";
        String numerical = "numerical";
        String k = "k";
        String unit = "unit";

        for(Map<String,Object> spec : specs){
            List<Map<String,Object>> params = (List<Map<String,Object>>)spec.get("params");
            params.forEach(param -> {
                if((boolean)param.get(searchable)){
                    if(param.containsKey(numerical) && (boolean)param.get(numerical)){
                        numericalUnits.put(param.get(k).toString(), param.get("unit").toString());
                    }else{
                        strSpec.add(param.get(k).toString());
                    }
                }
            });
        }

        //3.聚合计算数值类型的interval
        Map<String,Double> numericalInterval = getNumberInterval(id, numericalUnits.keySet());
        return this.aggForSpec(strSpec,numericalInterval, numericalUnits, basicQuery);

    }

    /**
     * 聚合得到各个数值类型规格参数的interval（间隔数）
     * @param id
     * @param keySet 数值类型的规格参数的key
     * @return
     */
    private Map<String, Double> getNumberInterval(Long id, Set<String> keySet) {
        Map<String,Double> numbericalSpecs = new HashMap<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //不查询任何数据
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",id.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
        //添加stats类型的聚合,同时返回avg、max、min、sum、count等
        for (String key : keySet){
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }

        /**
         * 完整代码：
         * Aggregations aggregations = esTemplate.query(searchQuery, new ResultsExtractor<Map<String,Aggregation>>() {
         *             @Override
         *             public Map<String,Aggregation> extract(SearchResponse response) {
         *                 return response.getAggregations().asMap();
         *             }
         *         });
         * 当函数中只有一个方法时，可以按如下方式简写。（lambda表达式）
         * */
        Map<String,Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(),
                searchResponse -> searchResponse.getAggregations().asMap()
        );

        for (String key : keySet){
            InternalStats stats = (InternalStats) aggregationMap.get(key);
            double interval = this.getInterval(stats.getMin(),stats.getMax(),stats.getSum());
            numbericalSpecs.put(key,interval);
        }
        return numbericalSpecs;
    }

    /**
     * 获取数字类型的间隔
     * @param min
     * @param max
     * @param sum
     * @return
     */
    private double getInterval(double min, double max, Double sum) {
        //要显示6个区间
        double interval = (max - min) /6.0d;
        //判断是否是小数
        if (sum.intValue() == sum){
            //不是小数，要取整十、整百
            int length = StringUtils.substringBefore(String.valueOf(interval),".").length();
            double factor = Math.pow(10.0,length - 1);
            return Math.round(interval / factor)*factor;
        }else {
            //是小数的话就保留一位小数
            return NumberUtils.scale(interval,1);
        }
    }

    /**
     * 根据规格参数，聚合得到过滤属性值
     * @param strSpec  字符串类型规格参数
     * @param numericalInterval 数值类型规格参数和参数的间隔
     * @param numericalUnits  带单位的规格参数及单位
     * @param basicQuery  简单查询
     * @return
     */
    private List<Map<String, Object>> aggForSpec(Set<String> strSpec, Map<String, Double> numericalInterval, Map<String, String> numericalUnits, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //聚合数值类型
        /**
         * 阶梯分桶Histogram相当于：
         * GET /goods/_search
         * {
         *   "size":0,
         *   "aggs":{
         *     "entry.getKey()":{
         *       "histogram": {
         *         "field": ""specs." + entry.getKey()",
         *         "interval": entry.getValue()
         *         "min_doc_count": 1
         *       }
         *     }
         *   }
         * }
         */
        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            queryBuilder.addAggregation(AggregationBuilders.histogram(entry.getKey()).field("specs." + entry.getKey()).interval(entry.getValue()).minDocCount(1));
        }

        //聚合字符串
        for (String key : strSpec) {
            queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
        }

        //解析聚合结果
        Map<String, Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(), SearchResponse::getAggregations).asMap();

        //解析数值类型，将数值类型的key和筛选范围按kv格式存入
        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            Map<String, Object> spec = new HashMap<>();
            String key = entry.getKey();
            spec.put("k", key);
            spec.put("unit", numericalUnits.get(key));
            //获取聚合结果
            InternalHistogram histogram = (InternalHistogram) aggregationMap.get(key);
            //遍历聚合结果
            spec.put("options", histogram.getBuckets().stream().map(bucket -> {
                //分阶数值桶的key为该阶层初始值
                Double begin = (Double) bucket.getKey();
                Double end = begin + numericalInterval.get(key);
                //对begin和end取整
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)) {
                    //确实是整数，直接取整
                    return begin.intValue() + "-" + end.intValue();
                } else {
                    //小数，取2位小数
                    begin = NumberUtils.scale(begin, 2);
                    end = NumberUtils.scale(end, 2);
                    return begin + "-" + end;
                }
            }));
            specs.add(spec);
        }

        //解析字符串类型
        strSpec.forEach(key -> {
            Map<String, Object> spec = new HashMap<>();
            spec.put("k", key);
            //获取聚合结果
            StringTerms terms = (StringTerms) aggregationMap.get(key);
            spec.put("options", terms.getBuckets().stream().map((Function<StringTerms.Bucket, Object>) StringTerms.Bucket::getKeyAsString));
            specs.add(spec);
        });
        return specs;
    }
}
