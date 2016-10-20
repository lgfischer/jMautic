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

/**
 * <p>Parses the Mautic REST API responses and constructs jMautic model objects from it.
 *
 * <p>Internally, it uses
 * <a href="https://github.com/FasterXML/jackson-databind" target="_top">jackson-databind</a> to
 * parse the JSON stream and map it to jMautic model classes.
 *
 * <p>Although we could use jackson-databind annotations to map JSON fields to jMautic model
 * classes, this parser does not relies on it. We cannot predict when the Mautic developers will
 * change it's API, and jMautic intention is to support all versions of the Mautic API. So the use
 * of annotations may make harder to support several distinct JSON formats in the same code.
 *
 * <p>For now, the Parser will change the parsed JSON, in a way that it is easy to map the JSON
 * tree into jMautic model objects. This includes renaming fields, moving fields around in the
 * tree, and other things.
 *
 * <p>Future versions of jMautic may handle changes in the Mautic API by creating an interface
 * from this class public methods, and then creating multiple implementations of this new
 * interface.
 */
public class Parser {

    ObjectMapper mapper;

    public Parser() {
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializer());
        mapper.registerModule(module);
    }

    /**
     * Parses the JSON returned by the <code>GET /contacts</code> Mautic endpoint, and creates
     * a ListContactsResult instance from it.
     * @param input the InputStream to read the endpoint response
     * @return the ListContactsResult built from the given JSON content in the stream
     * @throws MauticException if an error occurs mapping the input to the ListContactsResult
     *         instance
     */
    public ListContactsResult parseListContacts(InputStream input) throws MauticException {
        ObjectNode jsonTree;

        try {
            jsonTree = (ObjectNode)mapper.readTree(input);

            renameLeadsToContacts(jsonTree);

            Iterator<JsonNode> iterator = jsonTree.get("contacts").iterator();
            while( iterator.hasNext() ) {
                ObjectNode contact = (ObjectNode)iterator.next();
                moveFieldsDotAllToAllFields(contact);
                moveFieldsToField(contact);
                renameFieldOrder(contact);
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

    /**
     * Mautic current documentation uses "Contacts" instead of "Leads", but they keept the "leads"
     * field to avoid breaking changes. To make jMautic more uniform and allow it to use the
     * current namming scheme, we use only "contact" and "contacts" in the model classes. This
     * method renames the "leads" entry of the <code>GET /contacts</code> endpoint to "contacts",
     * making it easier to map into the "contacts" field of the
     * {@link com.leonardofischer.jmautic.model.ListContactsResult} class.
     */
    private void renameLeadsToContacts(ObjectNode jsonTree) {
        jsonTree.set( "contacts", jsonTree.get("leads") );
        jsonTree.remove("leads");
    }

    /**
     * The "fields" entry of a contact in the JSON has twoo kinds of sub-entries: groups and an
     * "all" entry. Although all are related to fields, it have very distinct structures: the
     * "group" field are very uniform, and the "all" field may have very different sub-fields
     * for each contact. This method removes the entry "all" from the "fields" entry, and put
     * it in the contact itself, using the name "allFields".
     */
    private void moveFieldsDotAllToAllFields(ObjectNode contact) {
        ObjectNode contactFields = (ObjectNode)contact.get("fields");
        if( contactFields!=null ) {
            contact.set( "allFields", contactFields.get("all") );
            contactFields.remove("all");
        }
    }

    /**
     * The "fields" entry of a contact may have several groups. The group names appear two times
     * in the JSON structure: as an entry inside the "fields" entry, and as a value of the
     * group in a field. In the same way, field entry inside a "group" entry is replicated in the
     * "alias" entry of a field. This method "flattens" this structure, converting the whole
     * "fields" entry to a list of top-level fields.
     */
    private void moveFieldsToField(ObjectNode contact) throws MauticException {
        ArrayNode fields = contact.arrayNode(); //--> new ArrayNode();

        ObjectNode groups = (ObjectNode)contact.get("fields");
        if( groups!=null ) {
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
        }

        contact.set("fields", fields);
    }

    /**
     * Right now the Mautic API is non-uniform in several points. For example, most of the time the
     * api returns the fields in camel-case (for example, "someField"). But, for some unknown
     * reason, the fieldOrder is returned in the JSON result as "some_field". We can't use the
     * ObjectMapper.setPropertyNamingStrategy() method, so we make the JSON more uniform by
     * renaming the "field_order" to "fieldOrder".
     */
    private void renameFieldOrder(ObjectNode contact) {
        ArrayNode fields = (ArrayNode)contact.get("fields");
        if( fields!=null ) {
            Iterator<JsonNode> fieldsIterator = fields.iterator();
            while( fieldsIterator.hasNext() ) {
                ObjectNode field = (ObjectNode)fieldsIterator.next();
                field.set("fieldOrder", field.get("field_order"));
                field.remove("field_order");
            }
        }
    }

    /**
     * The "ipAddresses" field of a contact has several entries, one for each IP address. And
     * each IP address has some details to it. This method flattens each IP entry, making the
     * IP address appear in the same level as its details, making easier to handle it.
     */
    private void simplifyIpAddresses(ObjectNode contact) {
        ArrayNode ipAddresses = contact.arrayNode(); //--> new ArrayNode();

        ObjectNode ipAddressNames = (ObjectNode)contact.get("ipAddresses");
        if( ipAddressNames!=null ) {
            Iterator<String> ipAddressNamesIterator = ipAddressNames.fieldNames();
            while( ipAddressNamesIterator.hasNext() ) {
                String ipAddress = ipAddressNamesIterator.next();
                ObjectNode ipAddressData = (ObjectNode)ipAddressNames.get(ipAddress);

                ObjectNode ipAddressDetails = (ObjectNode)ipAddressData.get("ipDetails");
                ipAddressDetails.put("ipAddress", ipAddress);
                ipAddresses.add( ipAddressDetails );
            }
        }

        contact.set("ipAddresses", ipAddresses);
    }

    /**
     * Parses the JSON returned by the <code>GET /contacts/ID</code> Mautic endpoint and creates
     * a GetContactResult object from it.
     *
     * @param input the InputStream to read the returned JSON from the API
     * @return the GetContactResult object build from the JSON read from the stream
     * @throws MauticException if an error occurrs while reading or mapping the JSON
     *         to a GetContactResult instance.
     */
    public GetContactResult parseGetContact(InputStream input) throws MauticException {
        ObjectNode jsonTree;

        try {
            jsonTree = (ObjectNode)mapper.readTree(input);

            renameLeadToContact(jsonTree);

            ObjectNode contact = (ObjectNode)jsonTree.get("contact");
            moveFieldsDotAllToAllFields(contact);
            moveFieldsToField(contact);
            renameFieldOrder(contact);
            simplifyIpAddresses(contact);
        }
        catch(IOException e) {
            throw new MauticException("Could not read json: "+e.getMessage(), e);
        }

        try {
            return mapper.treeToValue(jsonTree, GetContactResult.class);
        }
        catch(JsonProcessingException e) {
            throw new MauticException("Could not convert json to GetContactResult: "+e.getMessage(), e);
        }
    }

    /**
     * Renames the field "lead" to "contact" from a JSON returned by the
     * <code>GET /contacts/ID</code> API. This make the whole jMautic model more uniform by using
     * only the word "contact".
     */
    private void renameLeadToContact(ObjectNode jsonTree) {
        jsonTree.set( "contact", jsonTree.get("lead") );
        jsonTree.remove("lead");
    }
}
