package md.server;

import base.api.IndexOpsService;
import base.md.MdIndex;
import index.common.DCodeMap;
import index.dao.IndexDao;
import index.dao.impl.RocksdbDaoImpl;
import index.impl.IndexOpsServiceImpl;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang on 16-2-26.
 */
public class TestFunc extends TestSuite {
    private static Logger logger = LoggerFactory.getLogger("IndexOpsServiceImpl");

    private IndexDao indexDao = new RocksdbDaoImpl();
    private IndexOpsService service;

    @Before
    public void setUp() throws RemoteException {
        service = new IndexOpsServiceImpl();
    }
   /* @Test
    public void testDelDir(){

        MdIndex mdIndex1 = genDirIndex(100L,-10L);
        MdIndex mdIndex2 = genDirIndex(101L,-11L);
        MdIndex mdIndex3 = genDirIndex(102L,-12L);
        MdIndex mdIndex4 = genDirIndex(103L,-13L);
        indexDao.insertMdIndex("0:a",mdIndex1);
        indexDao.insertMdIndex("100:b",mdIndex2);
        indexDao.insertMdIndex("100:c",mdIndex3);
        indexDao.insertMdIndex("101:b",mdIndex4);
        List<MdIndex> dirIndexes = indexDao.findSubDirMdIndexAndRemove(100L);
        for (MdIndex index : dirIndexes){
            logger.info(index.toString());
        }

       logger.info("begin del");

        dirIndexes = indexDao.findSubDirMdIndexAndRemove(100L);
        for (MdIndex index : dirIndexes){
            logger.info(index.toString());
        }
    }*/
    @Test
    public void testDelDirFromService() throws RemoteException {
        service.createDirIndex("/","e");
        service.createDirIndex("/e","b");
        service.createDirIndex("/e","c");
        service.createDirIndex("/e/b","d");
       // logger.info(service.getMdPosList("/e").toString());
        service.deleteDir("/e");
     //   logger.info(service.getMdPosList("/e").toString());

    }

    private MdIndex genDirIndex(long fCode, DCodeMap dCode) {
        Map<Long, Integer> dCodeMap = new LinkedHashMap<Long, Integer>();
        dCodeMap.put(dCode.getdCode(), dCode.getBsNode());
        return new MdIndex(fCode, dCodeMap);
    }
}
