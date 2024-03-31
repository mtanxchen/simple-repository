package com.simple.repository;

import com.simple.repository.connect.RedisSession;
import org.junit.Test;

/**
 * reids工具测试
 */
public class RedisTest {

    @Test
    public void setNxTest() {
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                boolean pass = RedisSession.setNx("test:setNx", "1", 2);
                System.out.println("线程一setNx结果》》》pass=" + pass);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                boolean pass = RedisSession.setNx("test:setNx", "1", 2);
                System.out.println("线程二setNx结果》》》pass=" + pass);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        int i = 0;
        while (true) {
            i += 1;
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
