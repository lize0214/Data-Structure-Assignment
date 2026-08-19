package ADT;

/**
 * Interface for the List ADT. Positions in the list begin with 1.
 *
 * @author Frank M. Carrano
 * @version 2.0
 */
public interface ListInterface<T> {

    // Returns the number of entries in the list.
    public int size();

    // Adds a new entry to the end of the list.
    public boolean add(T newEntry);

    // Adds a new entry at the specified position.
    public boolean add(int newPosition, T newEntry);

    // Removes and returns the entry at the specified position.
    public T remove(int givenPosition);

    // Removes all entries from the list.
    public void clear();

    // Replaces the entry at the specified position.
    public boolean replace(int givenPosition, T newEntry);

    // Returns the entry at the specified position.
    public T getEntry(int givenPosition);

    // Checks whether the list contains the specified entry.
    public boolean contains(T anEntry);

    // Returns the number of entries in the list.
    public int getNumberOfEntries();

    // Checks whether the list is empty.
    public boolean isEmpty();

    // Checks whether the list is full.
    public boolean isFull();
}
