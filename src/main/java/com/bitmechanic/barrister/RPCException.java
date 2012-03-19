package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;

public class RPCException extends Exception {

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

        public RPCException exc(String msg) {
            return new RPCException(code, msg);
        }

    }


    private int code;
    private String message;
    private Object data;

    public RPCException(int code, String message) {
        this(code, message, null);
    }

    public RPCException(int code, String message, Object data) {
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

    public Map<String,Object> toMap() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("code", code);
        map.put("message", message);
        if (data != null) {
            map.put("data", data);
        }
        return map;
    }

}