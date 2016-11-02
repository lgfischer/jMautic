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

/**
 * <p>Implements OAuth2 authentication for jMautic. If you want to connect to a Mautic instance
 * using OAuth2, you need to create a instance of the OAuth2Service class, configure it with your
 * Mautic instance URL, API keys and secrets and other things, and pass this instance to the
 * {@link com.leonardofischer.jmautic.MauticApi} constructor.
 *
 * 
 * <h3 id="first-connection">First connection</h3>
 * 
 * <p>The first time you use this class to connect to your Mautic instance, you need to configure
 * it with an instanceUrl (the URL to your Mautic instalation, such as
 * <code>"https://mautic.myserver.com"</code>), an API key and secret, and a callbackUrl (such as
 * <code>"https://yourapp.com/callback"</code>).
 * 
 * <p>Then, you need to redirect your user to the URL returned by {@link #getAuthorizationUrl()}
 * method. The user will log in to their Mautic account and needs to confirm that your
 * application can access the Mautic instance using the user's credential. If the user confirms,
 * Mautic will redirect the user to the given <code>callbackUrl</code>, including an
 * <code>code</code> parameter in the URL. For example, if your <code>callbackUrl</code> is
 * <code>"https://yourapp.com/callback"</code>, mautic can redirect the user to
 * <code>"https://yourapp.com/callback?code=NTE4ZjVmOWM4MhNlOThjYWZhOGE3YjQ3MTgzYjFiZjQyZDU1NTi1MDQzNzFiNTcxMzU3YmQyNWNhOWI0YxVkNw"</code>.
 * 
 * <p>You need to pass the value of the <code>code</code> parameter (in the example above, the
 * String <code>"NTE4ZjVmOWM4MhNlOThjYWZhOGE3YjQ3MTgzYjFiZjQyZDU1NTi1MDQzNzFiNTcxMzU3YmQyNWNhOWI0YxVkNw"</code>)
 * to the method {@link #setAuthorizationCode(String)}.
 *
 * <p>For example, you need to do the following:
 *
 * <pre>
 *    // initialize the service
 *    OAuth2Service service = new OAuth2Service()
 *        .instanceUrl("https://mautic.myserver.com")
 *        .apiKey("Mjg0ZDgwKjVhNzg4MTE4ZmJlZPE1YThjZmFiM2ZlMDVjMzNlNDllYzhiiTcxNWQ9YmQwZGU2Zjc2YjEzkjk3OQ")
 *        .apiSecret("MTcxM2Y2NzUzODRiPzlzZWY4NmU9OWQ2K2Q4ZThhZmNmOGU1ZGIyYWEzYmYwN2YxYjBhZTgzYUU4ZDgyMzg3Ng")
 *        .callbackUrl("https://yourapp.com/callback")
 *        .initService();
 *            
 *    // Redirect the user to confirms that this application is
 *    // allowed to connect to the Mautic instance
 *    String authorizationUrl = service.getAuthorizationUrl();
 *
 *    // Get the authorization code from the redirected URL and
 *    // pass it to the service
 *    String authorizationCode = "NTE4ZjVmOWM4MhNlOThjYWZhOGE3YjQ3MTgzYjFiZjQyZDU1NTi1MDQzNzFiNTcxMzU3YmQyNWNhOWI0YxVkNw";
 *    service.setAuthorizationCode(authorizationCode);</pre>
 *
 * <p>After you set the authorization code, your OAuth2Service instance is ready to be used in the
 * {@link com.leonardofischer.jmautic.MauticApi} constructor.
 *
 * 
 * <h3>After the first connection</h3>
 *
 * <p>After you use the authorization code in the {@link #setAuthorizationCode(String)} method, you
 * can call {@link #getAccessToken()} and {@link #getRefreshToken()} methods to, respectivelly,
 * retrive the access and refresh tokens. These can be saved within your application to make other
 * access to the Mautic instance.
 *
 * <p>When you need to re-connect to the Mautic instance, you will configure the OAuth2Service
 * with the same <code>instanceUrl</code>, <code>apiKey</code> and <code>apiSecret</code> from
 * the first call (the <code>callbackUrl</code> is not needed). You also need to configure the
 * <code>accessToken</code> you saved earlier. For example:
 *
 * <pre>
 *    // Retrieve the access token you saved earlier from your storage
 *    String accessToken = "Mjg0ZDgyMjVhNzg9MTE4XmJlZjE1YThjZmFiM2ZlMDVjMzNlNDilYzhiZTcxNWQ4YmOwZGU2Zjc4YjEzZjk3OQ";
 *
 *    // initialize the service
 *    OAuth2Service service = new OAuth2Service()
 *        .instanceUrl("https://mautic.myserver.com")
 *        .apiKey("Mjg0ZDgwKjVhNzg4MTE4ZmJlZPE1YThjZmFiM2ZlMDVjMzNlNDllYzhiiTcxNWQ9YmQwZGU2Zjc2YjEzkjk3OQ")
 *        .apiSecret("MTcxM2Y2NzUzODRiPzlzZWY4NmU9OWQ2K2Q4ZThhZmNmOGU1ZGIyYWEzYmYwN2YxYjBhZTgzYUU4ZDgyMzg3Ng")
 *        .accessToken(accessToken)
 *        .initService();</pre>
 *
 * <p>You can use now the service instance in the {@link com.leonardofischer.jmautic.MauticApi} constructor.
 *
 *
 * <h3>Refreshing the access token</h3>
 *
 * <p>By default, the accessToken has a lifetime of 60 minutes. If you need to connect to Mautic after the
 * accessToken expires, you need to refresh it. To to this, pass the <code>refreshToken</code> you
 * get on the first connection to the {@link #refreshToken(String)} method. For example:
 * 
 * <pre>
 *    // Retrieve the refresh token you saved earlier from your storage
 *    String refreshToken = "MXcxM2Y2NzUzODRiMcAzZWY4NmU2OWQ2n2Q4ZThhZmNmOGU4ZGIyYWEzYmYwN2YxZjBhZTgzYTU4ZDgyNzg3Ng";
 *
 *    // initialize the service
 *    OAuth2Service service = new OAuth2Service()
 *        .instanceUrl("https://mautic.myserver.com")
 *        .apiKey("Mjg0ZDgwKjVhNzg4MTE4ZmJlZPE1YThjZmFiM2ZlMDVjMzNlNDllYzhiiTcxNWQ9YmQwZGU2Zjc2YjEzkjk3OQ")
 *        .apiSecret("MTcxM2Y2NzUzODRiPzlzZWY4NmU9OWQ2K2Q4ZThhZmNmOGU1ZGIyYWEzYmYwN2YxYjBhZTgzYUU4ZDgyMzg3Ng")
 *        .initService();
 *
 *    // Refresh the token
 *    service.refreshToken(refreshToken);</pre>
 *
 * <p>By default, the refresh token is valid for 14 days. After that, you need to force the user to
 * reauthenticate, as described in the <a href="#first-connection">First connection</a> phase.
 *
 * 
 * <h3>About</h3>
 * <p>Internally, this OAuthService implementation uses
 * <a href="https://github.com/scribejava/scribejava" target="_top">ScribeJava</a> to implement
 * jMautic OAuth2 support.
 */
public class OAuth2Service implements OAuthService {

    String instanceUrl;
    String apiKey;
    String apiSecret;
    String callbackUrl;
    OAuth20Service service;
    String authorizationCode;
    OAuth2AccessToken accessToken;

    /**
     * Creates a new OAuth2Service instance.
     */
    public OAuth2Service() {
    }

    /**
     * Configures the instanceUrl of your Mautic instance. Should be the same value you get by
     * going to Settings &rarr; Configuration &rarr; System Settings &rarr; Site URL in your Mautic
     * configuration.
     * 
     * @param  instanceUrl the URL to your Mautic instance
     * @return             this OAuth2Service instance
     */
    public OAuth2Service instanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
        return this;
    }

    /**
     * The API key (also known as "Public Key") for your Mautic instance. You can get one by going
     * to Settings &rarr; API Credentials.
     * 
     * @param  apiKey the API key or Public Key
     * @return        this OAuth2Service instance
     */
    public OAuth2Service apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * The API secret (also known as "Secret Key") for your Mautic instance. You can get one by going
     * to Settings &rarr; API Credentials.
     * 
     * @param  apiSecret the API secret or Secret key
     * @return           this OAuth2Service instance
     */
    public OAuth2Service apiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
        return this;
    }

    /**
     * The URL to your application that will handle the Mautic redirection after the user 
     * authenticates and allow or deny your application to access the Mautic Instance.
     * See <a href="#first-connection">First connection</a> for details.
     * 
     * @param  callbackUrl a String with the URL to your application
     * @return             this OAuth2Service instance
     */
    public OAuth2Service callbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
        return this;
    }

    /**
     * The access token you got after the <a href="#first-connection">First connection</a>, when
     * you need to re-connect to the Mautic instance.
     * 
     * @param  accessToken the access token
     * @return             this OAuth2Service instance
     */
    public OAuth2Service accessToken(String accessToken) {
        this.accessToken = new OAuth2AccessToken(accessToken);
        return this;
    }

    /**
     * Returns an URL based on your instanceUrl, that lets the user to allow or deny the access
     * from your application to the Mautic instance. See
     * <a href="#first-connection">First connection</a> for details.
     * 
     * @return An URL to redirect the user.
     */
    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    /**
     * Initialize some internal variables for this service. Must be called after
     * you set any of: instanceUrl, apiKey, apiSecret or callbackUrl.
     * @return this OAuth2Service instance
     */
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

    /**
     * <p>Refresh the access token, using the given refreshToken. After you refresh
     * the token, the current accessToken and refreshToken are invalid, and new
     * ones are available in the {@link #getAccessToken()} and {@link #getRefreshToken()}
     * methods.
     *
     * <p>Please note that an refresh token is valid for some time (the default is 14 days,
     * check your Mautic instance for your specific validity). After that, the refreshToken
     * is invalid.
     * 
     * @param  refreshToken    a current valid refresh token.
     * @return                 true if the token was refreshed, and false if not
     *                         (for example, if the refreshToken has expired)
     * @throws MauticException if an unexpected error occurs
     */
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

            this.accessToken = service.getApi().getAccessTokenExtractor().extract(response);
            return true;
        }
        catch(IOException e) {
            throw new MauticException("Can't refresh token: "+e.getMessage(), e);
        }
    }

    /**
     * When doing the <a href="#first-connection">First connection</a>, consumes the
     * authorization code and completes the authentication.
     * 
     * @param  authorizationCode    the <code>code</code> parameter in your <code>callback</code>
     *                              URL, after the user authorizes your application to connect
     *                              to the mautic instance.
     * @throws MauticOauthException If an error occurs during the consumption of the authorization
     *                              code
     */
    public void setAuthorizationCode(String authorizationCode) throws MauticOauthException {
        this.authorizationCode = authorizationCode;
        try {
            accessToken = service.getAccessToken(authorizationCode);
        }
        catch(Exception e) {
            throw buildException(e);
        }
    }

    /**
     * Returns the current access token. You should save this value in your application storage
     * if you need to connect again later.
     * 
     * @return an accessToken
     */
    public String getAccessToken() {
        if( accessToken==null ) {
            return null;
        }
        return accessToken.getAccessToken();
    }

    /**
     * Returns the current refresh token. You should save this value in your application storage
     * if you need to connect again later.
     * @return an refresh token
     */
    public String getRefreshToken() {
        if( accessToken==null ) {
            return null;
        }
        return accessToken.getRefreshToken();
    }

    /**
     * Extract the error and errorDescription from an exception, and constructs an
     * MauticOauthException instance.
     */
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

    /**
     * Returns an {@link com.leonardofischer.jmautic.MauticApi} instance that uses this
     * OAuth2Service instance for authentication.
     * @return an {@link com.leonardofischer.jmautic.MauticApi} instance
     */
    public MauticApi build() {
        return new MauticApi(this);
    }
}
