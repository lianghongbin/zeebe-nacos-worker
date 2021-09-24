package io.zeebe.http;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author eric.liang
 * @date 9/18/21
 */
@Component
@ConfigurationProperties(prefix = "zeebe.nacos")
public class ZeebeServiceProperties {

    @NestedConfigurationProperty
    private Service service = new Service();

    public static class Service {
        private String name;
        private String suffix;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
