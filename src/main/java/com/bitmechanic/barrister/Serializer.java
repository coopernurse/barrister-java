package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface Serializer {

    public List readList(InputStream is) throws IOException;
    public Map readMap(InputStream is) throws IOException;
    public Object readMapOrList(InputStream is) throws IOException;
    public void write(Map map, OutputStream os) throws IOException;
    public void write(List list, OutputStream os) throws IOException;

}