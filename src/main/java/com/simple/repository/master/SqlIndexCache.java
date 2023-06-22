package com.simple.repository.master;

import com.simple.repository.util.SimpleJson;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * sql预加载
 *
 * @author laiqx
 * @version 1.0.1
 */

public class SqlIndexCache {

    private final static Logger log = LoggerFactory.getLogger(SqlIndexCache.class);
    private static Map<String, SqlCacheDTO> sqlMap = new ConcurrentHashMap<>();

    public static String getCacheSql(String idnex) {
        return sqlMap.get(idnex).sql;
    }

    public static List<String> getRequiredFiled(String idnex) {
        return sqlMap.get(idnex).requiredField;
    }

    /**
     * 缓存sql语句
     *
     * @param index         sql索引
     * @param sql           sql对象
     * @param requiredField 非空字段
     */
    public static void pushCacheSql(String index, String sql, List<String> requiredField) {
        sqlMap.put(index, new SqlCacheDTO(index, sql, requiredField));
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
            analysisDocContent(fileTag, sqlContentMap.get(key));
        }
    }


    /**
     * 解析文档内容
     * <p>
     * 将文档的sql语句处理后保存到sqlMap中
     * 1.通过@name截取出sql方法名
     * 2.通过@required 截取非空传参
     * 3.将sql名称转为常量命名
     * 4.保存到sql缓存map中
     * </p>
     *
     * @param fileTag 文件标识
     * @param data    文本数据
     */
    private static void analysisDocContent(String fileTag, String data) {
        // 读取文档内容后进行解析
        String[] sqlArray = data.split(";");
        for (String sqlModel : sqlArray) {
            // 获取sql方法名
            String sqlName = getAnnotationText(sqlModel, SQL_NAME_TAG);
            sqlName = fileTag + "." + sqlName.trim();
            sqlName = SimpleStringUtils.replaceAll(sqlName, "  ", " ");
            // 获取必填的字段
            String filedStr = getAnnotationText(sqlModel, REQUIRED_FILED_TAG);
            List<String> requiredField = SimpleStringUtils.isEmpty(filedStr) ? new ArrayList<>()
                    : Arrays.stream(filedStr.split(",")).map(String::trim).collect(Collectors.toList());
            // 截取sql语句
            String sql = sqlModel.substring(sqlModel.indexOf("*/") + 2).replaceAll("\r\n", " ")
                    .replaceAll("\n", " ").toLowerCase().trim() + ";";
            sql = SimpleStringUtils.replaceAll(sql, "  ", " ");
            pushCacheSql(sqlName, sql, requiredField);
            log.info("sql预加载:装置sql语句完成,sqlName = {},requiredField={},sql = {}", sqlName, SimpleJson.toJsonString(requiredField), sql);
        }
    }

    /**
     * 获取注解内容
     *
     * @param sql 原sql语句
     * @return 非空参数列表
     */
    private static String getAnnotationText(String sql, String tag) {
        Integer index = sql.indexOf(tag);
        if (index <= 0) {
            return "";
        }
        String newSql = sql.substring(index + tag.length());
        Integer lastIndex = newSql.contains("\r\n") ? newSql.indexOf("\r\n") : newSql.indexOf("\n");
        return newSql.substring(0, lastIndex);
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


    /**
     * sql缓存对象
     */
    private static class SqlCacheDTO {
        /**
         * sql索引
         */
        public String index;
        /**
         * sql语句
         */
        public String sql;
        /**
         * 非空字段
         */
        public List<String> requiredField;

        public SqlCacheDTO(String index, String sql, List<String> requiredField) {
            this.index = index;
            this.sql = sql;
            this.requiredField = requiredField;
        }
    }

    /**
     * 必填字段标识
     */
    private static final String REQUIRED_FILED_TAG = "@required";

    /**
     * sql名称标识
     */
    private static final String SQL_NAME_TAG = "@name";
}
