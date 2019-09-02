package com.leyou.goods.client;

import com.leyou.item.api.SpecsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface SpecClient extends SpecsApi {
}
