package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.ChangeTypeEnum;
import cn.cpoet.patch.assistant.control.tree.PatchRootInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.FileNode;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.model.ReadMePathInfo;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 补丁Readme文件解析
 *
 * @author CPoet
 */
public class ReadMeFileService {

    private final Pattern pattern = Pattern.compile("([!+-?]?)[\\\\/]?(" + FileNameUtil.FILE_NAME_REGEX + "+)(\\s+[\\\\/]?(" + FileNameUtil.FILE_NAME_REGEX + "+))?(\\s+(" + FileNameUtil.FILE_NAME_REGEX + "*))?");

    public final static ReadMeFileService INSTANCE = new ReadMeFileService();

    /**
     * 获取补丁文件的路径信息
     *
     * @param patchTreeInfo 补丁树信息
     * @param rootNode      根节点
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchTreeInfo patchTreeInfo, TreeNode rootNode) {
        PatchRootInfo patchRootInfo = patchTreeInfo.getRootInfoByNode(rootNode);
        if (patchRootInfo == null) {
            return Collections.emptyList();
        }
        String readmeText = patchRootInfo.getPatchSign().getReadme();
        if (StringUtil.isBlank(readmeText)) {
            return Collections.emptyList();
        }
        return getPathInfos(readmeText, rootNode);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param readmeText 说明文件
     * @param rootNode   补丁根节点
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(String readmeText, TreeNode rootNode) {
        List<ReadMePathInfo> pathInfos = new ArrayList<>();
        String pathPrefix = null;
        if ((TreeNodeType.CUSTOM_ROOT.equals(rootNode.getType()) && rootNode.isDir())
                || (rootNode instanceof FileNode && rootNode.isDir())) {
            pathPrefix = rootNode.getPath();
        }
        try (StringReader reader = new StringReader(readmeText);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            int index = 0;
            while ((line = bufferedReader.readLine()) != null) {
                index = index + line.length() + 1;
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    TreeNode patchNode = null;
                    String path1 = matcher.group(2);
                    String path2 = matcher.group(4);
                    ChangeTypeEnum changeType = ChangeTypeEnum.ofCode(matcher.group(1));
                    if (!ChangeTypeEnum.DEL.equals(changeType)) {
                        patchNode = findPatchNode(rootNode, pathPrefix, path1, path2);
                        if (patchNode == null) {
                            continue;
                        }
                    }
                    ReadMePathInfo pathInfo = new ReadMePathInfo();
                    pathInfo.setType(changeType);
                    pathInfo.setPath1(path1);
                    pathInfo.setPath2(path2);
                    pathInfo.setPath3(matcher.group(6));
                    pathInfo.setPatchNode(patchNode);
                    pathInfo.setStartIndex(index - line.length() - 1);
                    pathInfo.setEndIndex(index);
                    pathInfos.add(pathInfo);
                }
            }
        } catch (Exception ignored) {
        }
        return pathInfos;
    }

    private TreeNode findPatchNode(TreeNode rootNode, String pathPrefix, String path1, String path2) {
        String filePath = pathPrefix == null ? path1 : FileNameUtil.joinPath(pathPrefix, path1);
        TreeNode patchNode = TreeNodeUtil.findNodeByPath(rootNode, filePath);
        if (patchNode == null && StringUtil.isEmpty(path2)) {
            String fileName = FileNameUtil.getFileName(path1);
            if (!Objects.equals(fileName, path1)) {
                filePath = pathPrefix == null ? fileName : FileNameUtil.joinPath(pathPrefix, fileName);
                patchNode = TreeNodeUtil.findNodeByPath(rootNode, filePath);
            }
        }
        return patchNode;
    }
}
