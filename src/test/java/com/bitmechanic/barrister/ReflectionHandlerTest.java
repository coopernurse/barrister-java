package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public class ReflectionHandlerTest {

    ReflectionHandler handler;

    @Test
    public void canCallNoParams() throws Exception {
        init();
        RpcRequest req = RpcRequest.Builder().method("A.sayHi").build();
        assertEquals("hi", handler.call(req));
    }

    @Test
    public void canCallLongParams() throws Exception {
        init();
        RpcRequest req = RpcRequest.Builder()
            .method("A.add").param(10L).param(5).build();
        assertEquals(15L, handler.call(req));
    }

    @Test
    public void canCallDoubleParams() throws Exception {
        init();
        RpcRequest req = RpcRequest.Builder()
            .method("A.sqrt").param(16.0).build();
        assertEquals(4.0, handler.call(req));
    }

    @Test
    public void canCallBooleanParams() throws Exception {
        init();
        RpcRequest req = RpcRequest.Builder()
            .method("A.echoBool").param(true).build();
        assertEquals(true, handler.call(req));
        req = RpcRequest.Builder().method("A.echoBool").param(false).build();
        assertEquals(false, handler.call(req));
    }

    @Test
    public void canCallStringListParams() throws Exception {
        init();
        EchoRequest er = new EchoRequest();
        er.setMessage("howdy");

        RpcRequest req = RpcRequest.Builder()
            .method("A.concat").param(Arrays.asList("a","b")).build();
        assertEquals("ab", handler.call(req));
    }

    @Test
    public void canCallStructParams() throws Exception {
        init();
        EchoRequest er = new EchoRequest();
        er.setMessage("howdy");

        RpcRequest req = RpcRequest.Builder().method("A.echo").param(er.serialize()).build();
        assertEquals("howdy", handler.call(req));
    }

    private void init() {
        handler = new ReflectionHandler(new A());
    }

    class A {

        public String sayHi() { return "hi"; }
        public long add(long a, long b) { return a+b; }
        public Double sqrt(double a) { return Math.sqrt(a); }
        public boolean echoBool(Boolean b) { return b; }
        public String concat(List<String> list) {
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                sb.append(s);
            }
            return sb.toString();
        }
        public String echo(EchoRequest request) {
            return request.getMessage();
        }
    }

}

class EchoRequest implements BarristerSerializable {
    private String message;
    
    public void setMessage(String m) { message = m; }
    public String getMessage() { return message; }
    
    public Map<String,Object> serialize() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("message", message);
        return map;
    }
    
    public void deserialize(Map<String,Object> map) {
        message = (String)map.get("message");
    }
    
}
