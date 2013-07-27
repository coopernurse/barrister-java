package com.bitmechanic.barrister;

import java.lang.reflect.Array;
import java.util.List;

/**
 * TypeConverter for IDL 'int' type. Marshals to and from the Java Long type.
 */
public class IntTypeConverter extends BaseTypeConverter {

    public IntTypeConverter(boolean isOptional) {
        super(isOptional);
    }

    public Class getTypeClass() {
        return Long.class;
    }

    /**
     * Accepts short, int, and long values.  Other types will result in a RpcException
     */
    public Object unmarshal(Object o) throws RpcException {
        return unmarshal(o, this.isOptional);
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(o);
    }

    public static Object unmarshal(Object o, boolean isOptional) throws RpcException {
        if (o == null)
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        else {
            Class c = o.getClass();
            if (c == Long.class || c == long.class)
                return o;
            else if (c == Integer.class || c == int.class)
                return ((Integer)o).longValue();
            else if (c == Short.class || c == short.class)
                return ((Short)o).longValue();
            else
                throw RpcException.Error.INVALID_PARAMS.exc("Expected int, got: " +
                        o.getClass().getSimpleName());
        }
    }

    public static Object unmarshalList(Object o, boolean isOptional) throws RpcException {
        if (o == null) {
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        }
        else if (o instanceof List) {
            List list = (List)o;
            Long arr[] = new Long[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (Long)unmarshal(list.get(i), isOptional);
            }
            return arr;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Expected List, got: " + o.getClass().getSimpleName());
        }
    }

}