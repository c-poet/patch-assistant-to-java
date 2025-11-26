package cn.cpoet.patch.assistant.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 搜索配置
 *
 * @author CPoet
 */
public class SearchConf implements Cloneable {

    /**
     * 历史记录保存大小
     */
    private Integer historyLimit = 30;

    /**
     * 历史列表
     */
    private LinkedList<SearchHisItem> history;

    public Integer getHistoryLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(Integer historyLimit) {
        this.historyLimit = historyLimit;
    }

    public LinkedList<SearchHisItem> getHistory() {
        return history;
    }

    public void setHistory(LinkedList<SearchHisItem> history) {
        this.history = history;
    }

    public void addHistory(SearchHisItem hisItem) {
        if (history == null) {
            history = new LinkedList<>();
        }
        history.removeIf(item -> Objects.equals(hisItem.getName(), item.getName()));
        history.addFirst(hisItem);
        if (history.size() > historyLimit) {
            int num = history.size() - historyLimit;
            Iterator<SearchHisItem> iterator = history.iterator();
            while (num > 0 && iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                --num;
            }
        }
    }

    @Override
    public SearchConf clone() {
        try {
            return (SearchConf) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
