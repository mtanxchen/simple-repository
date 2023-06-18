package com.simple.repository;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.generate.SimpleGenerate;
import com.simple.repository.generate.SimpleSqlConstGenerate;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 代码启动生成
 * @author laiqx
 * @date 2023-03-01
 */
public class GenerateStart {

    /**
     * 代码生成入库
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        SimpleConfig.setRunEnv(args);
        new SimpleGenerate().run();
        new SimpleSqlConstGenerate().run();
    }

}
