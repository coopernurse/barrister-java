package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.io.IOException;

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
        // columns:
        //   - raw params JSON string
        //   - expected parsed value (or Exception if we expect an error)
        //   - Class to marshal first JSON param to
        Object[][] tests = new Object[][] {
            new Object[] { "\"hi\"", "hi", String.class },
            new Object[] { "\"hello w\\u00f6rld\"", "hello w\u00f6rld", String.class },
            new Object[] { "12", 12L, Long.class },
            new Object[] { "99.1", 99.1, double.class },
            new Object[] { "true", true, boolean.class },
            new Object[] { "{ \"x\":\"blah\", \"y\":33 }", 
                           new Person("blah", 33L), Person.class },
            new Object[] { "[1,2,3]", 1L, Long.class },
            new Object[] { "[[1,2,3]]", new Long[] { 1L, 2L, 3L }, Long[].class },
            new Object[] { "{ \"name\":\"trevor\", \"color\":\"blue\" }",
                           new Cat("trevor", Color.blue), Cat.class },
            new Object[] { "\"hi\"", IOException.class, Long.class },
            new Object[] { "{ \"name\":\"bob\", \"color\":\"unknown\" }",
                           IOException.class, Cat.class },
            new Object[] { "{ \"name\":2, \"color\":\"blue\" }",
                           IOException.class, Cat.class }
        };

        int i = 0;
        for (Object[] t : tests) { 
            String json = "{ \"id\":\"" + i + "\", \"method\": \"foo.bar\", \"params\": " +
                t[0] + "}";

            RpcRequest req = ser.readRequest(json.getBytes("utf-8"));

            if (t[1].getClass() == Class.class) {
                try {
                    req.nextParam((Class)t[2]);
                    fail("nextParam should have thrown Exception for param:" + t[0]);
                }
                catch (Exception e) {
                    //System.out.println(e.getMessage());
                    assertEquals(t[1], e.getClass());
                }
            }
            else {
                Object val = req.nextParam((Class)t[2]);
                if (((Class)t[2]).isArray()) {
                    Object[] exp = (Object[])t[1];
                    Object[] actual = (Object[])val;
                    assertEquals(exp.getClass(), actual.getClass());
                    assertEquals(exp.length, actual.length);
                    for (int x = 0; x < exp.length; x++) {
                        assertEquals(exp[x], actual[x]);
                    }
                }
                else {
                    assertEquals(t[1], val);
                }
            }
            i++;
        }
    }

    @Test
    public void canWriteResponse() throws Exception {
        Object[][] tests = new Object[][] {
            new Object[] { "hello w\u00f6rld", "\"hello w\\u00F6rld\"" },
            new Object[] { new Cat("lily", Color.white), "{\"color\":\"white\",\"name\":\"lily\"}" }
        };

        RpcRequest req = new RpcRequestBean("1", "iface", "func");
        for (Object[] t : tests) {
            String expected = "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":" + t[1] + "}";
            RpcResponse resp = new RpcResponse(req, t[0]);
            assertEquals(expected, new String(ser.writeResponse(resp), "utf-8"));
        }
    }


}

enum Color {
    blue, black, white
}

class Cat {
    Color color;
    String name;

    public Cat() { } 
    public Cat(String n, Color c) { name = n; color = c; }

    public String getName() { return name; }
    public void setName(String n) { name = n; }

    public Color getColor() { return color; }
    public void setColor(Color c) { color = c; }

    public String toString() { return "Cat: " + color + ", " + name; }
    public boolean equals(Object other) {
        if (this == other) { return true; }
        if (other == null) { return false; }
        if (!(other instanceof Cat)) { return false; }
        Cat o = (Cat)other;
        if (color != o.color && color == null) { return false; }
        else if (color != null && !color.equals(o.color)) { return false; }
        if (name != o.name && name == null) { return false; }
        else if (name != null && !name.equals(o.name)) { return false; }
        return true;
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