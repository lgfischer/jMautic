package com.leonardofischer.jmautic.oauth;

import java.io.InputStream;
import com.leonardofischer.jmautic.MauticException;

public interface OAuthService {
    public InputStream executeRequest(Request request) throws MauticException;
}
