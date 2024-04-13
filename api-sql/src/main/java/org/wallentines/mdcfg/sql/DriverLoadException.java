package org.wallentines.mdcfg.sql;

public class DriverLoadException extends RuntimeException {

    public DriverLoadException(String message) {
        super(message);
    }

    public DriverLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
