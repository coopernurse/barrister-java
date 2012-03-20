package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class RpcRequest {

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {
        private String method;
        private List<Object> params = new ArrayList<Object>();

        public Builder method(String m) { method = m; return this; }
        public Builder param(Object o) { params.add(o); return this; }
        public RpcRequest build() throws RpcException {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("method", method);
            if (params.size() > 0) {
                Object arr[] = new Object[params.size()];
                params.toArray(arr);
                map.put("params", arr);
            }

            return new RpcRequest(map);
        }
    }

    ////////////////////

    private MethodParser method;
    private Object[] params;

    public RpcRequest(Map<String,Object> req) throws RpcException {
        String m = (String)req.get("method");
        if (m == null) {
            throw RpcException.Error.INVALID_REQ.exc("Invalid Request. No 'method'.");
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

    public int getParamCount() {
        return params.length;
    }

    public String getString(int offset) throws RpcException {
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

    public Long getLong(int offset) throws RpcException {
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

    public Double getDouble(int offset) throws RpcException {
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

    public Boolean getBool(int offset) throws RpcException {
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

    public Map getMap(int offset) throws RpcException {
        Object obj = get(offset);
        if (obj == null) {
            return null;
        }
        else {
            try {
                return (Map)obj;
            }
            catch (Throwable t) {
                throw err(offset, "Map", obj);
            }
        }
    }

    public List getList(int offset) throws RpcException {
        Object obj = get(offset);
        if (obj == null) {
            return null;
        }
        else {
            try {
                return (List)obj;
            }
            catch (Throwable t) {
                throw err(offset, "List", obj);
            }
        }
    }

    private RpcException err(int offset, String expected, Object val) {
        String msg = "Invalid param[" + offset + "] expected " + expected + " not " +
            val.getClass().getName();
        return RpcException.Error.INVALID_PARAMS.exc(msg);
    }

    private Object get(int offset) throws RpcException {
        if (offset > params.length) {
            throw RpcException.Error.INVALID_REQ.exc("Invalid Request. Params length="+
                                                     params.length + "<" + (offset+1));
        }

        return params[offset];
    }

}