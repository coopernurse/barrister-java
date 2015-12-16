package com.bitmechanic.barrister;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Set;

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
     * @param map Map to unmarshal
     * @throws RpcException If o does not comply with the IDL definition for this Struct
     */
    public Object unmarshal(Object map) throws RpcException {
        return unmarshal(getTypeClass(), map, this.s, this.isOptional);
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
            return validateMap(structToMap(o, this.s), this.s);
        }
        else if (o instanceof Map) {
            return validateMap((Map)o, this.s);
        }
        else {
            String msg = "Unable to convert class: " + o.getClass().getName();
            throw RpcException.Error.INVALID_RESP.exc(msg);
        }
    }

    public static Object unmarshal(Class clazz, Object o, boolean isOptional) throws RpcException {
        return unmarshal(clazz, o, null, isOptional);
    }

    public static Object unmarshal(Class clazz, Object map, Struct struct, boolean isOptional) throws RpcException {
        String name = (struct == null) ? clazz.getSimpleName() : struct.getName();
        if (map == null) {
            return BaseTypeConverter.returnNullIfOptional(isOptional);
        }
        else if (!(map instanceof Map)) {
            String msg = "struct " + name + " val must be Map, got: " + map.getClass().getName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        try {
            // try to use the unmarshal method that is generated on all Idl2Java struct classes
            return clazz.getConstructor(Map.class).newInstance(map);
        }
        catch (NoSuchMethodException e) {
            // try to unmarshal using no-arg constructor + setters
            if (struct != null) {
                return unmarshalUsingSetters(clazz, (Map)map, struct);
            }
            else {
                String msg = "Unable to create: " + e.getClass().getSimpleName() + ": no Map based constructor - was Idl2Java used to generate this class using verison 0.1.13 or later?";
                throw RpcException.Error.INTERNAL.exc(msg);
            }
        } catch (InvocationTargetException e) {
            handleException(e.getTargetException());
        } catch (Exception e) {
            handleException(e);
        }

        throw new IllegalStateException("Unreachable statement");
    }

    private static Object unmarshalUsingSetters(Class clazz, Map map, Struct struct) throws RpcException {
        Object inst;
        try {
            inst = clazz.newInstance();
        }
        catch (Exception e) {
            String msg = "Unable to create: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            throw RpcException.Error.INTERNAL.exc(msg);
        }

        Map<String,Field> allFields = struct.getFieldsPlusParents();
        Method methods[] = inst.getClass().getMethods();
        for (Method m : methods) {
            String name = m.getName();
            if (name.startsWith("set") && !name.equals("set")) {

                Field f = resolveStructField(name.substring(3), struct, allFields, null);
                if (f == null) {
                    // Field exists on generated Java class that isn't in the IDL
                    String msg = "field '" + name + "' missing from: " + struct.getName() +
                            " - are generated classes in sync with IDL?";
                    throw RpcException.Error.INVALID_PARAMS.exc(msg);
                }

                name = f.getName();
                if (!map.containsKey(name)) {
                    if (f.isOptional()) {
                        continue;
                    }
                    else {
                        String msg = "field '" + name + "' missing from input value: '" +
                                map + "'";
                        throw RpcException.Error.INVALID_PARAMS.exc(msg);
                    }
                }

                Object val = map.get(name);
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

    private static Map validateMap(Map map, Struct struct) throws RpcException {
        Map<String,Field> allFields = struct.getFieldsPlusParents();
        Set<String> allKeys = allFields.keySet();
        for (String key : allKeys) {
            Object val  = map.get(key);
            Field field = allFields.get(key);
            if (val == null && !field.isOptional()) {
                String msg = struct.getName() + "." + key + " cannot be null";
                throw RpcException.Error.INVALID_RESP.exc(msg);
            }
        }

        for (Object keyObj : map.keySet()) {
            if (!allKeys.contains(keyObj.toString())) {
                String msg = String.format("Struct '%s' does not contain the field: '%s'", struct.getName(), keyObj);
                throw RpcException.Error.INVALID_RESP.exc(msg);
            }
        }

        return map;
    }

    private static Map structToMap(Object o, Struct struct) throws RpcException {
        Map map = new HashMap();

        Map<String,Field> allFields = struct.getFieldsPlusParents();
        Set<String> descendantFieldNames = struct.getDescendantFieldNames();

        Method methods[] = o.getClass().getMethods();
        for (Method m : methods) {
            String name = m.getName();
            if (name.startsWith("get") && !name.equals("getClass") && !name.equals("get")) {
                Field f = resolveStructField(name.substring(3), struct, allFields, descendantFieldNames);
                if (f != null) {
                    Object val = null;
                    try {
                        val = m.invoke(o);
                    }
                    catch (Exception e) {
                        String msg = o.getClass().getSimpleName() + "." + m.getName() +
                            " unable to invoke getter - " + e.getMessage();
                        throw RpcException.Error.INTERNAL.exc(msg);
                    }
                    
                    if (val != null) {
                        val = f.getTypeConverter().marshal(val);
                        map.put(f.getName(), val);
                    }
                }
            }
        }

        return map;
    }

    static Field resolveStructField(String baseName,
                                    Struct struct,
                                    Map<String,Field> allFields,
                                    Set<String> descendantFieldNames) throws RpcException {

        if (baseName.length() == 0) {
            String msg = "Invalid get method: " + baseName + " for struct: " + struct.getName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }

        // try finding field for lowercase first letter
        // e.g. 'Email' -> 'email'
        String name = baseName.substring(0, 1).toLowerCase() + baseName.substring(1);
        Field f = allFields.get(name);
        if (f != null) {
            return f;
        }

        // try finding field for unchanged first letter
        // e.g. 'Email' -> 'Email'
        f = allFields.get(baseName);
        if (f != null) {
            return f;
        }

        if (descendantFieldNames != null) {
            if (descendantFieldNames.contains(name) ||
                descendantFieldNames.contains(baseName)) {
                // ok - skip this field
                return null;
            }
        }

        // Field exists on generated Java class that isn't in the IDL
        String msg = "field '" + name + "' missing from: " + struct.getName() +
            " - are generated classes in sync with IDL? Valid fields: " +
            allFields.keySet();
        throw RpcException.Error.INVALID_PARAMS.exc(msg);
    }

}
