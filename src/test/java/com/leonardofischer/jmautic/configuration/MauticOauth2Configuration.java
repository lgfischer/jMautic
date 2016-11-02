package com.leonardofischer.jmautic.configuration;

import java.util.Scanner;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.Properties;

import com.leonardofischer.jmautic.scribejava.MauticOauth2Api;
import com.leonardofischer.jmautic.PropertiesHelper;

public abstract class MauticOauth2Configuration {

    private static String MAUTIC_ENDPOINT;
    private static String PROTECTED_RESOURCE_URL;
    private static String MAUTIC_TOKEN_URL;
    private static String CALLBACK;
    private static String API_KEY;
    private static String API_SECRET;

    private static final String JMAUTIC_TEST_PROPERTIES = "jMauticTest.properties";

    public static void main(String... args) throws IOException {
        com.leonardofischer.letsencrypt.CertificateImporter.initialize();
        loadConfiguration();
        connectOauth2FirstTime();
        connectOauth2AfterRefreshToken();
    }

    private static void loadConfiguration() {
        Properties prop = PropertiesHelper.getFromFile(JMAUTIC_TEST_PROPERTIES);
        MAUTIC_ENDPOINT = (String)prop.get("instanceUrl");
        CALLBACK = (String)prop.get("callbackUrl");
        API_KEY = (String)prop.get("apiKey");
        API_SECRET = (String)prop.get("apiSecret");
        PROTECTED_RESOURCE_URL = MAUTIC_ENDPOINT + "/api/leads?access_token=";
        MAUTIC_TOKEN_URL = MAUTIC_ENDPOINT + "/oauth/v2/token";
    }

    private static void connectOauth2FirstTime() throws IOException {
        final OAuth20Service service = new ServiceBuilder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .callback(CALLBACK)
                .build(new MauticOauth2Api(MAUTIC_ENDPOINT));
        final Scanner in = new Scanner(System.in);

        System.out.println("=== Mautic's OAuth2 Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(if your curious it looks like this: " + accessToken
                + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
        System.out.println();

        // Store the token for further use...
        saveToken(accessToken);

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        String filter = "&search=email:c2824974@trbvn.com";
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL + accessToken.getAccessToken() + filter,
                service);
        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        //System.out.println(response.getBody());
        
        if( response.getCode()!=404 ) {
            System.out.println(response.getBody());
        }
        else {
            System.out.println("Not found :(");
        }

        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
    }

    private static void connectOauth2AfterRefreshToken() throws IOException {
        Properties prop = PropertiesHelper.getFromFile(JMAUTIC_TEST_PROPERTIES);

        final OAuth20Service service = new ServiceBuilder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .callback(CALLBACK)
                .build(new MauticOauth2Api(MAUTIC_ENDPOINT));

        OAuthRequest refreshTokenRequest = new OAuthRequest(Verb.POST, MAUTIC_TOKEN_URL, service);
        refreshTokenRequest.addBodyParameter("client_id", API_KEY);
        refreshTokenRequest.addBodyParameter("client_secret", API_SECRET);
        refreshTokenRequest.addBodyParameter("grant_type", "refresh_token");
        refreshTokenRequest.addBodyParameter("refresh_token", prop.getProperty("refreshToken"));

        Response refreshTokenResponse = refreshTokenRequest.send();
        if( refreshTokenResponse.getCode() == 400 ) {
            System.out.println("Can't refresh the access token, need to authorize again");
            return;
        }
        System.out.println( "Got response code " + refreshTokenResponse.getCode() );
        final OAuth2AccessToken accessToken = service.getApi().getAccessTokenExtractor()
            .extract(refreshTokenResponse);

        saveToken(accessToken);

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL + accessToken.getAccessToken(),
                service);
        service.signRequest(accessToken, request);
        final Response response = request.send();
        System.out.println("Got it! Lets see what we found...");
        System.out.println();
        System.out.println(response.getCode());
        //System.out.println(response.getBody());
        
        if( response.getCode()!=404 ) {
            System.out.println("Success accessing protected resource after refresh token");
            //System.out.println(response.getBody());
        }
        else {
            System.out.println("Not found :(");
        }

        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with ScribeJava! :)");
    }

    private static void saveToken(OAuth2AccessToken token) {
        Properties prop = new Properties();
        prop.setProperty("instanceUrl", MAUTIC_ENDPOINT);
        prop.setProperty("apiKey", API_KEY);
        prop.setProperty("apiSecret", API_SECRET);
        prop.setProperty("callbackUrl", CALLBACK);
        prop.setProperty("accessToken", token.getAccessToken());
        prop.setProperty("refreshToken", token.getRefreshToken());
        PropertiesHelper.saveToFile(prop, JMAUTIC_TEST_PROPERTIES);
    }
}
