package com.bitmechanic.barrister;

import java.io.IOException;

public interface Transport {

    public Contract getContract();
    public RpcResponse request(RpcRequest req);

}
