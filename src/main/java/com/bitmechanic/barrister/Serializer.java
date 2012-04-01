package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface responsible for serializing requests and responses to streams.
 * JSON (via Jackson) is the default implementation, but other serializers
 * that support similar semantics could also be implemented (e.g. MessagePack and BSON).
 *
 * Note that the Serializer is not responsible for marshaling to/from Java objects.
 * The TypeConverter classes handle that to ensure that it's done uniformly.
 */
public interface Serializer {

    /**
     * Reads the stream as a Java List
     * 
     * @throws IOException If there's a problem reading the stream, or if
     *         the stream does not represent a List
     */
    public List readList(InputStream is) throws IOException;

    /**
     * Reads the stream as a Java Map
     * 
     * @throws IOException If there's a problem reading the stream, or if
     *         the stream does not represent a Map
     */
    public Map readMap(InputStream is) throws IOException;

    /**
     * Reads the stream as a Java Map or List.  The implementation should
     * make a best effort to figure out which data structure to use.
     * 
     * @throws IOException If there's a problem reading the stream, or if
     *         the stream does not represent a Map or List
     */
    public Object readMapOrList(InputStream is) throws IOException;

    /**
     * Serializes the map to the given stream
     */
    public void write(Map map, OutputStream os) throws IOException;

    /**
     * Serializes the list to the given stream
     */
    public void write(List list, OutputStream os) throws IOException;

}