package io.zeebe.http;

/**
 * @author jeffrey
 */
public enum Method {

    GET("get"),
    POST("post"),
    DELETE("delete"),
    PUT("put");

    private final String code;

    Method(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
