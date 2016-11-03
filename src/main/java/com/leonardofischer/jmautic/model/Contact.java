package com.leonardofischer.jmautic.model;

import java.util.List;
import java.util.Map;
import java.util.Date;

public class Contact {
    public int id;
    public Date dateAdded;
    public int createdBy;
    public String createdByUser;
    public Date dateModified;
    public int modifiedBy;
    public String modifiedByUser;
    public int points;
    public Date lastActive;
    public Date dateIdentified;
    public String color;
    public Map<String, String> allFields;
    public List<ContactField> fields;
    public boolean isPublished;
    public String preferredProfileImage;
    public List<IPAddress> ipAddresses;

    // remaining fields to map correctly
    public Object owner;
    public Object tags;
}
