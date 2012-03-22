package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class RpcRequest {

    private String id;
    private MethodParser method;
    private Object params;

    public RpcRequest(String id, String method, Object params) {
        this.id = id;
        this.method = new MethodParser(method);
        this.params = params;
    }

    public RpcRequest(Map map) {
        this((String)map.get("id"), (String)map.get("method"), map.get("params"));
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method.getMethod();
    }

    public String getFunc() {
        return method.getFunc();
    }

    public String getIface() {
        return method.getIface();
    }

    public Object getParams() {
        return params;
    }

    public Object[] getParamsAsArray() {
        if (params == null) {
            return new Object[0];
        }
        else if (params instanceof List) {
            return ((List)params).toArray();
        }
        else if (params.getClass().isArray()) {
            return (Object[])params;
        }
        else {
            return new Object[] { params };
        }
    }

    @SuppressWarnings("unchecked") 
    public Map marshal(Contract contract) throws RpcException {
        Map map = new HashMap();
        map.put("jsonrpc", "2.0");

        if (id != null)
            map.put("id", id);

        map.put("method", method.getMethod());

        if (params != null) {
            Function f = contract.getFunction(getIface(), getFunc());
            map.put("params", f.marshalParams(this));
        }

        return map;
    }

}