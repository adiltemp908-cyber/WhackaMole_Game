package exceptions;

public class HighScoreException extends Exception {
    public HighScoreException(String msg) { 
        super(msg); 
    }
    
    public HighScoreException(String msg, Throwable cause) { 
        super(msg, cause); 
    }
}
