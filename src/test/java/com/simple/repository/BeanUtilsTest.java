package com.simple.repository;

import com.simple.repository.master.search.Condition;
import com.simple.repository.search.TestDTO;
import com.simple.repository.test.entity.TestEntity;
import com.simple.repository.util.SimpleBeanUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BeanUtilsTest {

    @Test
    public void copyObject(){

        TestDTO dto = new TestDTO();
        dto.name = "张三";
        dto.age = 11;
        TestEntity entity = SimpleBeanUtils.copyObject(dto, new TestEntity());

    }

    @Test
    public void copyListObject(){
        TestDTO dto = new TestDTO();
        dto.name = "张三";
        dto.age = 11;
        TestDTO dto2 = new TestDTO();
        dto2.name = "张三2";
        dto2.age = 112;
        List<TestEntity> entities = SimpleBeanUtils.copyListObject(Arrays.asList(dto, dto2), TestEntity.class);
    }

    @Test
    public void getFields(){
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(new TestDTO());
        fieldMap = SimpleBeanUtils.getFields(TestDTO.class);
    }

}
