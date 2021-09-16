package io.zeebe.http;

/**
 * @author jeffrey
 */
public class InstanceNotExistException  extends RuntimeException{

    public InstanceNotExistException(){
        super();
    }

    public InstanceNotExistException(String message){
        super(message);
    }

    public InstanceNotExistException(String message, Throwable cause){
        super(message,cause);
    }

    public InstanceNotExistException(Throwable cause) {
        super(cause);
    }
}
