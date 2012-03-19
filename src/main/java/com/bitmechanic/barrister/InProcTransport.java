package com.bitmechanic.barrister;

import java.util.Map;

public class InProcTransport implements Transport {

    private Server server;

    public InProcTransport(Server s) {
        this.server = s;
    }

    public Map<String,Object> request(Map<String,Object> req) throws RpcException {
        return server.call(req);
    }

}