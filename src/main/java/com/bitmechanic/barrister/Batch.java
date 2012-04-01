package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;

/**
 * Used to batch requests to a server.  Wraps an underlying transport and
 * defers submission to the server until send() is called.
 *
 * This class is not thread safe.  Make sure to use separate batches per thread.
 */
public class Batch implements Transport {

    private Transport child;
    private List<RpcRequest> reqList;
    private boolean sent;

    /**
     * Creates a new Batch
     *
     * @param child Underlying transport to send request with
     */
    public Batch(Transport child) {
        this.child = child;
        this.reqList = new ArrayList<RpcRequest>();
        this.sent = false;
    }

    /**
     * Returns contract associated with child Transport
     */
    public Contract getContract() {
        return child.getContract();
    }

    /**
     * Adds req to internal request list
     * 
     * @param req Request to add to call batch
     * @return Always returns null
     * @throws IllegalStateException if batch has already been sent
     */
    public RpcResponse request(RpcRequest req) {
        checkSent();
        reqList.add(req);
        return null;
    }

    /**
     * Adds all requests in reqList to internal request list
     *
     * @param reqList Requests to add to call batch
     * @return Always returns null
     * @throws IllegalStateException if batch has already been sent
     */
    public List<RpcResponse> request(List<RpcRequest> reqList) {
        for (RpcRequest req : reqList) {
            this.reqList.add(req);
        }

        return null;
    }

    /**
     * Sends the batch request using the child Transport and returns
     * the responses.  Responses will be in the same order as the requests they
     * correspond to.
     *
     * @return List of responses correlated to the requests in the batch
     * @throws IllegalStateException if batch has already been sent
     */
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
