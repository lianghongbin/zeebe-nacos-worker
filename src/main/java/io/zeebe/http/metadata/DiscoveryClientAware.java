package io.zeebe.http.metadata;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import io.zeebe.http.ServiceRequest;
import io.zeebe.http.ZeebeServiceProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author eric.liang
 * @date 9/24/21
 */
@Component
public class DiscoveryClientAware implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Value("${spring.application.name}")
    private String name;

    public Optional<List<ServiceInstance>> findInstance() {
        DiscoveryClient client;
        try {
            client = applicationContext.getBean(NacosDiscoveryClient.class);
        }catch (BeansException e) {
            return Optional.empty();
        }

        List<ServiceInstance> instances = client.getInstances(name);
        return Optional.ofNullable(instances);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
