package com.bitmechanic.barrister;

public interface Handler {

    public Object call(RpcRequest req) throws RpcException;

}