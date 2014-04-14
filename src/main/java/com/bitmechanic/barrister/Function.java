package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;

/**
 * Returns a single function in an Interface.
 */
public class Function extends BaseEntity {    

    private List<Field> params;
    private Field returns;

    /**
     * Creates a Function with the given name and return type.  
     * Creates an empty param list.
     */
    public Function(String name, Field returns) {
        this.name = name;
        this.params = new ArrayList<Field>();
        this.returns = returns;
    }

    /**
     * Creates a Function based on the parsed IDL Map representation.
     * Uses keys: 'name', 'comment', 'params', 'returns'
     */
    @SuppressWarnings("unchecked") 
    public Function(Map<String,Object> data) {
        super(data);
     
        params = new ArrayList<Field>();
        
        List<Map<String,Object>> plist = (List<Map<String,Object>>)data.get("params");
        for (Map<String,Object> p : plist) {
            params.add(new Field(p));
        }

        Map retMap = (Map)data.get("returns");
        returns = new Field("", (String)retMap.get("type"), 
                            (Boolean)retMap.get("is_array"), (Boolean)retMap.get("optional"));
    }

    /**
     * Returns the parameters for this Function
     */
    public List<Field> getParams() {
        return params;
    }

    /**
     * Returns the return type for this Function
     */
    public Field getReturns() {
        return returns;
    }

    /**
     * Sets the Contract for this Function.  Propegates this down to its param and
     * return Fields
     */
    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Field f : params) {
            f.setContract(c);
        }
        returns.setContract(c);
    }

    /**
     * Invokes this Function against the given handler Class for the given request. 
     * This is the heart of the RPC dispatch, and is where your application code gets run.
     *
     * @param req Request to invoke against handler. The req.params will be unmarshaled and
     *        used when calling the Java method on handler.
     * @param handler Your application class that implements the given interface.  The method
     *        on handler that matches this Function's name will be invoked.
     * @return Result of Java method invocation, marshaled to its RPC result type
     * @throws RpcException If the req.params do not match the parameters for this function as
     *         specified in the IDL
     * @throws IllegalAccessException If there is a problem invoking the method on handler
     * @throws InvocationTargetException If there is a problem invoking the method on handler
     */
    public Object invoke(RpcRequest req, Object handler)
        throws RpcException, IllegalAccessException, InvocationTargetException {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("req cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        Method method      = getMethod(handler);
        Object reqParams[] = unmarshalParams(req, method);

        return marshalResult(method.invoke(handler, reqParams));
    }

    /**
     * Marshals the req.params to their RPC format equivalents.  For example, a Java Struct
     * class will be converted to a Map.
     *
     * @param req RpcRequest to marshal params for
     * @return RPC representation of params in req
     * @throws RpcException If param length differs from expected param length for this function,
     *         or if any parameters fail IDL type validation
     */
    public Object[] marshalParams(RpcRequest req) throws RpcException {
        Object[] converted = new Object[params.size()];
        Object[] reqParams = req.getParams();
        if (reqParams.length != converted.length) {
            String msg = "Function '" + req.getMethod() + "' expects " + 
                   params.size() + " param(s). " + reqParams.length + " given.";
            throw invParams(msg);
        }
        
        for (int i = 0; i < converted.length; i++) {
            converted[i] = params.get(i).getTypeConverter().marshal(reqParams[i]);
        }
        
        return converted;
    }

    /**
     * Unmarshals respObj into its Java representation
     *
     * @throws RpcException if respObj does not match IDL type validation
     */
    public Object unmarshalResult(Object respObj) throws RpcException {
        return returns.getTypeConverter().unmarshal(respObj);
    }

    private Object[] unmarshalParams(RpcRequest req, Method method) throws RpcException {
        Class pTypes[]  = method.getParameterTypes();
        if (params.size() != pTypes.length) {
            String msg = "Param mismatch for: " + req.getMethod() + " - Java expects " +
                 pTypes.length + " param(s), But IDL expects: " + params.size() + 
                " - Make sure IDL and generated Java classes are in sync";
            throw invParams(msg);
        }

        return unmarshalParams(req);
    }

    /**
     * Unmarshals req.params into their Java representations
     *
     * @throws RpcException if req.params length does not match this Function's expected
     *         param list length, or if any parameter fails IDL type validation
     */
    public Object[] unmarshalParams(RpcRequest req) throws RpcException {
        Object reqParams[] = req.getParams();
        if (reqParams.length != params.size()) {
            String msg = "Function '" + req.getMethod() + "' expects " + 
               params.size() + " param(s). " + reqParams.length + " given.";
            throw invParams(msg);
        }

        Object convParams[] = new Object[reqParams.length];
        for (int i = 0; i < convParams.length; i++) {
            convParams[i] = params.get(i).getTypeConverter().unmarshal(reqParams[i]);
        }

        return convParams;
    }

    private Object marshalResult(Object res) throws RpcException {
        return returns.getTypeConverter().marshal(res);
    }

    private String getMethodName(RpcRequest req) {
        return req.getIface() + "." + req.getFunc();
    }

    private Method getMethod(Object handler) throws RpcException {
        Method methods[] = handler.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name))
                return m;
        }

        String msg = "Class '" + handler.getClass().getName() + 
            "' does not contain function: '" + name + "'";
        throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
    }

    private RpcException invParams(String msg) {
        return RpcException.Error.INVALID_PARAMS.exc(msg);
    }

}
