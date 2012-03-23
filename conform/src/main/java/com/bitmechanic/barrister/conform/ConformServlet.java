package com.bitmechanic.barrister.conform;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Server;
import com.bitmechanic.barrister.JacksonSerializer;
import com.bitmechanic.barrister.RpcRequest;
import com.bitmechanic.test.A;
import com.bitmechanic.test.B;

public class ConformServlet extends HttpServlet {

    private Contract contract;
    private JacksonSerializer serializer;
    private Server server;

    public ConformServlet() {
        String idlJson = System.getProperty("idlJson");

        try {
            contract = Contract.load(new File(idlJson));
            serializer = new JacksonSerializer();
            server = new Server(contract);
            server.addHandler(A.class, new AImpl());
            server.addHandler(B.class, new BImpl());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException {

        InputStream is = req.getInputStream();
        Map map = serializer.readMap(is);
        is.close();
        RpcRequest rpcReq = new RpcRequest(map);
        
        resp.addHeader("Content-Type", "application/json");
        serializer.write(server.call(rpcReq).marshal(), resp.getOutputStream());
    }

}