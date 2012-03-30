package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ValidationTest {

    @Test
    public void stringValidation() throws Exception {
        Object[] valid = { "adsf", "" };
        Object[] invalid = { true, 3, (short)3, (long)3, (float)323, (double)3 };
        validationTest("string", false, valid, invalid);
    }

    @Test
    public void intValidation() throws Exception {
        Object[] valid = { 1, 2L, (short)3 };
        Object[] invalid = { "hi", true, 3.3, (float)3.0 };
        validationTest("int", false, valid, invalid);
    }

    @Test
    public void floatValidation() throws Exception {
        Object[] valid = { 1, 2L, (short)3, (double)0.3, (float)3.2 };
        Object[] invalid = { "blah", false, true };
        validationTest("float", false, valid, invalid);
    }

    @Test
    public void boolValidation() throws Exception {
        Object[] valid = { true, false };
        Object[] invalid = { "hi", 1, 392.0, null };
        validationTest("bool", false, valid, invalid);
    }

    @Test
    public void structMustBeMap() throws Exception {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", 30);

        Object[] valid = { map };
        Object[] invalid = { "hi", 1, 392.0, true };
        validationTest(createPersonContract(), "Person", false, valid, invalid);
    }

    @Test
    public void structValidatesMembers() throws Exception {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", "30");

        Map<String,Object> map2 = new HashMap<String,Object>();
        map.put("name", 3033);
        map.put("age", 30);

        Object[] invalid = { map, map2 };
        validationTest(createPersonContract(), "Person", false, null, invalid);
    }

    @Test
    public void structValidatesParents() throws Exception {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", "29");
        map.put("yearsInPractice", 2.3);

        Map<String,Object> map2 = new HashMap<String,Object>();
        map2.put("name", 3033);
        map2.put("age", 30);
        map2.put("yearsInPractice", 0.32);

        Map<String,Object> validMap = new HashMap<String,Object>();
        validMap.put("name", "John");
        validMap.put("age", 53);
        validMap.put("yearsInPractice", 0.39);

        Object[] valid = { validMap };
        Object[] invalid = { map, map2 };
        validationTest(createPersonContract(), "Doctor", false, valid, invalid);
    }

    @Test
    public void enumValidatesVals() throws Exception {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", "29");
        map.put("faveColor", "black");

        Map<String,Object> map2 = new HashMap<String,Object>();
        map2.put("name", "Bob");
        map2.put("age", "29");

        Map<String,Object> validMap = new HashMap<String,Object>();
        validMap.put("name", "Mary");
        validMap.put("age", 30);
        validMap.put("faveColor", "blue");

        Map<String,Object> validMap2 = new HashMap<String,Object>();
        validMap2.put("name", "Mary");
        validMap2.put("faveColor", "blue");

        Object[] valid = { validMap, validMap2 };
        Object[] invalid = { map, map2 };
        validationTest(createPersonContract(), "Child", false, valid, invalid);        
    }

    private Contract createPersonContract() {
        Struct s = new Struct("Person", "");
        s.getFields().put("name", new Field("name", "string", false, false));
        s.getFields().put("age", new Field("age", "int", false, true));  

        Struct s2 = new Struct("Doctor", "Person");
        s2.getFields().put("yearsInPractice", new Field("yearsInPractice", "float", false, false));

        Struct s3 = new Struct("Child", "Person");
        s3.getFields().put("faveColor", new Field("faveColor", "Color", false, false));

        Enum e = new Enum("Color", "blue", "green");

        Contract c = new Contract();
        s.setContract(c);
        c.getStructs().put(s.getName(), s);
        s2.setContract(c);
        c.getStructs().put(s2.getName(), s2);
        s3.setContract(c);
        c.getStructs().put(s3.getName(), s3);

        e.setContract(c);
        c.getEnums().put(e.getName(), e);

        return c;
    }

    private void validationTest(String type, boolean isArray,
                                Object[] valid, Object[] invalid) throws Exception {
        validationTest(new Contract(), type, isArray, valid, invalid);
    }

    private void validationTest(Contract c, String type, boolean isArray,
                                Object[] valid, Object[] invalid) throws Exception {
        Field f = new Field("testfield", type, isArray, false);
        f.setContract(c);
        TypeConverter tc = f.getTypeConverter();
        String pkg = "com.bitmechanic.barrister";
        c.setPackage(pkg);

        if (valid != null) {
            for (Object o : valid) {
                tc.unmarshal(pkg, o);
            }
        }

        if (invalid != null) {
            for (Object o : invalid) {
                try {
                    tc.unmarshal(pkg, o);
                    fail("Should not have unmarshalled " + o + " for type " + type);
                }
                catch (RpcException e) {
                    // expected
                }
            }
        }

        // test nulls
        try {
             tc.unmarshal(pkg, null);
             fail("Should not have unmarshalled null for type " + type);
        }
        catch (RpcException e) {
            // ok
        }
        
        f = new Field("testfield2", type, isArray, true);
        f.setContract(c);
        tc = f.getTypeConverter();
        assertNull(tc.unmarshal(pkg, null));
    }
}
