package ADT;

/**
 * Generic interface for a binary heap data structure.
 * <p>
 * A heap is a complete binary tree that satisfies the heap property:
 * for a max-heap, every parent is greater than or equal to its children.
 * </p>
 *
 * @param <T> the type of elements stored in the heap; must implement {@code Comparable<T>}
 */
public interface HeapInterface<T extends Comparable<T>> {

    /**
     * Inserts a new entry into the heap and restores the heap invariant.
     *
     * @param newEntry the entry to insert
     */
    void add(T newEntry);

    /**
     * Removes and returns the maximum element from the heap.
     *
     * @return the maximum element, or {@code null} if the heap is empty
     */
    T removeMax();

    /**
     * Returns (but does not remove) the maximum element.
     *
     * @return the maximum element, or {@code null} if the heap is empty
     */
    T getMax();

    /**
     * Returns {@code true} if the heap contains no elements.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of elements currently in the heap.
     *
     * @return the size of the heap
     */
    int getSize();

    /**
     * Removes all elements from the heap.
     */
    void clear();
}
