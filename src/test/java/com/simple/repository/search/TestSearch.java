package com.simple.repository.search;

import com.simple.repository.master.search.BaseSearch;

import java.util.List;

public class TestSearch  extends BaseSearch {

    public List<String> ids;

    public String name;

    public void like(String fileName){
        fileName = "%"+fileName+"%";
    }
}
