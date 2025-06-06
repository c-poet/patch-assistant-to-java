package cn.cpoet.patch.assistant.core;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 搜索配置
 *
 * @author CPoet
 */
public class SearchConf {

    /**
     * 历史记录保存大小
     */
    private Integer historyLimit = 30;

    /**
     * 历史列表
     */
    private Set<SearchItem> history;

    public Integer getHistoryLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(Integer historyLimit) {
        this.historyLimit = historyLimit;
    }

    public Set<SearchItem> getHistory() {
        return history;
    }

    public void setHistory(Set<SearchItem> history) {
        this.history = history;
    }

    public void addHistory(SearchItem item) {
        if (history == null) {
            history = new LinkedHashSet<>();
        }
        history.add(item);
        if (history.size() > historyLimit) {
            Iterator<SearchItem> iterator = history.iterator();
            for (int i = 0; i < history.size() - historyLimit; ++i, iterator.next()) {
                iterator.remove();
            }
        }
    }
}
