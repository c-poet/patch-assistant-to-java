package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.view.tree.PatchTreeInfo;

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

    private final Pattern pattern = Pattern.compile("([!+-]?)([a-zA-Z-/.]+)\\s+([a-zA-Z-/.]+)(\\s+([a-zA-Z-/.]*))?");

    public static ReadMeFileService getInstance() {
        return AppContext.getInstance().getService(ReadMeFileService.class);
    }

    /**
     * 获取补丁文件的路径信息
     *
     * @param patchTreeInfo 补丁树
     * @return 补丁文件路径信息列表
     */
    public List<ReadMePathInfo> getPathInfos(PatchTreeInfo patchTreeInfo) {
        String readMeText = patchTreeInfo.getReadMeText();
        if (readMeText == null || readMeText.isBlank()) {
            return Collections.emptyList();
        }
        List<ReadMePathInfo> pathInfos = new ArrayList<>();
        Matcher matcher = pattern.matcher(readMeText);
        while (matcher.find()) {
            ReadMePathInfo pathInfo = new ReadMePathInfo();
            pathInfo.setType(ReadMePathInfo.TypeEnum.ofCode(matcher.group(1)));
            pathInfo.setFilePath(matcher.group(2));
            pathInfo.setFirstPath(matcher.group(3));
            pathInfo.setSecondPath(matcher.group(5));
            pathInfos.add(pathInfo);
        }
        return pathInfos;
    }
}
