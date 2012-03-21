package com.bitmechanic.barrister;

public interface TypeConverter {

    public Class getTypeClass();
    public Object fromRequest(String pkg, Object o) throws RpcException;
    public Object toResponse(Object o) throws RpcException;

}