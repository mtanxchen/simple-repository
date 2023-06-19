package com.simple.repository.search;

import com.simple.repository.master.search.BaseSearch;

import java.util.List;

public class TestSearch  extends BaseSearch {

    public List<Integer> ids;

    public String name;

    public Integer age;

    public void like(String fileName){
        fileName = "%"+fileName+"%";
    }
}
