package ADT;

/**
 * Specifies a first-in, first-out (FIFO) Queue ADT.
 *
 * <p>This interface is adapted from the Queue ADT by Frank M. Carrano.
 * It was adapted by Ben Chin for the TARUMT Resorts assignment by adding a
 * project-owned iterator. No Java Collections Framework
 * collection is required by this interface.</p>
 *
 * <p>The queue is appropriate for the walk-in registration module because a
 * guest who joins the waiting queue first should normally be processed first.
 * The VIP allocation module uses a separate priority-based ADT because VIP
 * guests are not served strictly by arrival order.</p>
 *
 * @param <T> the type of entry stored in the queue
 * @author Frank M. Carrano (original Queue ADT)
 * @author Ben Chin (assignment adaptation)
 * @version 2.1
 */
public interface QueueInterface<T> {

  /**
   * Returns an iterator that traverses entries from front to back without
   * removing them.
   *
   * @return a project-owned queue iterator
   */
  public QueueIterator<T> getIterator();

  /**
   * Adds a new entry to the back of the queue.
   *
   * @param newEntry the entry to add
   */
  public void enqueue(T newEntry);

  /**
   * Removes and returns the entry at the front of the queue.
   *
   * @return the front entry, or {@code null} when the queue is empty
   */
  public T dequeue();

  /**
   * Retrieves the entry at the front without removing it.
   *
   * @return the front entry, or {@code null} when the queue is empty
   */
  public T getFront();

  /**
   * Detects whether the queue contains no entries.
   *
   * @return {@code true} when the queue is empty
   */
  public boolean isEmpty();

  /**
   * Removes all entries from the queue.
   */
  public void clear();

}
