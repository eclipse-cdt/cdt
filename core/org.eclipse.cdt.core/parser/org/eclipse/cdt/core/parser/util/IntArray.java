/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Arrays;

/**
 * Automatically growing integer array.
 * 
 * @since 5.5
 */
public class IntArray {
    private static final int INITIAL_CAPACITY = 10;
    private static final int[] EMPTY_ARRAY = {}; 

    private int[] buffer = EMPTY_ARRAY;
    private int size;

    public IntArray() {
    }

    public IntArray(int initialCapacity) {
        this.buffer = new int[initialCapacity];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(int value) {
        grow(size + 1);
        buffer[size++] = value;
    }

    public void add(int index, int value) {
    	checkBounds(index);
        grow(size + 1);
        System.arraycopy(buffer, index, buffer, index + 1, size - index);
        buffer[index] = value;
        size++;
    }

    public void addAll(IntArray other) {
        grow(size + other.size());
        System.arraycopy(other.buffer, 0, buffer, size, other.size);
        size += other.size;
        return;
    }

    public void addAll(int[] array) {
        grow(size + array.length);
        System.arraycopy(array, 0, buffer, size, array.length);
        size += array.length;
        return;
    }

    public int remove(int index) {
    	checkBounds(index);
        int old = buffer[index];
        int n = size - index - 1;
        if (n > 0) {
            System.arraycopy(buffer, index + 1, buffer, index, n);
        }
        return old;
    }

    public void remove(int from, int to) {
    	checkBounds(from);
    	checkBounds(to);
        System.arraycopy(buffer, to, buffer, from, size - to);
    }

    public void clear() {
        size = 0;
    }

    public int get(int index) {
    	checkRange(index);
        return buffer[index];
    }

    public int set(int index, int value) {
    	checkBounds(index);
        int old = buffer[index];
        buffer[index] = value;
        return old;
    }

    public int[] toArray() {
        return size == 0 ? EMPTY_ARRAY : Arrays.copyOf(buffer, size);
    }

    public void trimToSize() {
    	if (size == 0) {
    		buffer = EMPTY_ARRAY;
    	} else if (size < buffer.length) {
            buffer = Arrays.copyOf(buffer, size);
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
    	if (minCapacity < 0) // Overflow
    		throw new OutOfMemoryError();

    	int capacity = buffer.length;
    	if (minCapacity > capacity) {
			int newCapacity = capacity == 0 ? INITIAL_CAPACITY : capacity + (capacity >> 1);
			// newCapacity may be negative due to overflow.
			if (newCapacity < minCapacity)
			    newCapacity = minCapacity;
			// newCapacity is guaranteed to be non negative.
			try {
				buffer = Arrays.copyOf(buffer, newCapacity);
			} catch (OutOfMemoryError e) {
				// Try again it case we were too aggressive in reserving capacity.
				buffer = Arrays.copyOf(buffer, minCapacity);
			}
    	}
    }

    private void checkBounds(int index) {
    	if (index < 0) {
    		throw new IndexOutOfBoundsException("Negative index: " + index); //$NON-NLS-1$
    	}
    	checkRange(index);
    }

    private void checkRange(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);  //$NON-NLS-1$//$NON-NLS-2$
        }
    }
}
