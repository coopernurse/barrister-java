package com.bitmechanic.barrister;

import java.util.Map;

public class RpcRequest {

    private MethodParser method;

    private Object[] params;

    public RpcRequest(Map<String,Object> req) throws RPCException {
        String m = (String)req.get("method");
        if (m == null) {
            throw RPCException.Error.INVALID_REQ.exc("Invalid Request. No 'method'.");
        }
        method = new MethodParser(m);

        Object p = req.get("params");
        if (p != null && p instanceof Object[]) {
            params = (Object[])p;
        }
        else {
            params = new Object[] { };
        }
    }

    public String getIface() {
        return method.getIface();
    }

    public String getFunc() {
        return method.getFunc();
    }

    public String getString(int offset) throws RPCException {
        Object obj = get(offset);
        if (obj == null) {
            return (String)obj;
        }
        else if (obj.getClass() == String.class) {
            return (String)obj;
        }
        else {
            throw err(offset, "String", obj);
        }
    }

    public Long getLong(int offset) throws RPCException {
        Object obj = get(offset);
        if (obj == null) {
            return (Long)obj;
        }
        else if (obj.getClass() == Long.class) {
            return (Long)obj;
        }
        else if (obj.getClass() == Integer.class) {
            return Long.valueOf((Integer)obj);
        }
        else if (obj.getClass() == Short.class) {
            return Long.valueOf((Short)obj);
        }
        else {
            throw err(offset, "Long", obj);
        }
    }

    public Double getDouble(int offset) throws RPCException {
        Object obj = get(offset);
        if (obj == null) {
            return (Double)obj;
        }
        else if (obj.getClass() == Double.class) {
            return (Double)obj;
        }
        else if (obj.getClass() == Float.class) {
            return Double.valueOf((Float)obj);
        }
        else if (obj.getClass() == Long.class) {
            return Double.valueOf((Long)obj);
        }
        else if (obj.getClass() == Integer.class) {
            return Double.valueOf((Integer)obj);
        }
        else if (obj.getClass() == Short.class) {
            return Double.valueOf((Short)obj);
        }
        else {
            throw err(offset, "Long", obj);
        }
    }

    public Boolean getBool(int offset) throws RPCException {
        Object obj = get(offset);
        if (obj == null) {
            return (Boolean)obj;
        }
        else if (obj.getClass() == Boolean.class) {
            return (Boolean)obj;
        }
        else {
            throw err(offset, "Bool", obj);
        }
    }

    private RPCException err(int offset, String expected, Object val) {
        String msg = "Invalid param[" + offset + "] expected " + expected + " not " +
            val.getClass().getName();
        return RPCException.Error.INVALID_PARAMS.exc(msg);
    }

    private Object get(int offset) throws RPCException {
        if (offset > params.length) {
            throw RPCException.Error.INVALID_REQ.exc("Invalid Request. Params length="+
                                                     params.length + "<" + (offset+1));
        }

        return params[offset];
    }

}