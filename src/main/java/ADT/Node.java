package ADT;

/**
 * Node used by the Doubly Linked List.
 *
 * @author Tan Pei Xing
 */
public class Node<T> {

    private T data;
    private Node<T> next;
    private Node<T> previous;

    // Creates a node with the given data.
    public Node(T data) {
        this.data = data;
        this.next = null;
        this.previous = null;
    }

    // Returns the stored data.
    public T getData() {
        return data;
    }

    // Updates the stored data.
    public void setData(T data) {
        this.data = data;
    }

    // Returns the next node.
    public Node<T> getNext() {
        return next;
    }

    // Sets the next node.
    public void setNext(Node<T> next) {
        this.next = next;
    }

    // Returns the previous node.
    public Node<T> getPrevious() {
        return previous;
    }

    // Sets the previous node.
    public void setPrevious(Node<T> previous) {
        this.previous = previous;
    }
}
