package index.dao.impl;

import base.md.MdAttr;
import base.md.MdIndex;
import base.md.MdPos;
import com.alibaba.fastjson.JSON;
import index.dao.IndexDao;
import index.tool.ConnTool;
import org.nutz.ssdb4j.spi.SSDB;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr-yang on 16-2-21.
 */
public class RocksdbDaoImpl implements IndexDao {

    private static Logger logger = LoggerFactory.getLogger(RocksdbDaoImpl.class);

    public static final String DB_PATH = "/data/index";
    public static Options options = new Options().setCreateIfMissing(true);
    public static RocksDB db = null;
    public static final String RDB_DECODE = "UTF8";

    static {
        RocksDB.loadLibrary();
        try {
            db = RocksDB.open(options, DB_PATH);
        } catch (RocksDBException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public boolean insertMdIndex(String key, MdIndex mdIndex) {
        try {
            db.put(key.getBytes(RDB_DECODE), JSON.toJSONString(mdIndex).getBytes());
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }

    @Override
    public MdIndex findMdIndex(String key) {
        try {
            byte[] indexBytes = db.get(key.getBytes(RDB_DECODE));
            if (indexBytes != null) {
                return JSON.parseObject(new String(indexBytes, RDB_DECODE), MdIndex.class);
            }
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return null;
    }

    @Override
    public boolean removeMdIndex(String key) {
        return removeKV(key.getBytes());
    }

    @Override
    public List<MdIndex> findSubDirMdIndexAndRemove(long fCode) {
        List<MdIndex> mdIndexes = new ArrayList<MdIndex>();
        RocksIterator it = db.newIterator(new ReadOptions());
        String startStr = fCode + "";
        String endStr = (fCode + 1) + "";
        MdIndex mdIndex;
        for (it.seek(startStr.getBytes());
             it.isValid() && (new String(it.key()).compareTo(endStr) < 0); it.next()) {
            mdIndex = JSON.parseObject(new String(it.value()), MdIndex.class);
            mdIndexes.add(mdIndex);
            removeKV(it.key());
        }
        return mdIndexes;
    }

    private boolean removeKV(byte[] keys){
        try {
            db.remove(keys);
            return true;
        } catch (Exception e) {
            logger.error(String.format("[ERROR] caught the unexpected exception -- %s\n", e));
        }
        return false;
    }
    @Override
    public boolean deleteDirMd(MdPos mdPos) {
        SSDB ssdb = ConnTool.getSSDB(mdPos);
        return ssdb.hclear(mdPos.getdCode()).ok();
    }

    @Override
    public boolean renameMd(MdPos mdPos, String oldName, String newName) {
        SSDB ssdb = ConnTool.getSSDB(mdPos);
        long dCode = mdPos.getdCode();
        MdAttr mdAttr = JSON.parseObject(ssdb.hget(dCode, oldName).asString(), MdAttr.class);
        mdAttr.setName(newName);
        ssdb.hdel(dCode, oldName);
        return ssdb.hset(dCode, newName, JSON.toJSONString(mdAttr)).ok();
    }
}
