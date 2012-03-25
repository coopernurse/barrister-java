package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

public class FunctionTest {

    Contract c;

    public FunctionTest() {
        c = new Contract();
        c.setValidateRequest(false);
        c.setValidateResponse(false);
    }

    @Test
    public void invokeEmpty() throws Exception {
        Function f = new Function("hi", "string", false);
        f.setContract(c);
        assertEquals("hi", f.invoke(req("hi", null), new Foo()));
    }

    @Test
    public void invokeStringParam() throws Exception {
        Function f = new Function("echo", "string", false);
        f.getParams().add(new Field("a", "string", false));
        f.setContract(c);
        assertEquals("yo", f.invoke(req("echo", "yo"), new Foo()));
    }

    @Test
    public void invokeArrayParam() throws Exception {
        Function f = new Function("add", "int", false);
        f.getParams().add(new Field("a", "int", true));
        f.setContract(c);
        Object param = new Long[] {1L, 8L, 1L };
        assertEquals(10L, f.invoke(req("add", asList(param)), new Foo()));
    }

    private RpcRequest req(String func, Object params) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", "myid");
        map.put("method", "iface."+func);
        map.put("params", params);
        return new RpcRequest(map);
    }

    private List asList(Object... args) {
        ArrayList list = new ArrayList();
        for (Object o : args) {
            list.add(o);
        }
        return list;
    }

    class Foo {
        public String hi() { return "hi"; }
        public String echo(String s) { return s; }
        public long add(Long[] list) { 
            long total = 0;
            for (Long x : list) { total += x; }
            return total;
        }
    }

}