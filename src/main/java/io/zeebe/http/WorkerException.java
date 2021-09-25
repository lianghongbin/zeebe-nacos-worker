package io.zeebe.http;

/**
 * @author jeffrey
 */
public class WorkerException extends RuntimeException{

    public WorkerException(){
        super();
    }

    public WorkerException(String message){
        super(message);
    }

    public WorkerException(String message, Throwable cause){
        super(message,cause);
    }

    public WorkerException(Throwable cause) {
        super(cause);
    }
}
