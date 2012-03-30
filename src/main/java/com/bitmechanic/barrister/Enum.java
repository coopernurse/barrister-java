package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Enum extends BaseEntity {

    private List<String> values;

    public Enum(String name, String... vals) {
        this.name = name;
        this.values = new ArrayList<String>();
        for (String v : vals) {
            values.add(v);
        }
    }

    @SuppressWarnings("unchecked")
    public Enum(Map<String,Object> data) {
        super(data);
        values = new ArrayList<String>();
        
        List<Map<String,Object>> dvals = (List<Map<String,Object>>)data.get("values");
        for (Map<String,Object> val : dvals) {
            values.add((String)val.get("value"));
        }
    }

    public List<String> getValues() {
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