package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a IDL interface.
 */
public class Interface extends BaseEntity {

    private List<Function> functions;

    /**
     * Creates an Interface based on the IDL Map representation
     * Uses keys: 'name', 'comment', 'functions'
     *
     * Parses the Functions as well.
     */
    public Interface(Map<String,Object> data) {
        super(data);

        functions = new ArrayList<Function>();
        List<Map<String,Object>> flist = (List<Map<String,Object>>)data.get("functions");
        for (Map<String,Object> f : flist) {
            functions.add(new Function(f));
        }        
    }

    /**
     * Returns the Functions associated with this Interface
     */
    public List<Function> getFunctions() {
        return functions;
    }

    /**
     * Returns the Function with the given name, or null if none matches.
     */
    public Function getFunction(String name) {
        for (Function f : functions) {
            if (f.getName().equals(name))
                return f;
        }

        return null;
    }

    /**
     * Sets the Contract this Interface is a part of. Propegates to its Functions
     */
    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Function f : functions) {
            f.setContract(c);
        }
    }
}