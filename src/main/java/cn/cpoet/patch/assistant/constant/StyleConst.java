package cn.cpoet.patch.assistant.constant;

import javafx.scene.paint.Color;

/**
 * 常用样式
 *
 * @author CPoet
 */
public interface StyleConst {
    /**
     * 字体加粗
     */
    String FONT_BOLD = "-fx-font-weight: bold";

    /**
     * 蓝色
     */
    Color COLOR_BLUE = Color.web("#4c89fb");

    /**
     * 绿色
     */
    Color COLOR_GREEN = Color.web("#4fc75c");

    /**
     * 灰色
     */
    Color COLOR_GRAY = Color.web("#979797");

    /**
     * 灰色（详情）
     */
    Color COLOR_GRAY_1 = Color.web("#6c707e");

    /**
     * CodeArea全局样式文件
     */
    String STYLE_FILE_CODE_AREA = "/css/code-area.css";

    /**
     * 说明文件样式
     */
    String STYLE_FILE_README = "/css/readme.css";
}
