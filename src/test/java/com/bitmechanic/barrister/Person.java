package com.bitmechanic.barrister;

public class Person implements BStruct {

    private String name;
    private Long age;

    public String getName() { return name; }
    public void setName(String n) { name = n; }

    public Long getAge() { return age; }
    public void setAge(Long a) { age = a; }

}