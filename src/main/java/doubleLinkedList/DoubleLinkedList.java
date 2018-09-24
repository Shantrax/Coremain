package main.java.doubleLinkedList;

import java.util.NoSuchElementException;

public class DoubleLinkedList<E> {
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

        if (head != null) {
            head.previous = tmpNode;
        }

        head = tmpNode;
        if (tail == null) {
            tail = tmpNode;
        }

        size++;
    }

    public void addLast(E element) {
        Node tmpNode = new Node(null, tail, element);

        if (tail != null) {
            tail.next = tmpNode;
        }

        tail = tmpNode;
        if (head == null) {
            head = tmpNode;
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
        head = head.previous;

        if (head != null) head.next = null;

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

        if (tail != null) tail.next = null;

        size--;

        return element;
    }

    private class Node {
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