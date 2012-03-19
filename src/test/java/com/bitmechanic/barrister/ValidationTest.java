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
    public void stringValidation() {
        Object[] valid = { "adsf", "" };
        Object[] invalid = { true, 3, (short)3, (long)3, (float)323, (double)3 };
        validationTest("string", valid, invalid);
    }

    @Test
    public void intValidation() {
        Object[] valid = { 1, 2L, (short)3 };
        Object[] invalid = { "hi", true, 3.3, (float)3.0 };
        validationTest("int", valid, invalid);
    }

    @Test
    public void floatValidation() {
        Object[] valid = { 1, 2L, (short)3, (double)0.3, (float)3.2 };
        Object[] invalid = { "blah", false, true };
        validationTest("float", valid, invalid);
    }

    @Test
    public void boolValidation() {
        Object[] valid = { true, false };
        Object[] invalid = { "hi", 1, 392.0, null };
        validationTest("bool", valid, invalid);
    }

    @Test
    public void unknownTypeInvalid() {
        Contract c = new Contract();
        Object obj = new HashMap<String,Object>();
        assertFalse(c.validate("foo", obj, false).isValid());
    }

    @Test
    public void structMustBeMap() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", 30);

        Object[] valid = { map };
        Object[] invalid = { "hi", 1, 392.0, null, true };
        validationTest(createPersonContract(), "Person", valid, invalid);
    }

    @Test
    public void structValidatesMembers() {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("name", "Bob");
        map.put("age", "30");

        Map<String,Object> map2 = new HashMap<String,Object>();
        map.put("name", 3033);
        map.put("age", 30);

        Object[] invalid = { map, map2 };
        validationTest(createPersonContract(), "Person", null, invalid);
    }

    @Test
    public void structValidatesParents() {
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
        validationTest(createPersonContract(), "Doctor", valid, invalid);
    }

    @Test
    public void enumValidatesVals() {
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

        Object[] valid = { validMap };
        Object[] invalid = { map, map2 };
        validationTest(createPersonContract(), "Child", valid, invalid);        
    }

    private Contract createPersonContract() {
        Struct s = new Struct("Person", "");
        s.getFields().put("name", new Field("name", "string"));
        s.getFields().put("age", new Field("age", "int"));

        Struct s2 = new Struct("Doctor", "Person");
        s2.getFields().put("yearsInPractice", new Field("yearsInPractice", "float"));

        Struct s3 = new Struct("Child", "Person");
        s3.getFields().put("faveColor", new Field("faveColor", "Color"));

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

    private void validationTest(String type, Object[] valid, Object[] invalid) {
        validationTest(new Contract(), type, valid, invalid);
    }

    private void validationTest(Contract c, String type, 
                                Object[] valid, Object[] invalid) {
        if (valid != null) {
            for (Object o : valid) {
                ValidationResult vr = c.validate(type, o, false);
                assertTrue(vr.getMessage(), vr.isValid());
            }
        }

        if (invalid != null) {
            for (Object o : invalid) {
                ValidationResult vr = c.validate(type, o, false);
                assertFalse(vr.getMessage(), vr.isValid());
            }
        }
    }
}
