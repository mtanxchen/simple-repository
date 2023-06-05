package com.simple.repository;

import com.simple.repository.search.TestDTO;
import com.simple.repository.test.entity.TestEntity;
import com.simple.repository.util.SimpleBeanUtils;
import com.simple.repository.util.SimpleJson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * bean工具测试类
 *
 * @author laiqx
 * @date 2023-06-05
 */
public class BeanUtilsTest {

    private final static Logger log = LoggerFactory.getLogger(BeanUtilsTest.class);

    @Test
    public void copyObject() {
        TestDTO dto = new TestDTO();
        dto.name = "张三";
        dto.age = 11;
        TestEntity entity = SimpleBeanUtils.copyObject(dto, new TestEntity());
        log.info("bean工具测试类:对象拷贝,dto={},entity={}", SimpleJson.toJsonString(dto), SimpleJson.toJsonString(entity));
    }

    @Test
    public void copyListObject() {
        TestDTO dto = new TestDTO();
        dto.name = "张三";
        dto.age = 11;
        TestDTO dto2 = new TestDTO();
        dto2.name = "张三2";
        dto2.age = 112;
        List<TestDTO> dtos = Arrays.asList(dto, dto2);
        List<TestEntity> entities = SimpleBeanUtils.copyListObject(dtos, TestEntity.class);
        log.info("bean工具测试类:对象列表拷贝,list={},entities={}", SimpleJson.toJsonString(entities), SimpleJson.toJsonString(entities));
    }

    @Test
    public void getFields() {
        Map<String, Field> fieldMap = SimpleBeanUtils.getFields(new TestDTO());
        Map<String, Field> fieldMap2 = SimpleBeanUtils.getFields(TestDTO.class);
        log.info("bean工具测试类:对象列表拷贝,fieldMap={},fieldMap2={}", SimpleJson.toJsonString(fieldMap), SimpleJson.toJsonString(fieldMap2));
    }

}
