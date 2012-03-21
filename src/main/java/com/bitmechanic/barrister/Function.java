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

    public Function(String name) {
        this.name = name;
    }

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

    public Object validateAndInvoke(RpcRequest req, Object handler) throws Exception {
        if (contract == null) {
            throw new IllegalStateException("contract cannot be null");
        }

        Method method   = getMethod(handler);
        Class pTypes[]  = method.getParameterTypes();
        Object params[] = new Object[pTypes.length];

        int i = 0;
        for (Class c : pTypes) {
            if (!req.hasNextParam()) {
                String msg = "Function '" + getMethodName(req) + 
                    "' expects " + pTypes.length + " param(s). " + i + " given.";
                throw RpcException.Error.INVALID_PARAMS.exc(msg);
            }

            try {
                params[i] = req.nextParam(c);
            }
            catch (Exception e) {
                String msg = "Unable to convert param " + getMethodName(req)
                    + "[" + i + "] - " + e.getMessage();
                throw RpcException.Error.INVALID_PARAMS.exc(msg);
            }
            i++;
        }

        if (req.hasNextParam()) {
            String msg = "Function '" + getMethodName(req) + 
                    "' expects " + pTypes.length + " param(s). More were given.";
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        return method.invoke(handler, params);
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

}