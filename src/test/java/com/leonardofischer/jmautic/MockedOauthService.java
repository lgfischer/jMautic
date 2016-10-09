package com.leonardofischer.jmautic;

import com.leonardofischer.jmautic.MauticException;
import com.leonardofischer.jmautic.oauth.OAuthService;
import com.leonardofischer.jmautic.oauth.Request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class MockedOauthService implements OAuthService {

    String json;

    public void loadJsonFromResource(String resource) throws IOException {
        json = IOUtils.toString(this.getClass().getResourceAsStream(resource),"UTF-8");
    }

    public InputStream executeRequest(Request request) throws MauticException {
        try {
            return new ByteArrayInputStream(json.getBytes("UTF-8"));
        }
        catch(Exception e) {
            throw new MauticException(e);
        }
    }
}
