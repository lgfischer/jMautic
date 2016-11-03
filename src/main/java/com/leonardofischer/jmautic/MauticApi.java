package com.leonardofischer.jmautic;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.leonardofischer.jmautic.model.*;
import com.leonardofischer.jmautic.oauth.OAuthService;
import com.leonardofischer.jmautic.oauth.Request;
import com.leonardofischer.jmautic.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * Implements the <a href="https://developer.mautic.org/#rest-api" target="_top">Mautic REST API</a>
 * endpoints as simple Java methods.
 *
 * Require an {@link com.leonardofischer.jmautic.oauth.OAuthService} instance, that will implement the
 * authentication protocol used to connect to the Mautic endpoints.
 */
public class MauticApi {

    OAuthService oauthService;
    ObjectMapper mapper;
    Parser parser;

    /**
     * Creates a new MauticApi object. Require an implementation of the
     * {@link com.leonardofischer.jmautic.oauth.OAuthService} interface to authenticate in the
     * Mautic instance and make requests.
     *
     * @param  oauthService an instance of an {@link com.leonardofischer.jmautic.oauth.OAuthService}
     */
    public MauticApi(OAuthService oauthService) {
        this.oauthService = oauthService;
        this.mapper = new ObjectMapper();
        this.parser = new Parser();
    }

    public ListContactsResult listContacts() throws MauticException {
        return listContacts(null);
    }

    public ListContactsResult listContacts(Search search) throws MauticException {
        Request request = new Request();
        request.setEndpoint("/api/contacts");
        if( search!=null ) {
            if( search.search!=null ) {
                request.addParameter("search", search.search);
            }
            if( search.start>=0 ) {
                request.addParameter("start", Integer.toString(search.start));
            }
            if( search.limit>=0 ) {
                request.addParameter("limit", Integer.toString(search.limit));
            }
            if( search.orderBy!=null ) {
                request.addParameter("orderBy", search.orderBy);
            }
            if( search.orderByDir!=null ) {
                request.addParameter("orderByDir", search.orderByDir);
            }
            if( search.publishedOnly!=null ) {
                request.addParameter("publishedOnly", Boolean.toString(search.publishedOnly));
            }
        }
        InputStream result = oauthService.executeRequest(request);
        return parser.parseListContacts( result );
    }

    public GetContactResult getContact(int contactId) throws MauticException {
        Request request = new Request();
        request.setEndpoint("/api/contacts/"+contactId);
        InputStream result = oauthService.executeRequest(request);
        return parser.parseGetContact( result );
    }

    public GetContactResult createContact(Map<String, String> contactFields, String ipAddress,
            Date lastActive, int ownerId) throws MauticException {
        Request request = new Request();
        request.setMethod(Request.Method.POST);
        request.setEndpoint("/api/contacts/new");
        Iterator<Map.Entry<String,String>> it = contactFields.entrySet().iterator();
        while( it.hasNext() ) {
            Map.Entry<String,String> field = (Map.Entry<String,String>)it.next();
            request.addBodyParameter(field.getKey(), field.getValue());
        }
        if( ipAddress!=null ) {
            request.addBodyParameter("ipAddress", ipAddress);
        }
        if( lastActive!=null ) {
            DateTime isoDateTime = new DateTime(lastActive);
            request.addBodyParameter("lastActive", isoDateTime.toString());
        }
        if( ownerId>=0 ) {
            request.addBodyParameter("owner", Integer.toString(ownerId));
        }
        InputStream result = oauthService.executeRequest(request);
        return parser.parseGetContact( result );
    }
}
