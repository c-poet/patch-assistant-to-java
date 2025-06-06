package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.view.tree.TreeNode;

import java.util.Objects;

/**
 * 搜索结果项
 *
 * @author CPoet
 */
public class SearchItem {
    /**
     * 路径
     */
    private String path;

    /**
     * 文件名称
     */
    private String name;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchItem item = (SearchItem) o;
        if (!Objects.equals(path, item.path)) return false;
        return Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
