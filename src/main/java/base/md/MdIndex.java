package base.md;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Mr-yang on 16-1-9.
 * 元数据索引信息
 */
public class MdIndex implements Serializable {
    private long fCode;
    private Map<Long,Integer> dCodeMap;

    public MdIndex() {
    }

    @Override
    public String toString() {
        return "MdIndex{" +
                "fCode=" + fCode +
                ", dCodeMap=" + dCodeMap +
                '}';
    }

    public MdIndex(long fCode, Map<Long, Integer> dCodeMap) {
        this.fCode = fCode;
        this.dCodeMap = dCodeMap;
    }

    public long getfCode() {
        return fCode;
    }

    public void setfCode(long fCode) {
        this.fCode = fCode;
    }

    public Map<Long, Integer> getdCodeMap() {
        return dCodeMap;
    }

    public void setdCodeMap(Map<Long, Integer> dCodeMap) {
        this.dCodeMap = dCodeMap;
    }
}
