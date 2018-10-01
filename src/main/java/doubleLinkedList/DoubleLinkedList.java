package main.java.doubleLinkedList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoubleLinkedList<E> implements Iterable<E> {
    private Node head;
    private Node tail;
    private int size = 0;

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(E element) {
        Node tmpNode = new Node(head, null, element);

        if (size == 0) {
            head = tmpNode;
            head.next = tail;

            tail = tmpNode;
            tail.previous = head;
        } else {
            tmpNode.next = head;
            tmpNode.previous = null;

            head.previous = tmpNode;
            head = tmpNode;
        }

        size++;
    }

    public void addLast(E element) {
        Node tmpNode = new Node(null, tail, element);

        if (size == 0) {
            head = tmpNode;
            head.next = tail;

            tail = tmpNode;
            tail.previous = head;
        } else {
            tmpNode.previous = tail;
            tmpNode.next = null;

            tail.next = tmpNode;
            tail = tmpNode;
        }

        size++;
    }

    public E getFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return head.element;
    }

    public E getLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return tail.element;
    }

    @SuppressWarnings("Duplicates")
    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        E element = head.element;
        head = head.next;

        if (head != null)
            head.previous = null;

        size--;

        return element;
    }

    @SuppressWarnings("Duplicates")
    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        E element = tail.element;
        tail = tail.previous;

        if (tail != null)
            tail.next = null;

        size--;

        return element;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Node node = head;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public E next() {
                E value = node.element;
                node = node.next;
                return value;
            }
        };
    }

    public class Node {
        private E element;
        private Node next;
        private Node previous;

        Node(Node previous, Node next, E element) {
            this.element = element;
            this.previous = previous;
            this.next = next;
        }
    }

}