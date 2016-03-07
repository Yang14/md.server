package index.common;

import base.md.MdPos;
import base.tool.PortEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public long genFCode() {
        return random.nextLong();
    }

    @Override
    public long genDCode() {
//        return (long) (new Random().nextInt());
        return random.nextLong();
    }

    @Override
    public boolean isDCodeFit(long dCode) {
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode) {
        MdPos md = new MdPos();
        /*if (dCode < two) {
            md.setIp("192.168.0.58");
        } else if (two <= dCode && dCode < three) {
            md.setIp("192.168.0.13");
        } else if (three <= dCode) {
            md.setIp("192.168.0.12");
        } else {
            md.setIp("192.168.0.12");
        }*/
        if (dCode < two) {
            md.setIp("192.168.0.10");
        } else if (dCode < three) {
            md.setIp("192.168.0.58");
        } else {
            md.setIp("192.168.0.60");
        }
        md.setdCode(dCode);
        md.setPort(PortEnum.SSDB_PORT);
        return md;
    }

    @Override
    public MdPos createMdPos() {
        return buildMdPos(genDCode());
    }

    @Override
    public List<MdPos> buildMdPosList(List<Long> dCodeList) {
        if (dCodeList == null) {
            return null;
        }
        List<MdPos> mdPoses = new ArrayList<MdPos>();
        for (long code : dCodeList) {
            mdPoses.add(buildMdPos(code));
        }
        return mdPoses;
    }
}
