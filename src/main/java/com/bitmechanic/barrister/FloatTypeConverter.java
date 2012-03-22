package com.bitmechanic.barrister;

public class FloatTypeConverter implements TypeConverter {

    public Class getTypeClass() {
        return Double.class;
    }

    public Object unmarshal(String pkg, Object o) throws RpcException {
        if (o == null)
            return o;
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
            else
                throw RpcException.Error.INVALID_PARAMS.exc("Expected float, got: " +
                                                            o.getClass().getSimpleName());
        }
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(null, o);
    }

}