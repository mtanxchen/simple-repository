package com.simple.repository.connect;

import com.simple.repository.config.SimpleConfig;
import com.simple.repository.util.SimpleStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis会话
 *
 * @author laiqx
 * @date 2023-02-25
 */
public class RedisSession {

    private static JedisPool jedisPool;

    private RedisSession() {
    }

    /**
     * 开启redis会话
     *
     * @throws IOException 初始化异常抛出
     */
    public synchronized static void open() throws IOException {
        if (null == jedisPool) {
            jedisPool = new JedisPool(SimpleConfig.initConfig().redis.getHost(), SimpleConfig.initConfig().redis.getPort(), null, SimpleConfig.initConfig().redis.getPassword());
        }
    }

    /**
     * 获取jedis操作对象
     *
     * @return 返回jedis操作对象
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 检测链接
     */
    public static void checkConnect() {
        if (null == jedisPool || jedisPool.isClosed()) {
            throw new RuntimeException("RedisSession not open!");
        }
    }

    /**
     * 检测过期时间，如果没有设置则设置默认
     *
     * @param jedis redis操作对象
     * @param key   索引
     */
    public static void checkExpire(Jedis jedis, String key) {
        if (0 > jedis.ttl(key)) {
            jedis.expire(key, DEFAULT_EXPIRES_SEC);
        }
    }


    public static void expire(String key, long sec) {
        Jedis jedis = jedisPool.getResource();
        jedis.expire(key, sec <= 0 ? DEFAULT_EXPIRES_SEC : sec);
        jedis.close();
    }

    public static Long incr(String key) {
        Jedis jedis = jedisPool.getResource();
        Long value = jedis.incr(key);
        jedis.close();
        return value;
    }

    public static Long incr(String key, long step) {
        Jedis jedis = jedisPool.getResource();
        Long value = jedis.incrBy(key, step);
        jedis.close();
        return value;
    }

    public static boolean setNx(String key, String value, long sec) {
        Jedis jedis = jedisPool.getResource();
        boolean result = jedis.setnx(key, value) > 0;
        if (true) {
            jedis.expire(key, sec <= 0 ? DEFAULT_EXPIRES_SEC : sec);
        } else {
            checkExpire(jedis, key);
        }
        jedis.close();
        return result;
    }

    public static boolean set(String key, String value, long sec) {
        Jedis jedis = jedisPool.getResource();
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
        if (keys.length < 0) {
            return new ArrayList<>();
        }
        Jedis jedis = jedisPool.getResource();
        List<String> values = jedis.mget(keys);
        jedis.close();
        return values;
    }

    public static void setHash(String key, String name, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset(key, name, value);
        jedis.close();
    }

    public static void setHash(String key, Map<String, String> map) {
        Jedis jedis = jedisPool.getResource();
        jedis.hset(key, map);
        jedis.close();
    }

    public static String getHash(String key, String name) {
        Jedis jedis = jedisPool.getResource();
        String value = jedis.hget(key, name);
        jedis.close();
        return value;
    }

    public static Map<String, String> getAllHash(String key) {
        Jedis jedis = jedisPool.getResource();
        Map<String, String> value = jedis.hgetAll(key);
        jedis.close();
        return value;
    }

    public static void removeHash(String key, String... names) {
        Jedis jedis = jedisPool.getResource();
        jedis.hdel(key, names);
        jedis.close();
    }

    public static void addSet(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.sadd(key, value);
        jedis.close();
    }

    public static Set<String> getSet(String key) {
        Jedis jedis = jedisPool.getResource();
        Set<String> value = jedis.smembers(key);
        jedis.close();
        return value;
    }

    public static void removeSet(String key, String... values) {
        Jedis jedis = jedisPool.getResource();
        jedis.srem(key, values);
        jedis.close();
    }

    public static void remove(String key) {
        Jedis jedis = jedisPool.getResource();
        jedis.del(key);
        jedis.close();
    }


    private static final long DEFAULT_EXPIRES_SEC = 60L;
}
