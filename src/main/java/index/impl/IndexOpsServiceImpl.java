package index.impl;

import base.api.IndexOpsService;
import base.md.MdIndex;
import base.md.MdPos;
import index.common.CommonModule;
import index.common.CommonModuleImpl;
import index.common.DCodeMap;
import index.dao.IndexDao;
import index.dao.impl.RocksdbDaoImpl;
import index.tool.MdIndexCacheTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class IndexOpsServiceImpl extends UnicastRemoteObject implements IndexOpsService {
    private static Logger logger = LoggerFactory.getLogger("IndexOpsServiceImpl");

    private IndexDao indexDao = new RocksdbDaoImpl();
    private CommonModule commonModule = new CommonModuleImpl();
    private int coreSize = Runtime.getRuntime().availableProcessors();
    private final Executor exec = new ThreadPoolExecutor(coreSize, coreSize + 1, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
    private final Object object = new Object();

    //    private final Executor exec = Executors.newCachedThreadPool();
    public IndexOpsServiceImpl() throws RemoteException {
        super();
        initRootDir();
    }

    private void initRootDir() {
        long parentCode = -1;
        long fCode = 0;
        DCodeMap dCodeMap = new DCodeMap(0L, 0);
        String name = "/";
        if (!isDirExist(parentCode, name)) {
            String key = buildKey(parentCode, name);
            MdIndex rootIndex = genDirIndex(fCode, dCodeMap);
            boolean isInit = indexDao.insertMdIndex(key, rootIndex);
            if (isInit) {
                logger.info("init root dir...");
            } else {
                logger.info("init root dir failed...");
            }
        }
    }

    private MdIndex genDirIndex(long fCode, DCodeMap dCode) {
        Map<Long, Integer> dCodeMap = new LinkedHashMap<Long, Integer>();
        dCodeMap.put(dCode.getdCode(), dCode.getBsNode());
        return new MdIndex(fCode, dCodeMap);
    }

    private boolean isDirExist(long pCode, String dirName) {
        return indexDao.findMdIndex(buildKey(pCode, dirName)) != null;
    }

    @Override
    public MdPos createDirIndex(String parentPath, String dirName) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(parentPath);
        if (isDirExist(parentIndex.getfCode(), dirName)) {
//            logger.info("dir exist:" + parentPath + " " + dirName);
            return null;
        }
        long fCode = commonModule.genFCode();
        DCodeMap dCode = commonModule.genDCode();
        indexDao.insertMdIndex(buildKey(parentIndex.getfCode(), dirName),
                genDirIndex(fCode, dCode));
        return getMdAttrPos(parentIndex, parentPath);
    }


    private MdPos getMdAttrPos(MdIndex parentIndex, String path) {
        Map<Long, Integer> dCodeMap = parentIndex.getdCodeMap();
        DCodeMap dCode = null;
        for (long key : dCodeMap.keySet()) {
            dCode = new DCodeMap(key, dCodeMap.get(key));
        }
        boolean isFit = commonModule.isDCodeFit(dCode.getdCode());
        if (!isFit) {
            dCode = commonModule.genDCode();
            updateDCodeListWithNewCode(parentIndex, path, dCode);
        }
        return commonModule.buildMdPos(dCode);
    }

    //先要得到保存父目录的键，再更新节点信息
    private boolean updateDCodeListWithNewCode(MdIndex mdIndex, String path, DCodeMap dCode) {
        int pos = path.lastIndexOf("/");
        String front = path.substring(0, pos);
        String end = path.substring(pos + 1);
        if (front.equals("")) {
            front = "/";
        }
        String parentKey = buildKey(getMdIndexByPath(front).getfCode(), end);
        Map<Long, Integer> dCodeMap = mdIndex.getdCodeMap();
        dCodeMap.put(dCode.getdCode(), dCode.getBsNode());
        mdIndex.setdCodeMap(dCodeMap);
        return indexDao.insertMdIndex(parentKey, mdIndex);
    }

    @Override
    public MdPos getMdPosForCreateFile(String path) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(path);
        return getMdAttrPos(parentIndex, path);
    }

    @Override
    public List<MdPos> getMdPosList(String path) throws RemoteException {
        MdIndex mdIndex = getMdIndexByPath(path);
        if (mdIndex == null) {
            return null;
        }
        return commonModule.buildMdPosList(mdIndex.getdCodeMap());
    }

    @Override
    public boolean renameDirIndex(String parentPath, final String oldName, final String newName) throws RemoteException {
        final MdIndex parentIndex = getMdIndexByPath(parentPath);
        String oldKey = buildKey(parentIndex.getfCode(), oldName);
        MdIndex mdIndex = indexDao.findMdIndex(oldKey);
        String newKey = buildKey(parentIndex.getfCode(), newName);
        String separator = parentPath.equals("/") ? "" : "/";
        MdIndexCacheTool.removeMdIndex(parentPath + separator + oldName);
        indexDao.insertMdIndex(newKey, mdIndex);
        indexDao.removeMdIndex(oldKey);
        /*List<MdPos> mdPosList = commonModule.buildMdPosList(parentIndex.getdCodeMap());
        boolean renameResult;
        for (MdPos mdPos : mdPosList) {
            renameResult = indexDao.renameMd(mdPos, oldName, newName);
            if (renameResult) {
                break;
            }
        }*/
        synchronized (object) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    doRenameDirByBackendThread(parentIndex.getdCodeMap(), oldName, newName);
                }
            });
        }
        return true;
    }

    private synchronized void doRenameDirByBackendThread(Map<Long,Integer> map,String oldName, String newName){
        List<MdPos> mdPosList = commonModule.buildMdPosList(map);
        boolean renameResult;
        for (MdPos mdPos : mdPosList) {
            renameResult = indexDao.renameMd(mdPos, oldName, newName);
            if (renameResult) {
                break;
            }
        }
    }

    @Override
    public boolean deleteDir(String path) throws RemoteException {
        MdIndex mdIndex;
        if (path.equals("/")) {
            mdIndex = getMdIndexByPath(path);
        } else {
            mdIndex = getMdIndexByPathAndRemove(path);
        }
        MdIndexCacheTool.clearMdIndexCache();
        //ExecutorService execs = Executors.newCachedThreadPool();
        List<MdIndex> mdIndexes = new ArrayList<MdIndex>();
        mdIndexes.add(mdIndex);
        parallelGetSubDir(mdIndexes);
        /*execs.shutdown();
        try {
            execs.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return true;
    }

    private void parallelGetSubDir(List<MdIndex> mdIndexes) {
        for (final MdIndex index : mdIndexes) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    delDirHashBucket(index);
                }
            });
            parallelGetSubDir(indexDao.findSubDirMdIndexAndRemove(index.getfCode()));
        }
    }

    private void delDirRecursive(final Queue<MdIndex> queue) {
        while (!queue.isEmpty()) {
            final MdIndex index = queue.poll();
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    delDirHashBucket(index);
                    queue.addAll(indexDao.findSubDirMdIndexAndRemove(index.getfCode()));
                }
            });
            delDirRecursive(queue);
        }
    }

    private void delDirHashBucket(MdIndex mdIndex) {
        List<MdPos> mdPoses = commonModule.buildMdPosList(mdIndex.getdCodeMap());
        for (MdPos mdPos : mdPoses) {
            indexDao.deleteDirMd(mdPos);
        }
    }

    public String[] splitPath(String path) {
        if (path == null || path.equals("") || path.charAt(0) != '/') {
            logger.info("splitPath params err: " + path);
            throw new IllegalArgumentException("splitPath params err: " + path);
        }
        if (path.equals("/")) {
            return new String[]{"/"};
        }
        String[] nameArray = path.split("/");
        nameArray[0] = "/";
        return nameArray;
    }

    public MdIndex getMdIndexByPath(String path) {
        MdIndex mdIndex = MdIndexCacheTool.getMdIndexFromCache(path);
        if (mdIndex != null) {
            return mdIndex;
        }
        String[] nameArray = splitPath(path);
        long code = -1;
        for (String name : nameArray) {
            mdIndex = indexDao.findMdIndex(buildKey(code, name));
            if (mdIndex == null) {
                logger.error(String.format("path %s not exist.", path));
                return null;
            }
            code = mdIndex.getfCode();
        }
        MdIndexCacheTool.setMdIndexToCache(path, mdIndex);
        return mdIndex;
    }

    public MdIndex getMdIndexByPathAndRemove(String path) {
        MdIndex mdIndex = null;
        String[] nameArray = splitPath(path);
        long code = -1;
        long pCode = 0;
        String dirName = null;
        for (String name : nameArray) {
            mdIndex = indexDao.findMdIndex(buildKey(code, name));
            if (mdIndex == null) {
                logger.error(String.format("path %s not exist.", path));
                return null;
            }
            pCode = code;
            code = mdIndex.getfCode();
            dirName = name;
        }
        indexDao.removeMdIndex(buildKey(pCode, dirName));
        return mdIndex;
    }

    private String buildKey(long pCode, String fileName) {
        return pCode + ":" + fileName;
    }
}
