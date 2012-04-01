package com.bitmechanic.barrister;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;

/**
 * Represents an 'enum' in the IDL
 */
public class Enum extends BaseEntity {

    private Set<String> values;

    /**
     * Creates an enum with the given name and values
     *
     * @param name Name of enum from IDL
     * @param vals Valid values for enum from IDL
     */
    public Enum(String name, String... vals) {
        this.name = name;
        this.values = new LinkedHashSet<String>();
        for (String v : vals) {
            values.add(v);
        }
    }

    /**
     * Creates an enum from IDL Map representation, parsing the 'name', 'comment', and 'values'
     */
    @SuppressWarnings("unchecked")
    public Enum(Map<String,Object> data) {
        super(data);
        values = new LinkedHashSet<String>();
        
        List<Map<String,Object>> dvals = (List<Map<String,Object>>)data.get("values");
        for (Map<String,Object> val : dvals) {
            values.add((String)val.get("value"));
        }
    }

    /**
     * Returns the list of values for this enum
     */
    public Set<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Enum name=");
        sb.append(name);
        sb.append(" values=");
        boolean first = true;
        for (String v : values) {
            if (first) { first = false; }
            else { sb.append(", "); }
            sb.append(v);
        }
        return sb.toString();
    }

}