package ADT;

/**
 * A generic array-based binary max-heap implementation.
 * <p>
 * The heap maintains the invariant that every parent node is greater than or
 * equal to its children (according to the natural ordering defined by
 * {@link Comparable#compareTo}). This guarantees that the maximum element
 * is always at the root (index 1).
 * </p>
 * <p>
 * Time complexity:
 * <ul>
 *   <li>{@code add(T)} — O(log n)</li>
 *   <li>{@code removeMax()} — O(log n)</li>
 *   <li>{@code getMax()} — O(1)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of elements held in this heap; must implement {@code Comparable<T>}
 */
public class MaxHeap<T extends Comparable<T>> implements HeapInterface<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private T[] heap;
    private int size;

    /**
     * Constructs an empty max-heap with default initial capacity (16).
     */
    @SuppressWarnings("unchecked")
    public MaxHeap() {
        this.heap = (T[]) new Comparable[DEFAULT_CAPACITY + 1]; // 1-indexed: index 0 is unused
        this.size = 0;
    }

    /**
     * Constructs an empty max-heap with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the heap
     * @throws IllegalArgumentException if initialCapacity is less than 1
     */
    @SuppressWarnings("unchecked")
    public MaxHeap(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be at least 1");
        }
        this.heap = (T[]) new Comparable[initialCapacity + 1];
        this.size = 0;
    }

    /**
     * Inserts a new entry into the heap, then restores the max-heap invariant
     * by sifting the entry up to its correct position.
     *
     * @param newEntry the entry to insert
     */
    @Override
    public void add(T newEntry) {
        if (newEntry == null) {
            throw new IllegalArgumentException("Cannot add null to heap");
        }
        ensureCapacity();
        heap[++size] = newEntry;
        siftUp(size);
    }

    /**
     * Removes and returns the maximum element (the root) from the heap.
     * The last element is moved to the root and sifted down to restore
     * the heap invariant.
     *
     * @return the maximum element, or {@code null} if the heap is empty
     */
    @Override
    public T removeMax() {
        if (isEmpty()) {
            return null;
        }
        T max = heap[1];
        heap[1] = heap[size];
        heap[size] = null;
        size--;
        if (size > 0) {
            siftDown(1);
        }
        return max;
    }

    /**
     * Returns (but does not remove) the maximum element (the root).
     *
     * @return the maximum element, or {@code null} if the heap is empty
     */
    @Override
    public T getMax() {
        if (isEmpty()) {
            return null;
        }
        return heap[1];
    }

    /**
     * Returns {@code true} if the heap contains no elements.
     *
     * @return {@code true} if the heap is empty
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements currently in the heap.
     *
     * @return the current size of the heap
     */
    @Override
    public int getSize() {
        return size;
    }

    /**
     * Removes all elements from the heap.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        for (int i = 1; i <= size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    // ───────────────────── Internal helpers ─────────────────────

    /**
     * Sifts the element at the given index up the heap until the max-heap
     * invariant is restored.
     */
    private void siftUp(int index) {
        T entry = heap[index];
        while (index > 1) {
            int parentIndex = index / 2;
            T parent = heap[parentIndex];
            // If entry <= parent, invariant is satisfied
            if (entry.compareTo(parent) <= 0) {
                break;
            }
            // Swap: move parent down, continue up
            heap[index] = parent;
            index = parentIndex;
        }
        heap[index] = entry;
    }

    /**
     * Sifts the element at the given index down the heap until the max-heap
     * invariant is restored.
     */
    private void siftDown(int index) {
        T entry = heap[index];
        int childIndex = 2 * index; // left child
        while (childIndex <= size) {
            // Pick the larger of the two children (if right child exists and is larger)
            if (childIndex < size && heap[childIndex + 1].compareTo(heap[childIndex]) > 0) {
                childIndex++; // right child is larger
            }
            // If entry >= larger child, invariant is satisfied
            if (entry.compareTo(heap[childIndex]) >= 0) {
                break;
            }
            // Move child up
            heap[index] = heap[childIndex];
            index = childIndex;
            childIndex = 2 * index;
        }
        heap[index] = entry;
    }

    /**
     * Doubles the internal array capacity when the heap is full.
     */
    @SuppressWarnings("unchecked")
    private void ensureCapacity() {
        if (size >= heap.length - 1) {
            int newCapacity = (heap.length - 1) * 2 + 1; // +1 for index 0
            T[] newHeap = (T[]) new Comparable[newCapacity];
            System.arraycopy(heap, 0, newHeap, 0, heap.length);
            heap = newHeap;
        }
    }
}
