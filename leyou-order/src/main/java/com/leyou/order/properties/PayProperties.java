package com.leyou.order.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author: 98050
 * @create: 2018-10-27 11:38
 **/
@ConfigurationProperties(prefix = "leyou.pay")
@Component
@Data
public class PayProperties {
    
    private String appId; //公众账号ID

    private String mchId; //商户号

    private String key; //生成签名的密钥

    private int connectTimeoutMs; //连接超时时间

    private int readTimeoutMs; //读取超时时间
}
