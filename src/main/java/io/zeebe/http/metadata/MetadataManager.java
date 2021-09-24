package io.zeebe.http.metadata;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author eric.liang
 * @date 9/24/21
 */
@Component
public class MetadataManager {

    @Resource
    private DiscoveryClientAware scanner;

    public void put(String k, String v) {
        Optional<List<ServiceInstance>> optionalInstances = scanner.findInstance();
        optionalInstances.ifPresent( instances -> {
            instances.forEach(instance -> {
                instance.getMetadata().put(k, v);
            });
        });
    }
}
