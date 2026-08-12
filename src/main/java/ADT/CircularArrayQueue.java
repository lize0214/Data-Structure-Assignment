package ADT;

import java.util.Iterator;

/**
 * CircularArrayQueue.java  A class that implements the ADT Queue using a
 * circular array with one unused location.
 *
 * @author Frank M. Carrano
 * @version 2.0
 */
public class CircularArrayQueue<T> implements QueueInterface<T> {

  private T[] array; // circular array of array entries and one unused location
  private int frontIndex;
  private int backIndex;
  private static final int DEFAULT_CAPACITY = 5;

  public CircularArrayQueue() {
    this(DEFAULT_CAPACITY);
  }

  public CircularArrayQueue(int initialCapacity) {
    array = (T[]) new Object[initialCapacity + 1];
    frontIndex = 0;
    backIndex = initialCapacity;
  }

  public void enqueue(T newEntry) {
    if (!isArrayFull()) {
      backIndex = (backIndex + 1) % array.length;
      array[backIndex] = newEntry;
    }
  }

  public T getFront() {
    T front = null;

    if (!isEmpty()) {
      front = array[frontIndex];
    }

    return front;
  }

  public T dequeue() {
    T front = null;

    if (!isEmpty()) {
      front = array[frontIndex];
      array[frontIndex] = null;
      frontIndex = (frontIndex + 1) % array.length;
    }

    return front;
  }

  public boolean isEmpty() {
    return frontIndex == ((backIndex + 1) % array.length);
  }

  public void clear() {
    if (!isEmpty()) {
      for (int index = frontIndex; index != backIndex; index = (index + 1) % array.length) {
        array[index] = null;
      }
      array[backIndex] = null;
    }

    frontIndex = 0;
    backIndex = array.length - 1;
  }

  private boolean isArrayFull() {
    return frontIndex == ((backIndex + 2) % array.length);
  }

  @Override
  public Iterator<T> getIterator() {
    return new CircularArrayQueueIterator();
  }

  private class CircularArrayQueueIterator implements Iterator<T> {
    private int nextIndex;          // next array slot to read
    private int count;              // how many entries already returned
    private final int totalEntries; // number of entries in the queue at iterator creation

    private CircularArrayQueueIterator() {
      nextIndex = frontIndex;
      count = 0;
      totalEntries = size();
    }

    @Override
    public boolean hasNext() {
      return count < totalEntries;
    }

    @Override
    public T next() {
      if (hasNext()) {
        T nextEntry = array[nextIndex];
        nextIndex = (nextIndex + 1) % array.length; // wrap around
        count++;
        return nextEntry;
      } else {
        return null;
      }
    }
  }

  /**
   * Task: Counts the number of entries currently in the queue.
   * @return the number of entries in the queue
   */
  public int size() {
    if (isEmpty()) {
      return 0;
    }
    int distance = (backIndex - frontIndex + array.length) % array.length;
    return distance + 1;
  }
}