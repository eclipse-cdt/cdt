/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ddaoust
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CharTable extends HashTable {
	protected char[][] keyTable;

	public CharTable(int initialSize) {
		super(initialSize);
		keyTable = new char[capacity()][];
	}

	@Override
	protected void resize(int size) {
		char[][] oldKeyTable = keyTable;
		keyTable = new char[size][];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, Math.min(size, oldKeyTable.length));
		super.resize(size);
	}

	@Override
	public void clear() {
		super.clear();
		Arrays.fill(keyTable, null);
	}

	@Override
	public Object clone() {
		CharTable newTable = (CharTable) super.clone();

		int size = capacity();
		newTable.keyTable = new char[size][];
		System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);

		return newTable;
	}

	protected final int hash(char[] source, int start, int length) {
		return hashTable == null ? 0 : hashToOffset(CharArrayUtils.hash(source, start, length));
	}

	@Override
	protected final int hash(int pos) {
		return hash(keyTable[pos], 0, keyTable[pos].length);
	}

	protected final int hash(char[] obj) {
		return hash(obj, 0, obj.length);
	}

	protected final int addIndex(char[] buffer) {
		return addIndex(buffer, 0, buffer.length);
	}

	public final int addIndex(char[] buffer, int start, int len) {
		if (hashTable == null) {
			int pos = lookup(buffer, start, len);
			if (pos != -1)
				return pos;
			// Key is not here, add it.
			if (currEntry + 1 >= capacity()) {
				resize();
				if (hashTable != null) {
					// If we grew from list to hash, then recurse and add as a hashtable.
					return addIndex(buffer, start, len);
				}
			}
			currEntry++;
			keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
		} else {
			int hash = hash(buffer, start, len);
			int pos = lookup(buffer, start, len, hash);
			if (pos != -1)
				return pos;

			// Key is not here, add it.
			if (currEntry + 1 >= capacity()) {
				resize();
				hash = hash(buffer, start, len);
			}
			currEntry++;
			keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
			linkIntoHashTable(currEntry, hash);
		}
		return currEntry;
	}

	protected void removeEntry(int i) {
		// Remove the entry from the keyTable, shifting everything over if necessary.
		int hash = hash(keyTable[i]);
		if (i < currEntry)
			System.arraycopy(keyTable, i + 1, keyTable, i, currEntry - i);

		keyTable[currEntry] = null;

		// Make sure you remove the value before calling super where currEntry will change
		removeEntry(i, hash);
	}

	public List<char[]> toList() {
		List<char[]> list = new ArrayList<>(size());
		int size = size();
		for (int i = 0; i < size; i++) {
			list.add(keyAt(i));
		}
		return list;
	}

	public final char[] keyAt(int i) {
		if (i < 0 || i > currEntry)
			return null;

		return keyTable[i];
	}

	public final boolean containsKey(char[] key, int start, int len) {
		return lookup(key, start, len) != -1;
	}

	public final boolean containsKey(char[] key) {
		return lookup(key) != -1;
	}

	public final char[] findKey(char[] buffer, int start, int len) {
		int idx = lookup(buffer, start, len);
		if (idx == -1)
			return null;

		return keyTable[idx];
	}

	public int lookup(char[] buffer) {
		return lookup(buffer, 0, buffer.length);
	}

	protected final int lookup(char[] buffer, int start, int len) {
		if (hashTable != null)
			return lookup(buffer, start, len, hash(buffer, start, len));
		for (int i = 0; i <= currEntry; i++) {
			if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
				return i;
		}
		return -1;
	}

	protected final int lookup(char[] buffer, int start, int len, int hash) {
		int i = hashTable[hash];
		if (i == 0)
			return -1;

		--i;
		if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
			return i;

		// Follow the next chain.
		for (i = nextTable[i] - 1; i >= 0 && i != nextTable[i] - 1; i = nextTable[i] - 1) {
			if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
				return i;
		}

		return -1;
	}

	/**
	 * @since 5.7
	 */
	public char[][] keys() {
		char[][] keys = new char[size()][];
		System.arraycopy(keyTable, 0, keys, 0, keys.length);
		return keys;
	}

	/**
	 * @deprecated Use {@link #keys()} instead.
	 */
	@Deprecated
	public Object[] keyArray() {
		return keys();
	}
}
