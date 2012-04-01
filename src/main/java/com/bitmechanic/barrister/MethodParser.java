package com.bitmechanic.barrister;

/**
 * Utility class for tokenizing JSON-RPC "method" values into interface/function strings
 *
 * e.g.  method "FooService.add" becomes iface="FooService", func="add"
 */
public class MethodParser {

    /**
     * Concatenates iface and func into a string, separated by a period
     */
    public static String toMethod(String iface, String func) {
        return iface + "." + func;
    }

    ////////////////////////

    private String iface;
    private String func;
    private String method;

    /**
     * Tokenizes the method into iface/func parts, period delimited
     *
     * If method has no period, iface is set to empty string and entire
     * String is set as func.  This is used for internal Barrister methods
     * like "barrister-idl"
     *
     * @param method Method to parse. e.g. "FooService.add"
     */
    public MethodParser(String method) {
        this.method = method;

        int pos = method.indexOf(".");
        if (pos < 1) {
            this.iface = "";
            this.func = method;
        }
        else {
            this.iface = method.substring(0, pos);
            this.func = method.substring(pos+1);
        }
    }

    /**
     * Returns raw method passed to constructor
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns interface part of method
     */
    public String getIface() {
        return iface;
    }

    /**
     * Returns function part of method
     */
    public String getFunc() {
        return func;
    }

}