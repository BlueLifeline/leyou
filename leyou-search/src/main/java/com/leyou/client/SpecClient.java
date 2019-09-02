package com.leyou.client;

import com.leyou.item.api.SpecsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface SpecClient extends SpecsApi {
}
