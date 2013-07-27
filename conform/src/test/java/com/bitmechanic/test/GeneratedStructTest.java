package com.bitmechanic.test;

import com.bitmechanic.barrister.Contract;
import com.bitmechanic.barrister.Struct;
import com.bitmechanic.barrister.StructTypeConverter;
import com.bitmechanic.test.conform.*;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.lang.Exception;

import static org.junit.Assert.*;

public class GeneratedStructTest {

    Contract contract;

    @Before
    public void before() throws Exception {
        contract = Contract.load(new File("src/main/resources/com/bitmechanic/test/conform.json"));
        contract.setPackage("com.bitmechanic.test.conform");
        contract.setNsPackage("com.bitmechanic.test");
    }

    @Test
    public void repeatRequestTest() throws Exception {
        RepeatRequest r = new RepeatRequest("hi", 3L, false);
        assertEquals(r, new RepeatRequest("hi", 3L, false));

        Struct rrStruct = contract.getStructs().get("RepeatRequest");
        StructTypeConverter conv = new StructTypeConverter(rrStruct, false);
        Object o = conv.marshal(r);
        assertEquals(r, conv.unmarshal(o));
    }

    @Test
    public void marshalSubclassWorks() throws Exception {
        Person person = new Person.Builder()
                .email("bob@example.com")
                .firstName("John")
                .lastName("Doe")
                .personId("123")
                .build();

        PersonWithRoles personWithRoles = new PersonWithRoles.Builder()
                .email("bob@example.com")
                .firstName("John")
                .lastName("Doe")
                .personId("123")
                .roles(new String[] { "admin" })
                .build();
        Struct s = contract.getStructs().get("Person");
        StructTypeConverter conv = new StructTypeConverter(s, false);
        Object o = conv.marshal(personWithRoles);
        assertEquals(person, conv.unmarshal(o));
    }

}