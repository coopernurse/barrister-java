package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;

public class RpcResponse {

    private String id;
    private Object result;
    private RpcException error;

    public RpcResponse(RpcRequest req, Contract contract, Map map) throws RpcException {
        unmarshal(contract.getFunction(req.getIface(), req.getFunc()), map);
    }

    public RpcResponse(RpcRequest req, Object result) {
        setId(req);
        this.result = result;
    }

    public RpcResponse(RpcRequest req, RpcException error) {
        setId(req);
        this.error = error;
    }

    private void setId(RpcRequest req) {
        if (req != null)
            id = req.getId();
    }

    public String getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public RpcException getError() {
        return error;
    }

    @SuppressWarnings("unchecked") 
    public Map marshal() {
        HashMap map = new HashMap();
        if (id != null)
            map.put("id", id);
        if (error != null)
            map.put("error", error.toMap());
        else
            map.put("result", result);
        return map;
    }

    private void unmarshal(Function func, Map map) throws RpcException {
        id = (String)map.get("id");
        
        Object res = map.get("result");
        if (res != null) {
            result = func.unmarshalResult(res);
        }
        
        Object err = map.get("error");
        if (err != null) {
            if (err instanceof Map) {
                Map errMap = (Map)err;
                int code = RpcException.Error.UNKNOWN.getCode();
                try { code = Integer.parseInt(String.valueOf(errMap.get("code"))); }
                catch (Exception e) { }
                error = new RpcException(code, (String)errMap.get("message"), errMap.get("data"));
            }
            else {
                String msg = "Invalid error in response. Expected Map. Got: " + 
                    err.getClass().getName();
                throw RpcException.Error.INVALID_RESP.exc(msg);
            }
        }
    }

}