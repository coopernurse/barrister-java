package com.bitmechanic.barrister.conform;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Server;
import com.bitmechanic.barrister.JacksonSerializer;
import com.bitmechanic.barrister.Serializer;
import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.barrister.RpcRequest;
import com.bitmechanic.test.*;

public class ConformServer implements HttpServer.Handler {

    public static void main(String argv[]) throws Exception {
        ConformServer s = new ConformServer(argv[0]);
        s.start();
    }

    ///////////////////////////////////////

    Contract contract;
    HttpServer httpServer;
    Serializer serializer;
    Server server;

    public ConformServer(String idlJson) throws Exception {
        contract = Contract.load(new File(idlJson));
        serializer = new JacksonSerializer();

        server = new Server(contract);
        server.addHandler("A", new AImpl());
        server.addHandler("B", new BImpl());
    }

    public void start() throws Exception {
        httpServer = new HttpServer(this);
        httpServer.serve(9233);
    }

    public String exec(String post) {
        if (post == null || post.trim().equals("")) {
            System.exit(0);
        }

        try {
            RpcRequest req = serializer.readRequest(post.getBytes("utf-8"));
            return new String(serializer.writeResponse(server.call(req)), "utf-8");
        }
        catch (Exception e) {
            e.printStackTrace();
            return "ERR: " + e.getMessage();
        }
    }

}
