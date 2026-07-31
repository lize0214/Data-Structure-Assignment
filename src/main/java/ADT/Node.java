/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ADT;
/**
 * Generic Node class for Doubly Linked List.
 *
 * @author User
 */
public class Node<T> {
    private T data;
    private Node<T> next;
    private Node<T> previous;
    public Node(T data) {
        this.data = data;
        this.next = null;
        this.previous = null;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public Node<T> getNext() {
        return next;
    }
    public void setNext(Node<T> next) {
        this.next = next;
    }
    public Node<T> getPrevious() {
        return previous;
    }
    public void setPrevious(Node<T> previous) {
        this.previous = previous;
    }
}