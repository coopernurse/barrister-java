package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;

public class TestUtil {

    static String idlFile = "conform.json";

    public static Contract loadConformContract() throws IOException {
        InputStream is = 
            TestUtil.class.getClassLoader().getResourceAsStream(idlFile);
        Contract c = Contract.load(is);
        c.setPackage(com.bitmechanic.test.BarristerMeta.PACKAGE_NAME);
        c.setNsPackage(com.bitmechanic.test.BarristerMeta.NS_PACKAGE_NAME);
        is.close();
        return c;
    }

}