package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.impl.JsonWriteContext;
import org.codehaus.jackson.JsonGenerationException;

/**
 * Serializer implementation that uses Jackson to encode/decode JSON.  The only interesting bit
 * is that JsonGenerator.Feature.ESCAPE_NON_ASCII is enabled to ensure that serialized values
 * are ASCII clean.
 *
 * @see <a href="http://jackson.codehaus.org/"> Jackson Home Page
 */
public class JacksonSerializer implements Serializer
{

    private JsonFactory jsonFactory;

    public JacksonSerializer() {
        jsonFactory = new JsonFactory();
    }
        
    public Map readMap(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, Map.class);
    }

    public List readList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, List.class);
    }

    public Object readMapOrList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, Object.class);
    }

    public void write(Map map, OutputStream os) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator gen = jsonFactory.createJsonGenerator(os);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.writeValue(gen, map);
        gen.close();
    }

    public void write(List list, OutputStream os) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator gen = jsonFactory.createJsonGenerator(os);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.writeValue(gen, list);
        gen.close();
    }

}
