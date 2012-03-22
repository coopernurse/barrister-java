package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

public class Function extends BaseEntity {    

    private List<Field> params;
    private Field returns;

    public Function(String name, String returns) {
        this.name = name;
        this.params = new ArrayList<Field>();
        this.returns = new Field("", returns);
    }

    @SuppressWarnings("unchecked") 
    public Function(Map<String,Object> data) {
        super(data);
     
        params = new ArrayList<Field>();
        
        List<Map<String,Object>> plist = (List<Map<String,Object>>)data.get("params");
        for (Map<String,Object> p : plist) {
            params.add(new Field(p));
        }

        returns = new Field("", (String)data.get("returns"));
    }

    public List<Field> getParams() {
        return params;
    }

    public Field getReturns() {
        return returns;
    }

    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Field f : params) {
            f.setContract(c);
        }
        returns.setContract(c);
    }

    public Object invoke(RpcRequest req, Object handler) throws Exception {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }

        Method method      = getMethod(handler);
        Object reqParams[] = unmarshalParams(req, method);

        return marshalResult(method.invoke(handler, reqParams));
    }

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

    public Object unmarshalResult(Object respObj) throws RpcException {
        return returns.getTypeConverter().unmarshal(contract.getPackage(), respObj);
    }

    private Object[] unmarshalParams(RpcRequest req, Method method) throws RpcException {
        Class pTypes[]  = method.getParameterTypes();
        if (params.size() != pTypes.length) {
            String mname = method.getDeclaringClass().getName() + "." + method.getName();
            String msg = "Param mismatch for: " + req.getMethod() + " - Java expects " +
                 pTypes.length + " param(s), But IDL expects: " + params.size() + 
                " - Make sure IDL and generated Java classes are in sync";
            throw invParams(msg);
        }

        return unmarshalParams(req);
    }

    public Object[] unmarshalParams(RpcRequest req) throws RpcException {
        Object reqParams[] = req.getParams();
        if (reqParams.length != params.size()) {
            String msg = "Function '" + req.getMethod() + "' expects " + 
               params.size() + " param(s). " + reqParams.length + " given.";
            throw invParams(msg);
        }

        String pkg = contract.getPackage();

        Object convParams[] = new Object[reqParams.length];
        for (int i = 0; i < convParams.length; i++) {
            convParams[i] = params.get(i).getTypeConverter().unmarshal(pkg, reqParams[i]);
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