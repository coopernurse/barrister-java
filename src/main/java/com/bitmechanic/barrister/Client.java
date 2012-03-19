package com.bitmechanic.barrister;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class Client {

    private Transport trans;

    public Client(Transport t) {
        this.trans = t;
    }
       
    public Map<String,Object> call(String iface, String func, Object... params) throws RPCException {
        Map<String,Object> req = toRequest(iface, func, params);
        return trans.request(req);
    }

    public String getReqId() {
        return UUID.randomUUID().toString();
    }
    
    Map<String,Object> toRequest(String iface, String func, Object... params) {
        Map<String,Object> req = new HashMap<String,Object>();
        req.put("jsonrpc", "2.0");
        req.put("id", getReqId());
        req.put("method", MethodParser.toMethod(iface, func));
        if (params != null && params.length > 0) {
            req.put("params", params);
        }
        return req;
    }

}