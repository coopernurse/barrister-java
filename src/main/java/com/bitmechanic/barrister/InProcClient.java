package com.bitmechanic.barrister;

import java.util.Map;

public class InProcClient implements Client {

    private Server server;

    public InProcClient(Server s) {
        this.server = s;
    }

    public Contract getContract() {
        return server.getContract();
    }

    public RpcResponse request(RpcRequest req) {
        return server.call(req);
    }

}