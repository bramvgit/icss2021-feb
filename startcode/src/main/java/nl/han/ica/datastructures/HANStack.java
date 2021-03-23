package nl.han.ica.datastructures;

import java.util.EmptyStackException;

public class HANStack<T> implements IHANStack<T> {
    /**
     * Array used by the stack
     */
    private Object[] array;

    /**
     * Points to index of the last pushed item
     */
    private int pointer = -1;

    public HANStack() {
        array = new Object[10];
    }

    public HANStack(int size) {
        array = new Object[size];
    }

    @Override
    public void push(T value) {
        pointer++;
        if (array.length == pointer) {
            Object[] tmp = array;
            array = new Object[tmp.length * 2];
            System.arraycopy(tmp, 0, array, 0, tmp.length);
        }
        array[pointer] = value;
    }

    @Override
    public T pop() {
        if (pointer == -1) throw new EmptyStackException();
        pointer--;
        return (T) array[pointer + 1];
    }

    @Override
    public T peek() {
        if (pointer == -1) {
            return null;
        }
        return (T) array[pointer];
    }

    public int size() {
        return pointer + 1;
    }
}
