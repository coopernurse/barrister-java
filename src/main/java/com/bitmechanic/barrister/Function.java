package com.bitmechanic.barrister;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Function extends BaseEntity {    

    private List<Field> params;
    private Field returns;

    public Function(Map<String,Object> data) {
        super(data);
     
        params = new ArrayList<Field>();
        
        List<Map<String,Object>> plist = (List<Map<String,Object>>)data.get("params");
        for (Map<String,Object> p : plist) {
            params.add(new Field(p));
        }

        returns = new Field("", (String)data.get("returns"));
    }

    public List<Field> getParams() {
        return params;
    }

    public Field getReturns() {
        return returns;
    }

}