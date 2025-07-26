package stylus;

import com.wjduquette.joe.JoeError;

public class DataFileException extends Exception {
    public DataFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getDetails() {
        return switch (getCause()) {
            case JoeError ex -> ex.getTraceReport();
            case Exception ex -> ex.getMessage();
            default -> "";
        };
    }
}
