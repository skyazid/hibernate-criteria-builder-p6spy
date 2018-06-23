package com.demo.conf.exception;

/**
 * BeanTechnicalException.
 */
public class BeanTechnicalException extends RuntimeException {

    /**
     * serial uid.
     */
    private static final long serialVersionUID = 20180623L;

    /**
     * default exception.
     */
    public BeanTechnicalException() {
    }

    /**
     * constructeur.
     *
     * @param message message
     */
    public BeanTechnicalException(final String message) {
        super(message);
    }

    /**
     * constructeur.
     *
     * @param cause cause
     */
    public BeanTechnicalException(final Throwable cause) {
        super(cause);
    }

}
