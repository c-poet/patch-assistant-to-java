package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeInfo;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.FileNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.AppPackSign;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.HashUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.view.home.HomeContext;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

/**
 * 应用包处理
 *
 * @author CPoet
 */
public class AppPackService extends BasePackService {

    public static final AppPackService INSTANCE = new AppPackService();

    /**
     * 获取树形节点
     *
     * @param file 文件
     * @return 树形信息
     */
    public AppTreeInfo getTreeNode(File file) {
        AppTreeInfo treeInfo = new AppTreeInfo();
        FileNode rootNode = new FileNode();
        rootNode.setName(file.getName());
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        rootNode.setType(TreeNodeType.ROOT);
        treeInfo.setRootNode(rootNode);
        byte[] bytes;
        try (InputStream in = new FileInputStream(file);) {
            bytes = in.readAllBytes();
        } catch (IOException ex) {
            throw new AppException("读取应用包内容失败", ex);
        }
        rootNode.setSize(bytes.length);
        AppPackSign appPackSign = new AppPackSign();
        appPackSign.setMd5(HashUtil.md5(bytes));
        rootNode.setMd5(appPackSign.getMd5());
        appPackSign.setSha1(HashUtil.sha1(bytes));
        treeInfo.setAppPackSign(appPackSign);
        getTreeNode(new ByteArrayInputStream(bytes), rootNode);
        TreeNode patchUpSignNode = removePatchUpSignNode(rootNode);
        treeInfo.setPatchUpSignNode(patchUpSignNode);
        return treeInfo;
    }

    private void getTreeNode(InputStream in, TreeNode rootNode) {
        try {
            buildChildrenWithCompress(rootNode, in, false);
        } catch (Exception ex) {
            throw new AppException("Failed to read application package", ex);
        }
    }

    private TreeNode removePatchUpSignNode(TreeNode rootNode) {
        if (rootNode == null || CollectionUtil.isEmpty(rootNode.getChildren())) {
            return null;
        }
        TreeNode metaInfoNode = null;
        for (TreeNode childNode : rootNode.getChildren()) {
            if (JarInfoConst.META_INFO.equals(childNode.getName())) {
                metaInfoNode = childNode;
                break;
            }
        }
        if (metaInfoNode == null || CollectionUtil.isEmpty(metaInfoNode.getChildren())) {
            return null;
        }
        Iterator<TreeNode> it = metaInfoNode.getChildren().iterator();
        while (it.hasNext()) {
            TreeNode childNode = it.next();
            if (AppConst.PATCH_UP_SIGN.equals(childNode.getName())) {
                it.remove();
                return childNode;
            }
        }
        return null;
    }

    /**
     * 生成并保存应用包
     *
     * @param context         上下文
     * @param progressContext 进度上下文
     * @param file            文件名
     * @param isDockerImage   是否Docker镜像
     */
    public void savePack(HomeContext context, ProgressContext progressContext, File file, boolean isDockerImage) {
        new AppPackWriteProcessor(context, progressContext, isDockerImage).exec(file);
    }

    /**
     * 创建补丁比较信息
     *
     * @param appTreeInfo 应用信息
     * @param appNode     应用节点
     * @param patchNode   补丁节点
     */
    public void createPatchDiffInfo(AppTreeInfo appTreeInfo, TreeNode appNode, TreeNode patchNode) {
        if (!Boolean.TRUE.equals(Configuration.getInstance().getPatch().getPatchFileDiff())) {
            return;
        }
        String appMd5 = appNode.getMd5OrInit();
        String patchMd5 = patchNode.getMd5OrInit();
        if (Objects.equals(appMd5, patchMd5)) {
            appTreeInfo.appendPatchDiffInfo(I18nUtil.tr("app.service.app-pack.diff-file-tip.hash", appNode.getPath(), patchNode.getPath()));
            return;
        }
        LocalDateTime appModifyTime = appNode.getModifyTime();
        LocalDateTime patchModifyTime = patchNode.getModifyTime();
        if (appModifyTime != null && patchModifyTime != null && !appModifyTime.isBefore(patchModifyTime)) {
            appTreeInfo.appendPatchDiffInfo(I18nUtil.tr("app.service.app-pack.diff-file-tip.time", appNode.getPath(), DateUtil.formatDateTime(appModifyTime), patchNode.getPath(), DateUtil.formatDateTime(patchModifyTime)));
        }
    }
}
