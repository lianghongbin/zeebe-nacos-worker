package io.zeebe.http;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.util.Map;

/**
 * @author eric.liang
 * @date 9/17/21
 */
final class ImmutableServiceRequest extends ServiceRequest {

    private final String name;
    private final Method method;
    private final String serviceUri;
    private final String variables;

    public ImmutableServiceRequest(ServiceRequestBuilder builder) {
        this.name = builder.name();
        this.method = builder.method();
        this.serviceUri = builder.serviceUri();
        this.variables = builder.variables();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Method method() {
        return this.method;
    }

    @Override
    public String serviceUri() {
        return this.serviceUri;
    }

    @Override
    public String variables() {
        return this.variables;
    }
}
