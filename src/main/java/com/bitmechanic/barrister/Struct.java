package com.bitmechanic.barrister;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Struct extends BaseEntity {

    private Map<String, Field> fields;
    private String extend;

    public Struct(String name, String extend) {
        this.name = name;
        this.extend = extend;
        this.fields = new HashMap<String,Field>();
    }

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

    public ValidationResult validate(Object obj, boolean allowMissing) {
        if (!(obj instanceof Map)) {
            String msg = "struct " + name + " val must be Map, got: " + 
                 obj.getClass().getName();
            return ValidationResult.invalid(msg);
        }
        else {
            Map map = (Map)obj;
            Map<String,Field> allFields = getFieldsPlusParents();
            for (Object keyObj : map.keySet()) {
                String key = keyObj.toString();
                if (allFields.containsKey(key)) {
                    Field field = allFields.get(key);
                    Object val = map.get(keyObj);
                    ValidationResult vr = this.contract.validate(field.getType(),
                                                                 val, allowMissing);
                    if (!vr.isValid()) {
                        return vr;
                    }
                }
                else {
                    String msg = "field '" + key + "' not found in struct " + name;
                    return ValidationResult.invalid(msg);
                }
            }

            if (!allowMissing) {
                for (String key : allFields.keySet()) {
                    if (!map.containsKey(key)) {
                        String msg = "field '" + key + "' missing from: " + name;
                        return ValidationResult.invalid(msg);
                    }
                }
            }

            return ValidationResult.valid();
        }
    }

}