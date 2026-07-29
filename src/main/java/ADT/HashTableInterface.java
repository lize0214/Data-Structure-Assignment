/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ADT;

import java.util.List;
/**
 *
 * @author USER
 */
public interface HashTableInterface<K, V> {
    void insert(K key, V value);
    V search(K key);
    boolean delete(K key);
    boolean isEmpty();
    int getSize();
    boolean contains(K key);
    List<V> values();
}
