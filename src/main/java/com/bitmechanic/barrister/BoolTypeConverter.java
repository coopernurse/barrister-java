package com.bitmechanic.barrister;

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
        if (o == null)
            return returnNullIfOptional();
        else if (o.getClass() == Boolean.class || o.getClass() == boolean.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected bool, got: " +
                                                        o.getClass().getSimpleName());
    }

    public Object marshal(Object o) throws RpcException {
        return unmarshal(o);
    }

}