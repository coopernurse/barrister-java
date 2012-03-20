package com.bitmechanic.barrister;

import java.util.Map;

public class InProcClient implements Client {

    private Server server;

    public InProcClient(Server s) {
        this.server = s;
    }

    public RpcResponse request(RpcRequest req) throws RpcException {
        return server.call(req);
    }

}