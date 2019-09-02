package com.leyou.user.service;

import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    static final String KEY_PREFIX = "user:code:phone:";
    
    static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    /**
     * 检查用户或号码是否是唯一的
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type){
            case 1 :
                user.setUsername(data);
                break;
            case 2 :
                user.setPhone(data);
                break;
            default:
                return null;
        }
        //selectCount:查找并返回聚合结果
        return this.userMapper.selectCount(user) == 0;
    }
    
    /**
     * 发送验证码（无短信功能，只放到redis）
     * @param phone
     * @return
     */
    public Boolean sendVerifyCode(String phone) {
    
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        System.out.println("验证码：" + code);
        // 将code存入redis
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 1, TimeUnit.MINUTES);
        return true;
    }
    
    /**
     * 注册
     * @param user
     * @param code
     * @return
     */
    public Boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        //1.从redis中取出验证码
        String codeCache = this.redisTemplate.opsForValue().get(key);
        //2.检查验证码是否正确
        if(!codeCache.equals(code)){
            //不正确，返回
            return false;
        }
        user.setId(null);
        user.setCreated(new Date());
        //3.密码加密
        String encodePassword = CodecUtils.passwordBcryptEncode(user.getUsername().trim(), user.getPassword().trim());
        System.out.println(encodePassword);
        user.setPassword(encodePassword);
        //4.写入数据库
        boolean result = this.userMapper.insertSelective(user) == 1;
        System.out.println(result);
        //5.如果注册成功，则删掉redis中的code
        if (result){
            try{
                this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
            }catch (Exception e){
                logger.error("删除缓存验证码失败，code:{}",code,e);
            }
        }
        return result;
    }
    
    /**
     * 登录。校验用户名和密码登录。
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        //校验用户名
        if(user == null){
            return null;
        }
        //校验密码
        boolean result = CodecUtils.passwordConfirm(username+password, user.getPassword());
        if(!result){
            return null;
        }
        
        return user;
    }
}
