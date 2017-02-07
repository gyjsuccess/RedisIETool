package com.d5.util;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
public class JedisIPPortUtil {
	private Logger log = LoggerFactory.getLogger(JedisIPPortUtil.class);
	private String JEDIS_IP;
	private int JEDIS_PORT;
	private String JEDIS_PASSWD;
	private JedisPool jedisPool;
	private JedisIPPortUtil(){}
	public static JedisIPPortUtil getInstance(){
		return new JedisIPPortUtil();
	}
	public JedisIPPortUtil init(String ip, int port, String passwd){
		ConfigurationUtil conf = ConfigurationUtil.getInstance(
				CommonUtil.getHome() + System.getProperty("file.separator") + "conf"
						+ CommonUtil.getProgramName() + System.getProperty("file.separator") + "conf_redis.ini");
		JEDIS_IP = ip;
		JEDIS_PORT = port;
		JEDIS_PASSWD = passwd;
		log.debug("JEDIS_IP:{}", JEDIS_IP);
		log.debug("JEDIS_PORT:{}", JEDIS_PORT);
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(conf.getInt("redis.pool.maxActive", 1000));
		config.setMaxIdle(conf.getInt("redis.pool.maxIdle", 20));
		config.setMaxWait(conf.getLong("redis.pool.maxWait", 1000L));
		config.setTestOnBorrow(conf.getBoolean("redis.pool.testOnBorrow", true));
		config.setTestOnReturn(true);
		config.setTestWhileIdle(true);
		config.setMinEvictableIdleTimeMillis(60000l);
		config.setTimeBetweenEvictionRunsMillis(3000l);
		config.setNumTestsPerEvictionRun(-1);
		if(StringUtils.isBlank(JEDIS_PASSWD)){
			jedisPool = new JedisPool(config, JEDIS_IP, JEDIS_PORT, 60000);
		} else {
			jedisPool = new JedisPool(config, JEDIS_IP, JEDIS_PORT, 60000, JEDIS_PASSWD);
		}
		return this;
	}
	/**
	 * 获取数据
	 * @param key
	 * @return
	 */
	public String get(String key, int index) {
		String value = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			value = jedis.get(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return value;
	}

	/**
	 *
	 * @param jedis
     */
	public void close(Jedis jedis) {
		try {
			jedisPool.returnResource(jedis);
		} catch (Exception e) {
			if (jedis.isConnected()) {
				jedis.quit();
				jedis.disconnect();
			}
		}
	}
	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public byte[] get(byte[] key, int index) {
		byte[] value = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			value = jedis.get(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return value;
	}

	/**
	 *
	 * @param key
	 * @param value
	 * @param index
     */
	public void set(byte[] key, byte[] value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.set(key, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 *
	 * @param key
	 * @param value
	 * @param time
     * @param index
     */
	public void set(byte[] key, byte[] value, int time, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.set(key, value);
			jedis.expire(key, time);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 *
	 * @param key
	 * @param field
	 * @param value
     * @param index
     */
	public void hset(byte[] key, byte[] field, byte[] value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.hset(key, field, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 *
	 * @param key
	 * @param field
	 * @param value
     * @param index
     */
	public void hset(String key, String field, String value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.hset(key, field, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public String hget(String key, String field, int index) {
		String value = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			value = jedis.hget(key, field);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return value;
	}
	/**
	 * 获取数据
	 *
	 * @param key
	 * @return
	 */
	public byte[] hget(byte[] key, byte[] field, int index) {
		byte[] value = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			value = jedis.hget(key, field);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return value;
	}

	/**
	 *
	 * @param key
	 * @param field
	 * @param index
     */
	public void hdel(byte[] key, byte[] field, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.hdel(key, field);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 存储REDIS队列 顺序存储
	 * @param byte[] key reids键名
	 * @param byte[] value 键值
	 */
	public void lpush(byte[] key, byte[] value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.lpush(key, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 存储REDIS队列 顺序存储
	 * @param String key reids键名
	 * @param String value 键值
	 */
	public void lpush(String key, String value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.lpush(key, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 存储REDIS队列 反向存储
	 * @param byte[] key reids键名
	 * @param byte[] value 键值
	 */
	public void rpush(byte[] key, byte[] value, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.rpush(key, value);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 将列表 source 中的最后一个元素(尾元素)弹出，并返回给客户端
	 * @param byte[] key reids键名
	 * @param byte[] value 键值
	 */
	public void rpoplpush(byte[] key, byte[] destination, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.rpoplpush(key, destination);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}
	/**
	 * 获取队列数据
	 * @param byte[] key 键名
	 * @return
	 */
	public List<byte[]> lpopList(byte[] key, int index) {
		List<byte[]> list = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			list = jedis.lrange(key, 0, -1);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return list;
	}
	/**
	 * 获取队列数据
	 * @param byte[] key 键名
	 * @return
	 */
	public byte[] rpop(byte[] key, int index) {
		byte[] bytes = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			bytes = jedis.rpop(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return bytes;
	}
	/**
	 * 获取队列数据
	 * @param String key 键名
	 * @return
	 */
	public String rpop(String key, int index) {
		String bytes = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			bytes = jedis.rpop(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return bytes;
	}

	/**
	 * 向hash中添加数据
	 * @param key
	 * @param hash
	 * @param index
     */
	public void hmset(Object key, Map<String, String> hash, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.hmset(key.toString(), hash);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 *
	 * @param key
	 * @param hash
	 * @param time
     * @param index
     */
	public void hmset(Object key, Map<String, String> hash, int time, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.hmset(key.toString(), hash);
			jedis.expire(key.toString(), time);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 * 获取数据
	 * @param key
	 * @param index
	 * @param fields
     * @return
     */
	public List<String> hmget(Object key, int index, String... fields) {
		List<String> result = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			result = jedis.hmget(key.toString(), fields);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}

	/**
	 *
	 * @param key
	 * @param index
     * @return
     */
	public Set<String> hkeys(String key, int index) {
		Set<String> result = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			result = jedis.hkeys(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}

	/**
	 *
	 * @param key
	 * @param from
	 * @param to
	 * @param index
     * @return
     */
	public List<byte[]> lrange(byte[] key, int from, int to, int index) {
		List<byte[]> result = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			result = jedis.lrange(key, from, to);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}

	/**
	 *
	 * @param key
	 * @param index
     * @return
     */
	public Map<byte[], byte[]> hgetAll(byte[] key, int index) {
		Map<byte[], byte[]> result = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			result = jedis.hgetAll(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}
	
	/**
	 *
	 * @param key
	 * @param index
    * @return
    */
	public Map<String, String> hgetAll(String key, int index) {
		Map<String, String> result = null;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			result = jedis.hgetAll(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return result;
	}

	/**
	 *
	 * @param key
	 * @param index
     */
	public void del(byte[] key, int index) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			jedis.del(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
	}

	/**
	 *
	 * @param key
	 * @param index
     * @return
     */
	public long llen(byte[] key, int index) {
		long len = 0;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			len = jedis.llen(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return len;
	}
	
	/**
	 *
	 * @param key
	 * @param index
     * @return
     */
	public long llen(String key, int index) {
		long len = 0;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			len = jedis.llen(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return len;
	}
	
	/**
	 *
	 * @param key
	 * @param index
     * @return
     */
	public long hlen(String key, int index) {
		long len = 0;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			len = jedis.hlen(key);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return len;
	}

	/**
	 *
	 * @param key
	 * @param fieldName
	 * @param index
     * @return
     */
	public boolean hexists(String key, String fieldName, int index) {
		Jedis jedis = null;
		Boolean isIn = false;
		try {
			jedis = jedisPool.getResource();
			if(index >= 0){jedis.select(index);}
			isIn = jedis.hexists(key, fieldName);
		} catch (Exception e) {
			//释放redis对象
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			//返还到连接池
			close(jedis);
		}
		return isIn;
	}
}