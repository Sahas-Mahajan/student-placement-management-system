package com.placement.exception;

/**
 * Custom runtime exception used across the Data Access Object (DAO) and Service layers
 * to wrap and abstract low-level SQLExceptions and database connectivity failures.
 */
public class DatabaseOperationException extends RuntimeException {

    private final String sqlState;
    private final int errorCode;

    public DatabaseOperationException(String message) {
        super(message);
        this.sqlState = null;
        this.errorCode = 0;
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof java.sql.SQLException sqlEx) {
            this.sqlState = sqlEx.getSQLState();
            this.errorCode = sqlEx.getErrorCode();
        } else {
            this.sqlState = null;
            this.errorCode = 0;
        }
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
