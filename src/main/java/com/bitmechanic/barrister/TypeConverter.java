package com.bitmechanic.barrister;

/**
 * Encapsulates methods used to convert data between the JSON-RPC representation and their
 * native Java representation.  Type validation occurs in the marshal and unmarshal methods.
 */
public interface TypeConverter {

    /**
     * Returns the Java Class that this type will unmarshal to
     */
    public Class getTypeClass();

    /**
     * Converts o to its Java type and returns it.
     *
     * @param o Object as represented on the wire (e.g. String, Boolean, Double, Long, 
     *          Map, List)
     * @return Java representation of type
     * @throws RpcException If o does not match the expected type rules in the IDL
     */
    public Object unmarshal(Object o) throws RpcException;

    /**
     * Converts o from its Java type to its wire representation. 
     * 
     * @param o Java type to marshal to the wire
     * @return Wire representation (e.g. String, Boolean, Double, Long, Map, List)
     * @throws RpcException If o does not match the expected type rules in the IDL
     */
    public Object marshal(Object o) throws RpcException;

}