package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class FunctionTest {

    Contract c;

    public FunctionTest() {
        c = new Contract();
        c.setValidateRequest(false);
        c.setValidateResponse(false);
    }

    @Test
    public void invokeEmpty() throws Exception {
        Function f = new Function("hi");
        f.setContract(c);
        assertEquals("hi", f.validateAndInvoke(new RpcRequestBean("myid", "iface", "hi"),
                                               new Foo()));
    }

    @Test
    public void invokeStringParam() throws Exception {
        Function f = new Function("echo");
        f.setContract(c);
        assertEquals("yo", f.validateAndInvoke(req("echo", "yo"), new Foo()));
    }

    @Test
    public void invokeArrayParam() throws Exception {
        Function f = new Function("add");
        f.setContract(c);
        assertEquals(10L, f.validateAndInvoke(req("add", new Long[] {1L, 8L, 1L }), new Foo()));
    }

    private RpcRequestBean req(String func, Object params) {
        return new RpcRequestBean("myid", "iface", func, params);
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