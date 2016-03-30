package syssim;

public class SysSimException extends Exception {
    public SysSimException() {
        super();
    }

    public SysSimException(String message, Throwable cause) {
        super(message, cause);
    }

    public SysSimException(String message) {
        super(message);
    }

    public SysSimException(Throwable cause) {
        super(cause);
    }
}
