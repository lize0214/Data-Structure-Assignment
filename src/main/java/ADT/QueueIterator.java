package ADT;

/**
 * Project-owned traversal interface for queue entries.
 *
 * @param <T> the type of entry returned during traversal
 * @author Chin Yik Heng
 */
public interface QueueIterator<T> {

  boolean hasNext();

  T next();
}
