package index.common;

import base.md.MdPos;

import java.util.List;
import java.util.Map;

/**
 * Created by Mr-yang on 16-1-11.
 */
public interface CommonModule {
    /**
     * 生成文件编码
     */
    public long genFCode();

    /**
     * 生成分布编码
     */
    public DCodeMap genDCode();


    /**
     * 检验桶能否继续保持新的元数据
     */
    public boolean isDCodeFit(long bsNode);

    /**
     * 获取分布编码对应的元数据节点信息
     */
    public MdPos buildMdPos(long dCode, int bsNode);

    public MdPos buildMdPos(DCodeMap dCodeMap);
    /**
     * 生成分布编码对应的元数据节点信息
     */
    public MdPos createMdPos();

    public List<MdPos> buildMdPosList(Map<Long,Integer> dCodeMap);
}
