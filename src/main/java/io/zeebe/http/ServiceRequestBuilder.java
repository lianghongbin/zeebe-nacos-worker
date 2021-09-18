package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.Resource;

/**
 * @author eric.liang
 * @date 9/17/21
 */
public class ServiceRequestBuilder implements ServiceRequest.Builder{

    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestBuilder.class);
    private String name;
    private Method method;
    private String serviceUri;
    private String variables;
    private ActivatedJob job;

    @Resource
    private ZeebeServiceProperties zeebeServiceProperties;
    @Resource
    private DiscoveryClient nacosDiscoveryClient;

    public ServiceRequestBuilder(){
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
     * 构建ServiceRequest
     * @return ServiceRequest
     * @throws ZeebeNacosWorkerException 执行异常
     */
    @Override
    public ServiceRequest build() throws ZeebeNacosWorkerException {

        if (job == null) {
            if (name == null || name.trim().length() == 0) {
                logger.error("微服务名称为空");
                throw new ZeebeNacosWorkerException("微服务名称为空");
            }

            return new ImmutableServiceRequest(this);
        }

        String m;
        this.name = job.getCustomHeaders().get(zeebeServiceProperties.getService().getName());
        String uriKey = job.getCustomHeaders().get(zeebeServiceProperties.getService().getSuffix());
        this.variables = job.getVariables();
        if (uriKey == null || uriKey.isBlank()) {
            return new ImmutableServiceRequest(this);
        }

        try {
            ServiceInstance instance = nacosDiscoveryClient.getInstances(name).get(0);
            String full = instance.getMetadata().get(uriKey);
            String[] contents = full.split(";");

            if (contents.length >= 2) {
                serviceUri = contents[0];
                m = contents[1];
            } else {
                serviceUri = contents[0];
                m = "get";
            }
        }catch (Exception e) {
            logger.error("没有发现微服务 {} 实例", name);
            throw  new ZeebeNacosWorkerException("没有发现微服务 " + name + "实例");
        }

        try {
            method = Method.valueOf(m.toUpperCase());
        }catch (Exception e) {
            logger.error("获取微服务请求方法错误, method={}", method);
            throw new ZeebeNacosWorkerException("获取微服务请求方法错误,method=" + method);
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
