package nl.han.ica.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.*;

class HANStackTest {
    private IHANStack<Integer> sut;

    @BeforeEach
    void setUp() {
        sut = new HANStack<>();
    }

    @Test
    void pushAddsItem() {
        sut.push(10);
        assertEquals(10, sut.peek());
    }

    @Test
    void pushIncreasesArraySize() {
        HANStack<Integer> sutb = new HANStack<>(2);
        sutb.push(1);
        sutb.push(2);
        sutb.push(88);

        assertEquals(3, sutb.size());
    }

    @Test
    void popThrowsWhenEmpty() {
        assertThrows(EmptyStackException.class, () -> sut.pop());
    }

    @Test
    void popReturnsItem() {
        sut.push(10);
        assertEquals(10, sut.pop());
    }

    @Test
    void popDeletesItem() {
        sut.push(10);
        assertEquals(10, sut.pop());
        assertNull(sut.peek());
    }

    @Test
    void peekReturnsNullWhenEmpty() {
        assertNull(sut.peek());
    }

    @Test
    void peekReturnsItem() {
        sut.push(10);
        assertEquals(10, sut.peek());
    }
}