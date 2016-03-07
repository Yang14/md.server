package index.common;

/**
 * Created by Mr-yang on 16-3-7.
 */
public class DCodeMap {
    private long dCode;
    private int bsNode;

    public DCodeMap(long dCode, int bsNode) {
        this.dCode = dCode;
        this.bsNode = bsNode;
    }

    @Override
    public String toString() {
        return "DCodeMap{" +
                "dCode=" + dCode +
                ", bsNode=" + bsNode +
                '}';
    }

    public long getdCode() {
        return dCode;
    }

    public void setdCode(long dCode) {
        this.dCode = dCode;
    }

    public int getBsNode() {
        return bsNode;
    }

    public void setBsNode(int bsNode) {
        this.bsNode = bsNode;
    }
}
