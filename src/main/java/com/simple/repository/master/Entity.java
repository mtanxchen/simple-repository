package com.simple.repository.master;


public class Entity<T extends Number> {

    public T id;
    public static final String ID = "id";

    public Class<?> getIdentityClass(){
        return null;
    }
}
