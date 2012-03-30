package com.bitmechanic.barrister.conform;

import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.barrister.RpcRequest;
import com.bitmechanic.test.*;

public class BImpl implements B {
    public String echo(String s) throws RpcException {
        if (s.equals("return-null"))
            return null;
        else
            return s;
    }
}