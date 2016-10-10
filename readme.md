jMautic
=======

jMautic is a Java client for the [Mautic REST API](https://developer.mautic.org/#rest-api). With it, you can write Java code to interact with several Mautic APIs, such as reading your contacts programatically.



Example
-------

    package com.leonardofischer.jmautic.examples;

    import com.leonardofischer.jmautic.*;
    import com.leonardofischer.jmautic.model.*;

    import java.util.Iterator;
    import java.util.Map;
    import java.util.Scanner;

    public class OAuth2Example {
        public static void main(String [] args) {
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
                System.out.println("There are "+result.getTotal()+" contacts. These are "
                    +result.getContacts().size()+" of them:");
                Iterator<Contact> contactsIterator = result.getContacts().iterator();
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



Features
--------

Right now, jMautic is able to:

- Access the Mautic REST API using OAuth2;
- Read contacts;

In the future, it should support:

- Access the Mautic REST API using OAuth1a;
- Updating contacts;
- Accessing other endpoints (such as Assets, Campaigns, Forms, Segments, Pages, etc).



How to Use
----------



Javadoc
-------

Not ready yet ;)



How to Build
------------

Just run

    ./gradlew jar

The code should be built on the <code>build/libs/jMautic-[version].jar</code> file.


### How to Test

Some jMautic tests need to connect to a real Mautic API to pass. You will need to configure the Mautic instance (as well as the API key and secret).

First, open <code>jMauticTest.properties</code> and add valid configurations for <code>instanceUrl</code>, <code>callbackUrl</code>, <code>apiKey</code> and <code>apiSecret</code>. Some of the tests try to connect to a real Mautic Rest API, and will use the one on available on these options.

Then, run

    ./gradlew configure
    
and follow the instructions on the output. This need to be done only once after you checkout the code.

Finally, run

    ./gradlew test



Contributing
------------


