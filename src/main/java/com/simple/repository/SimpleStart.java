package com.simple.repository;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.connect.SimpleSession;
import com.simple.repository.generate.SimpleGenerate;
import com.simple.repository.generate.SimpleSqlConstGenerate;
import com.simple.repository.master.SqlIndexCache;

import java.io.IOException;
import java.sql.SQLException;

/**
 * simple启动服务
 * @author laiqx
 * @date 2023-02-23
 */
public class SimpleStart {

    /**
     * 服务启动入口
     * <p>
     *     使用simple-repository时需要调用
     * </p>
     * @throws IOException
     */
    public static void run(String [] args) throws IOException {
        SimpleConfig.setRunEnv(args);
        SimpleSession.openSession();
        SqlIndexCache.initSqlIndex();
    }

}
