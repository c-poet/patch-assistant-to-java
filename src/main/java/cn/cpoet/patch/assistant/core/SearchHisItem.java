package cn.cpoet.patch.assistant.core;

import java.util.Date;
import java.util.Objects;

/**
 * @author CPoet
 */
public class SearchHisItem extends SearchItem {
    /**
     * 搜索时间
     */
    private Date searchTime;

    /**
     * 点击的结果
     */
    private String target;

    public Date getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(Date searchTime) {
        this.searchTime = searchTime;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SearchHisItem that = (SearchHisItem) o;

        if (!Objects.equals(searchTime, that.searchTime)) return false;
        return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (searchTime != null ? searchTime.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }
}
