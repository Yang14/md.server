package index.tool;

import base.md.MdPos;
import base.tool.PortEnum;
import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.spi.SSDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class ConnTool {
    private static Logger logger = LoggerFactory.getLogger("ConnTool");

    private static final Map<String, SSDB> ssdbHolder = new ConcurrentHashMap<String, SSDB>();

    public static SSDB getSSDB(MdPos mdPos) {
        SSDB ssdb = ssdbHolder.get(mdPos.getIp());
        if (ssdb == null) {
            ssdb = SSDBs.pool(mdPos.getIp(), PortEnum.SSDB_PORT, 1000000, null);
            ssdbHolder.put(mdPos.getIp(), ssdb);
        }
        return ssdb;
    }

}
