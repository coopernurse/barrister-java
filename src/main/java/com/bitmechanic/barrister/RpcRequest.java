package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a JSON-RPC request.  
 */
public class RpcRequest {

    private static final Object[] EMPTY_PARAM = new Object[0];

    private String id;
    private MethodParser method;
    private Object[] params;

    /**
     * Creates a new RpcRequest
     *
     * @param id ID of this request
     * @param method Method to invoke on the server. Barrister uses a dotted method
     *        "interface-name.function-name"
     * @param params Parameters to pass to the function. If null, an empty list is sent.
     */
    public RpcRequest(String id, String method, Object params) {
        this.id = id;
        this.method = new MethodParser(method);

        if (params == null) {
            this.params = EMPTY_PARAM;
        }
        else if (params instanceof List) {
            this.params = ((List)params).toArray();
        }
        else if (params.getClass().isArray()) {
            this.params = (Object[])params;
        }
        else {
            this.params = new Object[] { params };
        }
    }

    /**
     * Creates a new RpcRequest using Map input.
     * Keys: 'id', 'method', 'params'
     */
    public RpcRequest(Map map) {
        this((String)map.get("id"), (String)map.get("method"), map.get("params"));
    }

    /**
     * Returns the ID of this request. Used to correlated responses with requests.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the method for this request.
     */
    public String getMethod() {
        return method.getMethod();
    }

    /**
     * Returns the Barrister Function name expressed by the method
     */
    public String getFunc() {
        return method.getFunc();
    }

    /**
     * Returns the Barrister Interface name expressed by the method
     */
    public String getIface() {
        return method.getIface();
    }

    /**
     * Returns the parameters to the function for this request
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * Marshals this request to a Map that can be serialized and sent over the wire.
     * Uses the Contract to resolve the Function associated with the method.
     *
     * @param contract Contract to use to resolve Function
     * @return JSON-RPC formatted Map based on the request id, method, and params
     */
    @SuppressWarnings("unchecked") 
    public Map marshal(Contract contract) throws RpcException {
        Map map = new HashMap();
        map.put("jsonrpc", "2.0");

        if (id != null)
            map.put("id", id);

        map.put("method", method.getMethod());

        if (params != null && params.length > 0) {
            Function f = contract.getFunction(getIface(), getFunc());
            map.put("params", f.marshalParams(this));
        }

        System.out.println("req.marshal: " + map);
        return map;
    }

}