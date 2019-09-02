package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.parameter.pojo.SpuQueryByPageParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    /**
     * 分页查询spuBo的数据，并排序。
     * @param spuParams
     * @return
     */
    public PageResult<SpuBo> querySpuByPageAndSort(SpuQueryByPageParameter spuParams) {
        //分页。最多查询100条
        PageHelper.startPage(spuParams.getPage(), Math.min(spuParams.getRows(),100));

        //筛选
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //拼接条件：是否下架
        if(spuParams.getSaleable() != null){
            criteria.orEqualTo("saleable", spuParams.getSaleable());
        }
        //按标题模糊查询
        if(StringUtils.isNotBlank(spuParams.getKey())){
            criteria.andLike("title", "%"+spuParams.getKey()+"%");
        }

        //排序
        if(StringUtils.isNotBlank(spuParams.getSortBy())){
            example.setOrderByClause(spuParams.getSortBy() + ( spuParams.getDesc() ? " DESC" : " ASC"));
        }

        //查找
        List<Spu> list = spuMapper.selectByExample(example);

        if(CollectionUtils.isEmpty(list)){
            throw new MyRuntimeException(ExceptionEnum.SPU_INFO_NOT_FOUNT);
        }

        //封装信息
        PageInfo<Spu> pageInfo = new PageInfo<>(list);

        //将spu的信息封装SpuBo中
        List<SpuBo> blist = list.stream().map(spu -> {

            SpuBo spuBo = new SpuBo();
            //属性拷贝
            BeanUtils.copyProperties(spu, spuBo);
            //Arrays.asList()：将参数包装成List对象
            List<Long> alist = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
            //批量查询：查询商品分类名称
            List<String> names = categoryService.queryNameByIds(alist);
            //3.拼接名字,并存入
            spuBo.setCname(StringUtils.join(names,"/"));
            //4.查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), blist);
    }

    /**
     * 根据spu_id查询SpuDetail
     * @param id
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id查询sku
     * @param id
     * @return
     */
    public List<Sku> querySkuBySpuId(Long id) {
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",id);
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        for (Sku sku : skuList){
            Example temp = new Example(Stock.class);
            temp.createCriteria().andEqualTo("skuId", sku.getId());
            Stock stock = this.stockMapper.selectByExample(temp).get(0);
            sku.setStock(stock.getStock());
        }
        return skuList;
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    public SpuBo queryGoodsById(Long id) {
        /**
         * 第一页所需信息如下：
         * 1.商品的分类信息、所属品牌、商品标题、商品卖点（子标题）
         * 2.商品的包装清单、售后服务
         */
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spu.getId());

        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",spu.getId());
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        List<Long> skuIdList = new ArrayList<>();
        for (Sku sku : skuList){
            System.out.println(sku);
            skuIdList.add(sku.getId());
        }

        List<Stock> stocks = this.stockMapper.selectByIdList(skuIdList);

        for (Sku sku : skuList){
            for (Stock stock : stocks){
                if (sku.getId().equals(stock.getSkuId())){
                    sku.setStock(stock.getStock());
                }
            }
        }

        SpuBo spuBo = new SpuBo();
        //属性拷贝
        BeanUtils.copyProperties(spu, spuBo);
        //Arrays.asList()：将参数包装成List对象
        List<Long> alist = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        //批量查询：查询商品分类名称
        List<String> names = categoryService.queryNameByIds(alist);
        //3.拼接名字,并存入
        spuBo.setCname(StringUtils.join(names,"/"));
        //4.查询品牌名称
        Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
        spuBo.setBname(brand.getName());

        spuBo.setSpuDetail(spuDetail);
        spuBo.setSkus(skuList);
        return spuBo;
    }
    
    /**
     *根据skuId查找sku
     * @param skuId
     * @return
     */
    public Sku querySkuById(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
