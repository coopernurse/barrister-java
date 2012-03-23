package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Enum extends BaseEntity implements TypeConverter {

    private List<String> values;

    public Enum(String name, String... vals) {
        this.name = name;
        this.values = new ArrayList<String>();
        for (String v : vals) {
            values.add(v);
        }
    }

    @SuppressWarnings("unchecked")
    public Enum(Map<String,Object> data) {
        super(data);
        values = new ArrayList<String>();
        
        List<Map<String,Object>> dvals = (List<Map<String,Object>>)data.get("values");
        for (Map<String,Object> val : dvals) {
            values.add((String)val.get("value"));
        }
    }

    public List<String> getValues() {
        return values;
    }

    public Class getTypeClass() {
        try {
            return Class.forName(contract.getPackage() + "." + name);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object unmarshal(String pkg, Object obj) throws RpcException {
        if (obj == null) {
            return null;
        }
        else if (obj.getClass() != String.class) {
            String msg = "'" + obj + "' enum must be String, got: " + 
                 obj.getClass().getSimpleName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }
        else if (values.contains((String)obj)) {
            try {
                Class clz = Class.forName(contract.getPackage()+"."+name);
                return java.lang.Enum.valueOf(clz, (String)obj);
            }
            catch (Exception e) {
                String msg = "Could not set enum value '" + obj + "' - " + 
                    e.getClass().getSimpleName() + " - " + e.getMessage();
                throw RpcException.Error.INTERNAL.exc(msg);
            }
        }
        else {
            String msg = "'" + obj + "' is not in enum: " + values;
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }
    }

    public Object marshal(Object o) throws RpcException {
        if (o == null)
            throw RpcException.Error.INVALID_RESP.exc("enum " + name + " cannot be null");
        else 
            return o;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Enum name=");
        sb.append(name);
        sb.append(" values=");
        boolean first = true;
        for (String v : values) {
            if (first) { first = false; }
            else { sb.append(", "); }
            sb.append(v);
        }
        return sb.toString();
    }

}