/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.deltahex.delta.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Default implementation of doubly linked list of items.
 *
 * @version 0.1.0 2016/06/03
 * @author ExBin Project (http://exbin.org)
 * @param <T> doubly linked list item
 */
public class DefaultDoublyLinkedList<T extends DoublyLinkedItem> implements DoublyLinkedList<T> {

    private T first;

    /**
     * Cached values
     */
    private T last;
    private int size = 0;

    @Override
    public T first() {
        return first;
    }

    @Override
    public T last() {
        return last;
    }

    @Override
    public T nextTo(T item) {
        return (T) item.getNext();
    }

    @Override
    public T prevTo(T item) {
        return (T) item.getPrev();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public T get(int index) {
        T item = first;
        while (index > 0) {
            item = nextTo(item);
            index--;
        }

        return item;
    }

    @Override
    public T set(int index, T element) {
        T item = first;
        while (index > 0) {
            item = nextTo(item);
            index--;
        }

        T itemPrev = (T) item.getPrev();
        T itemNext = (T) item.getNext();
        item.setNext(null);
        item.setPrev(null);
        element.setPrev(itemPrev);
        element.setNext(itemNext);
        if (last == item) {
            last = element;
        }

        return item;
    }

    @Override
    public boolean contains(Object o) {
        T item = first;
        while (item != null) {
            if (item.equals(o)) {
                return true;
            }
            item = nextTo(item);
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean add(T e) {
        if (last != null) {
            last.setNext(e);
            e.setPrev(last);
            e.setNext(null);
            last = e;
        } else {
            first = e;
            last = e;
            e.setNext(null);
            e.setPrev(null);
        }

        size++;
        return true;
    }

    @Override
    public void add(int index, T element) {
        if (index == 0 && size == 0) {
            add(element);
            return;
        }

        if (size == 0) {
            T item = first;
            first = element;
            element.setNext(item);
            element.setPrev(null);
            item.setPrev(element);
        } else if (index == size) {
            last.setNext(element);
            element.setPrev(last);
            element.setNext(null);
            last = element;
        } else {
            T item = get(index);
            T prevItem = (T) item.getPrev();
            element.setPrev(prevItem);
            element.setNext(item);
            prevItem.setNext(element);
            item.setPrev(element);
        }
        size++;
    }

    public void addAfter(T positionItem, T element) {
        DoublyLinkedItem next = positionItem.getNext();
        if (next == null) {
            positionItem.setNext(element);
            element.setPrev(positionItem);
            element.setNext(null);
            last = element;
        } else {
            positionItem.setNext(element);
            next.setPrev(element);
            element.setPrev(positionItem);
            element.setNext(next);
        }
        size++;
    }

    public void addBefore(T positionItem, T element) {
        DoublyLinkedItem prev = positionItem.getPrev();
        if (prev == null) {
            positionItem.setPrev(element);
            element.setNext(positionItem);
            element.setPrev(null);
            first = element;
        } else {
            positionItem.setPrev(element);
            prev.setNext(element);
            element.setNext(positionItem);
            element.setPrev(prev);
        }
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T t : c) {
            result |= add(t);
        }

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int indexOf(Object o) {
        T item = first;
        int index = 0;
        while (item != null) {
            if (item.equals(o)) {
                return index;
            }
            item = (T) item.getNext();
            index++;
        }

        return -1;
    }

    @Override
    public T remove(int index) {
        T item = get(index);
        if (item != null) {
            removeItem(item);
            return item;
        }

        return null;
    }

    @Override
    public boolean remove(Object o) {
        T item = (T) o;
        if (item != null) {
            removeItem(item);
            return true;
        }

        return false;
    }

    private void removeItem(T item) {
        if (item == first) {
            first = (T) item.getNext();
            if (first != null) {
                first.setPrev(null);
            } else {
                last = null;
            }
        } else {
            T prev = (T) item.getPrev();
            T next = (T) item.getNext();
            prev.setNext(next);
            if (next != null) {
                next.setPrev(prev);
            } else {
                last = prev;
            }
        }

        size--;
        item.setPrev(null);
        item.setNext(null);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object item : c) {
            result |= remove(item);
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
