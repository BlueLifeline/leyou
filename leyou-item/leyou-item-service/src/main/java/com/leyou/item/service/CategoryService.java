package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 按parent_id查询商品类别
     * @param pid
     * @return
     * @throws Exception
     */
    public List<Category> queryCategoryByPid(Long pid){

        /**
         * 查询条件：mapper的select(T record)方法
         *  会将record实例中的非空属性作为查询条件，自动生成sql语句
         */
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        //CollectionUtils.isEmpty方法判断集合是否为空。如果为空则抛出异常给统一异常处理模块处理
        if(CollectionUtils.isEmpty(list)){
            throw new MyRuntimeException(ExceptionEnum.CATEGORY_IS_NULL);
        }
        return list;
    }


    /**
     * 批量查询：根据id批量查询name
     * @param ids
     * @return
     */
    public List<String> queryNameByIds(List<Long> ids) {

        /**
         * selectByIdList()方法：
         *  使用该方法Mapper需要先继承SelectByIdListMapper<T, PK>类
         *  根据主键字符串进行查询，类中只有存在一个带有@Id注解的字段。T是元素的类型，PK是主键的类型
         */
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        //使用java 8 的lambda表达式，简化程序
        List<String> names = categories.stream().map(Category :: getName).collect(Collectors.toList());
        return names;
    }

    /**
     * 批量查询：根据id，批量查询分类信息。
     * @param ids
     * @return
     */
    public List<Category> queryCategoryByIds(List<Long> ids) {
        /**
         * 批量查询一般使用selectByIdList()方法
         */
        return this.categoryMapper.selectByIdList(ids);
    }

    /**
     * 根据cid3查询其所有父层级分类
     * @param id
     * @return
    */
    public List<Category> queryAllCategoryLevelByCid3(Long id) {
        List<Category> list = new ArrayList<>();
        Category category = categoryMapper.selectByPrimaryKey(id);
        while(category.getParentId() != 0 ){
            list.add(category);
            category = categoryMapper.selectByPrimaryKey(category.getParentId());
        }
        list.add(category);
        return list;
    }
}
