package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * TypeConverter for user defined 'struct' types.  Native types are usually Java POJOs
 * created by Idl2Java.
 */
public class StructTypeConverter extends BaseTypeConverter {

    private Struct s;

    /**
     * Creates a new StructTypeConverter for the given IDL Struct
     *
     * @param s Struct to convert for
     * @param isOptional If true then this struct may be null.  Does not mean that
     *        its members may be null (that's delegated to the TypeConverters hung off each
     *        Field)
     */
    public StructTypeConverter(Struct s, boolean isOptional) {
        super(isOptional);
        this.s = s;
    }

    /**
     * Returns the Class that matches: Contract.packageName + "." + Struct.name
     *
     * In the common case this will result in an Idl2Java generated Class.
     */
    public Class getTypeClass() {
        try {
            return Class.forName(s.getContract().getClassNameForEntity(s.getName()));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts o from a Map back to the Java Class associated with this Struct.
     * Recursively unmarshals all the members of the map.
     *
     * @param o Map to unmarshal
     * @throws RpcException If o does not comply with the IDL definition for this Struct
     */
    public Object unmarshal(Object o) throws RpcException {
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
        try {
            inst = getTypeClass().newInstance();
        }
        catch (Exception e) {
            String msg = "Unable to create: " +
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
                val = f.getTypeConverter().unmarshal(val);

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

    /**
     * Marshals native Java type o to a Map that can be serialized.
     * Recursively marshals all of the Struct fields from o onto the map.
     *
     * @param o Java object to marshal to Map
     * @return Map containing the marshaled data
     * @throws RpcException If o does not validate against the Struct spec in the IDL.
     *         The most common validation error will be null properties on o for Struct
     *         fields that are not marked optional.
     */
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

                    if (val == null) {
                        if (!f.isOptional()) {
                            String msg = s.getName() + "." + name + " cannot be null";
                            throw RpcException.Error.INVALID_RESP.exc(msg);
                        }
                    }
                    else {
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