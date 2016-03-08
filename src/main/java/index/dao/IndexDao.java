package index.dao;

import base.md.MdIndex;
import base.md.MdPos;

import java.util.List;

/**
 * Created by Mr-yang on 16-2-18.
 */
public interface IndexDao {

    public boolean insertMdIndex(String key, MdIndex mdIndex);

    public MdIndex findMdIndex(String key);

    public boolean removeMdIndex(String key);

    public List<MdIndex> findSubDirMdIndexAndRemove(long fCode);

    public boolean deleteDirMd(MdPos mdPos);

    public boolean renameMd(MdPos mdPos, String oldName, String newName);
}
