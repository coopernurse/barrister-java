package com.bitmechanic.barrister;

import java.util.List;

public interface Transport {

    public Contract getContract();
    public RpcResponse request(RpcRequest req);
    public List<RpcResponse> request(List<RpcRequest> reqList);

}
