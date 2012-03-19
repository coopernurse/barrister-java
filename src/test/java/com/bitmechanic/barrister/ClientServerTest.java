package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;

public class ClientServerTest {

    Client client;
    Server server;

    @Test
    public void echoMethod() throws Exception {
        init();
        assertEquals("hi", client.call("B", "echo", "hi").get("result"));
    }

    @Test
    public void addMethod() throws Exception {
        init();
        assertEquals(5L, client.call("A", "add", (short)1, 4).get("result"));
    }

    @Test
    public void noParamsMethod() throws Exception {
        init();
        Map<String,Object> expected = new HashMap<String,Object>();
        expected.put("hi", "hello");
        assertEquals(expected, client.call("A", "say_hi").get("result"));
    }

    private void init() throws Exception {
        Contract c = TestUtil.loadConformContract();
        server = new Server(c);

        // normally a service would only handle one interface, but
        // for simplicity we're combining them here
        Svc svc = new Svc();
        server.addHandler("A", svc);
        server.addHandler("B", svc);

        Transport trans = new InProcTransport(server);
        client = new Client(trans);
    }

    class Svc implements Handler {
        
        public Object call(RpcRequest req) throws RpcException {
            String func = req.getFunc();
            if (func.equals("echo")) {
                return echo(req.getString(0));
            }
            else if (func.equals("add")) {
                return add(req.getLong(0), req.getLong(1));
            }
            else if (func.equals("say_hi")) {
                return say_hi();
            }

            String msg = "Method '" + func + "' not found in " + 
                this.getClass().getName();
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        public String echo(String s) {
            return s;
        }

        public long add(long a, long b) {
            return a + b;
        }

        public HiResult say_hi() {
            return new HiResult("hello");
        }

    }

    class HiResult implements BarristerSerializable {

        private String hi;

        public HiResult(String hi) {
            this.hi = hi;
        }

        public Map<String,Object> serialize() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("hi", this.hi);
            return map;
        }

        public void deserialize(Map<String,Object> m) {
            this.hi = (String)m.get("hi");
        }

    }

}