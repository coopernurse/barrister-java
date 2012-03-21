package com.bitmechanic.barrister;

import java.lang.reflect.Method;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Server {

    private static Logger logger = Logger.getLogger("barrister");

    private Contract contract;
    private Map<String, Object> handlers;    

    public Server(Contract c) {
        this.contract = c;
        this.handlers = new HashMap<String,Object>();
    }

    public Contract getContract() {
        return this.contract;
    }

    public void addHandler(String iface, Object handler) {
        handlers.put(iface, handler);
    }

    public RpcResponse call(RpcRequest req) throws RpcException {
        if (req.getFunc().equals("barrister-idl")) {
            return new RpcResponse(req, contract.getIdl());
        }

        RpcResponse resp = null;
        try {
            Function func = getFunction(req);
            Object handler = handlers.get(req.getIface());
            if (handler == null) {
                String msg = "No implementation of '" + req.getIface() + "' found";
                throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
            }

            Object result = func.validateAndInvoke(req, handler);
            resp = new RpcResponse(req, result);
        }
        catch (RpcException e) {
            resp = new RpcResponse(req, e);
        }
        catch (Throwable t) {
            t.printStackTrace();
            logger.throwing("Server", "call", t);
            resp = new RpcResponse(req, RpcException.Error.UNKNOWN.exc(t.getMessage()));
        }

        return resp;
    }

    private Function getFunction(RpcRequest req) throws RpcException {
        Interface iface = contract.getInterfaces().get(req.getIface());
        if (iface == null) {
            String msg = "No implementation of '" + req.getIface() + "' found";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        Function func = iface.getFunction(req.getFunc());
        if (func == null) {
            String msg = "Function '" + req.getFunc() + "' not found in '" +
                req.getIface() + "'";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        return func;
    }

    /*
    private Object serialize(Object result) throws RpcException {
        if (result == null) {
            return null;
        }

        Class clz = result.getClass();
        if (clz == String.class || clz == Short.class || clz == Integer.class ||
            clz == Long.class || clz == Float.class || clz == Double.class ||
            clz == Boolean.class || clz == short.class || clz == int.class ||
            clz == long.class || clz == float.class || clz == double.class ||
            clz == boolean.class) {

            return result;
        }
        else if (result instanceof List) {
            List list = (List)result;
            List tmp = new ArrayList<Object>();
            for (Object o : list) {
                tmp.add(serialize(o));
            }
            return tmp;
        }
        else if (result instanceof BarristerSerializable) {
            return ((BarristerSerializable)result).serialize();
        }
        else {
            String msg = "Class " + clz.getName() + " is not serializable";
            throw RpcException.Error.INVALID_RESP.exc(msg);
        }
    }
    */

}