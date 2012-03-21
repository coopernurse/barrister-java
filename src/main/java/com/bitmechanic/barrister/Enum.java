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

    public ValidationResult validate(Object obj) {
        if (obj == null) {
            String msg = "Enum value " + name + " cannot be null";
            return ValidationResult.invalid(msg);
        }
        else if (obj.getClass() != String.class) {
            String msg = "'" + obj + "' enum must be String, got: " + 
                 obj.getClass().getName();
            return ValidationResult.invalid(msg);
        }
        else {
            if (values.contains((String)obj)) {
                return ValidationResult.valid();
            }
            else {
                String msg = "'" + obj + "' is not in enum: " + values;
                return ValidationResult.invalid(msg);
            }
        }
    }

    public Class getTypeClass() {
        try {
            return Class.forName(contract.getPackage() + "." + name);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object fromRequest(String pkg, Object o) throws RpcException {
        ValidationResult vr = validate(o);
        if (vr.isValid())
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc(vr.getMessage());
    }

    public Object toResponse(Object o) throws RpcException {
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