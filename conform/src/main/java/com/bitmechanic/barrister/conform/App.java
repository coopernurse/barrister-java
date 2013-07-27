package com.bitmechanic.barrister.conform;

import spark.*;
import static spark.Spark.*;
import java.io.*;
import com.bitmechanic.barrister.*;
import com.bitmechanic.test.conform.A;
import com.bitmechanic.test.conform.B;

public class App {

    public static void main(String[] args) throws Exception {
        Contract contract = null;
        try {
            contract = Contract.load(new File(System.getProperty("idlJson")));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        final Server server = new Server(contract);
        server.addHandler(A.class, new AImpl());
        server.addHandler(B.class, new BImpl());

        final JacksonSerializer serializer = new JacksonSerializer();

        setPort(9233);
        post(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Content-Type", "application/json; charset=utf8");
                try {
                    InputStream is = request.raw().getInputStream();
                    ByteArrayOutputStream os = new ByteArrayOutputStream(1024);

                    // This will deserialize the request and invoke
                    // our ContactServiceImpl code based on the method and params
                    // specified in the request. The result, including any
                    // RpcException (if thrown), will be serialized to the OutputStream
                    server.call(serializer, is, os);

                    is.close();
                    os.close();
                    return os.toString("utf-8");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return "{ \"jsonrpc\" : \"2.0\", \"error\" : { \"code\" : " + -32000 +
                            ", \"message\" : \"Server Error\" } }";
                }
            }
        });
    }

}