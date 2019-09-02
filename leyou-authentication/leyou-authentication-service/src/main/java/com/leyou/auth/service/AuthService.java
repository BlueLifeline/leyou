package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private JwtProperties properties;
    
    @Autowired
    private UserClient userClient;
    
    public String authentication(String username, String password) {
        try {
            //1.调用微服务查询用户信息
            User user = this.userClient.queryUser(username,password);
            System.out.println(user);
            //2.查询结果为空，则直接返回null
            if (user == null){
                return null;
            }
            //3.查询结果不为空，则生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                        properties.getPrivateKey(), properties.getExpire());
            System.out.println(token);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
