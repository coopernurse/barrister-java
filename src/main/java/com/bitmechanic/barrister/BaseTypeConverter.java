package com.bitmechanic.barrister;

public abstract class BaseTypeConverter implements TypeConverter {

    protected boolean isOptional;

    public BaseTypeConverter(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public Object returnNullIfOptional() throws RpcException {
        if (isOptional) {
            return null;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Value may not be null");
        }
    }

}