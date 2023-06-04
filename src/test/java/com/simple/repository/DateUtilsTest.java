package com.simple.repository;

import com.simple.repository.util.SimpleDateUtils;
import org.junit.Test;

import java.util.Date;


public class DateUtilsTest {

    @Test
    public void test() {
        Date date = new Date();
        SimpleDateUtils.dateToStr(date, SimpleDateUtils.FormatType.DATE_FORMAT);

        String day = "2023-05-01";
        Date date2  = SimpleDateUtils.strToDate(day, SimpleDateUtils.FormatType.DAY_FORMAT);

        Integer dayNum = SimpleDateUtils.dimDd(date, date2);
        Date beforeDate = SimpleDateUtils.beforeDate(date, 3, SimpleDateUtils.TimeType.day);
    }


}
