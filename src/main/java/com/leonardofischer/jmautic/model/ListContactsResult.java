package com.leonardofischer.jmautic.model;

import java.util.List;

public class ListContactsResult {

    private List<Contact> contacts;

    private int total;

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Contact> getContacts() {
        return this.contacts;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
