package com.leonardofischer.jmautic;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import com.leonardofischer.jmautic.model.*;

public class MauticApiTests {

    MauticApi mauticApi;
    MockedOauthService oauthService;

    @Before
    public void setup() {
        oauthService = new MockedOauthService();
        mauticApi = new MauticApi(oauthService);
    }

    @Test
    public void testListContacts() throws Exception {
        //System.out.println("\n\n testListContacts");
        //oauthService.loadJsonFromResource("/listContacts.json");

        //ListContactsResult listContactsResult = mauticApi.listContacts();
        //assertNotNull(listContactsResult);
    }
}
