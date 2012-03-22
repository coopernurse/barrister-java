package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;

public class Struct extends BaseEntity implements TypeConverter {

    private Map<String, Field> fields;
    private String extend;

    public Struct(String name, String extend) {
        this.name = name;
        this.extend = extend;
        this.fields = new HashMap<String,Field>();
    }

    @SuppressWarnings("unchecked") 
    public Struct(Map<String,Object> data) {
        super(data);
        fields = new HashMap<String,Field>();

        extend = (String)data.get("extends");
        
        List<Map<String,Object>> flist = (List<Map<String,Object>>)data.get("fields");
        for (Map<String,Object> f : flist) {
            Field field = new Field(f);
            fields.put(field.getName(), field);
        }
    }

    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Field f : fields.values()) {
            f.setContract(c);
        }
    }

    public String getExtends() {
        return extend;
    }
   
    public Map<String,Field> getFields() {
        return fields;
    }

    public Map<String,Field> getFieldsPlusParents() {
        Map<String,Field> tmp = new HashMap<String,Field>();
        tmp.putAll(fields);
        if (extend != null && !extend.equals("")) {
            Struct parent = contract.getStructs().get(extend);
            tmp.putAll(parent.getFieldsPlusParents());
        }
        return tmp;
    }

    public ValidationResult validate(Object obj, boolean allowMissing) {
        if (!(obj instanceof Map)) {
            String msg = "struct " + name + " val must be Map, got: " + 
                 obj.getClass().getName();
            return ValidationResult.invalid(msg);
        }
        else {
            Map map = (Map)obj;
            Map<String,Field> allFields = getFieldsPlusParents();
            for (Object keyObj : map.keySet()) {
                String key = keyObj.toString();
                if (allFields.containsKey(key)) {
                    Field field = allFields.get(key);
                    Object val = map.get(keyObj);
                    ValidationResult vr = this.contract.validate(field.getType(),
                                                                 val, allowMissing);
                    if (!vr.isValid()) {
                        return vr;
                    }
                }
                else {
                    String msg = "field '" + key + "' not found in struct " + name;
                    return ValidationResult.invalid(msg);
                }
            }

            if (!allowMissing) {
                for (String key : allFields.keySet()) {
                    if (!map.containsKey(key)) {
                        String msg = "field '" + key + "' missing from: " + name;
                        return ValidationResult.invalid(msg);
                    }
                }
            }

            return ValidationResult.valid();
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

    public Object unmarshal(String pkg, Object o) throws RpcException {
        if (o == null) {
            return null;
        }
        else if (!(o instanceof Map)) {
            String msg = "struct " + name + " val must be Map, got: " + 
                o.getClass().getName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        Object inst;
        String className = pkg + "." + name;
        try {
            Class c = Class.forName(className);
            inst = c.newInstance();
        }
        catch (Exception e) {
            String msg = "Unable to create: " + className + " - " + 
               e.getClass().getSimpleName() + ": " + e.getMessage();
            throw RpcException.Error.INTERNAL.exc(msg);
        }

        Map input = (Map)o;

        Map<String,Field> allFields = getFieldsPlusParents();
        Method methods[] = inst.getClass().getMethods();
        for (Method m : methods) {
            String name = m.getName();
            if (name.startsWith("set")) {
                name = name.substring(3);
                if (name.length() > 1)
                    name = name.substring(0,1).toLowerCase() + name.substring(1);
                else
                    name = name.toLowerCase();

                Field f = allFields.get(name);
                if (f == null) {
                    String msg = "field '" + name + "' missing from: " + this.name;
                    throw RpcException.Error.INVALID_PARAMS.exc(msg);
                }

                Object val = input.get(name);
                if (val != null) {
                    val = f.getTypeConverter().unmarshal(pkg, val);
                
                    try {
                        m.invoke(inst, val);
                    }
                    catch (Exception e) {
                        String msg = "Unable to set field '" + f.getName() +
                            "' - " + e.getMessage();
                        throw RpcException.Error.INVALID_PARAMS.exc(msg);
                    }
                }
            }
        }

        return inst;
    }

    @SuppressWarnings("unchecked") 
    public Object marshal(Object o) throws RpcException {
        if (o == null) {
            return null;
        }
        else if (o instanceof BStruct) {
            Map map = new HashMap();
            Map<String,Field> allFields = getFieldsPlusParents();
            Method methods[] = o.getClass().getMethods();
            for (Method m : methods) {
                String name = m.getName();
                if (name.startsWith("get") && !name.equals("getClass")) {
                    name = name.substring(3);
                    if (name.length() > 1)
                        name = name.substring(0,1).toLowerCase() + name.substring(1);
                    else
                        name = name.toLowerCase();

                    Field f = allFields.get(name);
                    if (f == null) {
                        String msg = "field '" + name + "' missing from: " + this.name;
                        throw RpcException.Error.INVALID_RESP.exc(msg);
                    }

                    Object val = null;
                    try {
                        val = m.invoke(o);
                    }
                    catch (Exception e) {
                        String msg = this.name + "." + name + 
                            " unable to invoke getter - " + e.getMessage();
                        throw RpcException.Error.INTERNAL.exc(msg);
                    }

                    if (val != null) {
                        val = f.getTypeConverter().marshal(val);
                        map.put(name, val);
                    }
                }
            }

            return map;
        }
        else {
            String msg = "Unable to convert class: " + o.getClass().getName();
            throw RpcException.Error.INVALID_RESP.exc(msg);
        }
    }

}