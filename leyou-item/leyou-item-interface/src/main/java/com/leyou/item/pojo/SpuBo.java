package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

/**
 * 拓展Spu类，增加cname，bname， spuDetil，skus属性
 */
@Data
public class SpuBo extends  Spu {
    /**
     * 商品分类名称
     */
    @Transient
    private String cname;
    /**
     * 品牌名称
     */
    @Transient
    private String bname;

    /**
     * 商品详情
     */
    @Transient
    private SpuDetail spuDetail;

    /**
     * sku记录列表
     */
    @Transient
    private List<Sku> skus;
}
