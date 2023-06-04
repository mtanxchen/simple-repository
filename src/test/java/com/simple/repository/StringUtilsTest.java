package com.simple.repository;

import com.simple.repository.util.SimpleStringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void emptyTest(){
        String str = "test";
        Boolean isEmpty = SimpleStringUtils.isEmpty(str);
        System.out.println(isEmpty);
        Boolean isNotEmpty = SimpleStringUtils.isNotEmpty(str);
        System.out.println(isNotEmpty);
    }

    @Test
    public void humpToUnderline(){
        String str = SimpleStringUtils.humpToUnderline("test");
    }

    @Test
    public void replaceAll(){
        String str = SimpleStringUtils.replaceAll("test1test2", "test", "hello");
    }
}
