/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ADT;
import java.io.Serializable;
/**
 * DoublyLinkedList.java
 *
 * Generic Doubly Linked List implementation.
 *
 * @author User
 */
public class DoublyLinkedList<T> implements ListInterface<T>, Serializable {
    private Node<T> firstNode;
    private Node<T> lastNode;
    private int numberOfEntries;
    public DoublyLinkedList() {
        firstNode = null;
        lastNode = null;
        numberOfEntries = 0;
    }
    @Override
public int size() {
    return numberOfEntries;
}
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

        // Help garbage collection / avoid stale links
        nodeToRemove.setNext(null);
        nodeToRemove.setPrevious(null);

        numberOfEntries--;
        return nodeToRemove.getData();
    }
    @Override
public void clear() {
    firstNode = null;
    lastNode = null;
    numberOfEntries = 0;
}
    @Override
    public boolean replace(int givenPosition, T newEntry) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return false;
        }

        Node<T> nodeAtPosition = getNodeAt(givenPosition);
        nodeAtPosition.setData(newEntry);
        return true;
    }
    @Override
    public T getEntry(int givenPosition) {
        if (givenPosition < 1 || givenPosition > numberOfEntries) {
            return null;
        }

        return getNodeAt(givenPosition).getData();
    }
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
    @Override
public int getNumberOfEntries() {
    return numberOfEntries;
}
   @Override
public boolean isEmpty() {
    return numberOfEntries == 0;
}
    @Override
    public boolean isFull() {
        return false;
    }
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

    /**
     * Walks to the node at the given 1-indexed position.
     * Traverses from whichever end (front or back) is closer,
     * which keeps average traversal cost down for large lists.
     * Caller is responsible for ensuring position is valid
     * (1 <= position <= numberOfEntries).
     */
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