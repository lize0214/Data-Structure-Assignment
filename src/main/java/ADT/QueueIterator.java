package ADT;

/**
 * Project-owned traversal interface for queue entries.
 *
 * @param <T> the type of entry returned during traversal
 */
public interface QueueIterator<T> {

  boolean hasNext();

  T next();
}
