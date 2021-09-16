package io.zeebe.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author eric.liang
 * @date 9/16/21
 */
@Component
public class MicroserviceConfig {


    public static String serviceNameKey;

    @Value( "${service.name.key}" )
    public void setServiceNameKey(String key) {
        serviceNameKey = key;
    }
}
