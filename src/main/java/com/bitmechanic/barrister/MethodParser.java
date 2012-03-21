package com.bitmechanic.barrister;

public class MethodParser {

    public static String toMethod(String iface, String func) {
        return iface + "." + func;
    }

    ////////////////////////

    private String iface;
    private String func;

    public MethodParser(String method) {
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

    public String getIface() {
        return iface;
    }

    public String getFunc() {
        return func;
    }

}