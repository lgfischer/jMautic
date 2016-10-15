package com.leonardofischer.jmautic.scribejava;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.Verb;

/**
 * Extends ScribeJava to support Mautic endpoints using OAuth2.
 */
public class MauticOauth2Api extends DefaultApi20 {
    private String apiEndpoint;

    public MauticOauth2Api(String apiEndpoint) {
        if (apiEndpoint==null) {
            throw new IllegalArgumentException("apiEndpoint cannot be null");
        }
        if (apiEndpoint.endsWith("/")) {
            apiEndpoint = apiEndpoint.substring(0, apiEndpoint.length()-1);
        }
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return apiEndpoint + "/oauth/v2/authorize";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return apiEndpoint + "/oauth/v2/token";
    }
}
