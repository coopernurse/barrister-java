package com.bitmechanic.barrister;

public class IntTypeConverter implements TypeConverter {

    public Class getTypeClass() {
        return Long.class;
    }

    public Object fromRequest(String pkg, Object o) throws RpcException {
        if (o == null)
            return o;
        else {
            Class c = o.getClass();
            if (c == Long.class || c == long.class)
                return o;
            else if (c == Integer.class || c == int.class)
                return ((Integer)o).longValue();
            else
                throw RpcException.Error.INVALID_PARAMS.exc("Expected int, got: " +
                                                            o.getClass().getSimpleName());
        }
    }

    public Object toResponse(Object o) throws RpcException {
        return fromRequest(null, o);
    }

}