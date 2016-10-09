package com.leonardofischer.jmautic.oauth;

import com.leonardofischer.jmautic.MauticException;

public class MauticOauthException extends MauticException {

	private static final long serialVersionUID = -3321285893751882284L;

	String error;
    String errorDescription;

    public MauticOauthException() {
    }

    public MauticOauthException(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return this.error;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public String getMessage() {
        return this.error + ": " + this.errorDescription;
    }
}
