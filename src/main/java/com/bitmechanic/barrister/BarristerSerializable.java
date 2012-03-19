package com.bitmechanic.barrister;

import java.util.Map;

public interface BarristerSerializable {

    public Map<String,Object> serialize();
    public void deserialize(Map<String,Object> map);

}