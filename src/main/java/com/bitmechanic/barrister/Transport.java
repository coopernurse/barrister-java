package com.bitmechanic.barrister;

import java.util.Map;

public interface Transport {

    public Map<String,Object> request(Map<String, Object> req) throws RPCException;

}