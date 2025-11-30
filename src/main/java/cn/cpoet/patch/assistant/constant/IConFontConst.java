package cn.cpoet.patch.assistant.constant;

import cn.cpoet.patch.assistant.util.FileUtil;
import javafx.scene.text.Font;

/**
 * 图标字体常量
 *
 * @author CPoet
 */
public interface IConFontConst {
    /**
     * 字体
     */
    Font FONT = Font.loadFont(FileUtil.getFileAsStream("/fonts/iconfont.ttf"), 14);

    /**
     * 关闭
     */
    String CLOSE = "\ue611";

    /**
     * 箭头向上
     */
    String ARROW_UP = "\ue745";

    /**
     * 箭头向下
     */
    String ARROW_DOWN = "\ue743";
}
