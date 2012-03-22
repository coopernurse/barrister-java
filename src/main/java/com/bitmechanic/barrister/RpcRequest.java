package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class RpcRequest {

    private static final Object[] EMPTY_PARAM = new Object[0];

    private String id;
    private MethodParser method;
    private Object[] params;

    public RpcRequest(String id, String method, Object params) {
        this.id = id;
        this.method = new MethodParser(method);

        if (params == null) {
            this.params = EMPTY_PARAM;
        }
        else if (params instanceof List) {
            this.params = ((List)params).toArray();
        }
        else if (params.getClass().isArray()) {
            this.params = (Object[])params;
        }
        else {
            this.params = new Object[] { params };
        }
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

    public Object[] getParams() {
        return params;
    }

    @SuppressWarnings("unchecked") 
    public Map marshal(Contract contract) throws RpcException {
        Map map = new HashMap();
        map.put("jsonrpc", "2.0");

        if (id != null)
            map.put("id", id);

        map.put("method", method.getMethod());

        if (params != null && params.length > 0) {
            Function f = contract.getFunction(getIface(), getFunc());
            map.put("params", f.marshalParams(this));
        }

        System.out.println("req.marshal: " + map);
        return map;
    }

}