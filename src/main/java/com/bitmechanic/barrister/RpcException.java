package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;

public class RpcException extends Exception {

    public enum Error {
        INVALID_REQ(-32600), PARSE(-32700), METHOD_NOT_FOUND(-32601),
            INVALID_PARAMS(-32602), INTERNAL(-32603), 
            UNKNOWN(-32000), INVALID_RESP(-32001);

        /////

        private int code;
            
        Error(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public RpcException exc(String msg) {
            return new RpcException(code, msg);
        }

    }


    private int code;
    private String message;
    private Object data;

    public RpcException(int code, String message) {
        this(code, message, null);
    }

    public RpcException(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public Map toMap() {
        HashMap map = new HashMap();
        map.put("code", code);
        map.put("message", message);
        if (data != null)
            map.put("data", data);
        return map;
    }

    @Override
    public String toString() {
        return "RpcException: code=" + code + " message=" + message +
            " data=" + data;
    }
}