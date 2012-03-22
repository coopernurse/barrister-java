package com.bitmechanic.barrister;

public interface TypeConverter {

    public Class getTypeClass();
    public Object unmarshal(String pkg, Object o) throws RpcException;
    public Object marshal(Object o) throws RpcException;

}