package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.model.PatchSign;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.view.tree.PatchRootInfo;
import cn.cpoet.patch.assistant.view.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 补丁Readme文件解析
 *
 * @author CPoet
 */
public class ReadMeFileService {

    private final Pattern pattern = Pattern.compile("([!+-]?)([a-zA-Z-/.0-9]+)\\s+([a-zA-Z-/.0-9]+)(\\s+([a-zA-Z-/.0-9]*))?");

    public static ReadMeFileService getInstance() {
        return AppContext.getInstance().getService(ReadMeFileService.class);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param patchTreeInfo 补丁树信息
     * @param treeNode      根节点
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchTreeInfo patchTreeInfo, TreeNode treeNode) {
        PatchRootInfo patchRootInfo = patchTreeInfo.getRootInfoByNode(treeNode);
        if (patchRootInfo == null) {
            return Collections.emptyList();
        }
        return getPathInfos(patchRootInfo.getPatchSign());
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param patchSign 补丁签名信息
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchSign patchSign) {
        String readMeText = patchSign.getReadme();
        if (StringUtil.isBlank(readMeText)) {
            return Collections.emptyList();
        }
        List<ReadMePathInfo> pathInfos = new ArrayList<>();
        try (StringReader reader = new StringReader(readMeText);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    ReadMePathInfo pathInfo = new ReadMePathInfo();
                    pathInfo.setType(ReadMePathInfo.TypeEnum.ofCode(matcher.group(1)));
                    pathInfo.setFilePath(matcher.group(2));
                    pathInfo.setFirstPath(matcher.group(3));
                    pathInfo.setSecondPath(matcher.group(5));
                    pathInfos.add(pathInfo);
                }
            }
        } catch (Exception ignored) {
        }
        return pathInfos;
    }
}
