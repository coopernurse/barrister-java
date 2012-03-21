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

    public ValidationResult validate(String expectedType, Object obj,
                                     boolean allowMissing) {
        if (obj == null) {
            if (allowMissing) {
                return new ValidationResult(true, null);
            }
            else {
                return new ValidationResult(false, "value cannot be null");
            }
        }

        Class clz = obj.getClass();
        if (expectedType.equals("string")) {
            boolean v = clz == String.class;
            return expectType(v, expectedType, obj);
        }
        else if (expectedType.equals("int")) {
            boolean v = clz == Short.class || clz == Integer.class ||
                clz == Long.class || clz == short.class || clz == int.class ||
                clz == long.class;
            return expectType(v, expectedType, obj);
        }
        else if (expectedType.equals("float")) {
            boolean v = clz == Short.class || clz == Integer.class ||
                clz == Long.class || clz == Float.class || clz == Double.class ||
                clz == short.class || clz == int.class || clz == long.class ||
                clz == float.class || clz == double.class;
            return expectType(v, expectedType, obj);
        }
        else if (expectedType.equals("bool")) {
            boolean v = clz == Boolean.class || clz == boolean.class;
            return expectType(v, expectedType, obj);
        }
        else {
            Struct s = structs.get(expectedType);
            if (s != null) {
                return s.validate(obj, allowMissing);
            }

            Enum e = enums.get(expectedType);
            if (e != null) {
                return e.validate(obj);
            }

            return new ValidationResult(false, "Unknown type: " + expectedType);
        }
    }

    private ValidationResult expectType(boolean valid, String expectedType,
                                        Object val) {
        if (valid) {
            return new ValidationResult(true, null);
        }
        else {
            return new ValidationResult(false, "Expected " + expectedType + " '" +
                                        val + "' is type: " + 
                                        val.getClass().getName());
        }
    }

}