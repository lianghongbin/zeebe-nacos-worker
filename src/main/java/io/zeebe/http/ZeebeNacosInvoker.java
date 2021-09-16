package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author jeffrey
 */

@EnableDiscoveryClient
@EnableZeebeClient
@Service
public class ZeebeNacosInvoker {

    @Resource
    private DiscoveryClient nacosDiscoveryClient;
    private static final Logger logger = LoggerFactory.getLogger(ZeebeNacosInvoker.class);

    @Resource
    private RestTemplate restTemplate;

    @ZeebeWorker
    public void handle(final JobClient client, final ActivatedJob job) throws IOException, InterruptedException, ExecutionException, TimeoutException {

        String serviceName = job.getCustomHeaders().get("service_name");
        String uriKey = job.getCustomHeaders().get("uri_key");

        System.out.println(MetadataKeyword.serviceNameKey);
        System.out.println(job);

        try {
            System.out.println("-------------");
            ServiceInstance instance = nacosDiscoveryClient.getInstances(serviceName).get(0);
            System.out.println("======" + instance.toString());
            String uri = instance.getMetadata().get(uriKey);

            String url = "http://" + serviceName + "/" + uri;

            //ServiceInstance serviceInstance = loadBalancerClient.choose("buscien-service-01-app");

            System.out.println("url:" + url);

            String result = restTemplate.getForObject("http://ord/order/find", String.class);
            System.out.println("result:====" + result);
        }catch (Exception e) {
            logger.error("执行微服务出错: {}", e.getMessage());
        }

        client.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
    }
}
