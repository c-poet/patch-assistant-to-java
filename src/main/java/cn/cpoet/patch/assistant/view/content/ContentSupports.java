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
        String ext = FileNameUtil.getExt(node.getName());
        return getContentParser(ext);
    }

    public static ContentParser getContentParser(String ext) {
        if ("class".equals(ext)) {
            return new ClassContentParser();
        }
        return new TextContentParser();
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
                case "xml":
                    return new XmlCodeAreaFactory();
                case "yml":
                case "yaml":
                    return new YamlCodeAreaFactory();
                case "sql":
                    return new SQLCodeAreaFactory();
                default:
            }
        }
        return new CodeAreaFactory.DefaultCodeAreaFactory();
    }
}
