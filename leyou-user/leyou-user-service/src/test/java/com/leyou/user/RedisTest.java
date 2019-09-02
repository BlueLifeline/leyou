package com.leyou.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LyUserService.class)
public class RedisTest {
    
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    @Test
//    public void testRedis() {
//        // 存储数据
//        this.redisTemplate.opsForValue().set("key1", "value1");
//        // 获取数据
//        String val = this.redisTemplate.opsForValue().get("key1");
//        System.out.println("val = " + val);
//    }
//
//    @Test
//    public void testRedis2() {
//        // 存储数据，并指定剩余生命时间,10s
//        this.redisTemplate.opsForValue().set("key2", "value2",
//                10, TimeUnit.SECONDS);
//    }
    
}
