package ADT;

/**
 * Generic interface for a priority queue.
 * <p>
 * A priority queue is an abstract data type where each element has a "priority"
 * and the element with the highest priority is served before elements with
 * lower priority.
 * </p>
 *
 * @param <T> the type of elements held in the priority queue; must implement {@code Comparable<T>}
 */
public interface PriorityQueueInterface<T extends Comparable<T>> {

    /**
     * Adds a new entry to the priority queue.
     *
     * @param newEntry the entry to add
     */
    void enqueue(T newEntry);

    /**
     * Removes and returns the highest-priority entry from the queue.
     *
     * @return the highest-priority entry, or {@code null} if empty
     */
    T dequeue();

    /**
     * Returns (but does not remove) the highest-priority entry.
     *
     * @return the highest-priority entry, or {@code null} if empty
     */
    T peek();

    /**
     * Returns {@code true} if the queue contains no entries.
     *
     * @return {@code true} if empty
     */
    boolean isEmpty();

    /**
     * Returns the number of entries currently in the queue.
     *
     * @return the size of the queue
     */
    int size();

    /**
     * Removes all entries from the queue.
     */
    void clear();
}
