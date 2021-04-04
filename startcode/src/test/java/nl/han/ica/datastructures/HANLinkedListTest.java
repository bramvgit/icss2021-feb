package nl.han.ica.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HANLinkedListTest {
    private IHANLinkedList<Integer> sut;

    @BeforeEach
    void setUp() {
        sut = new HANLinkedList<>();
    }

    @Test
    void getFirstReturnsCorrectValue() {
        sut.addFirst(20);
        assertEquals(20, sut.getFirst());
    }

    @Test
    void getReturnsCorrectValue() {
        sut.addFirst(20);
        assertEquals(20, sut.get(0));
    }

    @Test
    void insertInsertsCorrectValue() {
        sut.addFirst(20);
        sut.insert(1, 22);
        sut.insert(2, 24);
        sut.insert(3, 23);
        assertEquals(24, sut.get(2));
    }

    @Test
    void insertOverridesExistingNode() {
        sut.addFirst(10);
        sut.insert(1, 22);
        sut.insert(1, 12);
        assertEquals(12, sut.get(1));
    }

    @Test
    void getReturnsNullWhenPosIsInvalid() {
        sut.addFirst(22);
        sut.insert(0, 22);
        assertNull(sut.get(55));
    }

    @Test
    void getSizeReturnsCorrectSize() {
        sut.addFirst(22);
        sut.insert(1, 5);
        sut.insert(2, 4);
        sut.insert(3, 6);
        sut.insert(4, 22);
        assertEquals(5, sut.getSize());
    }

    @Test
    void deleteDeletesMiddleItem() {
        sut.addFirst(22);
        sut.insert(1, 5);
        sut.insert(2, 4);
        sut.insert(3, 6); // delete
        sut.insert(4, 22);
        sut.delete(3);
        assertEquals(sut.get(3), 22);
    }

    @Test
    void deleteDeletesFirstItem() {
        sut.addFirst(22); // delete
        sut.insert(1, 4);
        sut.insert(2, 3);
        sut.delete(0);
        assertEquals(sut.get(0), 4);
    }

    @Test
    void deleteDeletesLastItem() {
        sut.addFirst(22);
        sut.insert(1, 4);
        sut.insert(2, 3); // delete
        sut.delete(2);
        assertNull(sut.get(2));
    }

    @Test
    void clearsAllItems() {
        sut.addFirst(22);
        sut.insert(1, 4);
        sut.insert(2, 3);
        sut.clear();
        assertNull(sut.get(10));
    }

    @Test
    void addFirstPrependsItem() {
        sut.addFirst(1);
        sut.addFirst(2);
        sut.addFirst(3);
        assertEquals(3, sut.get(0));
    }

    @Test
    void addFirstIncreasesSize() {
        sut.addFirst(1);
        sut.addFirst(2);
        sut.addFirst(3);
        assertEquals(3, sut.getSize());
    }

    @Test
    void deleteLastItem() {
        sut.addFirst(1);
        sut.delete(0);
        assertNull(sut.get(0));
    }

    @Test
    void removeFirstRemovesFirstItem() {
        sut.addFirst(22);
        sut.insert(1, 2);
        sut.insert(2, 4);
        sut.removeFirst();
        assertEquals(2, sut.get(0));
    }
}