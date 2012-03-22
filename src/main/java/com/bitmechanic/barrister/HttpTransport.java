package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpTransport implements Transport {

    private URL url;
    private Serializer serializer;
    private Contract contract;
    private Map<String,String> headers;

    public HttpTransport(String endpoint, Serializer serializer) throws IOException {
        this.url = new URL(endpoint);
        this.serializer = serializer;
        this.headers = new HashMap<String,String>();
        this.headers.put("Content-Type", "application/json");
        loadContract();
    }

    public void setHeader(String key, String val) {
        headers.put(key, val);
    }

    public Map<String,String> getHeaders() {
        return headers;
    }

    private void loadContract() throws IOException {
        InputStream is = null;
        try {
            RpcRequest req = new RpcRequest("1", "barrister-idl", null);
            is = requestRaw(req);
            Map map = serializer.readMap(is);
            if (map.get("error") != null) {
                throw new IOException("Unable to load IDL from " + url + " - " +
                                      map.get("error"));
            } 
            else if (map.get("result") == null) {
                throw new IOException("Unable to load IDL from " + url + " - " +
                                      "result is null");
            }
            else {
                this.contract = new Contract((java.util.List)map.get("result"));
            }
        }
        catch (RpcException e) {
            throw new IOException("Unable to load IDL from " + url + " - " +
                                  e.getMessage());
        }
        finally {
            closeQuietly(is);
        }
    }

    public Contract getContract() {
        return contract;
    }

    public RpcResponse request(RpcRequest req) {
        InputStream is = null;
        try {
            is = requestRaw(req);
            return new RpcResponse(req, contract, serializer.readMap(is));
        }
        catch (RpcException e) {
            return new RpcResponse(req, e);
        }
        catch (IOException e) {
            String msg = "IOException requesting " + req.getMethod() + " from: " + url +
                " - " + e.getMessage();
            RpcException exc = RpcException.Error.INTERNAL.exc(msg);
            return new RpcResponse(req, exc);
        }
        finally {
            closeQuietly(is);
        }
    }

    private InputStream requestRaw(RpcRequest req) throws IOException, RpcException {
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);

        for (String key : headers.keySet()) {
            conn.addRequestProperty(key, headers.get(key));
        }
        
        OutputStream os = conn.getOutputStream();
        serializer.write(req.marshal(contract), os);
        os.flush();

        InputStream is = conn.getInputStream();
        os.close();
        return is;
    }

    private void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            }
            catch (Exception e) { }
        }
    }

}