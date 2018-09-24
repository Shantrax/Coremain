package test.java.doubleLinkedList;

import main.java.doubleLinkedList.DoubleLinkedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DoubleLinkedListTest {

    @Test
    @DisplayName("Si solo añades un elemento se convierte en head y tail")
    void testOne() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.addFirst("Probando");

        assertEquals(1, doubleLinkedList.getSize());
        assertTrue(!doubleLinkedList.isEmpty());

        assertEquals(doubleLinkedList.getFirst(), doubleLinkedList.getLast());
    }

    @Test
    @DisplayName("Si no tienen ningún elemento lanza NoSuchElement al intentar coger el head o tail")
    void testTwo() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();

        assertThrows(NoSuchElementException.class, doubleLinkedList::getFirst);
        assertThrows(NoSuchElementException.class, doubleLinkedList::getLast);

        assertEquals(0, doubleLinkedList.getSize());
        assertTrue(doubleLinkedList.isEmpty());
    }

    @Test
    @DisplayName("Si añades dos elementos como first, el primero pasa a ser tail")
    void testThree() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.addFirst("Last");
        doubleLinkedList.addFirst("First");

        assertEquals("First", doubleLinkedList.getFirst());
        assertEquals("Last", doubleLinkedList.getLast());

        assertTrue(!doubleLinkedList.isEmpty());
    }

    @Test
    @DisplayName("Si añades dos elementos como last, el primero pasa a ser head")
    void testFour() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.addLast("First");
        doubleLinkedList.addLast("Last");

        assertEquals("First", doubleLinkedList.getFirst());
        assertEquals("Last", doubleLinkedList.getLast());

        assertTrue(!doubleLinkedList.isEmpty());
    }

    @Test
    @DisplayName("Si añades un elemento con first y otro como last guarda el orden")
    void testFive() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.addLast("Last");
        doubleLinkedList.addFirst("First");

        assertEquals("Last", doubleLinkedList.getLast());
        assertEquals("First", doubleLinkedList.getFirst());

        assertTrue(!doubleLinkedList.isEmpty());
    }

    @Test
    @DisplayName("Si solo hay un elemento y lo borras queda vacío")
    void testSix() {
        DoubleLinkedList<String> doubleLinkedList = new DoubleLinkedList<>();
        doubleLinkedList.addFirst("Element");

        doubleLinkedList.removeFirst();

        assertThrows(NoSuchElementException.class, doubleLinkedList::getFirst);
        assertThrows(NoSuchElementException.class, doubleLinkedList::getLast);

        assertThrows(NoSuchElementException.class, doubleLinkedList::removeFirst);
        assertThrows(NoSuchElementException.class, doubleLinkedList::removeLast);

        assertEquals(0, doubleLinkedList.getSize());
        assertTrue(doubleLinkedList.isEmpty());
    }
}
