package io.zeebe.http;

import com.google.gson.Gson;
import io.camunda.zeebe.client.api.response.ActivatedJob;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jeffrey
 */
public abstract class ServiceRequest {

    public interface Builder {

        Builder GET();

        Builder POST(String variables);

        Builder DELETE();

        Builder PUT(String variables);

        Builder name(String name);

        Builder method(Method method);

        Builder method(Method method, String variables);

        Builder serviceUri(String serviceUri);

        Builder variables(String variables);

        Builder job(ActivatedJob job);

        ServiceRequest build();
    }

    public static ServiceRequest.Builder newBuilder(String name) {
        return new ServiceRequestBuilder(name);
    }

    public static ServiceRequest.Builder newBuilder() {
        return new ServiceRequestBuilder();
    }

    public static ServiceRequest.Builder newBuilder(ActivatedJob job) {return new ServiceRequestBuilder(job);}

    public abstract String name();

    public abstract Method method();

    public abstract String serviceUri();

    public abstract String variables();

    public String url() {
        return "http://" + name() + serviceUri();
    }

    public String safeUrl() {
        return "https://" + name() + serviceUri();
    }

    public Optional<Map<String, Object>> variableMap() {
        Gson gson = new Gson();
        return Optional.of(gson.fromJson(variables(), Map.class));
    }

    @Override
    public final boolean equals(Object obj) {
        if (! (obj instanceof ServiceRequest))
            return false;
        ServiceRequest that = (ServiceRequest)obj;
        if (!that.method().equals(this.method()))
            return false;
        if (!that.name().equals(this.name()))
            return false;
        if (!that.serviceUri().equals(this.serviceUri()))
            return false;
        return that.variables().equals(this.variables());
    }

    public final int hashCode() {

        AtomicInteger hashCode = new AtomicInteger();
        variableMap().orElse(new HashMap<>()).forEach((k, v) -> {
            hashCode.addAndGet(v.hashCode());
        });
        return method().hashCode()
                + name().hashCode()
                + serviceUri().hashCode()
                + hashCode.get();
    }
}
