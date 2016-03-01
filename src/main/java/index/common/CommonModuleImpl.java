package index.common;

import base.md.MdPos;
import base.tool.PortEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Mr-yang on 16-1-11.
 */
public class CommonModuleImpl implements CommonModule {

    private static Random random = new Random(Long.MAX_VALUE);
    private final long one = Long.MIN_VALUE;
    private final long two = Long.MAX_VALUE * (1 / 3);
    private final long three = Long.MAX_VALUE * (2 / 3);
    private final long four = Long.MAX_VALUE;

    @Override
    public long genFCode() {
        return random.nextLong();
    }

    @Override
    public long genDCode() {
        return (long) (new Random().nextInt());
    }

    @Override
    public boolean isDCodeFit(long dCode) {
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode) {
        MdPos md = new MdPos();
        if (dCode < two) {
            md.setIp("192.168.0.58");
        } else if (two <= dCode && dCode < three) {
            md.setIp("192.168.0.13");
        } else if (three <= dCode) {
            md.setIp("192.168.0.12");
        } else {
            md.setIp("192.168.0.12");
        }
        md.setIp("192.168.0.10");
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
