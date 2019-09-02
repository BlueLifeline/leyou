package com.leyou.item.mapper;


import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand, Long> {

    /**
     * 新增商品分类和品牌中间表数据
     * @param cid 分类id
     * @param bid 品牌id
     */
    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES(#{cid}, #{bid})")
    public int savaBrand(@Param("cid") Long cid, @Param("bid") Long bid);


}
