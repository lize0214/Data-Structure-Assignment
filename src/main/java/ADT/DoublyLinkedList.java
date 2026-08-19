package ADT;

import java.io.Serializable;

/**
 * Generic doubly linked list implementation.
 *
 * @author Tan Pei Xing
 */
public class DoublyLinkedList<T> implements ListInterface<T>, Serializable {

    private Node<T> firstNode;
    private Node<T> lastNode;
    private int numberOfEntries;

    // Initializes an empty list.
    public DoublyLinkedList() {
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
    }

    // Returns the number of entries in the list.
    @Override
    public int size() {
        return numberOfEntries;
    }

    // Adds a new entry to the end of the list.
    @Override
    public boolean add(T newEntry) {
        Node<T> newNode = new Node<>(newEntry);

        if (isEmpty()) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            newNode.setPrevious(lastNode);
            lastNode.setNext(newNode);
            lastNode = newNode;
        }

        numberOfEntries++;
        return true;
    }

    // Adds a new entry at the specified position.
    @Override
    public boolean add(int newPosition, T newEntry) {
        if (newPosition < 1 || newPosition > numberOfEntries + 1) {
            return false;
        }

        // Adding at the end is the same as add(T)
        if (newPosition == numberOfEntries + 1) {
            return add(newEntry);
        }

        Node<T> nodeAtPosition = getNodeAt(newPosition);
        Node<T> newNode = new Node<>(newEntry);
        Node<T> nodeBefore = nodeAtPosition.getPrevious();

        newNode.setNext(nodeAtPosition);
        newNode.setPrevious(nodeBefore);
        nodeAtPosition.setPrevious(newNode);

        if (nodeBefore == null) {
            // Inserting at position 1 -> new first node
            firstNode = newNode;
        } else {
            nodeBefore.setNext(newNode);
        }

        numberOfEntries++;
        return true;
    }

    // Removes and returns the entry at the specified position.
    @Override
    public T remove(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }

        Node<T> nodeToRemove = getNodeAt(givenPosition);
        Node<T> nodeBefore = nodeToRemove.getPrevious();
        Node<T> nodeAfter = nodeToRemove.getNext();

        if (nodeBefore == null) {
            firstNode = nodeAfter;
        } else {
            nodeBefore.setNext(nodeAfter);
        }

        if (nodeAfter == null) {
            lastNode = nodeBefore;
        } else {
            nodeAfter.setPrevious(nodeBefore);
        }

        // Removes links from the deleted node.
        nodeToRemove.setNext(null);
        nodeToRemove.setPrevious(null);

        numberOfEntries--;
        return nodeToRemove.getData();
    }

    // Removes all entries from the list.
    @Override
    public void clear() {
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
    }

    // Replaces the entry at the specified position.
    @Override
    public boolean replace(int givenPosition, T newEntry) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return false;
        }

        Node<T> nodeAtPosition = getNodeAt(givenPosition);
        nodeAtPosition.setData(newEntry);
        return true;
    }

    // Returns the entry at the specified position.
    @Override
    public T getEntry(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }

        return getNodeAt(givenPosition).getData();
    }

    // Checks whether the list contains the specified entry.
    @Override
    public boolean contains(T anEntry) {
        Node<T> currentNode = firstNode;

        while (currentNode != null) {
            if (currentNode.getData().equals(anEntry)) {
                return true;
            }
            currentNode = currentNode.getNext();
        }

        return false;
    }

    // Returns the number of entries in the list.
    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    // Checks whether the list is empty.
    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    // Returns whether the list is full.
    @Override
    public boolean isFull() {
        return false;
    }

    // Returns all entries as a string.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> currentNode = firstNode;

        while (currentNode != null) {
            sb.append(currentNode.getData());
            if (currentNode.getNext() != null) {
                sb.append(", ");
            }
            currentNode = currentNode.getNext();
        }

        sb.append("]");
        return sb.toString();
    }

    // Finds a node by starting from the closer end of the list.
    private Node<T> getNodeAt(int position) {
        Node<T> currentNode;

        if (position <= numberOfEntries / 2) {
            currentNode = firstNode;
            for (int i = 1; i < position; i++) {
                currentNode = currentNode.getNext();
            }
        } else {
            currentNode = lastNode;
            for (int i = numberOfEntries; i > position; i--) {
                currentNode = currentNode.getPrevious();
            }
        }

        return currentNode;
    }
}
