package index.common;

import index.tool.JedisPoolUtils;
import redis.clients.jedis.Jedis;

/**
 * Created by Mr-yang on 16-4-14.
 */
public class SubThread extends Thread {
    private final MDSubscribe subscriber = new MDSubscribe();

    private final String[] channels = new String[]{"overSizeDCode","ipAddress"};

    @Override
    public void run() {
        System.out.println(String.format("subscribe redis, channel %s, thread will be blocked", channels));
        Jedis jedis = null;
        try {
            jedis = JedisPoolUtils.getJedis();
            jedis.subscribe(subscriber, channels);
        } catch (Exception e) {
            System.out.println(String.format("subscribe channel error, %s", e));
        } finally {
            if (jedis != null) {
                JedisPoolUtils.returnResource(jedis);
            }
        }
    }
}