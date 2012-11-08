package com.bitmechanic.barrister;

/**
 * The Filter interface provides hooks before and after individual
 * RpcRequest processing.  Register Filter implementations on Server
 * instances.
 */
public interface Filter {

    /**
     * Allows Filter to create a new RpcRequest derived from the one passed in.  
     * If the implementation does not wish to alter the request, it should return null.
     */
    public RpcRequest alterRequest(RpcRequest req);

    /**
     * Allows Filter to handle the request prior to invoking the underlying handler.
     * This is useful for tasks like authentication.
     *
     * If the Filter returns a null response, execution will continue normally.
     * If the Filter returns a non-null response, that response will be returned
     * immediately by the Server, terminating the filter execution chain.
     */
    public RpcResponse preInvoke(RpcRequest req);

    /**
     * Allows Filter to inspect and optionally mutate the response.
     *
     * If the Filter returns a null response, the null value will be ignored
     * and resp will be passed to the next Filter in the chain.
     *
     * If the Filter returns a non-null response, it will be passed to the
     * next Filter in the chain as resp.
     *
     * The last non-null RpcResponse value returned from the filter chain
     * is used as the response returned to the caller.
     */
    public RpcResponse postInvoke(RpcRequest req, RpcResponse resp);

}