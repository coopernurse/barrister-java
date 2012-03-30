package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

public class StructTypeConverter extends BaseTypeConverter {

    private Struct s;

    public StructTypeConverter(Struct s, boolean isOptional) {
        super(isOptional);
        this.s = s;
    }

    public Class getTypeClass() {
        try {
            return Class.forName(s.getContract().getPackage() + "." + s.getName());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object unmarshal(String pkg, Object o) throws RpcException {
        if (o == null) {
            return returnNullIfOptional();
        }
        else if (!(o instanceof Map)) {
            String msg = "struct " + s.getName() + " val must be Map, got: " + 
                o.getClass().getName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        String name = s.getName();
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

        Map<String,Field> allFields = s.getFieldsPlusParents();
        Method methods[] = inst.getClass().getMethods();
        for (Method m : methods) {
            name = m.getName();
            if (name.startsWith("set")) {
                name = name.substring(3);
                if (name.length() > 1)
                    name = name.substring(0,1).toLowerCase() + name.substring(1);
                else
                    name = name.toLowerCase();

                Field f = allFields.get(name);
                if (f == null) {
                    // Field exists on generated Java class that isn't in the IDL
                    String msg = "field '" + name + "' missing from: " + s.getName() + 
                        " - are generated classes in sync with IDL?";
                    throw RpcException.Error.INVALID_PARAMS.exc(msg);
                }

                if (!input.containsKey(name)) {
                    if (f.isOptional()) {
                        continue;
                    }
                    else {
                        String msg = "field '" + name + "' missing from input value: '" +
                            input + "'";
                        throw RpcException.Error.INVALID_PARAMS.exc(msg);
                    }
                }

                Object val = input.get(name);
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

        return inst;
    }

    @SuppressWarnings("unchecked")
    public Object marshal(Object o) throws RpcException {
        if (o == null) {
            return returnNullIfOptional();
        }
        else if (o instanceof BStruct) {
            Map map = new HashMap();
            Map<String,Field> allFields = s.getFieldsPlusParents();
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
                        String msg = "field '" + name + "' missing from: " + s.getName();
                        throw RpcException.Error.INVALID_RESP.exc(msg);
                    }

                    Object val = null;
                    try {
                        val = m.invoke(o);
                    }
                    catch (Exception e) {
                        String msg = s.getName() + "." + name + 
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