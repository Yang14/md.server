package index.common;

import base.md.MdPos;
import base.tool.PortEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mr-yang on 16-1-11.
 */
public class CommonModuleImpl implements CommonModule {
    private static Logger logger = LoggerFactory.getLogger("CommonModuleImpl");

    private static Random random = new Random();
    public static List<String> ipArray = new ArrayList<String>();
    public static int ipLen;
    private AtomicInteger cycle = new AtomicInteger(0);
    private final Object obj = new Object();
    //超过阈值的桶
    public final static Set<Long> dCodeSet =  Collections.synchronizedSet(new HashSet<Long>());

    public CommonModuleImpl() {
        //启动后台订阅线程
        new SubThread().start();
    }

    @Override
    public long genFCode() {
        long fCode = random.nextLong();
        return fCode >= 0 ? fCode : -fCode;
    }

    @Override
    public DCodeMap genDCode() {
        long dCode = random.nextLong();
        int bsNode;
        synchronized (obj) {
            bsNode = cycle.getAndIncrement();
            if (bsNode == ipLen - 1) {
                cycle.set(0);
            }
        }
        return new DCodeMap(dCode, bsNode);
    }


    @Override
    public boolean isDCodeFit(long dCode) {
        if (dCodeSet.contains(dCode)){
            dCodeSet.remove(dCode);
            return false;
        }
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode, int bsNode) {
        return new MdPos(ipArray.get(bsNode), PortEnum.SSDB_PORT, dCode);
    }

    @Override
    public MdPos buildMdPos(DCodeMap dCodeMap) {
        return buildMdPos(dCodeMap.getdCode(), dCodeMap.getBsNode());
    }

    @Override
    public MdPos createMdPos() {
        return null;
    }

    @Override
    public List<MdPos> buildMdPosList(Map<Long, Integer> dCodesMap) {
        if (dCodesMap == null) {
            return null;
        }
        List<MdPos> mdPoses = new ArrayList<MdPos>();
        for (long key : dCodesMap.keySet()) {
            mdPoses.add(buildMdPos(key, dCodesMap.get(key)));
        }
        return mdPoses;
    }
}
