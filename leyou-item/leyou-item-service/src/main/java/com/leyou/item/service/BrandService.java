package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author: 98050
 * Time: 2018-08-07 19:16
 * Feature: 分类的业务层
 */
@Service
public class BrandService{

    @Autowired
    private BrandMapper brandMapper;

    //---------------------------------- 查询 --------------------------------------------------------

    /**
     * 分页查询
     * @param page  当前页码
     * @param rows  每页查询条数
     * @param sortBy  排序字段
     * @param desc  排序方式
     * @param key  条件查询字段
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //1、分页
        PageHelper.startPage(page, rows);

        //2、过滤：拼接查询条件
        Example example = new Example(Brand.class);
        //当key不为空
        if(StringUtils.isNotBlank(key)){
            //where name like %key% or letter = key  :支持两种搜索方式：按名字或首字母
            example.createCriteria().orLike("name", "%"+key+"%").orEqualTo("letter", key.toUpperCase());
        }
        //3、排序：拼接排序语句
        if(StringUtils.isNotBlank(sortBy)){
            //升序或降序。注意：DESC和ASC前有个空格。
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }

        //4、执行查询
        List<Brand> list = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new MyRuntimeException(ExceptionEnum.BRAND_NOT_FIND);
        }

        //5、创建pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(list);

        //6、返回分页结果
        PageResult<Brand> pageResult = new PageResult<>();
        pageResult.setItems(list);
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setTotalPage(pageInfo.getPages());

        return pageResult;
    }

    /**
     * 批量查询：批量查询品牌
     * @param ids
     * @return
     */
    public List<Brand> queryBrandByBrandIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }



    //-------------------------------------- 新增 ---------------------------------------------------

    /**
     * 新增品牌
     * @param brand
     * @param categories
     */
    public void saveBrand(Brand brand, List<Long> categories) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new MyRuntimeException(ExceptionEnum.BRAND_NOT_SAVE);
        }
        //新增中间表数据
        for(Long catagory : categories){
           count = brandMapper.savaBrand(catagory, brand.getId());
            if(count != 1){
                throw new MyRuntimeException(ExceptionEnum.CATEGORY_BRAND_NOT_SAVE);
            }
        }
    }

}
