package com.leonardofischer.jmautic;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import com.leonardofischer.jmautic.MauticApi;
import com.leonardofischer.jmautic.oauth.OAuthService;
import com.leonardofischer.jmautic.oauth.Request;
import com.leonardofischer.jmautic.oauth.MauticOauthException;
import com.leonardofischer.jmautic.scribejava.MauticOauth2Api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.util.Map;

public class OAuth2Service implements OAuthService {

    String instanceUrl;
    String apiKey;
    String apiSecret;
    String callbackUrl;
    OAuth20Service service;
    String authorizationCode;
    OAuth2AccessToken accessToken;

    public OAuth2Service() {
    }

    public OAuth2Service instanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
        return this;
    }

    public OAuth2Service apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public OAuth2Service apiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }

    public OAuth2Service callbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    public OAuth2Service accessToken(String accessToken) {
        this.accessToken = new OAuth2AccessToken(accessToken);
        return this;
    }

    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    public OAuth2Service initService() {
        if( this.callbackUrl!=null ) {
            service = new ServiceBuilder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackUrl)
                .build(new MauticOauth2Api(instanceUrl));
        }
        else {
            service = new ServiceBuilder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build(new MauticOauth2Api(instanceUrl));
        }
        return this;
    }

    public boolean refreshToken(String refreshToken) throws MauticException {
        if( service==null ) {
            throw new MauticException("Service not initialized");
        }
        if( apiKey==null ) {
            throw new MauticException("apiKey cannot be null");
        }
        if( apiSecret==null ) {
            throw new MauticException("apiSecret cannot be null");
        }
        if( refreshToken==null ) {
            throw new MauticException("refreshToken cannot be null");
        }

        OAuthRequest refreshTokenRequest = new OAuthRequest(Verb.POST, this.instanceUrl +
            "/oauth/v2/token", this.service);
        refreshTokenRequest.addBodyParameter("client_id", apiKey);
        refreshTokenRequest.addBodyParameter("client_secret", apiSecret);
        refreshTokenRequest.addBodyParameter("grant_type", "refresh_token");
        refreshTokenRequest.addBodyParameter("refresh_token", refreshToken);

        try {
            Response response = refreshTokenRequest.send();
            if( response.getCode() == 400 ) {
                System.out.println(response.getBody());
                return false;
            }

            System.out.println( response.getCode() );

            this.accessToken = service.getApi().getAccessTokenExtractor()
                .extract(response.getBody());
            return true;
        }
        catch(IOException e) {
            throw new MauticException("Can't refresh token: "+e.getMessage(), e);
        }
    }

    public void setAuthorizationCode(String authorizationCode) throws MauticOauthException {
        this.authorizationCode = authorizationCode;
        try {
            accessToken = service.getAccessToken(authorizationCode);
        }
        catch(Exception e) {
            throw buildException(e);
        }
    }

    public String getAccessToken() {
        if( accessToken==null ) {
            return null;
        }
        return accessToken.getAccessToken();
    }

    public String getRefreshToken() {
        if( accessToken==null ) {
            return null;
        }
        return accessToken.getRefreshToken();
    }

    private MauticOauthException buildException(Exception e) {
        if( e instanceof com.github.scribejava.core.exceptions.OAuthException ) {
            System.out.println("Exception message: "+ e.getMessage());
            Pattern p = Pattern.compile("'(\\{.*\\})'");
            Matcher m = p.matcher(e.getMessage());
            if( m.find() ) {
                String json = m.group(1);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                    return mapper.readValue(json, MauticOauthException.class);
                }
                catch(IOException mapperException) {
                    System.out.println("Cannot extract json ("+ json +"): "+mapperException.getMessage());
                }
            }
        }

        System.out.println(e.getClass().getName());
        System.out.println(e.getMessage());
        return null;
    }

    public InputStream executeRequest(Request request) throws MauticException {
        try {
            String url = instanceUrl + request.getEndpoint() + "?access_token=" + accessToken.getAccessToken();

            Map parameters = request.getParameters();
            if( parameters!=null && !parameters.isEmpty() ) {
                Iterator it = parameters.entrySet().iterator();
                while( it.hasNext() ) {
                    Map.Entry parameter = (Map.Entry)it.next();
                    url = url + '&' + parameter.getKey() + '=' + 
                        URLEncoder.encode((String)parameter.getValue(), "UTF-8");
                }
            }

            OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, url, service);
            service.signRequest(accessToken, oauthRequest);
            Response response = oauthRequest.send();

            switch( response.getCode() ) {
                case 200:
                    return response.getStream();

                case 404:
                    throw new MauticException("Invalid request GET " + request.getEndpoint());

                default:
                    throw new MauticException(response.getBody());

            }
        }
        catch(IOException e) {
            throw new MauticException(e.getMessage(), e);
        }
    }

    public MauticApi build() {
        return new MauticApi(this);
    }
}
