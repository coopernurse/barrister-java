package com.bitmechanic.barrister;

import java.io.IOException;

public class RpcRequestBean implements RpcRequest {

    private String id;
    private String func;
    private String iface;
    private Object[] params;

    private int offset;

    public RpcRequestBean(String id, String iface, String func, Object... params) {
        this.id = id;
        this.iface = iface;
        this.func = func;
        this.params = params;
        this.offset = 0;
    }

    public String getId() {
        return id;
    }

    public String getFunc() {
        return func;
    }

    public String getIface() {
        return iface;
    }

    public boolean hasNextParam() {
        return params != null && offset < params.length;
    }

    public Object nextParam(Class t) throws IOException {
        if (hasNextParam()) {
            Object o = params[offset];
            offset++;
            return o;
        }
        else {
            throw new IOException("Params out of bounds: " + offset);
        }
    }

}