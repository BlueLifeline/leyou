package com.leyou.order.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "leyou.worker")
@Component
@Data
public class IdWorkerProperties {
    
    private long workerId;
    
    private long dataCenterId;  //序列号
    
}
