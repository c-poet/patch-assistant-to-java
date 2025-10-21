package cn.cpoet.patch.assistant.jdk;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author CPoet
 */
public class SortLinkedList<T> extends LinkedList<T> {

    private static final long serialVersionUID = -1764453128745120815L;

    private final Comparator<T> comparator;

    public SortLinkedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T t) {
        // addAndIndex(t);
        // return true;
        return super.add(t);
    }

    // public int addAndIndex(T t) {
    //     int i = 0;
    //     for (T item : this) {
    //         if (comparator.compare(t, item) < 0) {
    //             break;
    //         }
    //         ++i;
    //     }
    //     super.add(i, t);
    //     return i;
    // }
}
