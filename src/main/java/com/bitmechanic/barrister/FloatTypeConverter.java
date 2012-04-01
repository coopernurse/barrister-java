package com.bitmechanic.barrister;

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
        if (o == null)
            return returnNullIfOptional();
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

    public Object marshal(Object o) throws RpcException {
        return unmarshal(o);
    }

}