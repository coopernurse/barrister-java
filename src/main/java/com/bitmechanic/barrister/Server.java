package com.bitmechanic.barrister;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Server {

    private static Logger logger = Logger.getLogger("barrister");

    private Contract contract;
    private Map<String, Handler> handlers;    

    public Server(Contract c) {
        this.contract = c;
        this.handlers = new HashMap<String,Handler>();
    }

    public Contract getContract() {
        return this.contract;
    }

    public void addHandler(String iface, Handler handler) {
        handlers.put(iface, handler);
    }

    public Map<String,Object> call(Map<String,Object> rawReq) throws RPCException {
        String reqid = (String)rawReq.get("id");

        try {
            RpcRequest req = new RpcRequest(rawReq);
            Handler handler = handlers.get(req.getIface());
            if (handler == null) {
                String msg = "No implementation of '" + req.getIface() + "' found";
                throw RPCException.Error.METHOD_NOT_FOUND.exc(msg);
            }

            Object result = handler.call(req);
            return ok(reqid, result);
        }
        catch (RPCException e) {
            return err(reqid, e);
        }
        catch (Throwable t) {
            logger.throwing("Server", "call", t);
            return err(reqid, RPCException.Error.UNKNOWN.exc(t.getMessage()));
        }
    }

    private Map<String,Object> ok(String reqid, Object result) throws RPCException {
        Map<String,Object> map = init(reqid);
        if (result != null) {
            map.put("result", serialize(result));
        }

        return map;
    }

    private Object serialize(Object result) throws RPCException {
        if (result == null) {
            return null;
        }

        Class clz = result.getClass();
        if (clz == String.class || clz == Short.class || clz == Integer.class ||
            clz == Long.class || clz == Float.class || clz == Double.class ||
            clz == Boolean.class) {
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
            throw RPCException.Error.INVALID_RESP.exc(msg);
        }
    }

    private Map<String,Object> err(String reqid, RPCException exc) {
        Map<String,Object> map = init(reqid);
        map.put("error", exc.toMap());

        return map;
    }

    private Map<String,Object> init(String reqid) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("jsonrpc", "2.0");
        if (reqid != null) {
            map.put("id", reqid);
        }
        return map;
    }

}