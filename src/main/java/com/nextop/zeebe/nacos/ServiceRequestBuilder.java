package com.nextop.zeebe.nacos;

import com.nextop.zeebe.nacos.config.ServiceMetaProperties;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author eric.liang
 * @date 9/17/21
 */
@Slf4j
@Service
public class ServiceRequestBuilder implements ServiceRequest.Builder {

    private String name;
    private Method method;
    private String serviceUri;
    private String variables;
    private ActivatedJob job;

    @Resource
    private ServiceMetaProperties serviceMetaProperties;
    @Resource
    private DiscoveryClient nacosDiscoveryClient;

    public ServiceRequestBuilder() {
        this.method = Method.GET;
        this.serviceUri = "/";
        this.variables = "{}";
    }

    public ServiceRequestBuilder(String name) {
        this();
        this.name = name;
    }

    public ServiceRequestBuilder(ActivatedJob job) {
        this.job = job;
    }

    @Override
    public ServiceRequest.Builder GET() {
        return method(Method.GET);
    }

    @Override
    public ServiceRequest.Builder POST(String variables) {
        return method(Method.POST, variables);
    }

    @Override
    public ServiceRequest.Builder DELETE() {
        return method(Method.DELETE);
    }

    @Override
    public ServiceRequest.Builder PUT(String variables) {
        return method(Method.PUT, variables);
    }

    @Override
    public ServiceRequest.Builder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ServiceRequest.Builder method(Method method) {
        return method(method, null);
    }

    @Override
    public ServiceRequest.Builder method(Method method, String variables) {
        this.method = method;
        this.variables = variables;
        return this;
    }

    @Override
    public ServiceRequest.Builder serviceUri(String serviceUri) {
        if (serviceUri == null || serviceUri.isBlank()) {
            return this;
        }

        this.serviceUri = serviceUri.startsWith("/") ? serviceUri : "/" + serviceUri;
        return this;
    }

    @Override
    public ServiceRequest.Builder variables(String variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public ServiceRequest.Builder job(ActivatedJob job) {
        this.job = job;
        return this;
    }

    /**
     * ??????ServiceRequest
     *
     * @return ServiceRequest
     * @throws WorkerException ????????????
     */
    @Override
    public ServiceRequest build() throws WorkerException {

        if (job == null) {
            if (name == null || name.trim().length() == 0) {
                log.warn("?????????????????????");
                throw new WorkerException("?????????????????????");
            }

            return new ImmutableServiceRequest(this);
        }

        String m;
        this.name = job.getCustomHeaders().get(serviceMetaProperties.getService().getName());
        String uriKey = job.getCustomHeaders().get(serviceMetaProperties.getService().getSuffix());
        this.variables = job.getVariables();
        if (uriKey == null || uriKey.isBlank()) {
            return new ImmutableServiceRequest(this);
        }

        ServiceInstance instance;
        try {
            List<ServiceInstance> instances = nacosDiscoveryClient.getInstances(name);
            instance = instances.get(0);
        } catch (Exception e) {
            log.warn("????????????????????? {} ??????", name);
            throw new WorkerException("????????????????????? " + name + " ??????");
        }
        String full = instance.getMetadata().get(uriKey);
        if (full == null) {
            throw new WorkerException("?????????????????????????????? " + uriKey);
        }

        String[] contents = full.split(";");

        if (contents.length >= 2) {
            serviceUri = contents[0];
            m = contents[1];
        } else {
            serviceUri = contents[0];
            m = "get";
        }

        try {
            method = Method.valueOf(m.toUpperCase());
        } catch (Exception e) {
            log.error("?????????????????????????????????, method={}", method);
            throw new WorkerException("?????????????????????????????????,method=" + method);
        }

        return new ImmutableServiceRequest(this);
    }

    String name() {
        return name;
    }

    Method method() {
        return method;
    }

    String serviceUri() {
        return serviceUri;
    }

    String variables() {
        return variables;
    }
}
