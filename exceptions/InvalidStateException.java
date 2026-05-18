package exceptions;

public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String msg) { 
        super(msg); 
    }
}
