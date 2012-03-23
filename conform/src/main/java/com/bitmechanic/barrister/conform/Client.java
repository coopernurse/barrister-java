package com.bitmechanic.barrister.conform;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonFactory;
import com.bitmechanic.barrister.RpcRequest;
import com.bitmechanic.barrister.RpcResponse;
import com.bitmechanic.barrister.HttpTransport;
import com.bitmechanic.barrister.JacksonSerializer;
import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.RpcException;

public class Client {

    // Usage:
    //
    // java com.bitmechanic.barrister.conform.Client path_to_conform.json output.file
    //
    public static void main(String argv[]) throws Exception {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader in = new BufferedReader(new FileReader(argv[0]));
        PrintWriter out = new PrintWriter(new FileWriter(argv[1]));

        JacksonSerializer ser = new JacksonSerializer();
        HttpTransport trans = new HttpTransport("http://127.0.0.1:9233/", ser);
        Contract con = trans.getContract();
        con.setPackage("com.bitmechanic.test");

        String line = in.readLine();
        while (line != null) {
            line = line.trim();
            if (!line.equals("") && line.indexOf("#") != 0) {
                System.out.println("\nProcessing: " + line);
                String cols[] = line.split("\\|");
                String iface  = cols[0];
                String func   = cols[1];
                String params = cols[2];

                String id = UUID.randomUUID().toString();

                String status = "ok";
                Object respObj = null;

                try {
                    Object paramsObj = mapper.readValue(params, Object.class);
                    System.out.println("paramsObj: " + 
                                       paramsObj.getClass().getSimpleName() + " - " + 
                                       paramsObj);

                    String meth = iface+"."+func;
                    RpcRequest req = new RpcRequest(id, meth, paramsObj);
                    Object paramsConv[] = con.getFunction(iface,func).unmarshalParams(req);

                    req = new RpcRequest(id, meth, paramsConv);

                    RpcResponse resp = trans.request(req);
                    System.out.println("got response: " + resp);
                    if (resp.getError() != null) {
                        throw resp.getError();
                    }
                    respObj = resp.getResult();
                }
                catch (RpcException e) {
                    status = "rpcerr";
                    respObj = e.getCode();
                }
                catch (Throwable t) {
                    System.out.println("ERR: " + t.getClass().getSimpleName() + 
                                       ": " + t.getMessage());
                    status = "err";
                    respObj = "";
                }
                
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
            line = in.readLine();
        }
        in.close();
        out.close();
    }

}