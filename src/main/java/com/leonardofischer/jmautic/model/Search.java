package com.leonardofischer.jmautic.model;

public class Search {
    public String search = null;
    public int start = -1;
    public int limit = -1;
    // field name
    public String orderBy = null;
    // asc or desc
    public String orderByDir;
    public Boolean publishedOnly;
}
