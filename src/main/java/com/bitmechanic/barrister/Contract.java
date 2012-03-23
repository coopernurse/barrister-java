package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.FileInputStream;
import java.util.List;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

public class Contract extends BaseEntity {

    public static Contract load(File idlJson) throws IOException {
        FileInputStream fis = new FileInputStream(idlJson);
        Contract c = load(fis);
        fis.close();
        return c;
    }

    public static Contract load(InputStream idlJson) throws IOException {
        return load(idlJson, new JacksonSerializer());
    }

    @SuppressWarnings("unchecked")
    public static Contract load(InputStream idlJson, Serializer ser) throws IOException {
        return new Contract(ser.readList(idlJson));
    }

    //////////////////////////////

    private Map<String, Interface> interfaces;
    private Map<String, Struct> structs;
    private Map<String, Enum> enums;

    private List<Map<String,Object>> idl;

    private boolean validateRequest;
    private boolean validateResponse;

    private String packageName;

    public Contract() {
        interfaces = new HashMap<String, Interface>();
        structs    = new HashMap<String, Struct>();
        enums      = new HashMap<String, Enum>();
        validateRequest = true;
        validateResponse = true;
    }

    public Contract(List<Map<String,Object>> idl) {
        this();
        this.idl = idl;

        for (Map<String,Object> e : idl) {
            String type = String.valueOf(e.get("type"));
            if (type.equals("interface")) {
                Interface i = new Interface(e);
                i.setContract(this);
                interfaces.put(i.getName(), i);
            }
            else if (type.equals("struct")) {
                Struct s = new Struct(e);
                s.setContract(this);
                structs.put(s.getName(), s);
            }
            else if (type.equals("enum")) {
                Enum en = new Enum(e);
                en.setContract(this);
                enums.put(en.getName(), en);
            }
        }
    }

    public void setPackage(String pkgName) {
        this.packageName = pkgName;
    }
    
    public String getPackage() {
        return packageName;
    }

    public boolean isValidateRequest() {
        return validateRequest;
    }

    public boolean isValidateResponse() {
        return validateResponse;
    }

    public void setValidateRequest(boolean b) {
        validateRequest = b;
    }
    
    public void setValidateResponse(boolean b) {
        validateResponse = b;
    }

    public List<Map<String,Object>> getIdl() {
        return idl;
    }

    public Map<String, Interface> getInterfaces() {
        return interfaces;
    }

    public Map<String, Struct> getStructs() {
        return structs;
    }

    public Map<String, Enum> getEnums() {
        return enums;
    }

    public Function getFunction(String iface, String func) throws RpcException {
        Interface i = interfaces.get(iface);
        if (i == null) {
            String msg = "Interface '" + iface + "' not found";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        Function f = i.getFunction(func);
        if (f == null) {
            String msg = "Function '" + iface + "." + func + "' not found";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        return f;
    }

}