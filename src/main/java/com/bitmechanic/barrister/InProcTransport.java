package com.bitmechanic.barrister;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * In process transport implementation.  Bypasses serialization and the network and
 * calls the server directly.  
 */
public class InProcTransport implements Transport {

    private Server server;

    public InProcTransport(Server s) {
        this.server = s;
    }

    public Contract getContract() {
        return server.getContract();
    }

    public RpcResponse request(RpcRequest req) {
        return server.call(req);
    }

    public List<RpcResponse> request(List<RpcRequest> reqList) {
        List<RpcResponse> respList = new ArrayList<RpcResponse>();
        for (RpcRequest req : reqList) {
            respList.add(request(req));
        }

        return respList;
    }

}