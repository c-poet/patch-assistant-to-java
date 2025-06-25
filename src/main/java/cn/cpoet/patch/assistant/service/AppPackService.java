package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.JarInfoConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.view.HomeContext;
import cn.cpoet.patch.assistant.view.ProgressContext;
import cn.cpoet.patch.assistant.view.tree.AppTreeInfo;
import cn.cpoet.patch.assistant.view.tree.FileNode;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipInputStream;

/**
 * 应用包处理
 *
 * @author CPoet
 */
public class AppPackService extends BasePackService {

    public static AppPackService getInstance() {
        return AppContext.getInstance().getService(AppPackService.class);
    }

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
        rootNode.setText(file.getName());
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        treeInfo.setRootNode(rootNode);
        try (InputStream in = new FileInputStream(file);
             ZipInputStream zin = new ZipInputStream(in)) {
            doReadZipEntry(rootNode, zin, false);
        } catch (IOException ex) {
            throw new AppException("读取应用包失败", ex);
        }
        TreeNode patchUpSignNode = removePatchUpSignNode(rootNode);
        treeInfo.setPatchUpSignNode(patchUpSignNode);
        return treeInfo;
    }

    private TreeNode removePatchUpSignNode(TreeNode rootNode) {
        if (rootNode == null || CollectionUtil.isEmpty(rootNode.getChildren())) {
            return null;
        }
        TreeNode metaInfoNode = null;
        for (TreeNode childNode : rootNode.getChildren()) {
            if (JarInfoConst.META_INFO_DIR.equals(childNode.getName())) {
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
}
