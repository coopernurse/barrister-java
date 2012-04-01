package com.bitmechanic.barrister;

/**
 * Base TypeConverter that many implements derive from. Handles [optional] 
 * type enforcement when marshaling/unmarshaling.
 */
public abstract class BaseTypeConverter implements TypeConverter {

    protected boolean isOptional;

    /**
     * @param isOptional Whether this type is optional as specified in the IDL
     */
    public BaseTypeConverter(boolean isOptional) {
        this.isOptional = isOptional;
    }

    /**
     * Convenience method used by many subclasses.
     * If isOptional, returns null.  Otherwise throws a RpcException.
     */
    public Object returnNullIfOptional() throws RpcException {
        if (isOptional) {
            return null;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Value may not be null");
        }
    }

}