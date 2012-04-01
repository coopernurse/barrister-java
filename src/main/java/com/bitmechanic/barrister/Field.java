package com.bitmechanic.barrister;

import java.util.Map;

/**
 * Represents a single field in a Struct, a parameter in a Function, or a 
 * Function return type.
 */
public class Field extends BaseEntity {

    private boolean isArray;
    private boolean isPrimitive;
    private boolean isOptional;
    private String type;

    /**
     * Creates a new Field
     *
     * @param name Name of Field. For Function return values this is an empty string.
     * @param type IDL type for Field. May be a user defined type or IDL primitive
     * @param isArray Whether field is an array
     * @param isOptional If true, values of this field may be missing or null
     */
    public Field(String name, String type, boolean isArray, boolean isOptional) {
        this.name = name;
        setType(type, isArray, isOptional);
    }

    /**
     * Creates a new Field from the Map using 'name', 'comment', 'type', 'is_array', 'optional' keys.
     */
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

    /**
     * Returns a new TypeConverter appropriate for the Field's type.
     * 
     * @throws RpcException If type is not defined in the Contract
     */
    public TypeConverter getTypeConverter() throws RpcException {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }
        if (type == null) {
            throw new IllegalStateException("field type cannot be null");
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

    /** 
     * Returns IDL type.  May be a user defined type or primitive.  If type is
     * defined as an array in the IDL, this returns a string with the '[]' removed
     * (e.g. "[]string" would return "string", but isArray() would be true)
     */
    public String getType() {
        return type;
    }

    /**
     * Returns true if Field is an array type
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Returns true if this field is defined as optional in the IDL
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Returns true if this type is a Barrister primitive type (string, bool, int, float)
     */
    public boolean isPrimitive() {
        return isPrimitive;
    }

    /**
     * Returns the Java type this type maps to including the array if appropriate.  
     * Used by Idl2Java code generator.
     */
    public String getJavaType() {
        return getJavaType(true);
    }

    /**
     * Returns the Java type this type maps to. Used by Idl2Java code generator.
     *
     * @param wrapArray If true, returned String will include '[]' after type to designate
     *        a Java array type
     */
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