package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;

public class Batch implements Transport {

    private Transport child;
    private List<RpcRequest> reqList;
    private boolean sent;

    public Batch(Transport child) {
        this.child = child;
        this.reqList = new ArrayList<RpcRequest>();
        this.sent = false;
    }

    public Contract getContract() {
        return child.getContract();
    }

    public RpcResponse request(RpcRequest req) {
        checkSent();
        reqList.add(req);
        return null;
    }

    public List<RpcResponse> request(List<RpcRequest> reqList) {
        for (RpcRequest req : reqList) {
            this.reqList.add(req);
        }

        return null;
    }

    public List<RpcResponse> send() {
        checkSent();
        sent = true;
        return child.request(reqList);
    }

    private void checkSent() {
        if (sent) {
            throw new IllegalStateException("Batch already sent!");
        }
    }

}
