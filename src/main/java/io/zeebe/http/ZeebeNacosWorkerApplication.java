/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.http;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZeebeClient
public class ZeebeNacosWorkerApplication {

  @Resource
  private DiscoveryClient nacosDiscoveryClient;

  @Resource
  private RestTemplate restTemplate;

  public static void main(String[] args) {
    SpringApplication.run(ZeebeNacosWorkerApplication.class, args);
  }

  @ZeebeWorker
  public void handleFooJob(final JobClient client, final ActivatedJob job) throws IOException, InterruptedException, ExecutionException, TimeoutException {

    String serviceName = job.getCustomHeaders().get("service_name");
    String uriKey = job.getCustomHeaders().get("uri_key");

    System.out.println(MicroserviceConfig.serviceNameKey);
    System.out.println(job);

    try {
      System.out.println("-------------");
      ServiceInstance instance = nacosDiscoveryClient.getInstances(serviceName).get(0);
      System.out.println("======" + instance.toString());
      String uri = instance.getMetadata().get(uriKey);

      String url = "http://" + serviceName + "/" + uri;

      //ServiceInstance serviceInstance = loadBalancerClient.choose("buscien-service-01-app");

      System.out.println("url:" + url);

      String result = restTemplate.getForObject("http://order-service/order/find", String.class);
      System.out.println("result:====" + result);
    }catch (Exception e) {
      System.out.println("error:" + e.getMessage());
    }

    client.newCompleteCommand(job.getKey()).variables(job.getVariables()).send().join();
  }
}
