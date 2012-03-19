package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;

public class TestUtil {

    static String idlFile = "conform.json";

    public static Contract loadConformContract() throws IOException {
        InputStream is = 
            TestUtil.class.getClassLoader().getResourceAsStream(idlFile);
        Contract c = Contract.load(is);
        is.close();
        return c;
    }

}