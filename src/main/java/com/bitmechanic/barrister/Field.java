package com.bitmechanic.barrister;

import java.util.Map;

public class Field extends BaseEntity {

    private boolean isArray;
    private boolean isPrimitive;
    private String type;

    public Field(String name, String type) {
        this.name = name;
        setType(type);
    }

    public Field(Map<String,Object> data) {
        super(data);
        setType((String)data.get("type"));
    }

    public TypeConverter getTypeConverter() throws RpcException {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }

        TypeConverter tc = null;

        if (type.equals("string"))
            tc = new StringTypeConverter();
        else if (type.equals("int"))
            tc = new IntTypeConverter();
        else if (type.equals("float"))
            tc = new FloatTypeConverter();
        else if (type.equals("bool"))
            tc = new BoolTypeConverter();
        else {
            Struct s = contract.getStructs().get(type);
            if (s != null) {
                tc = s;
            }

            Enum e = contract.getEnums().get(type);
            if (e != null) {
                tc = e;
            }
        }

        if (tc == null) {
            throw RpcException.Error.INTERNAL.exc("Unknown type: " + type);
        }
        else if (isArray) {
            return new ArrayTypeConverter(tc);
        }
        else {
            return tc;
        }
    }

    private void setType(String type) {
        this.type = type;
        if (type.startsWith("[]")) {
            isArray = true;
            this.type = type.substring(2);
        }

        this.isPrimitive = this.type.equals("string") ||
            this.type.equals("float") ||
            this.type.equals("int") ||
            this.type.equals("bool");
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getJavaType() {
        return getJavaType(true);
    }

    public String getJavaType(boolean wrapArray) {
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

        if (wrapArray && isArray) {
            return t + "[]";
        }
        else {
            return t;
        }
    }

}