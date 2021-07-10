package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a JSON-RPC response
 */
public class RpcResponse {

    private String id;
    private Object result;
    private RpcException error;

    /**
     * Creates a new RpcRequest and unmarshals the result/error fields based on the
     * map.
     *
     * @param req RpcRequest that this response correlates with
     * @param contract Contract that this request is associated with.  Used to validate the
     *        response result.
     * @param map JSON-RPC representation of RpcResponse with possible keys: 'id', 'result', 'error'
     */
    public RpcResponse(RpcRequest req, Contract contract, Map map) throws RpcException {
        unmarshal(contract.getFunction(req.getIface(), req.getFunc()), map);
    }

    /**
     * Creates a new RpcResponse associated with the given request and result object.  This 
     * represents a "successful" call.
     *
     * @param req RpcRequest that this response correlates with
     * @param result Java result from the request method invocation. This object should be the 
     *        native Java type, not the marshaled representation.
     */
    public RpcResponse(RpcRequest req, Object result) {
        setId(req);
        this.result = result;
    }

    /**
     * Creates a new RpcResponse that failed
     *
     * @param req RpcRequest that this response correlates with
     * @param error RpcException that describes the error that occurred when processing this request
     */
    public RpcResponse(RpcRequest req, RpcException error) {
        setId(req);
        this.error = error;
    }

    private void setId(RpcRequest req) {
        if (req != null)
            id = req.getId();
    }

    /**
     * Returns the id associated with this response. It is derived from the RpcRequest passed
     * to the constructor
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the result associated with this response.  This is the unmarshaled native
     * Java type
     */
    public Object getResult() {
        return result;
    }

    /**
     * Returns the RpcException associated with this response.
     */
    public RpcException getError() {
        return error;
    }

    /**
     * Marshals this response to a Map that can be serialized
     */
    @SuppressWarnings("unchecked") 
    public Map marshal() {
        HashMap map = new HashMap();
        map.put("jsonrpc", "2.0");
        if (id != null)
            map.put("id", id);
        if (error != null)
            map.put("error", error.toMap());
        else
            map.put("result", result);
        return map;
    }

    private void unmarshal(Function func, Map map) throws RpcException {
        id = (String)map.get("id");
        
        Object res = map.get("result");
        if (res != null) {
            result = func.unmarshalResult(res);
        }
        
        Object err = map.get("error");
        if (err != null) {
            if (err instanceof Map) {
                Map errMap = (Map)err;
                int code = RpcException.Error.UNKNOWN.getCode();
                try { code = Integer.parseInt(String.valueOf(errMap.get("code"))); }
                catch (NumberFormatException e) { }
                error = new RpcException(code, (String)errMap.get("message"), errMap.get("data"));
            }
            else {
                String msg = "Invalid error in response. Expected Map. Got: " + 
                    err.getClass().getName();
                throw RpcException.Error.INVALID_RESP.exc(msg);
            }
        }
    }

    @Override
    public String toString() {
        return "RpcResponse: id=" + id + " error=" + error + " result=" + result;
    }

}