package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

public class Struct extends BaseEntity {

    private Map<String, Field> fields;
    private String extend;

    public Struct(String name, String extend) {
        this.name = name;
        this.extend = extend;
        this.fields = new HashMap<String,Field>();
    }

    @SuppressWarnings("unchecked") 
    public Struct(Map<String,Object> data) {
        super(data);
        fields = new HashMap<String,Field>();

        extend = (String)data.get("extends");
        
        List<Map<String,Object>> flist = (List<Map<String,Object>>)data.get("fields");
        for (Map<String,Object> f : flist) {
            Field field = new Field(f);
            fields.put(field.getName(), field);
        }
    }

    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Field f : fields.values()) {
            f.setContract(c);
        }
    }

    public String getExtends() {
        return extend;
    }
   
    public Map<String,Field> getFields() {
        return fields;
    }

    public Map<String,Field> getFieldsPlusParents() {
        Map<String,Field> tmp = new HashMap<String,Field>();
        tmp.putAll(fields);
        if (extend != null && !extend.equals("")) {
            Struct parent = contract.getStructs().get(extend);
            tmp.putAll(parent.getFieldsPlusParents());
        }
        return tmp;
    }

}