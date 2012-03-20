package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class JacksonSerializerTest {

    JacksonSerializer ser = new JacksonSerializer();

    @Test
    public void canParseRequestIdAndMethod() throws Exception {
        String json = "{ \"id\":\"123\", \"method\": \"MyService.echo\" }";

        RpcRequest req = ser.readRequest(json.getBytes("utf-8"));
        assertEquals("123", req.getId());
        assertEquals("MyService", req.getIface());
        assertEquals("echo", req.getFunc());
    }

    @Test
    public void canParseParams() throws Exception {
        Object[][] tests = new Object[][] {
            new Object[] { "\"hi\"", "hi", String.class },
            new Object[] { "12", 12L, Long.class },
            new Object[] { "99.1", 99.1, double.class },
            new Object[] { "true", true, boolean.class },
            new Object[] { "{ \"x\":\"blah\", \"y\":33 }", 
                           new Person("blah", 33L), Person.class },
            new Object[] { "[1,2,3]", 1L, Long.class },
            new Object[] { "[[1,2,3]]", new Long[] { 1L, 2L, 3L }, Long[].class } 
        };

        int i = 0;
        for (Object[] t : tests) { 
            String json = "{ \"id\":\"" + i + "\", \"method\": \"foo.bar\", \"params\": " +
                t[0] + "}";

            RpcRequest req = ser.readRequest(json.getBytes("utf-8"));
            Object val = req.nextParam((Class)t[2]);
            if (((Class)t[2]).isArray()) {
                Object[] exp = (Object[])t[1];
                Object[] actual = (Object[])val;
                assertEquals(exp.length, actual.length);
                for (int x = 0; x < exp.length; x++) {
                    assertEquals(exp[x], actual[x]);
                }
            }
            else {
                assertEquals(t[1], val);
            }
            i++;
        }
    }

}

class Person {

    private String x;
    private Long y;

    public Person() { } 
    public Person(String x, Long y) { this.x = x; this.y = y; }

    public void setY(long val) { y = val; }
    public void setX(String val) { x = val; }

    public String getX() { return x; }
    public Long getY() { return y; }
   
    @Override
    public boolean equals(Object other) {
        Person p = (Person)other;
        return p.x.equals(x) && p.y.equals(y);
    }

    @Override
    public String toString() { return "x="+x + " y=" + y; }
}