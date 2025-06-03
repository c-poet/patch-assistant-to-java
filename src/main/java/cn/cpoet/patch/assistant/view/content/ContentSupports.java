package cn.cpoet.patch.assistant.view.content;

import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.view.tree.TreeNode;

/**
 * @author CPoet
 */
public abstract class ContentSupports {
    private ContentSupports() {
    }

    public static ContentParser getContentParser(TreeNode node) {
        return getContentParser(FileNameUtil.getExt(node.getName()));
    }

    public static ContentParser getContentParser(String ext) {
        if (ext != null) {
            switch (ext) {
                case "class":
                    return new ClassContentParser();
                case "txt":
                case "yaml":
                case "yml":
                case "properties":
                case "xml":
                case "json":
                case "html":
                case "htm":
                case "md":
                    return new TextContentParser();
                default:
            }
        }
        return null;
    }


    public static CodeAreaFactory getCodeAreaFactory(TreeNode node) {
        return getCodeAreaFactory(FileNameUtil.getExt(node.getName()));
    }

    public static CodeAreaFactory getCodeAreaFactory(String ext) {
        if (ext != null) {
            switch (ext) {
                case "java":
                case "class":
                    return new JavaCodeAreaFactory();
                case "js":
                    return new JavaScriptCodeAreaFactory();
                case "html":
                case "htm":
                    return new HtmlCodeAreaFactory();
                case "css":
                    return new CssCodeAreaFactory();
                default:
            }
        }
        return new CodeAreaFactory.DefaultCodeAreaFactory();
    }
}
