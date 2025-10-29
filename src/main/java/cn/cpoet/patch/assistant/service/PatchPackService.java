package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.tree.*;
import cn.cpoet.patch.assistant.control.tree.node.*;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.core.PatchConf;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.HashInfo;
import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.model.ReadMePathInfo;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 补丁包处理
 *
 * @author CPoet
 */
public class PatchPackService extends BasePackService {

    public static final PatchPackService INSTANCE = new PatchPackService();

    /**
     * 更新根节点信息
     *
     * @param rootNode 根节点信息
     */
    public void refreshReadmeNode(PatchTreeInfo patchTreeInfo, TreeNode rootNode) {
        PatchRootInfo rootInfo = patchTreeInfo.getRootInfoByNode(rootNode);
        if (rootInfo == null) {
            return;
        }
        String readmeFileName = Configuration.getInstance().getPatch().getReadmeFile();
        if (StringUtil.isBlank(readmeFileName)) {
            readmeFileName = AppConst.README_FILE;
        }
        TreeNode readmeNode = null;
        for (TreeNode child : rootNode.getChildren()) {
            if (TreeNodeType.README.equals(child.getType())) {
                readmeNode = child;
                break;
            }
            if (readmeFileName.equalsIgnoreCase(child.getName())) {
                readmeNode = child;
            }
        }
        if (readmeNode != null) {
            readmeNode.setType(TreeNodeType.README);
            rootInfo.setReadmeNode(readmeNode);
            byte[] bytes = TreeNodeUtil.readNodeBytes(readmeNode);
            if (bytes.length > 0) {
                rootInfo.getPatchSign().setReadme(new String(bytes, CharsetUtil.getCharset(bytes)));
            }
            patchTreeInfo.updateReadmeText();
        }
    }

    /**
     * 清空节节点绑定的信息
     *
     * @param totalInfo     统计信息
     * @param rootNode      根节点
     * @param excludeReadme 是否排除Readme节点
     */
    public void cleanMappedNode(TotalInfo totalInfo, TreeNode rootNode, boolean excludeReadme) {
        cleanMappedNode(totalInfo, rootNode, excludeReadme, null);
    }

    /**
     * 清空节节点绑定的信息
     *
     * @param totalInfo     统计信息
     * @param rootNode      根节点
     * @param excludeReadme 是否排除Readme节点
     */
    public void cleanMappedNode(TotalInfo totalInfo, TreeNode rootNode, boolean excludeReadme, Predicate<TreeNode> filter) {
        if (excludeReadme) {
            TreeNodeUtil.deepCleanMappedNode(totalInfo, rootNode, node -> {
                if (Objects.equals(rootNode, node.getParent()) && TreeNodeType.README.equals(node.getType())) {
                    return true;
                }
                return filter != null && filter.test(node);
            });
        } else {
            TreeNodeUtil.deepCleanMappedNode(totalInfo, rootNode, filter);
        }
    }

    /**
     * 刷新补丁文件映射信息
     *
     * @param totalInfo     统计信息
     * @param appTreeInfo   应用树形信息
     * @param patchTreeInfo 补丁树形信息
     * @param rootNode      根节点
     */
    public void refreshMappedNode(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo, TreeNode rootNode) {
        cleanMappedNode(totalInfo, rootNode, true, node -> {
            if (node == rootNode) {
                return true;
            }
            if (TreeNodeType.CUSTOM_ROOT.equals(node.getType())) {
                patchTreeInfo.removeCustomRootInfo(node);
            }
            return false;
        });
        if (appTreeInfo == null || patchTreeInfo == null) {
            return;
        }
        refreshMappedNodeWithReadme(totalInfo, appTreeInfo, patchTreeInfo, rootNode);
        refreshMappedNodeWithPathOrName(totalInfo, appTreeInfo, patchTreeInfo);
    }

    private void refreshMappedNodeWithReadme(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo, TreeNode treeNode) {
        List<ReadMePathInfo> pathInfos = ReadMeFileService.INSTANCE.getPathInfos(patchTreeInfo, treeNode);
        if (CollectionUtil.isEmpty(pathInfos)) {
            return;
        }
        String pathPrefix = null;
        if ((TreeNodeType.CUSTOM_ROOT.equals(treeNode.getType()) && treeNode.isDir()) || (treeNode instanceof FileNode && treeNode.isDir())) {
            pathPrefix = treeNode.getPath();
        }
        for (ReadMePathInfo pathInfo : pathInfos) {
            String filePath = pathPrefix == null ? pathInfo.getPath1() : FileNameUtil.joinPath(pathPrefix, pathInfo.getPath1());
            TreeNode patchNode = TreeNodeUtil.findNodeByPath(treeNode, filePath);
            if (patchNode == null && StringUtil.isEmpty(pathInfo.getPath2())) {
                String fileName = FileNameUtil.getFileName(pathInfo.getPath1());
                if (!Objects.equals(fileName, pathInfo.getPath1())) {
                    filePath = pathPrefix == null ? fileName : FileNameUtil.joinPath(pathPrefix, fileName);
                    patchNode = TreeNodeUtil.findNodeByPath(treeNode, filePath);
                }
            }
            if (patchNode == null && !ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                continue;
            }
            matchMappedNodeWithReadme(totalInfo, pathInfo, appTreeInfo, patchNode);
        }
    }

    private void matchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, AppTreeInfo appTreeInfo, TreeNode patchNode) {
        String appNodePath;
        if (!StringUtil.isEmpty(pathInfo.getPath3())) {
            appNodePath = FileNameUtil.joinPath(pathInfo.getPath2(), pathInfo.getPath3());
            String fileName = FileNameUtil.getFileName(pathInfo.getPath1());
            if (!appNodePath.endsWith(fileName)) {
                appNodePath = FileNameUtil.joinPath(appNodePath, fileName);
            }
        } else if (!StringUtil.isEmpty(pathInfo.getPath2())) {
            appNodePath = pathInfo.getPath2();
            String fileName = FileNameUtil.getFileName(pathInfo.getPath1());
            if (!appNodePath.endsWith(fileName)) {
                appNodePath = FileNameUtil.joinPath(appNodePath, fileName);
            }
        } else {
            appNodePath = pathInfo.getPath1();
        }
        String[] paths = appNodePath.split(FileNameUtil.SEPARATOR);
        doMatchMappedNodeChildrenWithReadme(totalInfo, pathInfo, paths, 0, appTreeInfo.getRootNode(), patchNode);
    }

    private boolean doMatchMappedNodeChildrenWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, TreeNode appNode, TreeNode patchNode) {
        if (index >= paths.length) {
            return false;
        }
        List<TreeNode> children = appNode.getChildren();
        if (CollectionUtil.isNotEmpty(children)) {
            for (TreeNode child : children) {
                if (doMatchMappedNodeWithReadme(totalInfo, pathInfo, paths, index, child, patchNode)) {
                    return true;
                }
            }
        }
        if (!ReadMePathInfo.TypeEnum.ADD.equals(pathInfo.getType()) && !ReadMePathInfo.TypeEnum.NONE.equals(pathInfo.getType())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        TreeNode parent = appNode;
        TreeNode newAppNode = null;
        while (index < paths.length) {
            if (index + 1 == paths.length) {
                newAppNode = new MappedNode(patchNode);
            } else {
                VirtualNode virtualNode = new VirtualNode();
                virtualNode.setName(paths[index]);
                virtualNode.setDir(true);
                virtualNode.setModifyTime(now);
                newAppNode = virtualNode;
            }
            newAppNode.setParent(parent);
            newAppNode.setPath(TreeNodeUtil.getNodePathByParent(parent, newAppNode));
            parent.getAndInitChildren().add(newAppNode);
            parent = newAppNode;
            ++index;
        }
        TreeNodeUtil.mappedNode(totalInfo, newAppNode, patchNode, TreeNodeType.ADD);
        addMappedNodeChildren(totalInfo, newAppNode, patchNode);
        return true;
    }

    private void addMappedNodeChildren(TotalInfo totalInfo, TreeNode appNode, TreeNode patchNode) {
        if (CollectionUtil.isEmpty(patchNode.getChildren())) {
            return;
        }
        Map<String, TreeNode> childAppNodeMap = Collections.emptyMap();
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            childAppNodeMap = appNode.getChildren().stream().collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        }
        for (TreeNode childPatchNode : patchNode.getChildren()) {
            TreeNode childAppNode = childAppNodeMap.get(childPatchNode.getName());
            if (childAppNode == null) {
                if (!childPatchNode.isDir()) {
                    childAppNode = new MappedNode(childPatchNode);
                } else {
                    VirtualNode virtualNode = new VirtualNode();
                    virtualNode.setName(childPatchNode.getName());
                    virtualNode.setDir(true);
                    virtualNode.setModifyTime(childPatchNode.getModifyTime());
                    childAppNode = virtualNode;
                }
                childAppNode.setParent(appNode);
                childAppNode.setPath(TreeNodeUtil.getNodePathByParent(appNode, childPatchNode));
                appNode.getAndInitChildren().add(childAppNode);
            }
            TreeNodeUtil.mappedNode(totalInfo, childAppNode, childPatchNode, TreeNodeType.ADD);
            addMappedNodeChildren(totalInfo, childAppNode, childPatchNode);
        }
    }

    private boolean doMatchMappedNodeWithReadme(TotalInfo totalInfo, ReadMePathInfo pathInfo, String[] paths, int index, TreeNode appNode, TreeNode patchNode) {
        if (!matchPatchName(appNode, paths[index])) {
            return false;
        }
        if (index == paths.length - 1) {
            if (ReadMePathInfo.TypeEnum.DEL.equals(pathInfo.getType())) {
                TreeNodeUtil.countAndSetNodeType(totalInfo, appNode, TreeNodeType.DEL);
            } else if (TreeNodeType.DEL.equals(appNode.getType())) {
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.ADD);
                if (ReadMePathInfo.TypeEnum.ADD.equals(pathInfo.getType())) {
                    addMappedNodeChildren(totalInfo, appNode, patchNode);
                }
            } else if (ReadMePathInfo.TypeEnum.NONE.equals(pathInfo.getType())
                    || ReadMePathInfo.TypeEnum.MOD.equals(pathInfo.getType())) {
                TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
                mappedInnerClassNode(totalInfo, appNode, patchNode);
            }
            return true;
        }
        if (CollectionUtil.isNotEmpty(appNode.getChildren())) {
            return doMatchMappedNodeChildrenWithReadme(totalInfo, pathInfo, paths, ++index, appNode, patchNode);
        }
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR) && buildChildrenWithCompress(appNode, false)) {
            return doMatchMappedNodeChildrenWithReadme(totalInfo, pathInfo, paths, ++index, appNode, patchNode);
        }
        return false;
    }

    private void refreshMappedNodeWithPathOrName(TotalInfo totalInfo, AppTreeInfo appTreeInfo, PatchTreeInfo patchTreeInfo) {
        PatchConf patch = Configuration.getInstance().getPatch();
        boolean isWithPath = Boolean.TRUE.equals(patch.getPathMatch());
        boolean isWithName = Boolean.TRUE.equals(patch.getFileNameMatch());
        if (isWithPath || isWithName) {
            PatchMatchProcessor processor = new PatchMatchProcessor(totalInfo, appTreeInfo, isWithPath, isWithName);
            processor.setAppRootNode(appTreeInfo.getRootNode());
            Map<TreeNode, PatchRootInfo> customRootInfoMap = patchTreeInfo.getCustomRootInfoMap();
            if (CollectionUtil.isNotEmpty(customRootInfoMap)) {
                for (Map.Entry<TreeNode, PatchRootInfo> entry : customRootInfoMap.entrySet()) {
                    processor.setPatchRootNode(entry.getKey());
                    processor.exec();
                }
            } else {
                processor.setPatchRootNode(patchTreeInfo.getRootNode());
                processor.exec();
            }
        }
    }

    /**
     * 绑定内部类
     *
     * @param totalInfo 统计信息
     * @param appNode   应用节点
     * @param patchNode 补丁节点
     */
    public void mappedInnerClassNode(TotalInfo totalInfo, TreeNode appNode, TreeNode patchNode) {
        if (!appNode.getName().endsWith(FileExtConst.DOT_CLASS) || !patchNode.getName().endsWith(FileExtConst.DOT_CLASS)) {
            return;
        }
        if (CollectionUtil.isEmpty(appNode.getChildren()) && CollectionUtil.isEmpty(patchNode.getChildren())) {
            return;
        }
        Map<String, TreeNode> innerNodeMap = appNode
                .getChildren()
                .stream()
                .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
        for (TreeNode patchInnerNode : patchNode.getChildren()) {
            TreeNode appInnerNode = innerNodeMap.get(patchInnerNode.getName());
            if (appInnerNode == null) {
                appInnerNode = new MappedNode(patchInnerNode);
                appInnerNode.setParent(appNode);
                appInnerNode.setPath(FileNameUtil.joinPath(FileNameUtil.getDirPath(appNode.getPath()), patchInnerNode.getName()));
                appNode.getAndInitChildren().add(appInnerNode);
                TreeNodeUtil.mappedNode(totalInfo, appInnerNode, patchInnerNode, TreeNodeType.ADD);
                continue;
            }
            TreeNodeUtil.mappedNode(totalInfo, appInnerNode, patchInnerNode, TreeNodeType.MOD);
            mappedInnerClassNode(totalInfo, appInnerNode, patchInnerNode);
            innerNodeMap.remove(patchInnerNode.getName());
        }
        if (CollectionUtil.isNotEmpty(innerNodeMap)) {
            innerNodeMap.forEach((k, v) -> TreeNodeUtil.countAndSetNodeType(totalInfo, v, TreeNodeType.DEL));
        }
    }

    /**
     * 匹配名称
     * <p>Jar包采用前缀匹配的方式</p>
     *
     * @param appNode   应用节点
     * @param patchNode 补丁节点
     * @return 是否匹配
     */
    public boolean matchPatchName(TreeNode appNode, TreeNode patchNode) {
        return matchPatchName(appNode, patchNode.getName());
    }


    /**
     * 匹配名称
     * <p>Jar包采用前缀匹配的方式</p>
     *
     * @param appNode   应用节点
     * @param patchName 补丁节点名称
     * @return 是否匹配
     */
    public boolean matchPatchName(TreeNode appNode, String patchName) {
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR)) {
            return appNode.getName().startsWith(patchName);
        }
        return appNode.getName().equals(patchName);
    }

    /**
     * 解析树形节点
     *
     * @param file 文件
     * @return 树形信息
     */
    public PatchTreeInfo getTreeNode(File file) {
        PatchTreeInfo treeInfo = new PatchTreeInfo();
        FileNode rootNode = new FileNode();
        rootNode.setName(file.getName());
        rootNode.setPath(file.getPath());
        rootNode.setFile(file);
        rootNode.setPatch(true);
        rootNode.setType(TreeNodeType.ROOT);
        PatchSign patchSign = new PatchSign();
        patchSign.setName(rootNode.getName());
        if (file.isDirectory()) {
            doGetTreeNodeWithDir(file, rootNode);
        } else {
            doGetTreeNodeWithCompress(patchSign, file, rootNode);
        }
        treeInfo.setRootNode(rootNode);
        PatchRootInfo patchRootInfo = createPatchRootInfo(rootNode);
        treeInfo.setRootInfo(patchRootInfo);
        return treeInfo;
    }

    private void doGetTreeNodeWithDir(File file, TreeNode parentNode) {
        File[] files = file.listFiles();
        if (files != null) {
            List<TreeNode> innerClasses = null;
            for (File childFile : files) {
                FileNode fileNode = new FileNode();
                fileNode.setName(childFile.getName());
                fileNode.setPath(childFile.getPath());
                fileNode.setFile(childFile);
                fileNode.setParent(parentNode);
                fileNode.setPatch(true);
                if (childFile.isDirectory()) {
                    doGetTreeNodeWithDir(childFile, fileNode);
                    parentNode.getAndInitChildren().add(fileNode);
                } else if (childFile.getName().endsWith(FileExtConst.DOT_CLASS) && childFile.getName().indexOf('$') != -1) {
                    if (innerClasses == null) {
                        innerClasses = new ArrayList<>();
                    }
                    innerClasses.add(fileNode);
                } else {
                    parentNode.getAndInitChildren().add(fileNode);
                }
            }
            if (CollectionUtil.isNotEmpty(innerClasses)) {
                handleInnerClass(parentNode, innerClasses);
            }
        }
    }

    private void doGetTreeNodeWithCompress(PatchSign patchSign, File file, TreeNode rootNode) {
        HashInfo hashInfo = FileUtil.getHashInfo(file);
        rootNode.setSize(hashInfo.getLength());
        patchSign.setMd5(hashInfo.getMd5());
        rootNode.setMd5(hashInfo.getMd5());
        patchSign.setSha1(hashInfo.getSha1());
        try (InputStream in = new FileInputStream(file)) {
            doGetTreeNodeWithCompress(in, rootNode);
        } catch (IOException ex) {
            throw new AppException("Failed to read the contents of the patch package", ex);
        }
    }

    private void doGetTreeNodeWithCompress(InputStream in, TreeNode rootNode) {
        try {
            buildChildrenWithCompress(rootNode, in, true);
        } catch (IOException ex) {
            throw new AppException("Failed to read patch compressed package", ex);
        }
    }

    /**
     * 新增补丁信息
     *
     * @param treeNode 节点
     * @return 包装后的{@link PatchSignTreeNode}节点
     */
    public PatchRootInfo createPatchRootInfo(TreeNode treeNode) {
        PatchSign patchSign = new PatchSign();
        patchSign.setName(treeNode.getName());
        if (!treeNode.isDir()) {
            MessageDigest sha1Digest = HashUtil.createSha1Digest();
            treeNode.consumeBytes(((len, buf) -> sha1Digest.update(buf, 0, len)));
            patchSign.setMd5(treeNode.getMd5());
            patchSign.setSha1(HashUtil.toHexStr(sha1Digest));
        }
        PatchRootInfo patchRootInfo = new PatchRootInfo();
        patchRootInfo.setPatchSign(patchSign);
        return patchRootInfo;
    }

    private FileNode createReadmeNode(ProgressContext progressContext, TreeNode rootNode) {
        progressContext.step("Create a readme file to : " + rootNode.getName());
        FileNode readmeNode = rootNode instanceof CompressNode || TreeNodeUtil.isCompressNode(rootNode) ? new CompressNode() : new FileNode();
        String readmeFileName = Configuration.getInstance().getPatch().getReadmeFile();
        if (StringUtil.isBlank(readmeFileName)) {
            readmeFileName = AppConst.README_FILE;
        }
        readmeNode.setName(readmeFileName);
        readmeNode.setPath(TreeNodeUtil.isCompressNode(rootNode) ? readmeFileName : FileNameUtil.joinPath(rootNode.getPath(), readmeFileName));
        readmeNode.setPatch(true);
        readmeNode.setType(TreeNodeType.README);
        readmeNode.setParent(rootNode);
        rootNode.getAndInitChildren().add(readmeNode);
        return readmeNode;
    }

    /**
     * 保存补丁包
     *
     * @param progressContext 进行条上下文
     * @param patchTree       补丁树
     * @param rootNode        根节点
     * @param text            文本信息
     */
    public void updatePatchReadme(ProgressContext progressContext, PatchTreeView patchTree, TreeNode rootNode, String text) {
        UIUtil.runNotUI(() -> {
            PatchTreeInfo treeInfo = patchTree.getTreeInfo();
            PatchRootInfo patchRootInfo = treeInfo.getRootInfoByNode(rootNode);
            FileNode readmeNode = (FileNode) patchRootInfo.getReadmeNode();
            FileNode oldReadmeNode = readmeNode == null ? null : (FileNode) readmeNode.clone();
            if (readmeNode == null) {
                readmeNode = createReadmeNode(progressContext, rootNode);
            }
            try {
                progressContext.step("Update readme text to file");
                byte[] bytes = text.getBytes();
                if (readmeNode instanceof CompressNode) {
                    File file = new File(AppContext.getInstance().getTempDir(), FileNameUtil.uniqueFileName(readmeNode.getName()));
                    FileUtil.writeFile(file, bytes);
                    readmeNode.setFile(file);
                    CompressNode cNode = (CompressNode) readmeNode;
                    cNode.setModifyTime(LocalDateTime.now());
                    cNode.setAccessTime(cNode.getModifyTime());
                    cNode.setMd5(HashUtil.md5(bytes));
                    cNode.setSize(bytes.length);
                    updatePatchReadme(progressContext, (FileNode) rootNode);
                } else {
                    readmeNode.setMd5(HashUtil.md5(bytes));
                    readmeNode.setSize(bytes.length);
                    if (readmeNode.getFile() == null) {
                        readmeNode.setFile(new File(readmeNode.getPath()));
                    }
                    FileUtil.writeFile(readmeNode.getFile(), bytes);
                }
                progressContext.end(true);
                patchRootInfo.setReadmeNode(readmeNode);
                patchRootInfo.getPatchSign().setReadme(text);
                treeInfo.updateReadmeText();
                UIUtil.runUI(() -> {
                    if (oldReadmeNode == null) {
                        FileTreeItem fileTreeItem = new FileTreeItem();
                        TreeNodeUtil.bindTreeNodeAndItem(patchRootInfo.getReadmeNode(), fileTreeItem);
                        rootNode.getTreeItem().getChildren().add(fileTreeItem);
                    }
                    patchTree.refresh();
                });
            } catch (Exception e) {
                if (oldReadmeNode == null) {
                    rootNode.getChildren().remove(readmeNode);
                } else {
                    readmeNode.setFile(oldReadmeNode.getFile());
                    if (readmeNode instanceof CompressNode) {
                        CompressNode cNode = (CompressNode) readmeNode;
                        cNode.setMd5(oldReadmeNode.getMd5());
                        cNode.setSize(oldReadmeNode.getSize());
                        cNode.setModifyTime(oldReadmeNode.getModifyTime());
                        cNode.setAccessTime(((CompressNode) oldReadmeNode).getAccessTime());
                    }
                }
                progressContext.step(ExceptionUtil.asString(e));
                progressContext.end(false);
            }
        });
    }

    private void updatePatchReadme(ProgressContext progressContext, FileNode rootNode) throws IOException {
        progressContext.step("Write in the patch package");
        LinkedList<FileNode> rootNodes = new LinkedList<>();
        while (rootNode != null) {
            if (TreeNodeUtil.isCompressNode(rootNode)) {
                rootNodes.add(rootNode);
            }
            if (!(rootNode instanceof CompressNode)) {
                break;
            }
            rootNode = (FileNode) rootNode.getParent();
        }
        FileNode[] oldRootNodes = new FileNode[rootNodes.size()];
        int i = 0;
        for (FileNode node : rootNodes) {
            oldRootNodes[i++] = (FileNode) node.clone();
        }
        try {
            for (FileNode fileNode : rootNodes) {
                updatePatchPackWithZip(progressContext, fileNode);
                // 重新计算文件大小和md5值
                fileNode.consumeBytes(((len, buf) -> {
                }));
            }
            FileNode fileNode = rootNodes.getLast();
            FileUtil.copyTo(fileNode.getFile(), oldRootNodes[oldRootNodes.length - 1].getFile());
            fileNode.setFile(oldRootNodes[oldRootNodes.length - 1].getFile());
        } catch (Exception e) {
            i = 0;
            for (FileNode node : rootNodes) {
                node.setName(oldRootNodes[i].getName());
                node.setFile(oldRootNodes[i].getFile());
                node.setMd5(oldRootNodes[i].getMd5());
                node.setSize(oldRootNodes[i].getSize());
            }
            throw e;
        }
        progressContext.step("Write the patch package successfully");
    }

    private void updatePatchPackWithZip(ProgressContext progressContext, FileNode rootNode) throws IOException {
        progressContext.step("Writing to a zip file:" + rootNode.getName());
        if (!rootNode.getName().endsWith(FileExtConst.DOT_ZIP)) {
            String name = FileNameUtil.getName(rootNode.getName()) + FileExtConst.DOT_ZIP;
            rootNode.setName(name);
        }
        File file = new File(AppContext.getInstance().getTempDir(), FileNameUtil.uniqueFileName(rootNode.getName()));
        try (FileOutputStream out = new FileOutputStream(file);
             ZipOutputStream zout = new ZipOutputStream(out)) {
            writePatchPackWithZip(progressContext, zout, rootNode);
            rootNode.setFile(file);
        }
    }

    private void writePatchPackWithZip(ProgressContext progressContext, ZipOutputStream zout, FileNode rootNode) throws IOException {
        List<TreeNode> children = rootNode.getChildren();
        if (CollectionUtil.isEmpty(children)) {
            return;
        }
        for (TreeNode child : children) {
            CompressNode cNode = (CompressNode) child;
            ZipEntry entry = new ZipEntry(child.isDir() ? FileNameUtil.perfectDirPath(child.getPath()) : child.getPath());
            entry.setCreationTime(DateUtil.toFileTimeOrCur(cNode.getCreateTime()));
            entry.setLastAccessTime(DateUtil.toFileTimeOrCur(cNode.getAccessTime()));
            entry.setLastModifiedTime(DateUtil.toFileTimeOrCur(cNode.getModifyTime()));
            entry.setTimeLocal(cNode.getModifyTime());
            entry.setExtra(cNode.getExtra());
            entry.setComment(cNode.getComment());
            zout.putNextEntry(entry);
            if (!child.isDir()) {
                progressContext.step("Write to the file:" + child.getName());
                child.consumeBytes(((len, buf) -> zout.write(buf, 0, len)));
            }
            if (CollectionUtil.isNotEmpty(child.getChildren()) && !TreeNodeUtil.isCompressNode(child)) {
                writePatchPackWithZip(progressContext, zout, cNode);
            }
        }
    }
}
