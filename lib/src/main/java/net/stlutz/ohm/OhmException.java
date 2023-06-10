package net.stlutz.ohm;

public class OhmException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public OhmException() {
        super();
    }
    
    public OhmException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OhmException(String message) {
        super(message);
    }
    
    public OhmException(Throwable cause) {
        super(cause);
    }
    
}
