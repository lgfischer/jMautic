package com.leonardofischer.jmautic;

import com.leonardofischer.jmautic.oauth.MauticOauthException;
import com.leonardofischer.jmautic.oauth.Request;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class OAuth2ServiceTests {

    String instanceUrl;
    String apiKey;
    String apiSecret;
    String callbackUrl;
    String expiredAuthorizationCode;
    String authorizationCode;
    String accessToken;
    String refreshToken;

    Properties properties;
    String propertiesFile = "jMauticTest.properties";

    @Before
    public void setup() {

        com.leonardofischer.letsencrypt.CertificateImporter.initialize();

        properties = PropertiesHelper.getFromFile(propertiesFile);

        instanceUrl = properties.getProperty("instanceUrl");
        apiKey = properties.getProperty("apiKey");
        apiSecret = properties.getProperty("apiSecret");
        callbackUrl = properties.getProperty("callbackUrl");
        expiredAuthorizationCode = properties.getProperty("expiredAuthorizationCode");
        authorizationCode = properties.getProperty("jmautic.test.authorizationCode");
        accessToken = properties.getProperty("accessToken");
        refreshToken = properties.getProperty("refreshToken");

        if( accessToken==null ) {
            fail("Tests not configured correctly: type 'gradle run'");
        }
    }

    //@Test
    public void testExpiredAccessCode() {
        System.out.println("\n\n testExpiredAccessCode");
        try {
            OAuth2Service service = new OAuth2Service()
                .instanceUrl(instanceUrl)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .callbackUrl(callbackUrl)
                .initService();
            System.out.println("Using authorizationCode: "+expiredAuthorizationCode);
            service.setAuthorizationCode(expiredAuthorizationCode);
            fail();
        }
        catch(MauticOauthException e) {
            assertEquals("invalid_grant: The authorization code has expired", e.getMessage() );
        }
        System.out.println("Test finished");
    }

    /**
     * This test is hard to automate: for now I would need to open a browser,
     * open the 'authorizationUrl', enter the user credentials, and click on
     * buttons from the Mautic UI to authorize the request and get the
     * 'authorizationCode'. I've tested this manually, and disabled the
     * test for now, but this should be automated.
     */
    //@Test
    public void testBasicAuthflow() throws Exception {
        System.out.println("\n\n testBasicAuthflow");
        OAuth2Service service = new OAuth2Service()
            .instanceUrl(instanceUrl)
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .callbackUrl(callbackUrl)
            .initService();

        String authorizationUrl = service.getAuthorizationUrl();

        assertNotNull( authorizationUrl );
        assertTrue( authorizationUrl.indexOf("oauth/v2/authorize")>0 );
        assertTrue( authorizationUrl.indexOf("client_id")>0 );

        System.out.println("authorizationUrl: " + authorizationUrl);

        System.out.println("Using authorizationCode: "+authorizationCode);
        service.setAuthorizationCode(authorizationCode);

        assertNotNull( service.getAccessToken() );
        System.out.println("Got access token: "+service.getAccessToken());
        System.out.println("Got refresh token: "+service.getRefreshToken());

        System.out.println("Test finished");
    }

    @Test
    public void testBasicWorkflowToProtectedResource() throws Exception {
        System.out.println("\n\n testBasicWorkflowToProtectedResource");
        OAuth2Service service = new OAuth2Service()
            .instanceUrl(instanceUrl)
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .accessToken(accessToken)
            .initService();

        Request request = new Request();
        request.setEndpoint("/api/contacts");
        Object obj = service.executeRequest(request);

        System.out.println("Result: "+obj);

        assertNotNull(obj);

        System.out.println("Test finished");
    }

    @Test
    public void testBasicWorkflowWithRefreshToken() throws Exception {
        System.out.println("\n\n testBasicWorkflowWithRefreshToken");
        OAuth2Service service = new OAuth2Service()
            .instanceUrl(instanceUrl)
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .initService();

        if( !service.refreshToken(refreshToken) ) {
            fail("Cant refresh token");
        }

        Request request = new Request();
        request.setEndpoint("/api/contacts");
        InputStream inputStream = service.executeRequest(request);

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String theString = writer.toString();

        System.out.println("Result: "+theString);

        assertNotNull(theString);

        properties.setProperty("accessToken", service.getAccessToken());
        properties.setProperty("refreshToken", service.getRefreshToken());
        PropertiesHelper.saveToFile(properties, propertiesFile);

        System.out.println("Test finished");
    }

    @Test
    public void testAccessToWrongAPI() throws Exception {
        System.out.println("\n\n testAccessToWrongAPI");
        try {
            OAuth2Service service = new OAuth2Service()
                .instanceUrl(instanceUrl)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .accessToken(accessToken)
                .initService();

            Request request = new Request();
            request.setEndpoint("/invalid/url");
            service.executeRequest(request);

            fail();
        }
        catch(MauticException e) {
            assertEquals("Invalid request GET /invalid/url", e.getMessage());
        }
    }
}

