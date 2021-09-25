package io.zeebe.http;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import io.zeebe.http.metadata.AnnotationScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public NacosDiscoveryProperties discoveryProperties(ApplicationContext applicationcontext) {
        NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
        AnnotationScanner scanner = new AnnotationScanner(applicationcontext, properties);
        scanner.scan();
        properties.getMetadata().put("zeebe.nacos.metadata.setting", "true");
        return properties;
    }
}
