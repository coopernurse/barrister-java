package com.bitmechanic.barrister.conform;

import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.test.conform.*;

public class BImpl implements B {
    public String echo(String s) throws RpcException {
        if (s.equals("return-null"))
            return null;
        else
            return s;
    }
}