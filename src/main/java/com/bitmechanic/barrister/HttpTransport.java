package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/**
 * Client Transport implementation that uses HTTP(S)
 */
public class HttpTransport implements Transport {

    private String endpoint;
    private Serializer serializer;
    private Contract contract;
    private Map<String,String> headers;

    /**
     * Creates a new HttpTransport using an empty header list and the JacksonSerializer.
     *
     * @param endpoint URL of the Barrister server to consume
     * @throws IOException If there is a problem loading the IDL from the endpoint
     */
    public HttpTransport(String endpoint) throws IOException {
        this(endpoint, null, new JacksonSerializer());
    }

    /**
     * Creates a new HttpTransport.  When this constructor is called, the Contract for the
     * given endpoint URL will be immediately requested.
     *
     * @param endpoint URL of the Barrister server to consume
     * @param headers HTTP headers to add to requests against this transport. For example,
     *        "Authorization"
     * @param serializer Serializer to use with this transport
     * @throws IOException If there is a problem loading the IDL from the endpoint
     */
    public HttpTransport(String endpoint, Map<String,String> headers, Serializer serializer) 
        throws IOException {

        if (headers == null) {
            headers = new HashMap<String,String>();
        }

        this.endpoint = endpoint;
        this.serializer = serializer;
        this.headers = headers;
        this.headers.put("Content-Type", "application/json");
        loadContract();
    }

    /**
     * Returns the HTTP headers associated with this Transport. 
     * This returns an immutable copy of the headers map, so its
     * contents may not be modified.
     */
    public Map<String,String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @SuppressWarnings("unchecked")
    private void loadContract() throws IOException {
        InputStream is = null;
        try {
            RpcRequest req = new RpcRequest("1", "barrister-idl", null);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.write(req.marshal(contract), bos);
            bos.close();
            byte[] data = bos.toByteArray();

            is = requestRaw(data);
            Map map = serializer.readMap(is);
            if (map.get("error") != null) {
                throw new IOException("Unable to load IDL from " + endpoint + " - " +
                                      map.get("error"));
            } 
            else if (map.get("result") == null) {
                throw new IOException("Unable to load IDL from " + endpoint + " - " +
                                      "result is null");
            }
            else {
                this.contract = new Contract((java.util.List)map.get("result"));
            }
        }
        catch (RpcException e) {
            throw new IOException("Unable to load IDL from " + endpoint + " - " +
                                  e.getMessage());
        }
        finally {
            closeQuietly(is);
        }
    }

    /**
     * Returns the Contract associated with this transport.  This is loaded
     * from the server endpoint in the constructor.
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Makes a RPC call via HTTP(S) to the remote server
     */
    public RpcResponse request(RpcRequest req) {
        InputStream is = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.write(req.marshal(contract), bos);
            bos.close();
            byte[] data = bos.toByteArray();

            is = requestRaw(data);
            return unmarshal(req, serializer.readMap(is));
        }
        catch (RpcException e) {
            return new RpcResponse(req, e);
        }
        catch (IOException e) {
            String msg = "IOException requesting " + req.getMethod() + 
                " from: " + endpoint + " - " + e.getMessage();
            RpcException exc = RpcException.Error.INTERNAL.exc(msg);
            return new RpcResponse(req, exc);
        }
        finally {
            closeQuietly(is);
        }
    }

    /**
     * Makes JSON-RPC batch request against the remote server as a single HTTP request.
     */
    @SuppressWarnings("unchecked")
    public List<RpcResponse> request(List<RpcRequest> reqList) {
        List<RpcResponse> respList = new ArrayList<RpcResponse>();

        List<Map> marshaledReqs = new ArrayList<Map>();
        Map<String,RpcRequest> byReqId = new HashMap<String,RpcRequest>();
        for (RpcRequest req : reqList) {
            try {
                marshaledReqs.add(req.marshal(contract));
                byReqId.put(req.getId(), req);
            }
            catch (RpcException e) {
                respList.add(new RpcResponse(req, e));
            }
        }

        InputStream is = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.write(marshaledReqs, bos);
            bos.close();
            byte[] data = bos.toByteArray();
            
            is = requestRaw(data);
            List<Map> responses = serializer.readList(is);
            for (Map map : responses) {
                String id = (String)map.get("id");
                if (id != null) {
                    RpcRequest req = byReqId.get(id);
                    if (req == null) {
                        // TODO: ?? log error?
                    }
                    else {
                        byReqId.remove(id);
                        respList.add(unmarshal(req, map));
                    }
                }
                else {
                    // TODO: ?? log error?
                }
            }

            if (byReqId.size() > 0) {
                for (RpcRequest req : byReqId.values()) {
                    String msg = "No response in batch for request " + req.getId();
                    RpcException exc = RpcException.Error.INVALID_RESP.exc(msg);
                    RpcResponse resp = new RpcResponse(req, exc);
                    respList.add(resp);
                }
            }
        }
        catch (IOException e) {
            String msg = "IOException requesting batch " +
                " from: " + endpoint + " - " + e.getMessage();
            RpcException exc = RpcException.Error.INTERNAL.exc(msg);
            respList.add(new RpcResponse(null, exc));
        }
        finally {
            closeQuietly(is);
        }
        
        return respList;
    }

    private RpcResponse unmarshal(RpcRequest req, Map map) {
        try {
            return new RpcResponse(req, contract, map);
        }
        catch (RpcException e) {
            return new RpcResponse(req, e);
        }
    }

    private InputStream requestRaw(byte[] data) throws IOException {
        URL url = new URL(this.endpoint);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);

        for (String key : headers.keySet()) {
            conn.addRequestProperty(key, headers.get(key));
        }

        conn.addRequestProperty("Content-Length", String.valueOf(data.length));
        
        OutputStream os = conn.getOutputStream();
        os.write(data);
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