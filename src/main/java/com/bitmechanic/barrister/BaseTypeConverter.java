package com.bitmechanic.barrister;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base TypeConverter that many implements derive from. Handles [optional] 
 * type enforcement when marshaling/unmarshaling.
 */
public abstract class BaseTypeConverter implements TypeConverter {

    private static Logger logger = Logger.getLogger("barrister");

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
        return returnNullIfOptional(this.isOptional);
    }

    public static Object returnNullIfOptional(boolean isOptional) throws RpcException {
        if (isOptional) {
            return null;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Value may not be null");
        }
    }

    public static void handleException(Throwable t) throws RpcException {
        if (t instanceof RpcException) {
            throw (RpcException)t;
        }
        else {
            String msg = "Unhandled exception: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            logger.log(Level.SEVERE, msg, t);
            throw RpcException.Error.INTERNAL.exc(msg);
        }
    }

}