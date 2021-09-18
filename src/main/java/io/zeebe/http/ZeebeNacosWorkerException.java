package io.zeebe.http;

/**
 * @author jeffrey
 */
public class ZeebeNacosWorkerException extends RuntimeException{

    public ZeebeNacosWorkerException(){
        super();
    }

    public ZeebeNacosWorkerException(String message){
        super(message);
    }

    public ZeebeNacosWorkerException(String message, Throwable cause){
        super(message,cause);
    }

    public ZeebeNacosWorkerException(Throwable cause) {
        super(cause);
    }
}
