package com.bitmechanic.barrister.conform;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonFactory;
import com.bitmechanic.barrister.RpcRequest;
import com.bitmechanic.barrister.RpcResponse;
import com.bitmechanic.barrister.HttpTransport;
import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.barrister.Batch;

public class Client {

    // Usage:
    //
    // java com.bitmechanic.barrister.conform.Client path_to_conform.json output.file
    //
    public static void main(String argv[]) throws Exception {
        new Client(argv[0], argv[1]);
    }

    
    PrintWriter out;
    ObjectMapper mapper;
    JsonFactory jsonFactory;
    HttpTransport trans;
    Contract con;
    Batch batch;

    Map<String,RpcRequest> byReqId;
    Map<String,String> paramsByReqId;

    Client(String idlJson, String outFile) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(idlJson));
        out = new PrintWriter(new FileWriter(outFile));

        jsonFactory = new JsonFactory();
        mapper = new ObjectMapper();

        trans = new HttpTransport("http://127.0.0.1:9233/");
        con = trans.getContract();
        con.setPackage("com.bitmechanic.test");

        String line = in.readLine();
        while (line != null) {
            line = line.trim();
            if (!line.equals("") && line.indexOf("#") != 0) {
                processLine(line);
            }
            line = in.readLine();
        }
        in.close();
        out.close();
    }

    void processLine(String line) throws Exception {
        if (line.equals("start_batch")) {
            batch = new Batch(trans);
            byReqId = new HashMap<String,RpcRequest>();
            paramsByReqId = new HashMap<String,String>();
        }
        else if (line.equals("end_batch")) {
            for (RpcResponse resp : batch.send()) {
                String id = resp.getId();
                RpcRequest req = byReqId.get(id);

                String status = "ok";
                Object respObj = resp.getResult();
                if (resp.getError() != null) {
                    status = "rpcerr";
                    respObj = resp.getError().getCode();
                }

                logResponse(req.getIface(), req.getFunc(), paramsByReqId.get(id),
                            status, respObj);
            }
            batch = null;
            byReqId = null;
            paramsByReqId = null;
        }
        else {
            processCommandLine(line);
        }
    }

    void processCommandLine(String line) throws Exception {
        System.out.println("\nProcessing: " + line);
        String cols[] = line.split("\\|");
        String iface  = cols[0];
        String func   = cols[1];
        String params = cols[2];
            
        String id = UUID.randomUUID().toString();
        
        String status  = "ok";
        Object respObj = null;
            
        try {
            Object paramsObj = mapper.readValue(params, Object.class);
            //System.out.println("paramsObj: " + 
            //                   paramsObj == null ? null : paramsObj.getClass().getSimpleName() + " - " + 
            //                   paramsObj);
            
            String meth = iface+"."+func;
            RpcRequest req = new RpcRequest(id, meth, paramsObj);
            Object paramsConv[] = con.getFunction(iface,func).unmarshalParams(req);
            
            req = new RpcRequest(id, meth, paramsConv);
            
            if (batch == null) {
                RpcResponse resp = trans.request(req);
                System.out.println("got response: " + resp);
                if (resp.getError() != null) {
                    throw resp.getError();
                }
                respObj = resp.getResult();
            }
            else {
                byReqId.put(req.getId(), req);
                paramsByReqId.put(req.getId(), params);
                batch.request(req);
            }
        }
        catch (RpcException e) {
            status = "rpcerr";
            respObj = e.getCode();
        }
        catch (Throwable t) {
            t.printStackTrace();
            System.out.println("ERR: " + t.getClass().getSimpleName() + 
                               ": " + t.getMessage());
            status = "err";
            respObj = "";
        }
        
        if (batch == null) {
            logResponse(iface, func, params, status, respObj);
        }
    }

    void logResponse(RpcResponse resp) throws Exception {

    }

    void logResponse(String iface, String func, String params, 
                     String status, Object respObj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator gen = jsonFactory.createJsonGenerator(bos);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.writeValue(gen, respObj);
        gen.close();
        bos.close();
        String outStr = new String(bos.toByteArray(), "utf-8");
        
        String rStr = String.format("%s|%s|%s|%s|%s", iface, func, params, 
                                    status, outStr);
        
        System.out.println("Writing: " + rStr);
        out.println(rStr);
    }

}