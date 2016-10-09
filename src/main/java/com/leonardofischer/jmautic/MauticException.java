package com.leonardofischer.jmautic;

public class MauticException extends Exception {

	private static final long serialVersionUID = 1829943217171824306L;

	public MauticException() {
        super();
    }

    public MauticException(String message) {
        super(message);
    }

    public MauticException(String message, Throwable cause) {
        super(message, cause);
    }

    public MauticException(Throwable t) {
        super(t);
    }
}
