package com.leonardofischer.jmautic.scribejava;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

public class MauticOauth1aApi extends DefaultApi10a {
    private String apiEndpoint;

    public MauticOauth1aApi(String apiEndpoint) {
        if (apiEndpoint==null) {
            throw new IllegalArgumentException("apiEndpoint cannot be null");
        }
        if (apiEndpoint.endsWith("/")) {
            apiEndpoint = apiEndpoint.substring(0, apiEndpoint.length()-1);
        }
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public String getRequestTokenEndpoint() {
        System.out.println(">>>> getRequestTokenEndpoint");
        return apiEndpoint + "/oauth/v1/request_token";
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        System.out.println(">>>> getAuthorizationUrl");
        return apiEndpoint + String.format("/oauth/v1/authorize?oauth_token=%s", requestToken.getToken());
    }

    @Override
    public String getAccessTokenEndpoint(){
        System.out.println(">>>> getAccessTokenEndpoint");
        return apiEndpoint + "/oauth/v1/access_token";
    }
}
