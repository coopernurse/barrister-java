package com.bitmechanic.barrister;

import org.junit.Test;
import static org.junit.Assert.*;
import com.bitmechanic.test.inc.Response;
import com.bitmechanic.test.inc.MathOp;
import com.bitmechanic.test.Person;

public class TypeConverterTest {
	
	@Test
	public void loadClass() throws Exception {
		Contract c = TestUtil.loadConformContract();

		Struct s = c.getStructs().get("inc.Response");
		StructTypeConverter conv = new StructTypeConverter(s, false);
		Response resp = (Response)conv.getTypeClass().newInstance();

		s = c.getStructs().get("Person");
		conv = new StructTypeConverter(s, false);
		Person p = (Person)conv.getTypeClass().newInstance();

		Enum e = c.getEnums().get("inc.MathOp");
		EnumTypeConverter conv2 = new EnumTypeConverter(e, false);

		assertEquals(MathOp.add, java.lang.Enum.valueOf(conv2.getTypeClass(), "add"));
	}

}