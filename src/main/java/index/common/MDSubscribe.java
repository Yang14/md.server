package index.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by Mr-yang on 16-4-14.
 */
public class MDSubscribe extends JedisPubSub {
    private static Logger logger = LoggerFactory.getLogger("MDSubscribe");

    @Override
    public void onMessage(String s, String s2) {
        if (s.equals("overSizeDCode")){
            CommonModuleImpl.dCodeSet.add(Long.valueOf(s2));
        }else if (s.equals("ipAddress")){
            CommonModuleImpl.ipArray.add(s2);
            CommonModuleImpl.ipLen++;
            logger.info("register MDS from channel: " + s + " and ip:" + s2);
        }
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
