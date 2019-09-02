package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 商品信息查询
 */

@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {

}
