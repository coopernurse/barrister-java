package com.bitmechanic.barrister.conform;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Server;
import com.bitmechanic.barrister.JacksonSerializer;
import com.bitmechanic.test.conform.A;
import com.bitmechanic.test.conform.B;

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            InputStream is = req.getInputStream();
            OutputStream os = resp.getOutputStream();
            resp.addHeader("Content-Type", "application/json");
            server.call(serializer, is, os);
            is.close();
            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}