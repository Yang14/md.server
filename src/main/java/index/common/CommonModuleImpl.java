package index.common;

import base.md.MdPos;
import base.tool.PortEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Mr-yang on 16-1-11.
 */
public class CommonModuleImpl implements CommonModule {
    private static Logger logger = LoggerFactory.getLogger("CommonModuleImpl");

    private static Random random = new Random();
    private String[] ipArray;
    private int ipLen;

    public CommonModuleImpl() {
        try {
            BufferedReader buf = new BufferedReader(new FileReader("/home/yang/workspace/bs_ip"));
            List<String> ipList = new ArrayList<String>();
            String ipStr;
            while ((ipStr = buf.readLine()) != null) {
                ipList.add(ipStr);
            }
            ipArray = (String[]) ipList.toArray();
            ipLen = ipList.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long genFCode() {
        long fCode = random.nextLong();
        return fCode >= 0 ? fCode : -fCode;
    }

    @Override
    public DCodeMap genDCode() {
        return new DCodeMap(random.nextLong(), random.nextInt(ipLen));
    }

    @Override
    public boolean isDCodeFit(int bsNode) {
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode, int bsNode) {
        return new MdPos(ipArray[bsNode], PortEnum.SSDB_PORT, dCode);
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
