package com.bitmechanic.barrister;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ReflectionHandler implements Handler {

    private Object target;

    public ReflectionHandler(Object o) {
        this.target = o;
    }

    public Object call(RpcRequest req) throws RpcException {
        String func = req.getFunc();
        Method m = getMethod(func);

        Class expParam[] = m.getParameterTypes();
        if (expParam.length != req.getParamCount()) {
            String msg = "Method '" + func + "' expects " + expParam.length +
                " params but received " + req.getParamCount();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        Object params[] = new Object[expParam.length];
        for (int i = 0; i < expParam.length; i++) {
            Class clz = expParam[i];
            if (clz == String.class)
                params[i] = req.getString(i);
            else if (clz == Long.class || clz == long.class)
                params[i] = req.getLong(i);
            else if (clz == Double.class || clz == double.class)
                params[i] = req.getDouble(i);
            else if (clz == Boolean.class || clz == boolean.class)
                params[i] = req.getBool(i);
            else if (Util.implementsIface(clz, java.util.List.class)) {
                params[i] = req.getList(i);
            }
            else if (Util.implementsIface(clz, BarristerSerializable.class)) {
                Map obj = req.getMap(i);
                try {
                    params[i] = clz.newInstance();
                    ((BarristerSerializable)params[i]).deserialize(obj);
                }
                catch (InstantiationException e) {
                    throw RpcException.Error.INTERNAL.exc(e.getMessage());
                }
                catch (IllegalAccessException e) {
                    throw RpcException.Error.INTERNAL.exc(e.getMessage());
                }
            }
            else {
                String msg = "Unable to convert param: " + clz.getName();
                throw RpcException.Error.INVALID_PARAMS.exc(msg);
            }
        }

        try {
            return m.invoke(target, params);
        }
        catch (IllegalAccessException e) {
            throw RpcException.Error.INTERNAL.exc(e.getMessage());
        }
        catch (InvocationTargetException e) {
            throw RpcException.Error.INTERNAL.exc(e.getMessage());
        }
    }

    private Method getMethod(String func) throws RpcException {
        Method methods[] = target.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(func)) {
                return m;
            }
        }
        
        String msg = "Method '" + func + "' not found on class " + 
            target.getClass().getName();
        throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
    }

}