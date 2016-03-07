package index.common;

import base.md.MdPos;
import base.tool.PortEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mr-yang on 16-1-11.
 */
public class CommonModuleImpl implements CommonModule {
    private static Logger logger = LoggerFactory.getLogger("CommonModuleImpl");

    private static Random random = new Random(Long.MAX_VALUE);
    private long seed = (0x7fffffffffffffffL / 3) * 2;
    private long two = 0x8000000000000000L + seed;
    private long three = two + seed;
    private String[] ipArray = new String[]{"192.168.0.10", "192.168.0.60", "192.168.0.58"};
    private int ipLen = ipArray.length;

    @Override
    public long genFCode() {
        return random.nextLong();
    }

    @Override
    public DCodeMap genDCode() {
        return new DCodeMap(random.nextLong(),random.nextInt(ipLen));
    }

    @Override
    public boolean isDCodeFit(int bsNode) {
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode,int bsNode) {
        MdPos md = new MdPos();
        md.setIp(ipArray[bsNode]);
        md.setdCode(dCode);
        md.setPort(PortEnum.SSDB_PORT);
        return md;
    }

    @Override
    public MdPos buildMdPos(DCodeMap dCodeMap) {
        return buildMdPos(dCodeMap.getdCode(),dCodeMap.getBsNode());
    }

    @Override
    public MdPos createMdPos() {
        return null;
    }

    @Override
    public List<MdPos> buildMdPosList(Map<Long,Integer> dCodesMap) {
        if (dCodesMap == null) {
            return null;
        }
        List<MdPos> mdPoses = new ArrayList<MdPos>();
        for (long key : dCodesMap.keySet()) {
            mdPoses.add(buildMdPos(key,dCodesMap.get(key)));
        }
        return mdPoses;
    }
}
