package com.bitmechanic.barrister;

import java.util.Map;

public class Field extends BaseEntity {

    private boolean isArray;
    private String type;

    public Field(String name, String type) {
        this.name = name;
        setType(type);
    }

    public Field(Map<String,Object> data) {
        super(data);
        setType((String)data.get("type"));
    }

    private void setType(String type) {
        this.type = type;
        if (type.startsWith("[]")) {
            isArray = true;
            this.type = type.substring(2);
        }
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getJavaType() {
        String t = "";
        if (type.equals("string")) {
            t = "String";
        }
        else if (type.equals("float")) {
            t = "Double";
        }
        else if (type.equals("int")) {
            t = "Long";
        }
        else if (type.equals("bool")) {
            t = "Boolean";
        }
        else {
            t = type;
        }

        if (isArray) {
            return "java.util.List<" + t + ">";
        }
        else {
            return t;
        }
    }

}