import com.leyou.LySearchService;
import com.leyou.client.GoodsClient;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuBo;
import com.leyou.pojo.Goods;
import com.leyou.repository.GoodsRepository;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchService.class)
public class ElasticsearchTest {

//    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
//
//    @Autowired
//    private GoodsClient goodsClient;

//    @Autowired
//    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

//    @Test
//    public void createIndex(){
//        // 创建索引
//        this.elasticsearchTemplate.createIndex(Goods.class);
//        // 配置映射
//        this.elasticsearchTemplate.putMapping(Goods.class);
//    }
//
//    @Test
//    public void loadData() throws IOException {
//
//        List<SpuBo> list = new ArrayList<>();
//
//        int page = 1;
//        int row = 100;
//        int size;
//
//        do {
//            //分页查询数据
//            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page, row, null, true, null, true);
//            List<SpuBo> spus = result.getItems();
//            size = spus.size();
//            page ++;
//            list.addAll(spus);
//        }while (size == 100);
//
//        //创建Goods集合
//        List<Goods> goodsList = new ArrayList<>();
//
//        //遍历spu
//        for (SpuBo spu : list) {
//            try {
//                System.out.println("spu id" + spu.getId());
//                Goods goods = this.searchService.buildGoods(spu);
//                goodsList.add(goods);
//            } catch (IOException e) {
//                System.out.println("查询失败：" + spu.getId());
//            }
//        }
//        this.goodsRepository.saveAll(goodsList);
//    }
//
//    @Test
//    public void testAgg(){
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        // 不查询任何结果
//        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",76)).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
//        Page<Goods> goodsPage = this.goodsRepository.search(queryBuilder.build());
//        goodsPage.forEach(System.out::println);
//    }
//
//    @Test
//    public void testDelete(){
//        this.goodsRepository.deleteById((long) 2);
//    }
//
//    @Test
//    public void mytest(){
//
//        List<SpuBo> list = new ArrayList<>();
//
//        int page = 1;
//        int row = 10;
//        int size;
//
//        do {
//            //分页查询数据
//            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page, row, null, true, null, true);
//            List<SpuBo> spus = result.getItems();
//            size = spus.size();
//            page ++;
//            list.addAll(spus);
//        }while (size == 100);
//
//        //遍历spu
//        for (SpuBo spu : list) {
//            try {
//                System.out.println("spu id" + spu.getId());
//                Goods goods = this.searchService.buildGoods(spu);
//                System.out.println(goods.getSkus());
//                System.out.println(goods.getPrice());
//                System.out.println(goods.getSpecs());
//                System.out.println("----------------------------------------------------");
//            } catch (IOException e) {
//                System.out.println("查询失败：" + spu.getId());
//            }
//        }
//    }

//    @Test
//    public void ElasticTest(){
//
//        QueryBuilder queryBuilder = QueryBuilders.matchQuery("name","xiong").operator(Operator.AND);
//
//        Iterable<Goods> iterable = goodsRepository.search(queryBuilder);
//
//        Iterator<Goods> it = iterable.iterator();
//
//        while(it.hasNext()){
//            Goods goods = it.next();
//            System.out.println(goods.getSubTitle());
//        }
//    }

}
