package com.bitmechanic.barrister;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Dispatches incoming RpcRequests to your application code.
 */
public class Server {

    private static Logger logger = Logger.getLogger("barrister");

    private Contract contract;
    private Map<String, Object> handlers;    

    /**
     * Creates a new Server for the given Contract
     */
    public Server(Contract c) {
        if (c == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }

        this.contract = c;
        this.handlers = new HashMap<String,Object>();
    }

    /** 
     * Returns the Contract associated with this server
     */
    public Contract getContract() {
        return this.contract;
    }

    /**
     * Associates the handler instance with the given IDL interface.  Replaces
     * an existing handler for this iface if one was previously registered.
     *
     * @param iface Interface class that this handler implements.  This is usually
     *        an Idl2Java generated interface Class
     * @param handler Object that implements iface.  Generally one of your application classes
     * @throws IllegalArgumentException if iface is not an interface on this Server's Contract
     *         or if handler cannot be cast to iface
     */
    public void addHandler(Class iface, Object handler) {
        try {
            iface.cast(handler);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Handler: " + handler.getClass().getName() +
                                               " does not implement: " + iface.getName());
        }

        if (contract.getInterfaces().get(iface.getSimpleName()) == null) {
            throw new IllegalArgumentException("Interface: " + iface.getName() + 
                                               " is not a part of this Contract");
        }

        contract.setPackage(iface.getPackage().getName());

        handlers.put(iface.getSimpleName(), handler);
    }

    /**
     * Reads a RpcRequest from the input stream, deserializes it, invokes
     * the matching handler method, serializes the result, and writes it to the output stream.
     *
     * @param ser Serializer to use to decode the request from the input stream, and serialize
     *        the result to the the output stream
     * @param is InputStream to read the request from
     * @param os OutputStream to write the response to
     * @throws IOException If there is a problem reading or writing to either stream, or if the
     *         request cannot be deserialized.
     */
    @SuppressWarnings("unchecked")
    public void call(Serializer ser, InputStream is, OutputStream os) 
        throws IOException {

        Object obj = null;
        try {
            obj = ser.readMapOrList(is);
        }
        catch (Exception e) {
            String msg = "Unable to deserialize request: " + e.getMessage();
            ser.write(new RpcResponse(null, RpcException.Error.PARSE.exc(msg)).marshal(), os);
            return;
        }
            
        if (obj instanceof List) {
            List list = (List)obj;
            List respList = new ArrayList();
            for (Object o : list) {
                RpcRequest rpcReq = new RpcRequest((Map)o);
                respList.add(call(rpcReq).marshal());
            }
            ser.write(respList, os);
        }
        else if (obj instanceof Map) {
            RpcRequest rpcReq = new RpcRequest((Map)obj);
            ser.write(call(rpcReq).marshal(), os);
        }
        else {
            ser.write(new RpcResponse(null, RpcException.Error.INVALID_REQ.exc("Invalid Request")).marshal(), os);
        }
    }

    /**
     * Calls the method associated with the RpcRequest and wraps the result as a RpcResponse
     *
     * @param req Request to process
     * @return RpcResponse that pairs with this request.  May contain a result or an error
     */
    public RpcResponse call(RpcRequest req) {
        if (req.getFunc().equals("barrister-idl")) {
            return new RpcResponse(req, contract.getIdl());
        }

        RpcResponse resp = null;
        try {
            Function func = getFunction(req);
            Object handler = handlers.get(req.getIface());
            if (handler == null) {
                String msg = "No implementation of '" + req.getIface() + "' found";
                return new RpcResponse(req, RpcException.Error.METHOD_NOT_FOUND.exc(msg));
            }

            Object result = func.invoke(req, handler);
            resp = new RpcResponse(req, result);
        }
        catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RpcException) {
                resp = new RpcResponse(req, (RpcException)target);
            }
            else {
                logger.throwing("Server", "call", target);
                resp = new RpcResponse(req, RpcException.Error.UNKNOWN.exc(target.getClass().getName() + ": " + target.getMessage()));
            }
        }
        catch (Throwable t) {
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

}