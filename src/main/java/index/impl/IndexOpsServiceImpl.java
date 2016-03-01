package index.impl;

import base.api.IndexOpsService;
import base.md.MdIndex;
import base.md.MdPos;
import index.common.CommonModule;
import index.common.CommonModuleImpl;
import index.dao.IndexDao;
import index.dao.impl.RocksdbDaoImpl;
import index.tool.MdIndexCacheTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mr-yang on 16-2-18.
 */
public class IndexOpsServiceImpl extends UnicastRemoteObject implements IndexOpsService {
    private static Logger logger = LoggerFactory.getLogger("IndexOpsServiceImpl");

    private IndexDao indexDao = new RocksdbDaoImpl();
    private CommonModule commonModule = new CommonModuleImpl();
    private final Executor exec = new ThreadPoolExecutor(6, 6, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(10000));

    public IndexOpsServiceImpl() throws RemoteException {
        super();
        initRootDir();
    }

    private void initRootDir() {
        long parentCode = -1;
        long fCode = -1;
        long dCode = 0;
        String name = "/";
        if (!isDirExist(parentCode, name)) {
            String key = buildKey(parentCode, name);
            MdIndex rootIndex = genDirIndex(fCode, dCode);
            boolean isInit = indexDao.insertMdIndex(key, rootIndex);
            if (isInit) {
                logger.info("init root dir...");
            } else {
                logger.info("init root dir failed...");
            }
        }
    }

    private MdIndex genDirIndex(long fCode, long dCode) {
        List<Long> dCodes = new ArrayList<Long>();
        dCodes.add(dCode);
        return new MdIndex(fCode, dCodes);
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
        long dCode = commonModule.genDCode();
        indexDao.insertMdIndex(buildKey(parentIndex.getfCode(), dirName),
                genDirIndex(fCode, dCode));
        return getMdAttrPos(parentIndex, parentPath);
    }


    private MdPos getMdAttrPos(MdIndex parentIndex, String path) {
        List<Long> dCodeList = parentIndex.getdCodeList();
        long dCode = dCodeList.get(dCodeList.size() - 1);
        boolean isFit = commonModule.isDCodeFit(dCode);
        if (!isFit) {
            dCode = commonModule.genDCode();
            updateDCodeListWithNewCode(parentIndex, path, dCode);
        }
        return commonModule.buildMdPos(dCode);
    }

    //先要得到保存父目录的键，再更新节点信息
    private boolean updateDCodeListWithNewCode(MdIndex mdIndex, String path, long newDCode) {
        int pos = path.lastIndexOf("/");
        String front = path.substring(0, pos);
        String end = path.substring(pos + 1);
        if (front.equals("")) {
            front = "/";
        }
        String parentKey = buildKey(getMdIndexByPath(front).getfCode(), end);
        List<Long> dCodeList = mdIndex.getdCodeList();
        dCodeList.add(newDCode);
        mdIndex.setdCodeList(dCodeList);
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
        return commonModule.buildMdPosList(mdIndex.getdCodeList());
    }

    @Override
    public List<MdPos> renameDirIndex(String parentPath, String oldName, String newName) throws RemoteException {
        MdIndex parentIndex = getMdIndexByPath(parentPath);
        String oldKey = buildKey(parentIndex.getfCode(), oldName);
        MdIndex mdIndex = indexDao.findMdIndex(oldKey);
        String newKey = buildKey(parentIndex.getfCode(), newName);
        String separator = parentPath.equals("/") ? "" : "/";
        MdIndexCacheTool.removeMdIndex(parentPath + separator + oldName);
        indexDao.insertMdIndex(newKey, mdIndex);
        indexDao.removeMdIndex(oldKey);
        return commonModule.buildMdPosList(parentIndex.getdCodeList());
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
        Queue<MdIndex> queue = new LinkedList<MdIndex>();
        queue.offer(mdIndex);
        delDirRecursive(queue);
        /*MdIndex beDelIndex;
        while (!queue.isEmpty()) {
            beDelIndex = queue.poll();
            delDirHashBucket(beDelIndex);
            List<MdIndex> mdIndexes = indexDao.findSubDirMdIndexAndRemove(beDelIndex.getfCode());
            for (MdIndex temp : mdIndexes) {
                queue.offer(temp);
            }
        }*/
        return true;
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
        List<MdPos> mdPoses = commonModule.buildMdPosList(mdIndex.getdCodeList());
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
        long pCode = -1;
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
