package com.bitmechanic.barrister;

import java.util.Map;

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

}