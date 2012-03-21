package com.bitmechanic.barrister;

public class BoolTypeConverter implements TypeConverter {

    public Class getTypeClass() {
        return Boolean.class;
    }

    public Object fromRequest(String pkg, Object o) throws RpcException {
        if (o == null || o.getClass() == Boolean.class || o.getClass() == boolean.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected bool, got: " +
                                                        o.getClass().getSimpleName());
    }

    public Object toResponse(Object o) throws RpcException {
        return fromRequest(null, o);
    }

}