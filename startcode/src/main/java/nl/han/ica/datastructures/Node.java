package nl.han.ica.datastructures;

public class Node<T> {
    Node<T> next;
    T value;

    public Node(T value) {
        this.value = value;
    }
}
