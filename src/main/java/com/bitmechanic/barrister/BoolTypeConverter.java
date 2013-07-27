package com.bitmechanic.barrister;

import java.util.List;

/**
 * TypeConverter for IDL 'bool' types
 */
public class BoolTypeConverter extends BaseTypeConverter {

    public BoolTypeConverter(boolean isOptional) {
        super(isOptional);
    }

    public Class getTypeClass() {
        return Boolean.class;
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
        else if (o.getClass() == Boolean.class || o.getClass() == boolean.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected bool, got: " +
                    o.getClass().getSimpleName());
    }

    public static Object unmarshalList(Object o, boolean isOptional) throws RpcException {
        if (o == null) {
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        }
        else if (o instanceof List) {
            List list = (List)o;
            Boolean arr[] = new Boolean[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (Boolean)unmarshal(list.get(i), isOptional);
            }
            return arr;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Expected List, got: " + o.getClass().getSimpleName());
        }
    }

}