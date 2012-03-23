package com.bitmechanic.barrister;

public class BoolTypeConverter implements TypeConverter {

    public Class getTypeClass() {
        return Boolean.class;
    }

    public Object unmarshal(String pkg, Object o) throws RpcException {
        if (o == null)
            throw RpcException.Error.INVALID_PARAMS.exc("bool values may not be null");
        else if (o.getClass() == Boolean.class || o.getClass() == boolean.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected bool, got: " +
                                                        o.getClass().getSimpleName());
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(null, o);
    }

}