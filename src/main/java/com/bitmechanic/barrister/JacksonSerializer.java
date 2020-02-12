package com.bitmechanic.barrister;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializer implementation that uses Jackson to encode/decode JSON.  The only interesting bit
 * is that JsonGenerator.Feature.ESCAPE_NON_ASCII is enabled to ensure that serialized values
 * are ASCII clean.
 *
 * @see <a href="http://jackson.codehaus.org/">Jackson Home Page</a>
 */
public class JacksonSerializer implements Serializer
{

    private static final ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private JsonFactory jsonFactory;

    public JacksonSerializer() {
        jsonFactory = new JsonFactory();
    }
        
    public Map readMap(InputStream is) throws IOException {
        return mapper.readValue(is, Map.class);
    }

    public List readList(InputStream is) throws IOException {
        return mapper.readValue(is, List.class);
    }

    public Object readMapOrList(InputStream is) throws IOException {
        return mapper.readValue(is, Object.class);
    }

    public void write(Map map, OutputStream os) throws IOException {
        JsonGenerator gen = jsonFactory.createJsonGenerator(os);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.writeValue(gen, map);
        gen.close();
    }

    public void write(List list, OutputStream os) throws IOException {
        JsonGenerator gen = jsonFactory.createJsonGenerator(os);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);        
        mapper.writeValue(gen, list);
        gen.close();
    }

}
