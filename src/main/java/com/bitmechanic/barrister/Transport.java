package com.bitmechanic.barrister;

import java.util.List;

/**
 * Client Transport interface that encapsulates how to send a request to a 
 * server.  Both single requests and batches are supported.
 *
 * Transports load the Contract from the server using the 'barrister-idl' method.
 */
public interface Transport {

    /**
     * Returns the Contract loaded by this Transport
     */
    public Contract getContract();

    /**
     * Makes a single request against the server.  This call MUST be thread safe.
     */
    public RpcResponse request(RpcRequest req);

    /**
     * Makes a batch request against the server.  Implementations should
     * ensure that the order of the response list matches the order of the reqList.
     * That is, resp.get(0).getId().equals(reqList.get(0).getId()), and so on.
     *
     * This call MUST be thread safe.
     */
    public List<RpcResponse> request(List<RpcRequest> reqList);

}
