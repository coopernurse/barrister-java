package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.bitmechanic.test.A;

public class LoadContractTest {

    @Test
    public void canLoadContract() throws Exception {
        Contract c = TestUtil.loadConformContract();

        assertEquals(2, c.getInterfaces().size());
        assertEquals("A", c.getInterfaces().get("A").getName());
        assertEquals("B", c.getInterfaces().get("B").getName());

        Interface i = c.getInterfaces().get("A");
        assertEquals(7, i.getFunctions().size());
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

        assertEquals(5, c.getStructs().size());

        Struct s = c.getStructs().get("inc.Response");
        Map<String,Field> fields = s.getFields();
        assertEquals(1, fields.size());
        assertEquals("", s.getExtends());
        assertEquals("status", fields.get("status").getName());
        assertEquals("inc.Status", fields.get("status").getType());

        s = c.getStructs().get("RepeatResponse");
        fields = s.getFields();
        assertEquals("inc.Response", s.getExtends());
        assertEquals(2, fields.size());
        assertEquals("count", fields.get("count").getName());
        assertEquals("int", fields.get("count").getType());
        assertFalse(fields.get("count").isArray());

        assertEquals("items", fields.get("items").getName());
        assertEquals("string", fields.get("items").getType());
        assertTrue(fields.get("items").isArray());
        
        HashSet<String> expected = new HashSet<String>();
        expected.add("ok");
        expected.add("err");
        assertEquals(2, c.getEnums().size());
        assertEquals(expected, c.getEnums().get("inc.Status").getValues());
    }

    @Test
    public void testGetClassNameForEntity() throws Exception {
        Contract c = TestUtil.loadConformContract();
        c.setPackage("com.foo");
        c.setNsPackage("com.ns");

        assertEquals("com.foo.Person", c.getClassNameForEntity("Person"));
        assertEquals("com.ns.common.Foo", c.getClassNameForEntity("common.Foo"));
    }

    @Test
    public void testSetContractPackages() throws Exception {
        Contract c = TestUtil.loadConformContract();
        Server server = new Server(c);

        server.setContractPackage(A.class);

        assertEquals("com.bitmechanic.test", server.getContract().getPackage());
        assertEquals("com.bitmechanic.test", server.getContract().getNsPackage());
    }

}