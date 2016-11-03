package com.leonardofischer.jmautic.examples;

import com.leonardofischer.jmautic.*;
import com.leonardofischer.jmautic.model.*;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class CreateContactExample {
    public static void main(String [] args) {
        
        com.leonardofischer.letsencrypt.CertificateImporter.initialize();

        try {
            // Connect to Mautic
            OAuth2Service service = new OAuth2Service()
                .instanceUrl("YOUR MAUTIC INSTANCE URL")
                .apiKey("API KEY")
                .apiSecret("API SECRET")
                .accessToken("AN VALID ACCESS TOKEN")
                .initService();

            // In your Mautic instance, open your Settings > Custom Fields page to see
            // the the fields you can use here. Use the 'Alias' column to get the
            // field names.
            HashMap<String,String> contactFields = new HashMap<String,String>();
            contactFields.put("firstname", "Chuck");
            contactFields.put("lastname", "Norris");
            contactFields.put("email", "chucknorris@anyserver.com");

            // The IP address of the contact
            String ipAddress = "192.168.38.99";

            // The date you last saw the contact
            Date lastActive = new Date(1462090200000L /* Sun, 01 May 2016 08:10:00 GMT */);

            // The ID of the user that is responsible for the contact. Check the page
            // Settings > Users to see the available user IDs. Or use -1 to let Mautic
            // chose one for you.
            int ownerId = 1;

            // Create the contact
            MauticApi mauticApi = service.build();
            GetContactResult result = mauticApi.createContact(contactFields, ipAddress, lastActive, ownerId);

            // Check the returned contact
            printContact(result.contact);
        }
        catch(MauticException e) {
            e.printStackTrace();
        }
    }

    static void printContact(Contact contact) {
        System.out.print("- id="+contact.id+"; ");
        Iterator it = contact.allFields.entrySet().iterator();
        while( it.hasNext() ) {
            Map.Entry field = (Map.Entry)it.next();
            if( field.getValue()!=null ) {
                System.out.print(field.getKey() + "="+field.getValue()+"; ");
            }
        }
        System.out.println();
    }
}
