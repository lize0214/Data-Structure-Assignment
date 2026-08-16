package ADT;

/**
 *
 * @author Chong Zhi Yi
 */

public class HashTable<K, V> implements HashTableInterface<K, V> {

    private static final int DEFAULT_CAPACITY = 101;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V>[] buckets;
    private final int capacity;
    private int size;

    @SuppressWarnings("unchecked")
    public HashTable() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public HashTable(int capacity) {
        this.capacity = capacity;
        this.buckets = (Node<K, V>[]) new Node[capacity];
        this.size = 0;
    }

    private int hashFunction(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (key instanceof String) {
            String s = (String) key;
            int sum = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                int value = Character.isDigit(c) ? (c - '0') : c;
                sum += value * (i + 1); // weight by position to reduce collisions
            }
            return Math.abs(sum) % capacity;
        }
        return Math.abs(key.hashCode()) % capacity;
    }

    @Override
    public void insert(K key, V value) {
        int index = hashFunction(key);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value; // update existing entry
                return;
            }
            current = current.next;
        }

        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        size++;
    }

    @Override
    public V search(K key) {
        int index = hashFunction(key);
        Node<K, V> current = buckets[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    @Override
    public boolean delete(K key) {
        int index = hashFunction(key);
        Node<K, V> current = buckets[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.key.equals(key)) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean contains(K key) {
        return search(key) != null;
    }

    @Override
    public ListInterface<V> values() {

        ListInterface<V> all =
                new ArrayList<>();

        for (Node<K, V> head : buckets) {

            Node<K, V> current = head;

            while (current != null) {
                all.add(current.value);
                current = current.next;
            }
        }

        return all;
    }
}
