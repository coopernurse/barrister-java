package com.bitmechanic.barrister.conform;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        server.addHandler(A.class, new AImpl());
        server.addHandler(B.class, new BImpl());
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
            ByteArrayInputStream bis = new ByteArrayInputStream(post.getBytes("utf-8"));
            Map map = serializer.readMap(bis);
            bis.close();
            RpcRequest req = new RpcRequest(map);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.write(server.call(req).toMap(), bos);
            String s = new String(bos.toByteArray(), "utf-8");
            bos.close();
            return s;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "ERR: " + e.getMessage();
        }
    }

}
