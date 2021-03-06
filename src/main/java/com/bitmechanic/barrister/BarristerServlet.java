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

/**
 * Generic Servlet that can expose a Barrister RPC interface implementation.
 * If you're not using a DI framework like Spring, you may find that this is
 * sufficient for exposing your implementation classes.  If you are using
 * a DI framework, this class can serve as an example of how to wire the raw
 * Servlet Input/Output streams to the Barrister Server class.
 * <p>
 * This Servlet has two required init properties: "idl", and "handler.1".
 * "idl" is the path to the IDL JSON file to load.  It should either be a full path
 * on the filesystem, or a "classpath:/foo.json" style string.
 * <p>
 * "handler.1" specifies the interface class and the implementation class for an interface
 * in the IDL.  If your IDL has more than one interface, you may have multiple "handler.x" 
 * properties (e.g. "handler.1", "handler.2", etc) - one per interface.
 * <p>
 * The implementation class will be instantiated by the servlet, so it must have a no
 * argument constructor.
 * <p>
 * Example web.xml configuration:
 * <pre>
 *   &lt;servlet&gt;
 *     &lt;servlet-name&gt;example&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;com.bitmechanic.barrister.BarristerServlet&lt;/servlet-class&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;idl&lt;/param-name&gt;
 *         &lt;param-value&gt;classpath:/example.json&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;handler.1&lt;/param-name&gt;
 *
 *         &lt;!-- example.CalculatorService is the interface generated by idl2java --&gt;
 *         &lt;param-value&gt;example.CalculatorService=example.CalculatorServiceImpl&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *   &lt;/servlet&gt;
 * </pre>
 *
 */
public class BarristerServlet extends HttpServlet {

    private Contract contract;
    private Server server;
    private JacksonSerializer serializer;

    /**
     * Creates the servlet and initializes the JacksonSerializer
     */
    public BarristerServlet() {
        // Serialize requests/responses as JSON using Jackson
        serializer = new JacksonSerializer();
    }

    /**
     * Initializes the servlet based on the init parameters in web.xml
     */
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

    /**
     * Handles RPC requests on POST
     */
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