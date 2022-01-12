/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Doug Schaefer
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CharArrayObjectMap<T> extends CharTable {
	/**
	 * An empty immutable {@code CharArrayObjectMap}.
	 */
	public static final CharArrayObjectMap<?> EMPTY_MAP = new CharArrayObjectMap<Object>(0) {
		@Override
		public Object clone() {
			return this;
		}

		@Override
		public List<char[]> toList() {
			return Collections.emptyList();
		}

		@Override
		public Object put(char[] key, int start, int length, Object value) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <T> CharArrayObjectMap<T> emptyMap() {
		return (CharArrayObjectMap<T>) EMPTY_MAP;
	}

	private Object[] valueTable;

	public CharArrayObjectMap(int initialSize) {
		super(initialSize);
		valueTable = new Object[capacity()];
	}

	public T put(char[] key, int start, int length, T value) {
		int i = addIndex(key, start, length);
		@SuppressWarnings("unchecked")
		T oldvalue = (T) valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	final public T put(char[] key, T value) {
		return put(key, 0, key.length, value);
	}

	@SuppressWarnings("unchecked")
	final public T get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return (T) valueTable[i];
		return null;
	}

	final public T get(char[] key) {
		return get(key, 0, key.length);
	}

	@SuppressWarnings("unchecked")
	final public T getAt(int i) {
		if (i < 0 || i > currEntry)
			return null;
		return (T) valueTable[i];
	}

	final public T remove(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i < 0)
			return null;

		@SuppressWarnings("unchecked")
		T value = (T) valueTable[i];

		if (i < currEntry)
			System.arraycopy(valueTable, i + 1, valueTable, i, currEntry - i);

		valueTable[currEntry] = null;

		removeEntry(i);

		return value;
	}

	@Override
	public Object clone() {
		@SuppressWarnings("unchecked")
		CharArrayObjectMap<T> newTable = (CharArrayObjectMap<T>) super.clone();
		newTable.valueTable = new Object[capacity()];
		System.arraycopy(valueTable, 0, newTable.valueTable, 0, valueTable.length);

		return newTable;
	}

	@Override
	protected void resize(int size) {
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
		super.resize(size);
	}

	@Override
	public void clear() {
		super.clear();
		for (int i = 0; i < capacity(); i++)
			valueTable[i] = null;
	}

	@Override
	protected int partition(Comparator<Object> c, int p, int r) {
		char[] x = keyTable[p];
		Object temp = null;
		int i = p;
		int j = r;

		while (true) {
			while (c.compare(keyTable[j], x) > 0) {
				j--;
			}
			if (i < j) {
				while (c.compare(keyTable[i], x) < 0) {
					i++;
				}
			}

			if (i < j) {
				temp = keyTable[j];
				keyTable[j] = keyTable[i];
				keyTable[i] = (char[]) temp;

				temp = valueTable[j];
				valueTable[j] = valueTable[i];
				valueTable[i] = temp;
			} else {
				return j;
			}
		}
	}

	public Object[] valueArray() {
		Object[] values = new Object[size()];
		System.arraycopy(valueTable, 0, values, 0, values.length);
		return values;
	}

	public Object[] valueArray(Class<?> clazz) {
		Object[] values = (Object[]) Array.newInstance(clazz, size());
		System.arraycopy(valueTable, 0, values, 0, values.length);
		return values;
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are reflected
	 * in the collection, and vice-versa.
	 *
	 * @since 6.0
	 */
	public Collection<T> values() {
		return new Values();
	}

	/**
	 * Checks if the map values contain the given object.
	 *
	 * @since 6.0
	 */
	public boolean containsValue(Object v) {
		int n = size();
		for (int i = 0; i < n; i++) {
			if (Objects.equals(valueTable[i], v)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append('{');
		for (int i = 0; i < size(); i++) {
			char[] key = keyAt(i);
			if (key != null) {
				if (i != 0)
					buf.append(", "); //$NON-NLS-1$
				buf.append(key);
				buf.append("="); //$NON-NLS-1$
				buf.append(getAt(i));
			}
		}
		buf.append('}');
		return buf.toString();
	}

	private class Values extends AbstractCollection<T> {
		@Override
		public final int size() {
			return CharArrayObjectMap.this.size();
		}

		@Override
		public final void clear() {
			CharArrayObjectMap.this.clear();
		}

		@Override
		public final boolean contains(Object v) {
			return containsValue(v);
		}

		@Override
		public final Iterator<T> iterator() {
			return new ValueIterator();
		}

		@Override
		@SuppressWarnings("unchecked")
		public final void forEach(Consumer<? super T> action) {
			for (int i = 0; i < size(); i++) {
				action.accept((T) valueTable[i]);
			}
		}
	}

	private final class ValueIterator implements Iterator<T> {
		int index;

		@Override
		public boolean hasNext() {
			return index < size();
		}

		@Override
		@SuppressWarnings("unchecked")
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return (T) valueTable[index++];
		}
	}
}
