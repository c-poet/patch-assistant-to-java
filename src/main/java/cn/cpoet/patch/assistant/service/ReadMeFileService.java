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
     * @param patchRootNode 补丁根节点
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchTreeInfo patchTreeInfo, TreeNode patchRootNode) {
        return getPathInfos(patchTreeInfo, patchRootNode, null);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param patchTreeInfo 补丁树信息
     * @param patchRootNode 补丁根节点
     * @param appRootNode   应用根节点，可为空
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchTreeInfo patchTreeInfo, TreeNode patchRootNode, TreeNode appRootNode) {
        PatchRootInfo patchRootInfo = patchTreeInfo.getRootInfoByNode(patchRootNode);
        if (patchRootInfo == null) {
            return Collections.emptyList();
        }
        String readmeText = patchRootInfo.getPatchSign().getReadme();
        if (StringUtil.isBlank(readmeText)) {
            return Collections.emptyList();
        }
        return getPathInfos(readmeText, patchRootNode, appRootNode);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param readmeText    说明文件
     * @param patchRootNode 补丁根节点
     * @param appRootNode   应用根节点（可为空）
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(String readmeText, TreeNode patchRootNode, TreeNode appRootNode) {
        List<ReadMePathInfo> pathInfos = new ArrayList<>();
        String pathPrefix = null;
        if ((TreeNodeType.CUSTOM_ROOT.equals(patchRootNode.getType()) && patchRootNode.isDir())
                || (patchRootNode instanceof FileNode && patchRootNode.isDir())) {
            pathPrefix = patchRootNode.getPath();
        }
        try (StringReader reader = new StringReader(readmeText);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            int index = 0;
            while ((line = bufferedReader.readLine()) != null) {
                index = index + line.length() + 1;
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    ReadMePathInfo pathInfo = new ReadMePathInfo();
                    pathInfo.setType(ChangeTypeEnum.ofCode(matcher.group(1)));
                    pathInfo.setPath1(matcher.group(2));
                    pathInfo.setPath2(matcher.group(4));
                    pathInfo.setPath3(matcher.group(6));
                    if (ChangeTypeEnum.DEL.equals(pathInfo.getType())) {
                        if (appRootNode != null) {
                            pathInfo.setAppNode(findAppNode(appRootNode, pathInfo));
                            if (pathInfo.getAppNode() == null) {
                                continue;
                            }
                        }
                    } else {
                        pathInfo.setPatchNode(findPatchNode(patchRootNode, pathPrefix, pathInfo));
                        if (pathInfo.getPatchNode() == null) {
                            continue;
                        }
                    }
                    pathInfo.setStartIndex(index - line.length() - 1);
                    pathInfo.setEndIndex(index);
                    pathInfos.add(pathInfo);
                }
            }
        } catch (Exception ignored) {
        }
        return pathInfos;
    }

    private TreeNode findAppNode(TreeNode appRootNode, ReadMePathInfo pathInfo) {
        return TreeNodeUtil.findNodeByPath(appRootNode, pathInfo.getAppNodePath());
    }

    private TreeNode findPatchNode(TreeNode patchRootNode, String pathPrefix, ReadMePathInfo pathInfo) {
        String filePath = pathPrefix == null ? pathInfo.getPath1() : FileNameUtil.joinPath(pathPrefix, pathInfo.getPath1());
        TreeNode patchNode = TreeNodeUtil.findNodeByPath(patchRootNode, filePath);
        if (patchNode == null && StringUtil.isEmpty(pathInfo.getPath2())) {
            String fileName = FileNameUtil.getFileName(pathInfo.getPath1());
            if (!Objects.equals(fileName, pathInfo.getPath1())) {
                filePath = pathPrefix == null ? fileName : FileNameUtil.joinPath(pathPrefix, fileName);
                patchNode = TreeNodeUtil.findNodeByPath(patchRootNode, filePath);
            }
        }
        return patchNode;
    }
}
