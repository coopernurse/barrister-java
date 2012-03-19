package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Serializer {

    public byte[] serialize(Object o);

    public List<Map<String,Object>> readList(InputStream is) throws IOException;

}