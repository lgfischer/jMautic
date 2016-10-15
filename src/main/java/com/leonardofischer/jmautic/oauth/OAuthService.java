package com.leonardofischer.jmautic.oauth;

import java.io.InputStream;
import com.leonardofischer.jmautic.MauticException;

/**
 * An interface that any OAuth (either 1a or 2) need to implement to be used with jMautic.
 *
 * If you want to extend jMautic with your own OAuth implementation, implement this interface,
 * and pass an instance of it to the {@link com.leonardofischer.jmautic.MauticApi} constructor.
 */
public interface OAuthService {

    /**
     * Execute the given request, returning an InputSteam to read the request response.
     * 
     * @param  request         the request that must be executed
     * @return                 an InputStream instance to read the response from the request
     * @throws MauticException if any error occurs during the request. Errors may occur at
     *                         any level (connection problems, invalid request configuration,
     *                         Mautic internal errors, etc.). Please check the
     *                         {@link com.leonardofischer.jmautic.MauticException#getMessage()}
     *                         method for details.
     */
    public InputStream executeRequest(Request request) throws MauticException;
}
