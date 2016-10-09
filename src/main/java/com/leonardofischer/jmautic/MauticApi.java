package com.leonardofischer.jmautic;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.leonardofischer.jmautic.model.ListContactsResult;
import com.leonardofischer.jmautic.model.Search;
import com.leonardofischer.jmautic.oauth.OAuthService;
import com.leonardofischer.jmautic.oauth.Request;
import com.leonardofischer.jmautic.parser.Parser;

import java.io.IOException;
import java.io.InputStream;

public class MauticApi {

    OAuthService oauthService;
    ObjectMapper mapper;
    Parser parser;

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
}
