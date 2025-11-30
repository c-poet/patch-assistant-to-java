package cn.cpoet.patch.assistant.control.code;

/**
 * 搜索结果
 *
 * @author CPoet
 */
public class SearchResult {

    private int total;

    private String keyword;

    private SearchItem item;

    private long version;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SearchItem getItem() {
        return item;
    }

    public void setItem(SearchItem item) {
        this.item = item;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public static class SearchItem {
        /**
         * 第几个结果
         */
        private int no;

        /**
         * 开始索引
         */
        private int startIndex;

        /**
         * 结束索引
         */
        private int endIndex;

        /**
         * 上一个结果
         */
        private SearchItem pre;

        /**
         * 下一个结果
         */
        private SearchItem next;

        public int getNo() {
            return no;
        }

        public void setNo(int no) {
            this.no = no;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }

        public SearchItem getPre() {
            return pre;
        }

        public void setPre(SearchItem pre) {
            this.pre = pre;
        }

        public SearchItem getNext() {
            return next;
        }

        public void setNext(SearchItem next) {
            this.next = next;
        }
    }
}
