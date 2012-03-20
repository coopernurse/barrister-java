package com.bitmechanic.barrister;

public class RpcResponse {

    private String id;
    private Object result;
    private RpcException error;

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

}