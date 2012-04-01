package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a JSON-RPC error.  Errors have three parts:
 *
 * <ul>
 *   <li>code</li> Integer that identifies the error type
 *   <li>message</li> String description of the error
 *   <li>data</li> Optional additional info about the error. This will be serialized
 *   verbatim, so we suggest sticking to Java primitive types, or List/Maps of primitives, 
 *   otherwise the serialization behavior may be undefined.
 * </ul>
 */
public class RpcException extends Exception {

    /**
     * Enum of JSON-RPC standard error types.  Used to generate internal
     * errors.
     */
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

    /**
     * Creates a new RpcException
     *
     * @param code Error code
     * @param message Error message
     */
    public RpcException(int code, String message) {
        this(code, message, null);
    }

    /**
     * Creates a new RpcException
     *
     * @param code Error code
     * @param message Error message
     * @param data Additional error information. Please use primitives or Maps/Lists of 
     *        primitives
     */
    public RpcException(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Returns the error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the optional data 
     */
    public Object getData() {
        return data;
    }

    /**
     * Used to marshal this exception to a Map suiteable for serialization to JSON
     */
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