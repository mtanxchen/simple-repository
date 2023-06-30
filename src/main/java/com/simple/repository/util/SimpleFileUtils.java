package com.simple.repository.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件处理工具
 *
 * @author laiqx
 * date 2023-02-11
 */
public class SimpleFileUtils {

    /**
     * 创建目录
     *
     * @param path 路径
     */
    public static void createDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 创建文件
     * @param path 目录
     * @param content 文档
     * @throws IOException 创建文件异常
     */
    public static void createFile(String path, String content) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(content.getBytes());
        outputStream.close();
    }

    /**
     * 判断是否已存在
     * @param path 文件路径
     * @return 返回是否存在文件
     */
    public static boolean exists(String path) {
        return new File(path).exists();
    }

    /**
     * 相对转绝对目录
     * @param path 相对路径
     * @return 返回绝对目录
     * @throws IOException 文件转换异常
     */
    public static String relativeToAbsolutePath(String path) throws IOException {
        File directory = new File(path);
        return directory.getCanonicalPath();
    }

}
