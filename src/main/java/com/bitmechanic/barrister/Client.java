package com.bitmechanic.barrister;

public interface Client {

    public RpcResponse request(RpcRequest req) throws RpcException;

}
