package cn.cpoet.patch.assistant.service;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.SpringConst;
import cn.cpoet.patch.assistant.control.tree.AppTreeInfo;
import cn.cpoet.patch.assistant.control.tree.TotalInfo;
import cn.cpoet.patch.assistant.control.tree.TreeNodeType;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;
import cn.cpoet.patch.assistant.view.progress.ProgressContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 补丁匹配处理器
 *
 * @author CPoet
 */
public class PatchMatchProcessor {

    private volatile static Pattern FILTER_FILE_PATTERN = null;

    /**
     * 统计信息
     */
    private final TotalInfo totalInfo;

    /**
     * 应用信息
     */
    private final AppTreeInfo appTreeInfo;

    /**
     * 路径匹配
     */
    private final boolean isWithPath;

    /**
     * 名称匹配
     */
    private final boolean isWithName;

    /**
     * 应用包根节点
     */
    private TreeNode appRootNode;

    /**
     * 补丁包根节点
     */
    private TreeNode patchRootNode;

    /**
     * 进度器
     */
    private final ProgressContext pc;

    public PatchMatchProcessor(TotalInfo totalInfo, AppTreeInfo appTreeInfo, boolean isWithPath, boolean isWithName, ProgressContext pc) {
        this.totalInfo = totalInfo;
        this.appTreeInfo = appTreeInfo;
        this.isWithPath = isWithPath;
        this.isWithName = isWithName;
        this.pc = pc;
    }

    public void setAppRootNode(TreeNode appRootNode) {
        this.appRootNode = appRootNode;
    }

    public void setPatchRootNode(TreeNode patchRootNode) {
        this.patchRootNode = patchRootNode;
    }

    private boolean checkRootNode() {
        return appRootNode != null && CollectionUtil.isNotEmpty(appRootNode.getChildren())
                && patchRootNode != null && CollectionUtil.isNotEmpty(patchRootNode.getChildren());
    }

    private static boolean isAcceptPatchFile(String name) {
        String filterFileRegex = Configuration.getInstance().getPatch().getFilterFileRegex();
        if (StringUtil.isBlank(filterFileRegex)) {
            FILTER_FILE_PATTERN = null;
            return false;
        }
        if (FILTER_FILE_PATTERN == null) {
            synchronized (PatchMatchProcessor.class) {
                if (FILTER_FILE_PATTERN == null) {
                    FILTER_FILE_PATTERN = Pattern.compile(filterFileRegex);
                }
            }
        } else {
            if (!filterFileRegex.equals(FILTER_FILE_PATTERN.pattern())) {
                synchronized (PatchMatchProcessor.class) {
                    if (!filterFileRegex.equals(FILTER_FILE_PATTERN.pattern())) {
                        FILTER_FILE_PATTERN = Pattern.compile(filterFileRegex);
                    }
                }
            }
        }
        return !FILTER_FILE_PATTERN.matcher(name).matches();
    }

    public void exec() {
        if (!checkRootNode()) {
            return;
        }
        Map<String, TreeNode> nameMapping = Collections.emptyMap();
        if (isWithName) {
            nameMapping = patchRootNode.getChildren().stream()
                    .filter(node -> !node.isDir() && node.getMappedNode() == null)
                    .filter(node -> TreeNodeType.NONE.equals(node.getType()))
                    .filter(node -> isAcceptPatchFile(node.getName()))
                    .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
            pc.step("number of nodes matched by name " + nameMapping.size());
        }
        match(appRootNode.getChildren(), isWithPath ? patchRootNode.getChildren() : null, nameMapping);
    }

    private boolean match(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        if (CollectionUtil.isEmpty(appNodes)) {
            return false;
        }
        Map<String, TreeNode> patchNodeMapping = createPatchNodeMapping(patchNodes);
        boolean flag = false;
        for (TreeNode appNode : appNodes) {
            if (match(appNode, patchNodeMapping, nameMapping)) {
                flag = true;
            } else {
                pc.step("not matched " + appNode.getPath());
            }
        }
        return flag;
    }

    private Map<String, TreeNode> createPatchNodeMapping(List<TreeNode> patchNodes) {
        if (CollectionUtil.isEmpty(patchNodes)) {
            return Collections.emptyMap();
        }
        return patchNodes.stream().filter(node -> isAcceptPatchFile(node.getName()))
                .collect(Collectors.toMap(TreeNode::getName, Function.identity()));
    }

    private boolean matchWithThread(List<TreeNode> appNodes, List<TreeNode> patchNodes, Map<String, TreeNode> nameMapping) {
        if (CollectionUtil.isEmpty(appNodes)) {
            return false;
        }
        Map<String, TreeNode> patchNodeMapping = createPatchNodeMapping(patchNodes);
        // 上百个线程，后期评估是否引入线程池或者固定线程数量
        AtomicBoolean flagAtomic = new AtomicBoolean(false);
        CountDownLatch downLatch = new CountDownLatch(appNodes.size());
        for (TreeNode appNode : appNodes) {
            new Thread(() -> {
                try {
                    if (match(appNode, patchNodeMapping, nameMapping)) {
                        flagAtomic.set(true);
                    } else {
                        pc.step("not matched " + appNode.getPath());
                    }
                } finally {
                    downLatch.countDown();
                }
            }).start();
        }
        try {
            downLatch.await();
        } catch (Exception ignored) {
        }
        return flagAtomic.get();
    }

    private boolean match(TreeNode appNode, Map<String, TreeNode> patchNodeMapping, Map<String, TreeNode> nameMapping) {
        if (appNode.getMappedNode() != null) {
            return true;
        }
        if (CollectionUtil.isNotEmpty(patchNodeMapping)) {
            TreeNode matchPatchNode = findPatchNode(appNode, patchNodeMapping);
            if (matchPatchNode != null) {
                if (appNode.isDir()) {
                    if (SpringConst.LIB_PATH.equals(appNode.getPath())) {
                        return matchWithThread(appNode.getChildren(), matchPatchNode.getChildren(), nameMapping);
                    }
                    return match(appNode.getChildren(), matchPatchNode.getChildren(), nameMapping);
                }
                if (appNode.getName().endsWith(FileExtConst.DOT_JAR) && !matchPatchNode.getName().endsWith(FileExtConst.DOT_JAR)) {
                    if (appNode.getChildren() != null) {
                        return match(appNode.getChildren(), matchPatchNode.getChildren(), nameMapping);
                    }
                    if (!AppPackService.INSTANCE.buildChildrenWithCompress(appNode, false)) {
                        return false;
                    }
                    if (match(appNode.getChildren(), matchPatchNode.getChildren(), nameMapping)) {
                        return true;
                    }
                    appNode.setChildren(null);
                    return false;
                }
                mappedNode(appNode, matchPatchNode);
                return true;
            }
        }
        return matchWithName(appNode, nameMapping);
    }

    private boolean matchWithName(TreeNode appNode, Map<String, TreeNode> nameMapping) {
        if (CollectionUtil.isEmpty(nameMapping)) {
            return false;
        }
        if (appNode.isDir()) {
            if (SpringConst.LIB_PATH.equals(appNode.getPath())) {
                return matchWithThread(appNode.getChildren(), null, nameMapping);
            }
            return match(appNode.getChildren(), null, nameMapping);
        }
        TreeNode patchNode = nameMapping.remove(appNode.getName());
        if (patchNode != null) {
            mappedNode(appNode, patchNode);
            return true;
        }
        if (!appNode.getName().endsWith(FileExtConst.DOT_JAR)) {
            return false;
        }
        if (appNode.getChildren() != null) {
            return match(appNode.getChildren(), null, nameMapping);
        }
        if (!AppPackService.INSTANCE.buildChildrenWithCompress(appNode, false)) {
            return false;
        }
        if (match(appNode.getChildren(), null, nameMapping)) {
            return true;
        }
        appNode.setChildren(null);
        return false;
    }

    private TreeNode findPatchNode(TreeNode appNode, Map<String, TreeNode> patchNodeMapping) {
        if (appNode.getName().endsWith(FileExtConst.DOT_JAR)) {
            for (String patchName : patchNodeMapping.keySet()) {
                if (PatchPackService.INSTANCE.matchPatchName(appNode, patchName)) {
                    return patchNodeMapping.remove(patchName);
                }
            }
        }
        return patchNodeMapping.remove(appNode.getName());
    }

    private void mappedNode(TreeNode appNode, TreeNode patchNode) {
        pc.step("successful match " + appNode.getPath());
        TreeNodeUtil.mappedNode(totalInfo, appNode, patchNode, TreeNodeType.MOD);
        AppPackService.INSTANCE.createPatchDiffInfo(appTreeInfo, appNode, patchNode);
        PatchPackService.INSTANCE.mappedInnerClassNode(totalInfo, appTreeInfo, appNode, patchNode);
    }
}
