package com.nextop.zeebe.nacos;

/**
 * @author jeffrey
 */
public class WorkerException extends Exception{

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
