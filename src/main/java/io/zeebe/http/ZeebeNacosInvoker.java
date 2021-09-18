package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author jeffrey
 */
@EnableDiscoveryClient
@EnableZeebeClient
@Service
public class ZeebeNacosInvoker {

    private static final Logger logger = LoggerFactory.getLogger(ZeebeNacosInvoker.class);
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ServiceRequest.Builder serviceRequestBuilder;

    @ZeebeWorker
    public void handle(final JobClient client, final ActivatedJob job) throws Exception {
        ServiceRequest request;
        try {
            request = serviceRequestBuilder.job(job).build();
        }catch (Exception e) {
            logger.error("微服务参数设置错误: {}", e.toString());
            client.newFailCommand(job.getKey());
            return;
        }

        try {
            switch (request.method()) {
                case GET:
                    String result = restTemplate.getForObject(request.url(), String.class, request.variables());
                    System.out.println(result);
                    break;

                case POST:
                    restTemplate.postForObject(request.url(), request.variables(), String.class, request.variables());
                    break;

                case PUT:
                    restTemplate.put(request.url(), request.variables(), String.class, request.variables());
                    break;

                default:
                    restTemplate.delete(request.url(), request.variables(), String.class, request.variables());
            }

            client.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
        }catch (Exception e) {
            logger.error("执行微服务出错: {}", e.toString());
            client.newFailCommand(job.getKey());
        }
    }
}
