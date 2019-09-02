package com.leyou.goods.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 商品分类查询
 */
@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi{

}