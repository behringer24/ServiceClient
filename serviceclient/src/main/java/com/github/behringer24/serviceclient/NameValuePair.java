package com.github.behringer24.serviceclient;

/**
 * Simple name/value object to store string data
 * @author Andreas Behringer <abe@activecube.de>
 */
public class NameValuePair {
    private String name;
    private String value;

    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
