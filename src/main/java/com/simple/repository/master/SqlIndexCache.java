package com.simple.repository.master;

import com.simple.repository.util.SimpleStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sql预加载
 *
 * @author laiqx
 * @version 1.0.1
 */

public class SqlIndexCache {

    private final static Logger log = LoggerFactory.getLogger(SqlIndexCache.class);
    private static Map<String, String> sqlMap = new ConcurrentHashMap<>();

    public static String getCacheSql(String idnex) {
        return sqlMap.get(idnex);
    }

    public static void pushCacheSql(String idnex, String sql) {
        sqlMap.put(idnex, sql);
    }

    public static void pushCacheSql(Map<String, String> sqlMap) {
        for (String key : sqlMap.keySet()) {
            sqlMap.put(key, sqlMap.get(key));
        }
    }

    /**
     * 重新加载sql游标
     *
     * @throws IOException
     */
    public static void initSqlIndex() throws IOException {
        Map<String, String> sqlContentMap = getFileSqlContent("sql/");
        for (String key : sqlContentMap.keySet()) {
            // 解析文档中的sql语句
            String fileTag = SimpleStringUtils.underlineToHump(key);
            log.info("sql预加载:装置sql中,fileTag={}", fileTag);
            analysisDocContent(fileTag, sqlContentMap.get(key));
        }
    }


    /**
     * 解析文档内容
     * <p>
     * 将文档的sql语句处理后保存到sqlMap中
     * 1.通过-- 和; 截取出每个sql语句的名称和sql
     * 2.并将sql名称转为常量命名
     * </p>
     *
     * @param fileTag 文件标识
     * @param data    文本数据
     */
    public static void analysisDocContent(String fileTag, String data) {
        // 读取文档内容后进行解析
        String[] sqlArray = data.split(";");
        for (String sqlModel : sqlArray) {
            sqlModel = sqlModel.substring(sqlModel.indexOf("@name") + 5);
            Integer index = sqlModel.contains("\r\n") ? sqlModel.indexOf("\r\n") : sqlModel.indexOf("\n");
            String sqlName = sqlModel.substring(0, index);
            sqlName = fileTag + "." + sqlName.trim();
            String sql = sqlModel.substring(sqlModel.indexOf("*/") + 2).replaceAll("\r\n", " ")
                    .replaceAll("\n", " ").toLowerCase().trim() + ";";
            for (int i = 0; i < 10; i++) {
                sqlName = sqlName.replaceAll("  ", " ");
                sql = sql.replaceAll("  ", " ");
            }
            pushCacheSql(sqlName, sql);
            log.info("sql预加载:装置sql语句完成,sqlName = {},sql = {}", sqlName, sql);
        }
    }

    /**
     * 获取Sql的yml配置
     *
     * @param path sql的yml文件路径
     * @return
     */
    private static Map<String, String> getFileSqlContent(String path) throws IOException {
        // 获取目录下的所有文件
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + "/sql/*.sql");
        if (resources.length <= 0) {
            throw new RuntimeException("加载sql模板失败,未找到sql文件");
        }
        Map<String, String> map = new HashMap<>();
        for (Resource resource : resources) {
            InputStream inputStream = resource.getInputStream();
            OutputStream outputStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);//将读到的字节写入输出流
            }
            inputStream.close();
            String content = outputStream.toString();
            outputStream.close();
            String name = resource.getFilename().toLowerCase().replaceAll(".sql", "");
            map.put(name, content);
        }
        return map;
    }
}
