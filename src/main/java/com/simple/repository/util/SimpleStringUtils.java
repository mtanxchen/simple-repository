package com.simple.repository.util;

/**
 * 字符处理工具
 *
 * @author laiqx
 * @version 1.0.1
 * @date 2022-11-14
 */
public class SimpleStringUtils {

    /**
     * 驼峰转下划线
     *
     * @param str 待转换字符
     * @return 返回下划线字符
     */
    public static String humpToUnderline(String str) {
        StringBuilder result = new StringBuilder();
        if (str != null && str.length() > 0) {
            result.append(str.substring(0, 1).toLowerCase());
            for (int i = 1; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                result.append(s.toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     *
     * @param text             内容
     * @param initialUpperCase 是否首字母大写
     * @return
     */
    public static String underlineToHump(String text, boolean initialUpperCase) {
        if (isEmpty(text)) {
            return text;
        } else if (!text.contains("_")) {
            // 不含下划线，仅将首字母大写
            return initialUpperCase ? initialUpperCase(text) : initialLowerCase(text);
        }
        // 用下划线将原始字符串分割
        StringBuilder result = new StringBuilder();
        String[] camels = text.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (isNotEmpty(camel)) {
                result.append(initialUpperCase(camel));
            }
        }
        text = result.toString();
        return initialUpperCase ? initialUpperCase(text) : initialLowerCase(text);
    }

    /**
     * 替换所有
     *
     * @param text    内容
     * @param target  待替换的文本
     * @param replace 替换的文本
     * @return 返回替换完成的内容
     */
    public static String replaceAll(String text, String target, String replace) {
        if (isEmpty(text)) {
            return text;
        }
        String oldText = text;
        text = text.replace(target, replace);
        if (!text.equals(oldText)) {
            text = replaceAll(text, target, replace);
        }
        return text;
    }

    /**
     * 下划线转驼峰
     *
     * @param text 内容
     */
    public static String underlineToHump(String text) {
        return underlineToHump(text, true);
    }

    /**
     * 首字母小写
     *
     * @param text 字符串
     */
    public static String initialLowerCase(String text) {
        if (isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    /**
     * 首字母大写
     *
     * @param text 字符串
     */
    public static String initialUpperCase(String text) {
        if (isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }


    /**
     * 空判断
     *
     * @param cs
     * @return 返回是否为空
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 非空判断
     *
     * @param cs 字符
     * @return 返回是否非空
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }
}
