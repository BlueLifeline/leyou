package com.leyou.gateway.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: 98050
 * @Time: 2018-10-24 16:21
 * @Feature: 登录拦截器
 */
@Component
public class LoginFilter extends ZuulFilter {
    
    @Autowired
    private JwtProperties properties;
    
    @Autowired
    private FilterProperties filterProperties;
    
    /**
     * 定义拦截器类型：pre、route、post、error
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }
    
    /**
     * 定义优先级
     * @return
     */
    @Override
    public int filterOrder() {
        return 5;
    }
    
    /**
     * 定义是否执行拦截器：即执行run()方法。
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //1.获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //2.获取request
        HttpServletRequest request = context.getRequest();
        //3.获取路径
        String requestUri = request.getRequestURI();
        System.out.println(requestUri);
        //4.判断白名单
        return !isAllowPath(requestUri);
        
    }
    
    /**
     * 判断是否拦截
     * @param requestUri
     * @return
     */
    private boolean isAllowPath(String requestUri) {
        //1.定义一个标记
        boolean flag = false;
        //2.遍历允许访问的路径
        for (String path : this.filterProperties.getAllowPaths()){
            if (requestUri.startsWith(path)){
                flag = true;
                break;
            }
        }
        return flag;
    }
    
    @Override
    public Object run() throws ZuulException {
        //1.获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //2.获取request
        HttpServletRequest request = context.getRequest();
        //3.获取token
        String token = CookieUtils.getCookieValue(request,this.properties.getCookieName());
    
        //4.校验
        try{
            //4.1 校验通过，放行
            JwtUtils.getInfoFromToken(token,this.properties.getPublicKey());
        }catch (Exception e){
            //4.2 校验不通过，返回403
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        }
        return null;
    }
}
