package io.zeebe.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author eric.liang
 * @date 9/16/21
 */
@Component
public class MetadataKeyword {


    public static String serviceNameKey;
    public static String serviceUrlSuffix;

    @Value( "${service.name.key}" )
    public void setServiceNameKey(String key) {
        serviceNameKey = key;
    }

    @Value( "${service.urlSuffix.key}")
    public void setServiceUrlSuffix(String key) {
        serviceUrlSuffix = key;
    }
}
