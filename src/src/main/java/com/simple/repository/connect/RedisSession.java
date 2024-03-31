package com.simple.repository.connect;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.util.SimpleDateUtils;
import com.simple.repository.util.SimpleStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis会话
 *
 * @author laiqx
 * date 2023-02-25
 */
public class RedisSession {

    private static SimpleJedisPool simpleJedisPool;

    private static final List<SimpleJedisPool> expiresPools = new ArrayList<>();

    private RedisSession() {
    }

    /**
     * 获取jedis操作对象
     *
     * @return 返回jedis操作对象
     */
    public static Jedis getJedis() {
        if (null == simpleJedisPool || simpleJedisPool.expire()) {
            initSimpleJedisPool();
        }
        return simpleJedisPool.jedisPool.getResource();
    }

    /**
     * 检测过期时间，如果没有设置则设置默认
     *
     * @param jedis redis操作对象
     * @param key   索引
     * @param sec 过期秒数
     */
    public static void checkExpire(Jedis jedis, String key, long sec) {
        if (0 > jedis.ttl(key)) {
            jedis.expire(key, sec);
        }
    }

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param sec 过期秒数
     */
    public static void expire(String key, long sec) {
        Jedis jedis = getJedis();
        jedis.expire(key, sec <= 0 ? DEFAULT_EXPIRES_SEC : sec);
        jedis.close();
    }

    public static Long incr(String key) {
        Jedis jedis = getJedis();
        Long value = jedis.incr(key);
        jedis.close();
        return value;
    }

    public static Long incr(String key, long step) {
        Jedis jedis = getJedis();
        Long value = jedis.incrBy(key, step);
        jedis.close();
        return value;
    }

    public static boolean setNx(String key, String value, long sec) {
        sec = sec <= 0 ? DEFAULT_EXPIRES_SEC : sec;
        Jedis jedis = getJedis();
        boolean result = jedis.setnx(key, value) > 0;
        if (result) {
            jedis.expire(key, sec);
        } else {
            checkExpire(jedis, key, sec);
        }
        jedis.close();
        return result;
    }

    public static boolean set(String key, String value, long sec) {
        Jedis jedis = getJedis();
        sec = sec <= 0 ? DEFAULT_EXPIRES_SEC : sec;
        jedis.setex(key, sec, value);
        jedis.close();
        return true;
    }

    public static String get(String key) {
        if (SimpleStringUtils.isEmpty(key)) {
            return null;
        }
        return list(key).get(0);
    }

    public static List<String> list(String... keys) {
        Jedis jedis = getJedis();
        List<String> values = jedis.mget(keys);
        jedis.close();
        return values;
    }

    public static void setHash(String key, String name, String value) {
        Jedis jedis = getJedis();
        jedis.hset(key, name, value);
        jedis.close();
    }

    public static void setHash(String key, Map<String, String> map) {
        Jedis jedis = getJedis();
        jedis.hset(key, map);
        jedis.close();
    }

    public static String getHash(String key, String name) {
        Jedis jedis = getJedis();
        String value = jedis.hget(key, name);
        jedis.close();
        return value;
    }

    public static Map<String, String> getAllHash(String key) {
        Jedis jedis = getJedis();
        Map<String, String> value = jedis.hgetAll(key);
        jedis.close();
        return value;
    }

    public static void removeHash(String key, String... names) {
        Jedis jedis = getJedis();
        jedis.hdel(key, names);
        jedis.close();
    }

    public static void addSet(String key, String value) {
        Jedis jedis = getJedis();
        jedis.sadd(key, value);
        jedis.close();
    }

    public static Set<String> getSet(String key) {
        Jedis jedis = getJedis();
        Set<String> value = jedis.smembers(key);
        jedis.close();
        return value;
    }

    public static void removeSet(String key, String... values) {
        Jedis jedis = getJedis();
        jedis.srem(key, values);
        jedis.close();
    }

    public static void remove(String key) {
        Jedis jedis = getJedis();
        jedis.del(key);
        jedis.close();
    }


    /**
     * Redis链接对象
     * <p>
     * 解决jedis存在链接时间过长会报错问题
     * 每个连接只保持两小时
     * </p>
     */
    private static class SimpleJedisPool {

        /**
         * redis链接
         */
        public JedisPool jedisPool;

        /**
         * jedisPool创建时间
         */
        public Long time;

        /**
         * 过期时间
         */
        public final Long EXPIRES = 2 * SimpleDateUtils.HOUR_MSEL;

        public SimpleJedisPool() {
            this.jedisPool = new JedisPool(SimpleConfig.initConfig().redis.getHost(), SimpleConfig.initConfig().redis.getPort(), null, SimpleConfig.initConfig().redis.getPassword());
            this.time = System.currentTimeMillis();
        }

        /**
         * 是否过期
         */
        public boolean expire() {
            return System.currentTimeMillis() - time > EXPIRES;
        }
    }

    /**
     * 初始化SimpleJedisPool并异步执行回收过期链接
     */
    private static synchronized void initSimpleJedisPool() {
        if (null != simpleJedisPool && simpleJedisPool.expire()) {
            expiresPools.add(simpleJedisPool);
        }
        simpleJedisPool = new SimpleJedisPool();
        recyclePool();
    }

    /**
     * 回收过期的链接
     */
    private static void recyclePool() {
        new Thread(() -> {
            if (expiresPools.isEmpty() || expiresPools.size() == 1) {
                return;
            }
            for (int i = 0; i < expiresPools.size() - 1; i++) {
                expiresPools.remove(i).jedisPool.close();
            }
        }).start();
    }

    /**
     * 默认缓存时长
     */
    private static final long DEFAULT_EXPIRES_SEC = 60L;
}
