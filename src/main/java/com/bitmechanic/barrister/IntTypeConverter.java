package com.bitmechanic.barrister;

public class IntTypeConverter extends BaseTypeConverter {

    public IntTypeConverter(boolean isOptional) {
        super(isOptional);
    }

    public Class getTypeClass() {
        return Long.class;
    }

    public Object unmarshal(String pkg, Object o) throws RpcException {
        if (o == null)
            return returnNullIfOptional();
        else {
            Class c = o.getClass();
            if (c == Long.class || c == long.class)
                return o;
            else if (c == Integer.class || c == int.class)
                return ((Integer)o).longValue();
            else if (c == Short.class || c == short.class)
                return ((Short)o).shortValue();
            else
                throw RpcException.Error.INVALID_PARAMS.exc("Expected int, got: " +
                                                            o.getClass().getSimpleName());
        }
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(null, o);
    }

}