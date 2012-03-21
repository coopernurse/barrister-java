package com.bitmechanic.barrister;

import java.util.Map;
import java.util.List;

public class RpcRequest {

    private String id;
    private MethodParser method;
    private Object params;

    public RpcRequest(Map map) {
        id = (String)map.get("id");
        method = new MethodParser((String)map.get("method"));
        params = map.get("params");
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

}