package com.bitmechanic.barrister;

import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Array;

/**
 * TypeConverter implementation for array types. This class wraps the 
 * TypeConverter for the actual type (e.g. IntTypeConverter, StructTypeConverter)
 * and uses it to marshal/unmarshal the elements in the array.
 */
public class ArrayTypeConverter extends BaseTypeConverter {

    private TypeConverter child;

    /**
     * @param child TypeConverter to delegate to for marshaling/unmarshaling 
     *        elements in the array
     * @param isOptional If true, null arrays are permitted
     */
    public ArrayTypeConverter(TypeConverter child, boolean isOptional) {
        super(isOptional);
        this.child = child;
    }

    public Class getTypeClass() {
        return Array.newInstance(child.getTypeClass(), 0).getClass();
    }

    public Object unmarshal(Object o) throws RpcException {
        if (o == null) {
            return returnNullIfOptional();
        }
        else if (o instanceof List) {
            List input = (List)o;
            Object arr[] = (Object[])Array.newInstance(child.getTypeClass(), input.size());
            for (int i = 0; i < arr.length; i++) {
                arr[i] = child.unmarshal(input.get(i));
            }

            System.out.println("Returning arr: " + arr.getClass() + " - " + Arrays.deepToString(arr));
            return arr;
        }
        else if (o.getClass().isArray()) {
            Object[] input = (Object[])o;
            Object arr[] = (Object[])Array.newInstance(child.getTypeClass(), input.length);
            for (int i = 0; i < arr.length; i++) {
                arr[i] = child.unmarshal(input[i]);
            }

            return arr;
        }
        else {
            throw RpcException.Error.INVALID_PARAMS.exc("Expected array, got: " +
                                                        o.getClass().getSimpleName());
        }
    }

    public Object marshal(Object o) throws RpcException {
        if (o == null) {
            return returnNullIfOptional();
        }
        else if (o.getClass().isArray()) {
            Object[] input = (Object[])o;
            Object arr[] = new Object[input.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = child.marshal(input[i]);
            }
            return arr;
        }
        else if (o instanceof List) {
            List input = (List)o;
            Object arr[] = new Object[input.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = child.marshal(input.get(i));
            }
            return arr;
        }
        else {
            throw RpcException.Error.INVALID_RESP.exc("Expected array, got: " +
                                                      o.getClass().getSimpleName());
        }
    }

}