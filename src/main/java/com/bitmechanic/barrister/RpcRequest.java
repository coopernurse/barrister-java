package com.bitmechanic.barrister;

import java.io.IOException;
import java.util.List;

public interface RpcRequest {

    public String getId();
    public String getFunc();
    public String getIface();
    public boolean hasNextParam();
    public Object nextParam(Class t) throws IOException;

}