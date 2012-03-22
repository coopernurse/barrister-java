package com.bitmechanic.barrister;

import java.io.IOException;

public interface Client {

    public Contract getContract();
    public RpcResponse request(RpcRequest req) throws IOException;

}
