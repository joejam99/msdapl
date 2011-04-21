package org.yeastrc.ms.domain.run.ms2file.impl;

import org.yeastrc.ms.domain.run.ms2file.MS2NameValuePair;


public class NameValuePairBean implements MS2NameValuePair {

    private String name;
    private String value;

    public NameValuePairBean() {
        super();
    }
    
    public NameValuePairBean(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}