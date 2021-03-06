package com.bitmechanic.barrister.conform;

import com.bitmechanic.barrister.RpcException;
import com.bitmechanic.test.conform.*;
import com.bitmechanic.test.inc.*;
import java.util.List;
import java.util.ArrayList;

public class AImpl implements A {

    public Long add(Long a, Long b) throws RpcException {
        return a+b;
    }

    public Double calc(Double[] nums, MathOp op) throws RpcException {
        double total = 0;
        if (op == MathOp.multiply)
            total = 1;

        for (Double d : nums) {
            if (op == MathOp.multiply)
                total = total * d;
            else if (op == MathOp.add)
                total += d;
        }
        return total;
    }
    
    public Double sqrt(Double a) throws RpcException {
        return Math.sqrt(a);
    }
    
    public RepeatResponse repeat(RepeatRequest req1) throws RpcException {
        String s = req1.getForce_uppercase() ? req1.getTo_repeat().toUpperCase() : req1.getTo_repeat();
        List<String> items = new ArrayList<String>();
        for (int i = 0; i < req1.getCount(); i++) {
            items.add(s);
        }
        return new RepeatResponse(req1.getCount(), items.toArray(new String[0]), Status.ok);
    }
    
    public HiResponse say_hi() throws RpcException {
        return new HiResponse("hi");
    }
    
    public Long[] repeat_num(Long num, Long count) throws RpcException {
        Long out[] = new Long[count.intValue()];
        for (int i = 0; i < out.length; i++) {
            out[i] = num;
        }
        return out;
    }

    public String putPerson(Person p) throws RpcException {
        return p.getPersonId();
    }
}
