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
        RpcRequestBean req = new RpcRequestBean("id", "B", "echo", "hi");
        assertEquals("hi", client.request(req).getResult());
    }

    @Test
    public void addMethod() throws Exception {
        init();
        RpcRequestBean req = new RpcRequestBean("id", "A", "add", (short)1, 4);
        assertEquals(5L, client.request(req).getResult());
    }

    @Test
    public void noParamsMethod() throws Exception {
        init();
        RpcRequestBean req = new RpcRequestBean("id", "A", "say_hi");
        HiResult res = (HiResult)client.request(req).getResult();
        assertEquals("hello", res.getHi());
    }

    private void init() throws Exception {
        Contract c = TestUtil.loadConformContract();
        server = new Server(c);

        // normally a service would only handle one interface, but
        // for simplicity we're combining them here
        Svc svc = new Svc();
        server.addHandler("A", svc);
        server.addHandler("B", svc);

        client = new InProcClient(server);
    }

    class Svc {
        
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

    class HiResult {

        private String hi;

        public HiResult(String hi) {
            this.hi = hi;
        }

        public String getHi() { return hi; }

    }

}