package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "ly.upload")
/**
 * 获取application.yml文件中的配置的属性
 */
public class UploadProperties {

    private String baseUrl;
    private List<String> allowImageTypes;

}
