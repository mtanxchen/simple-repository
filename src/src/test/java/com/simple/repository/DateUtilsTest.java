package com.simple.repository;

import com.simple.repository.util.SimpleDateUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 日期工具测试类
 *
 * @author laiqx
 * @date 2023-06-01
 */
public class DateUtilsTest {

    private final static Logger log = LoggerFactory.getLogger(DateUtilsTest.class);

    @Test
    public void format() {
        Date date = new Date();
        String dateStr = SimpleDateUtils.dateToStr(date, SimpleDateUtils.FormatType.DATE_FORMAT);
        log.info("日期工具测试类: 测试日期转字符串;format={},date={},dateStr={}", SimpleDateUtils.FormatType.DATE_FORMAT.getFormatLabel(), date, dateStr);
        dateStr = "2023-05-01";
        date = SimpleDateUtils.strToDate(dateStr, SimpleDateUtils.FormatType.DAY_FORMAT);
        log.info("日期工具测试类: 测试字符串转;format={},dayStr={} date={},dateStr={}", SimpleDateUtils.FormatType.DAY_FORMAT, dateStr, date);
    }


}
