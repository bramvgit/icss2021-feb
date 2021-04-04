package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private Node<T> header;

    @Override
    public void addFirst(T value) {
        if (header == null) {
            header = new Node<>(value);
        } else {
            Node<T> newHeader = new Node<>(value);
            newHeader.next = header;
            header = newHeader;
            header.value = value;
        }
    }

    @Override
    public void clear() {
        header = null;
    }

    @Override
    public void insert(int index, T value) {
        Node<T> current = header;

        for (int i = 0; i < index - 1; i++) {
            if (current == null) throw new IndexOutOfBoundsException();
            current = current.next;
        }
        current.next = new Node<>(value);
    }

    @Override
    public void delete(int pos) {
        Node<T> current = header;
        Node<T> prev = null;

        for (int i = 0; i < pos; i++) {
            if (current == null) throw new IndexOutOfBoundsException();
            prev = current;
            current = current.next;
        }

        if (prev != null && current.next != null) {
            prev.next = current.next;
        } else if (prev != null) {
            prev.next = null;
        } else if (current.next != null) {
            header = header.next;
        } else {
            header = null;
        }
    }

    @Override
    public T get(int pos) {
        if (header == null) return null;
        if (pos == 0) return header.value;

        Node<T> current = header;

        for (int i = 0; i < pos; i++) {
            if (current.next == null) return null;
            current = current.next;
        }
        return current.value;
    }

    @Override
    public void removeFirst() {
        if (header != null) header = header.next;
    }

    @Override
    public T getFirst() {
        return header.value;
    }

    @Override
    public int getSize() {
        if (header == null) return 0;

        int size = 1;
        Node<T> current = header;

        while (current.next != null) {
            current = current.next;
            size++;
        }
        return size;
    }
}
