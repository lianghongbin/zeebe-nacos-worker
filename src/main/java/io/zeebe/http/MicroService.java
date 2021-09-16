package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jeffrey
 */
public class MicroService {

    private String name;
    private Method method;
    private String serviceUri;
    private String variables;
    private final ActivatedJob job;
    private final DiscoveryClient discoveryClient;

    public MicroService(ActivatedJob job, DiscoveryClient discoveryClient) {
        this.job = job;
        this.discoveryClient = discoveryClient;
    }

    public MicroService parse() throws InstanceNotExistException {
        this.name = job.getCustomHeaders().get(MetadataKeyword.serviceNameKey);
        this.variables = job.getVariables();

        List<ServiceInstance> instances = discoveryClient.getInstances(name);
        if (instances == null || instances.isEmpty()) {
            throw new InstanceNotExistException("没有找到服务 "+ name+" 的实例！");
        }

        String uri = instances.get(0).getMetadata().get(job.getCustomHeaders().get(MetadataKeyword.serviceUrlSuffix));
        serviceUri = uri==null ? "" : uri;

        return this;
    }
}
