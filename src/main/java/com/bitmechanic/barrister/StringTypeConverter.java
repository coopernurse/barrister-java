package com.bitmechanic.barrister;

import java.util.List;

/**
 * TypeConverter for IDL primitive 'string' type.  Does not do any implicit type 
 * conversion.
 */
public class StringTypeConverter extends BaseTypeConverter {

    public StringTypeConverter(boolean isOptional) {
        super(isOptional);
    }

    public Class getTypeClass() {
        return String.class;
    }

    public Object unmarshal(Object o) throws RpcException {
        return unmarshal(o, this.isOptional);
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(o);
    }

    public static Object unmarshal(Object o, boolean isOptional) throws RpcException {
        if (o == null)
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        else if (o.getClass() == String.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected string, got: " +
                    o.getClass().getSimpleName());
    }

    public static Object unmarshalList(Object o, boolean isOptional) throws RpcException {
        if (o == null) {
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        }
        else if (o instanceof List) {
            List list = (List)o;
            String arr[] = new String[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (String)unmarshal(list.get(i), isOptional);
            }
            return arr;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Expected List, got: " + o.getClass().getSimpleName());
        }
    }

}