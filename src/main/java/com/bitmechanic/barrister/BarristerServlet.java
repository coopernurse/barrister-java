package com.bitmechanic.barrister;

import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Server;
import com.bitmechanic.barrister.JacksonSerializer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.Enumeration;

public class BarristerServlet extends HttpServlet {

    private Contract contract;
    private Server server;
    private JacksonSerializer serializer;

    public BarristerServlet() {
        // Serialize requests/responses as JSON using Jackson
        serializer = new JacksonSerializer();
    }

    public void init(ServletConfig config) throws ServletException {        
        try {
            String idlPath = config.getInitParameter("idl");
            if (idlPath == null) {
                throw new ServletException("idl init param is required. Set to path to .json file, or classpath:/mycontract.json");
            }

            if (idlPath.startsWith("classpath:")) {
                idlPath = idlPath.substring(10);
                contract = Contract.load(getClass().getResourceAsStream(idlPath));
            }
            else {
                contract = Contract.load(new File(idlPath));
            }
            
            server = new Server(contract);

            int handlerCount = 0;
            Enumeration params = config.getInitParameterNames();
            while (params.hasMoreElements()) {
                String key = params.nextElement().toString();
                if (key.indexOf("handler.") == 0) {
                    String val = config.getInitParameter(key);
                    int pos = val.indexOf("=");
                    if (pos == -1) {
                        throw new ServletException("Invalid init param: key=" + key + 
                                                   " value=" + val +
                                                   " -- should be: interfaceClass=implClass");
                    }

                    String ifaceCname = val.substring(0, pos);
                    String implCname  = val.substring(pos+1);

                    Class ifaceClazz = Class.forName(ifaceCname);
                    Class implClazz  = Class.forName(implCname);
                    server.addHandler(ifaceClazz, implClazz.newInstance());
                    handlerCount++;
                }
            }

            if (handlerCount == 0) {
                throw new ServletException("At least one handler.x init property is required");
            }
        }
        catch (ServletException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            InputStream is = req.getInputStream();
            OutputStream os = resp.getOutputStream();
            resp.addHeader("Content-Type", "application/json");

            // This will deserialize the request and invoke
            // our ContactServiceImpl code based on the method and params
            // specified in the request. The result, including any
            // RpcException (if thrown), will be serialized to the OutputStream
            server.call(serializer, is, os);

            is.close();
            os.close();
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }

}