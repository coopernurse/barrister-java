package com.bitmechanic.barrister;

import java.util.List;

/**
 * TypeConverter for IDL 'float' types.  Allows Java short, int, long, float, and doubles, but
 * always returns Java Doubles.
 */
public class FloatTypeConverter extends BaseTypeConverter {

    public FloatTypeConverter(boolean isOptional) {
        super(isOptional);
    }

    public Class getTypeClass() {
        return Double.class;
    }

    /**
     * Returns o as a Java Double
     *
     * @throws RpcException If o is not a short, int, long, float, or double
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
            if (c == Double.class || c == double.class)
                return o;
            else if (c == Float.class || c == float.class)
                return ((Float)o).doubleValue();
            else if (c == Long.class || c == long.class)
                return ((Long)o).doubleValue();
            else if (c == Integer.class || c == int.class)
                return ((Integer)o).doubleValue();
            else if (c == Short.class || c == short.class)
                return ((Short)o).doubleValue();
            else
                throw RpcException.Error.INVALID_PARAMS.exc("Expected float, got: " +
                        o.getClass().getSimpleName());
        }
    }

    public static Object unmarshalList(Object o, boolean isOptional) throws RpcException {
        if (o == null) {
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        }
        else if (o instanceof List) {
            List list = (List)o;
            Double arr[] = new Double[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (Double)unmarshal(list.get(i), isOptional);
            }
            return arr;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Expected List, got: " + o.getClass().getSimpleName());
        }
    }

}