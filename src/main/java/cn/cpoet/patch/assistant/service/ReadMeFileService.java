package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.control.tree.PatchRootInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeInfo;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.model.ReadMePathInfo;
import cn.cpoet.patch.assistant.util.StringUtil;

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

    private final Pattern pattern = Pattern.compile("([!+-]?)([a-zA-Z-/.0-9]+)(\\s+([a-zA-Z-/.0-9]+))?(\\s+([a-zA-Z-/.0-9]*))?");

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
        return getPathInfos(readmeText);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param readmeText 说明文件
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(String readmeText) {
        List<ReadMePathInfo> pathInfos = new ArrayList<>();
        try (StringReader reader = new StringReader(readmeText);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    ReadMePathInfo pathInfo = new ReadMePathInfo();
                    pathInfo.setType(ReadMePathInfo.TypeEnum.ofCode(matcher.group(1)));
                    pathInfo.setPath1(matcher.group(2));
                    pathInfo.setPath2(matcher.group(4));
                    pathInfo.setPath3(matcher.group(6));
                    pathInfos.add(pathInfo);
                }
            }
        } catch (Exception ignored) {
        }
        return pathInfos;
    }
}
