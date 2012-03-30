package com.bitmechanic.barrister;

import java.util.Map;

public abstract class BaseEntity {

    protected String name;
    protected String comment;
    protected Contract contract;

    public BaseEntity() {

    }

    public BaseEntity(Map<String, Object> obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Map cannot be null!");
        }

        if (obj.containsKey("name")) {
            name = (String)obj.get("name");
        }
        if (obj.containsKey("comment")) {
            comment = (String)obj.get("comment");
        }
    }

    public String getName() {
        return name;
    }

    public String getUpperName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String getComment() {
        return comment;
    }

    public void setContract(Contract c) {
        contract = c;
    }

    public Contract getContract() {
        return contract;
    }

}