package index.common;

import redis.clients.jedis.JedisPubSub;

/**
 * Created by Mr-yang on 16-4-14.
 */
public class MDSubscribe extends JedisPubSub {
    @Override
    public void onMessage(String s, String s2) {
        if (s.equals("overSizeDCode")){
            CommonModuleImpl.dCodeSet.add(Long.valueOf(s2));
        }
        System.out.println("recv from " + s + " channel, msg:" + s2);
    }

    @Override
    public void onPMessage(String s, String s2, String s3) {

    }

    @Override
    public void onSubscribe(String s, int i) {

    }

    @Override
    public void onUnsubscribe(String s, int i) {

    }

    @Override
    public void onPUnsubscribe(String s, int i) {

    }

    @Override
    public void onPSubscribe(String s, int i) {

    }
}
