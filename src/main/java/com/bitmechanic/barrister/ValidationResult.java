package com.bitmechanic.barrister;

public class ValidationResult {

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String msg) {
        return new ValidationResult(false, msg);
    }

    ////////////////////////////

    private boolean valid;
    private String msg;

    public ValidationResult(boolean valid, String msg) {
        this.valid = valid;
        this.msg = msg;
    }

    public boolean isValid() { 
        return valid; 
    }

    public String getMessage() { 
        return msg; 
    }

}