package com.leonardofischer.jmautic.parser;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import com.leonardofischer.jmautic.model.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class ParserTests {
    Parser parser;
    InputStream input = null;

    @Before
    public void setup() {
        parser = new Parser();
    }

    @After
    public void finish() throws Exception {
        if(input!=null){
            input.close();
            input = null;
        }
    }

    @Test
    public void testParseListContacts() throws Exception {
        input = ParserTests.class.getClassLoader().getResourceAsStream("listContacts.json");
        ListContactsResult listContactsResult = parser.parseListContacts(input);
        assertNotNull(listContactsResult);

        // checking ListContactsResult
        assertEquals( 5295, listContactsResult.total );
        assertNotNull( listContactsResult.contacts );
        assertEquals( 2, listContactsResult.contacts.size() );

        // checking 1st contact
        Contact contact = listContactsResult.contacts.get(0);
        assertNotNull( contact );
        assertEquals(449, contact.id);
        assertEquals(new Date(1464117805000L/* 2016-05-24T19:23:25+00:00 */), contact.dateAdded);
        assertEquals(333, contact.createdBy);
        assertEquals("admin@server.com", contact.createdByUser);
        assertEquals(new Date(1464117813000L /* 2016-05-24T19:23:33+00:00 */), contact.dateModified);
        assertEquals(334, contact.modifiedBy);
        assertEquals("admin2@server.com", contact.modifiedByUser);
        assertEquals(18, contact.points);
        assertEquals(new Date(1464719856000L /* 2016-05-31T18:37:36+00:00 */), contact.lastActive);
        assertEquals(new Date(1464117806000L /* 2016-05-24T19:23:26+00:00 */), contact.dateIdentified);
        assertEquals("blue", contact.color);
        assertEquals(true, contact.isPublished);
        assertEquals("image.png", contact.preferredProfileImage);
        assertEquals("user123@example.com", contact.allFields.get("email"));
        assertEquals("11987654321", contact.allFields.get("phone"));
        assertEquals("Sao Paulo", contact.allFields.get("city"));
        assertEquals(null, contact.allFields.get("address1") );
        assertNotNull(contact.fields);
        assertEquals(22, contact.fields.size() );

        boolean coreEmailAsserted = false;
        Iterator<ContactField> contactFieldsIterator = contact.fields.iterator();
        while (contactFieldsIterator.hasNext()) {
            ContactField field = contactFieldsIterator.next();
            if( field.group.equals("core") && field.alias.equals("email") ) {
                assertEquals("6", field.id);
                assertEquals("Email", field.label);
                assertEquals("email", field.type);
                assertEquals("user123@example.com", field.value);
                assertFalse("The field core-email should be found only once", coreEmailAsserted);
                coreEmailAsserted = true;
            }
        }
        assertTrue(coreEmailAsserted);

        assertNotNull(contact.ipAddresses);
        assertEquals(1, contact.ipAddresses.size());
        IPAddress ipAddress = contact.ipAddresses.get(0);
        assertEquals("192.168.1.198", ipAddress.ipAddress);
        assertEquals("Sao Paulo", ipAddress.city);
        assertEquals("Brazil", ipAddress.country);
        assertEquals("", ipAddress.extra);
        assertEquals("", ipAddress.isp);
        assertEquals(-30.0333, ipAddress.latitude, 0.001);
        assertEquals(-51.2, ipAddress.longitude, 0.001);
        assertEquals("", ipAddress.organization);
        assertEquals("Sao Paulo", ipAddress.region);
        assertEquals("America/Sao_Paulo", ipAddress.timezone);
        assertEquals(null, ipAddress.zipcode);


        //Object owner;
        //Object tags;

        // checking 2nd contact
        contact = listContactsResult.contacts.get(1);
        assertNotNull( contact );
        assertEquals(5301, contact.id);
        assertEquals(new Date(1470698577000L/* 2016-08-08T23:22:57+00:00 */), contact.dateAdded);
        assertEquals(1, contact.createdBy);
        assertEquals("", contact.createdByUser);
        assertEquals(null, contact.dateModified);
        assertEquals(0, contact.modifiedBy);// null on json
        assertEquals(null, contact.modifiedByUser);
        assertEquals(0, contact.points);
        assertEquals(new Date(1470702409000L /* 2016-08-09T00:26:49+00:00 */), contact.lastActive);
        assertEquals(null, contact.dateIdentified);
        assertEquals("", contact.color);
        assertEquals(false, contact.isPublished);
        assertEquals(null, contact.preferredProfileImage);
        //Map<String, Map<String, IpAddressDetails>> ipAddresses;
        //ContactFields fields;
        //Object owner;
        //Object tags;
    }



    @Test
    public void testParseGetContact() throws Exception {
        input = ParserTests.class.getClassLoader().getResourceAsStream("getContact.json");
        GetContactResult getContactResult = parser.parseGetContact(input);
        assertNotNull(getContactResult);
        assertNotNull(getContactResult.contact);

        // Most fields were already tested in 'testParseListContacts'
        // For now, I'll just test some few fields, and others will be tested when bugs arrive
        assertEquals(435, getContactResult.contact.id);
        assertEquals(22, getContactResult.contact.allFields.size());
        assertEquals(22, getContactResult.contact.fields.size());
        assertEquals(1, getContactResult.contact.ipAddresses.size());
    }
}
