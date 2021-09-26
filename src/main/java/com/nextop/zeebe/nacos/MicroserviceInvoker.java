package com.nextop.zeebe.nacos;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author jeffrey
 */
@Slf4j
@EnableDiscoveryClient
@EnableZeebeClient
@Service
public class MicroserviceInvoker {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ServiceRequest.Builder serviceRequestBuilder;

    @ZeebeWorker
    public void handle(final JobClient client, final ActivatedJob job) {
        log.debug(job.toString());
        ServiceRequest request;
        try {
            request = serviceRequestBuilder.job(job).build();
        }catch (Exception e) {
            log.warn("微服务参数设置异常: {}", e.toString());
            return;
        }

        log.info(" invoke " + request.url());
        try {
            switch (request.method()) {
                case DELETE:
                    restTemplate.delete(request.url(), request.variables(), String.class, request.variables());
                    break;

                case POST:
                    restTemplate.postForObject(request.url(), request.variables(), String.class, request.variables());
                    break;

                case PUT:
                    restTemplate.put(request.url(), request.variables(), String.class, request.variables());
                    break;

                default:
                    String result = restTemplate.getForObject(request.url(), String.class, request.variables());
                    log.info(result);
            }

            client.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
        }catch (Exception e) {
            log.error("执行微服务异常: {}", e.toString());
            client.newFailCommand(job.getKey());
        }
    }
}
