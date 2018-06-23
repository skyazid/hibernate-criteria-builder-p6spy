package com.demo.conf.exception;

public class DataAccessException extends RuntimeException {

    /**
     * serial uid.
     */
    private static final long serialVersionUID = 20180623L;

    /**
     * default exception.
     */
    public DataAccessException() {
    }

    /**
     * constructeur.
     *
     * @param message message
     */
    public DataAccessException(final String message) {
        super(message);
    }

    /**
     * constructeur.
     *
     * @param cause cause
     */
    public DataAccessException(final Throwable cause) {
        super(cause);
    }

}
