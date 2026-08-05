package ADT;

/**
 * A max-heap-based implementation of {@link PriorityQueueInterface}.
 * <p>
 * Internally delegates to {@link MaxHeap} for all operations. The highest-priority
 * element (according to the natural ordering of {@code T}) is always at the front
 * of the queue.
 * </p>
 *
 * @param <T> the type of elements held in the priority queue; must implement {@code Comparable<T>}
 */
public class HeapPriorityQueue<T extends Comparable<T>> implements PriorityQueueInterface<T> {

    private final MaxHeap<T> heap;

    /**
     * Constructs an empty priority queue backed by a max-heap with default capacity.
     */
    public HeapPriorityQueue() {
        this.heap = new MaxHeap<>();
    }

    /**
     * Constructs an empty priority queue backed by a max-heap with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the underlying heap
     */
    public HeapPriorityQueue(int initialCapacity) {
        this.heap = new MaxHeap<>(initialCapacity);
    }

    @Override
    public void enqueue(T newEntry) {
        heap.add(newEntry);
    }

    @Override
    public T dequeue() {
        return heap.removeMax();
    }

    @Override
    public T peek() {
        return heap.getMax();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public int size() {
        return heap.getSize();
    }

    @Override
    public void clear() {
        heap.clear();
    }
}
