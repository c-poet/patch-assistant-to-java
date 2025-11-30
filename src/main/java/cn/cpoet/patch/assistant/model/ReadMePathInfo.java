package cn.cpoet.patch.assistant.model;

import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

/**
 * Readme填写的路径信息
 *
 * @author CPoet
 */
public class ReadMePathInfo {

    /**
     * 类型
     */
    private ChangeTypeEnum type;

    /**
     * 文件名或者路径
     */
    private String path1;

    /**
     * 一级路径
     */
    private String path2;

    /**
     * 二级路径
     */
    private String path3;

    /**
     * 应用节点
     */
    private TreeNode appNode;

    /**
     * 关联的补丁节点
     */
    private TreeNode patchNode;

    /**
     * 路径起始下标
     */
    private int startIndex;

    /**
     * 路径结束下标
     */
    private int endIndex;

    public ChangeTypeEnum getType() {
        return type;
    }

    public void setType(ChangeTypeEnum type) {
        this.type = type;
    }

    public String getPath1() {
        return path1;
    }

    public void setPath1(String fileName) {
        this.path1 = fileName;
    }

    public String getPath2() {
        return path2;
    }

    public void setPath2(String path2) {
        this.path2 = path2;
    }

    public String getPath3() {
        return path3;
    }

    public void setPath3(String path3) {
        this.path3 = path3;
    }

    public TreeNode getAppNode() {
        return appNode;
    }

    public void setAppNode(TreeNode appNode) {
        this.appNode = appNode;
    }

    public TreeNode getPatchNode() {
        return patchNode;
    }

    public void setPatchNode(TreeNode patchNode) {
        this.patchNode = patchNode;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * 拼接 AppNodePath
     *
     * @return AppNodePath
     */
    public String getAppNodePath() {
        String appNodePath;
        if (!StringUtil.isEmpty(path3)) {
            appNodePath = FileNameUtil.joinPath(path2, path3);
            String fileName = FileNameUtil.getFileName(path1);
            if (!appNodePath.endsWith(fileName)) {
                appNodePath = FileNameUtil.joinPath(appNodePath, fileName);
            }
        } else if (!StringUtil.isEmpty(path2)) {
            appNodePath = path2;
            String fileName = FileNameUtil.getFileName(path1);
            if (!appNodePath.endsWith(fileName)) {
                appNodePath = FileNameUtil.joinPath(appNodePath, fileName);
            }
        } else {
            appNodePath = path1;
        }
        return appNodePath;
    }
}
