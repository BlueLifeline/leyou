package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 用户校验
 */
@FeignClient(value = "user-service")
public interface UserClient extends UserApi {
}
