package com.bitmechanic.barrister;

import java.util.Map;

public class Field extends BaseEntity {

    private boolean isArray;
    private boolean isPrimitive;
    private boolean isOptional;
    private String type;

    public Field(String name, String type, boolean isArray, boolean isOptional) {
        this.name = name;
        setType(type, isArray, isOptional);
    }

    public Field(Map<String,Object> data) {
        super(data);
        if (!data.containsKey("type") ||
            !data.containsKey("is_array")) {
            throw new IllegalArgumentException("Invalid field: " + data);
        }

        boolean optional = false;
        if (data.containsKey("optional")) {
            optional = (Boolean)data.get("optional");
        }

        setType((String)data.get("type"), (Boolean)data.get("is_array"), optional);
    }

    public TypeConverter getTypeConverter() throws RpcException {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }

        TypeConverter tc = null;

        if (type.equals("string"))
            tc = new StringTypeConverter(isOptional);
        else if (type.equals("int"))
            tc = new IntTypeConverter(isOptional);
        else if (type.equals("float"))
            tc = new FloatTypeConverter(isOptional);
        else if (type.equals("bool"))
            tc = new BoolTypeConverter(isOptional);
        else {
            Struct s = contract.getStructs().get(type);
            if (s != null) {
                tc = new StructTypeConverter(s, isOptional);
            }

            Enum e = contract.getEnums().get(type);
            if (e != null) {
                tc = new EnumTypeConverter(e, isOptional);
            }
        }

        if (tc == null) {
            throw RpcException.Error.INTERNAL.exc("Unknown type: " + type);
        }
        else if (isArray) {
            return new ArrayTypeConverter(tc, isOptional);
        }
        else {
            return tc;
        }
    }

    private void setType(String type, boolean isArray, boolean isOptional) {
        this.type = type;
        this.isArray = isArray;
        this.isOptional = isOptional;

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

    public boolean isOptional() {
        return isOptional;
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