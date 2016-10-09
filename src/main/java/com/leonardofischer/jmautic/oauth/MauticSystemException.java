package com.leonardofischer.jmautic.oauth;

import com.leonardofischer.jmautic.MauticException;

public class MauticSystemException extends MauticException {

	private static final long serialVersionUID = 5602954486513693399L;

	private MauticSystemException.Error error;

    public MauticSystemException() {
    }

    public MauticSystemException.Error getError() {
        return this.error;
    }

    public void setError(MauticSystemException.Error error) {
        this.error = error;
    }

    public class Error {
        private String message;
        private int code;

        public Error() {
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return this.code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}
