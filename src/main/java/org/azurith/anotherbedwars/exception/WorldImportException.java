package org.azurith.anotherbedwars.exception;

public class WorldImportException extends RuntimeException {
    public WorldImportException(String message) {
        super(message);
    }
    public WorldImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
