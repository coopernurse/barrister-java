package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Interface extends BaseEntity {

    private List<Function> functions;

    public Interface(Map<String,Object> data) {
        super(data);

        functions = new ArrayList<Function>();
        List<Map<String,Object>> flist = (List<Map<String,Object>>)data.get("functions");
        for (Map<String,Object> f : flist) {
            functions.add(new Function(f));
        }        
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public Function getFunction(String name) {
        for (Function f : functions) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
    }

    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Function f : functions) {
            f.setContract(c);
        }
    }
}