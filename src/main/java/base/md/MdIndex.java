package base.md;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Mr-yang on 16-1-9.
 * 元数据索引信息
 */
public class MdIndex implements Serializable {
    private long fCode;
    private List<Long> dCodeList;

    @Override
    public String toString() {
        return "MdIndex{" +
                "fCode=" + fCode +
                ", dCodeList=" + dCodeList +
                '}';
    }

    public MdIndex(long fCode, List<Long> dCodeList) {
        this.fCode = fCode;
        this.dCodeList = dCodeList;
    }

    public MdIndex() {

    }

    public long getfCode() {

        return fCode;
    }

    public void setfCode(long fCode) {
        this.fCode = fCode;
    }

    public List<Long> getdCodeList() {
        return dCodeList;
    }

    public void setdCodeList(List<Long> dCodeList) {
        this.dCodeList = dCodeList;
    }
}
