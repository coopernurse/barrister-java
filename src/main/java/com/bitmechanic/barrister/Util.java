package com.bitmechanic.barrister;

public class Util {

    public static boolean implementsIface(Class clz, Class iface) {
        if (clz == iface) {
            return true;
        }

        for (Class c : clz.getInterfaces()) {
            if (c == iface)
                return true;
        }
        return false;
    }

}