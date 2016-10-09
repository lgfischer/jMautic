package com.leonardofischer.jmautic.parser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Date;
import java.util.ArrayList;

import com.leonardofischer.jmautic.MauticException;
import com.leonardofischer.jmautic.model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Parser {

    ObjectMapper mapper = new ObjectMapper();

    public Parser() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializer());
        mapper.registerModule(module);
    }

    public ListContactsResult parseListContacts(InputStream input) throws MauticException {
        ObjectNode jsonTree;

        try {
            jsonTree = (ObjectNode)mapper.readTree(input);

            renameContactsToLeads(jsonTree);

            Iterator<JsonNode> iterator = jsonTree.get("contacts").iterator();
            while( iterator.hasNext() ) {
                ObjectNode contact = (ObjectNode)iterator.next();
                moveFieldsDotAllToAllFields(contact);
                moveFieldsToField(contact);
                simplifyIpAddresses(contact);
            }
        }
        catch(IOException e) {
            throw new MauticException("Could not read json: "+e.getMessage(), e);
        }

        try {
            return mapper.treeToValue(jsonTree, ListContactsResult.class);
        }
        catch(JsonProcessingException e) {
            throw new MauticException("Could not convert json to ListContactsResult: "+e.getMessage(), e);
        }
    }

    private void renameContactsToLeads(ObjectNode jsonTree) {
        jsonTree.put( "contacts", jsonTree.get("leads") );
        jsonTree.remove("leads");
    }

    private void moveFieldsDotAllToAllFields(ObjectNode contact) {
        ObjectNode contactFields = (ObjectNode)contact.get("fields");
        contact.put( "allFields", contactFields.get("all") );
        contactFields.remove("all");
    }

    private void moveFieldsToField(ObjectNode contact) throws MauticException {
        ArrayNode fields = contact.arrayNode(); //--> new ArrayNode();

        ObjectNode groups = (ObjectNode)contact.get("fields");
        Iterator<String> groupNamesIterator = groups.fieldNames();
        while( groupNamesIterator.hasNext() ) {
            String groupName = groupNamesIterator.next();
            JsonNode groupNode = groups.get(groupName);

            if( groupNode instanceof ObjectNode ) {
                ObjectNode group = (ObjectNode)groupNode;
                Iterator<String> fieldNamesIterator = group.fieldNames();
                while( fieldNamesIterator.hasNext() ) {
                    String fieldName = fieldNamesIterator.next();
                    fields.add( group.get(fieldName) );
                }
            }
            else if (groupNode instanceof ArrayNode) {
                ArrayNode groupArray = (ArrayNode) groupNode;
                if( groupArray.size()!=0 ) {
                    throw new MauticException("Expecting empty array node, but found '"+groupNode+"'");
                }
            }
        }

        contact.put("fields", fields);
    }

    private void simplifyIpAddresses(ObjectNode contact) {
        ArrayNode ipAddresses = contact.arrayNode(); //--> new ArrayNode();

        ObjectNode ipAddressNames = (ObjectNode)contact.get("ipAddresses");
        Iterator<String> ipAddressNamesIterator = ipAddressNames.fieldNames();
        while( ipAddressNamesIterator.hasNext() ) {
            String ipAddress = ipAddressNamesIterator.next();
            ObjectNode ipAddressData = (ObjectNode)ipAddressNames.get(ipAddress);

            ObjectNode ipAddressDetails = (ObjectNode)ipAddressData.get("ipDetails");
            ipAddressDetails.put("ipAddress", ipAddress);
            ipAddresses.add( ipAddressDetails );
        }

        contact.put("ipAddresses", ipAddresses);
    }

}
