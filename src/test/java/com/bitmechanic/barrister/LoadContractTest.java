package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoadContractTest {

    @Test
    public void canLoadContract() throws Exception {
        Contract c = TestUtil.loadConformContract();

        assertEquals(2, c.getInterfaces().size());
        assertEquals("A", c.getInterfaces().get("A").getName());
        assertEquals("B", c.getInterfaces().get("B").getName());

        Interface i = c.getInterfaces().get("A");
        assertEquals(5, i.getFunctions().size());
        Function f = i.getFunctions().get(0);
        assertEquals(2, f.getParams().size());
        assertEquals("a", f.getParams().get(0).getName());
        assertEquals("b", f.getParams().get(1).getName());
        assertEquals("int", f.getParams().get(0).getType());
        assertEquals("int", f.getParams().get(1).getType());
        assertEquals("int", f.getReturns().getType());
        f = i.getFunctions().get(4);
        assertEquals("say_hi", f.getName());
        assertEquals(0, f.getParams().size());
        assertEquals("HiResponse", f.getReturns().getType());

        assertEquals(4, c.getStructs().size());

        Struct s = c.getStructs().get("Response");
        Map<String,Field> fields = s.getFields();
        assertEquals(1, fields.size());
        assertEquals("", s.getExtends());
        assertEquals("status", fields.get("status").getName());
        assertEquals("Status", fields.get("status").getType());

        s = c.getStructs().get("RepeatResponse");
        fields = s.getFields();
        assertEquals("Response", s.getExtends());
        assertEquals(2, fields.size());
        assertEquals("count", fields.get("count").getName());
        assertEquals("int", fields.get("count").getType());
        assertFalse(fields.get("count").isArray());

        assertEquals("items", fields.get("items").getName());
        assertEquals("string", fields.get("items").getType());
        assertTrue(fields.get("items").isArray());
        
        assertEquals(1, c.getEnums().size());
                assertEquals(Arrays.asList("ok", "err"), 
                             c.getEnums().get("Status").getValues());
    }

}