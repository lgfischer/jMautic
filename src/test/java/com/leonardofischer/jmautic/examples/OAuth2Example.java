package com.leonardofischer.jmautic.examples;

import com.leonardofischer.jmautic.*;
import com.leonardofischer.jmautic.model.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class OAuth2Example {
    public static void main(String [] args) {
        
        com.leonardofischer.letsencrypt.CertificateImporter.initialize();

        try {
            // Initialize a service
            OAuth2Service service = new OAuth2Service()
                .instanceUrl("YOUR MAUTIC INSTANCE URL")
                .apiKey("API KEY")
                .apiSecret("API SECRET")
                .callbackUrl("AUTHORIZED CALLBACK URL")
                .initService();

            // Get authorization to access the Mautic API
            String url = service.getAuthorizationUrl();
            System.out.println("Go to '"+url+"', authorize, and paste the code here");
            Scanner in = new Scanner(System.in);
            String code = in.nextLine();
            service.setAuthorizationCode(code);
            MauticApi mauticApi = service.build();

            // Execute API calls
            ListContactsResult result = mauticApi.listContacts();
            System.out.println("There are "+result.total+" contacts. These are "
                +result.contacts.size()+" of them:");
            Iterator<Contact> contactsIterator = result.contacts.iterator();
            while( contactsIterator.hasNext() ) {
                Contact contact = contactsIterator.next();
                printContact(contact);
            }
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
