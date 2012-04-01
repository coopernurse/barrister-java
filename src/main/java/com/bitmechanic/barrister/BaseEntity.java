package com.bitmechanic.barrister;

import java.util.Map;

/**
 * Base class for all Barrister IDL entity classes: Enum, Struct, Interface
 */
public abstract class BaseEntity {

    protected String name;
    protected String comment;
    protected Contract contract;

    public BaseEntity() {

    }

    /**
     * Extracts 'name' and 'comment' fields from map
     */
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

    /**
     * Returns name of entity as defined in IDL
     */
    public String getName() {
        return name;
    }

    /**
     * Returns name of entity using Java camel case convention.
     * For example, name "firstName" becomes "FirstName"
     */
    public String getUpperName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Returns the comment for this entity as defined in the IDL
     */
    public String getComment() {
        return comment;
    }

    /**
     * Associates this entity with its Contract
     */
    public void setContract(Contract c) {
        contract = c;
    }

    /**
     * Returns the Contract associated with this entity
     */
    public Contract getContract() {
        return contract;
    }

}