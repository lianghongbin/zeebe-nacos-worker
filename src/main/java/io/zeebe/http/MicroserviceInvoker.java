package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MicroserviceInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MicroserviceInvoker.class);
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ServiceRequest.Builder serviceRequestBuilder;

    @ZeebeWorker
    public void handle(final JobClient client, final ActivatedJob job) {

        ServiceRequest request;
        try {
            request = serviceRequestBuilder.job(job).build();
        }catch (Exception e) {
            logger.warn("微服务参数设置异常: {}", e.toString());
            client.newFailCommand(job.getKey());
            return;
        }

        logger.info(" invoke " + request.url());
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
                    logger.info(result);
            }

            client.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
        }catch (Exception e) {
            logger.error("执行微服务异常: {}", e.toString());
            client.newFailCommand(job.getKey());
        }
    }
}
