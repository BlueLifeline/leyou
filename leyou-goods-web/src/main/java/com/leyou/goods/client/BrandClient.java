package com.leyou.goods.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 品牌查询
 */

@FeignClient (value = "item-service")
public interface BrandClient extends BrandApi {

}
