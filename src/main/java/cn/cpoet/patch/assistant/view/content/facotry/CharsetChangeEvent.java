package cn.cpoet.patch.assistant.view.content.facotry;

import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.event.Event;
import javafx.event.EventType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符集变化事件
 *
 * @author CPoet
 */
public class CharsetChangeEvent extends Event {

    private static final long serialVersionUID = -8543392647031547287L;

    private String charset;

    public CharsetChangeEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Charset toCharset() {
        return StringUtil.isBlank(charset) ? StandardCharsets.UTF_8 : Charset.forName(charset);
    }
}
