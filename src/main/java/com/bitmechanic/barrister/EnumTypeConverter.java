package com.bitmechanic.barrister;

public class EnumTypeConverter extends BaseTypeConverter {

    private Enum e;

    public EnumTypeConverter(Enum e, boolean isOptional) throws RpcException {
        super(isOptional);
        this.e = e;
    }

    public Class getTypeClass() {
        try {
            return Class.forName(e.getContract().getPackage() + "." + e.getName());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object unmarshal(String pkg, Object obj) throws RpcException {
        if (obj == null) {
            return returnNullIfOptional();
        }
        else if (obj.getClass() != String.class) {
            String msg = "'" + obj + "' enum must be String, got: " + 
                 obj.getClass().getSimpleName();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }
        else if (e.getValues().contains((String)obj)) {
            try {
                Class clz = Class.forName(e.getContract().getPackage()+"."+e.getName());
                return java.lang.Enum.valueOf(clz, (String)obj);
            }
            catch (Exception e) {
                String msg = "Could not set enum value '" + obj + "' - " + 
                    e.getClass().getSimpleName() + " - " + e.getMessage();
                throw RpcException.Error.INTERNAL.exc(msg);
            }
        }
        else {
            String msg = "'" + obj + "' is not in enum: " + e.getValues();
            throw RpcException.Error.INVALID_PARAMS.exc(msg);
        }
    }

    public Object marshal(Object o) throws RpcException {
        if (o == null)
            return returnNullIfOptional();
        else 
            return o;
    }

}