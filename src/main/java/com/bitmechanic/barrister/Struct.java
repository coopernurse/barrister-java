package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an IDL 'struct'. 
 */
public class Struct extends BaseEntity {

    private Map<String, Field> fields;
    private List<String> fieldNames;
    private String extend;

    /**
     * Creates a new struct that may optionally extend another struct.
     * Initializes an empty Field list.
     */
    public Struct(String name, String extend) {
        this.name = name;
        this.extend = extend;
        this.fields = new HashMap<String,Field>();
        this.fieldNames = new ArrayList<String>();
    }

    /**
     * Creates a new struct from the IDL Map representation.  
     * Keys: 'name', 'comment', 'fields', 'extends'
     *
     * Unpacks the 'fields' on the map into Field objects.
     */
    @SuppressWarnings("unchecked") 
    public Struct(Map<String,Object> data) {
        super(data);
        fields = new HashMap<String,Field>();
        fieldNames = new ArrayList<String>();

        extend = (String)data.get("extends");
        
        List<Map<String,Object>> flist = (List<Map<String,Object>>)data.get("fields");
        for (Map<String,Object> f : flist) {
            Field field = new Field(f);
            fields.put(field.getName(), field);
            fieldNames.add(field.getName());
        }
    }

    /**
     * Sets the Contract associated with this Struct.  Propagates to Fields.
     */
    @Override
    public void setContract(Contract c) {
        super.setContract(c);
        for (Field f : fields.values()) {
            f.setContract(c);
        }
    }

    /**
     * Returns the 'extends' String associated with this Struct.
     */
    public String getExtends() {
        return extend;
    }
   
    /**
     * Returns a Map of the Fields belonging to this Struct. Keys are the Field names.
     */
    public Map<String,Field> getFields() {
        return fields;
    }

    /**
     * Returns a List of the Field names belonging to this Struct.
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * Returns a Map of the Fields belonging to this Struct and all its ancestors.
     * Keys are the Field names.
     */
    public Map<String,Field> getFieldsPlusParents() {
        Map<String,Field> tmp = new HashMap<String,Field>();
        tmp.putAll(fields);
        if (extend != null && !extend.equals("")) {
            Struct parent = contract.getStructs().get(extend);
            tmp.putAll(parent.getFieldsPlusParents());
        }
        return tmp;
    }

    /**
     * Returns a List of the Field names belonging to this Struct and all its ancestors.
     */
    public List<String> getFieldNamesPlusParents() {
        List<String> tmp = new ArrayList<String>();
        tmp.addAll(getFieldNames());
        if (extend != null && !extend.equals("")) {
            Struct parent = contract.getStructs().get(extend);
            tmp.addAll(parent.getFieldNamesPlusParents());
        }
        return tmp;
    }

    public Set<String> getDescendantFieldNames() {
        Set<String> names = new HashSet<String>();
        List<Struct> descendants = contract.getStructDescendants(this);
        for (Struct s : descendants) {
            names.addAll(s.getFieldNames());
        }
        return names;
    }

}