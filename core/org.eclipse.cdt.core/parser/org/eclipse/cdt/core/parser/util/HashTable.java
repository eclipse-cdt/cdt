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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author ddaoust
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HashTable implements Cloneable {
	// Prime numbers from http://planetmath.org/goodhashtableprimes
	private static final int[] PRIMES = { 17, 29, 53, 97, 193, 389, 769, 1543, 3079, 6151, 12289, 24593, 49157, 98317,
			196613, 393241, 786433, 1572869, 3145739, 6291469, 12582917, 25165843, 50331653, 100663319, 201326611,
			402653189, 805306457, 1610612741 };

	private static final int MIN_HASH_SIZE = 9;
	/** @deprecated Don't depend on this implementation detail. @noreference This field is not intended to be referenced by clients. */
	@Deprecated
	protected static final int minHashSize = MIN_HASH_SIZE - 1;

	protected int currEntry = -1;
	protected int[] hashTable;
	protected int[] nextTable;

	public boolean isEmpty() {
		return currEntry < 0;
	}

	public final int size() {
		return currEntry + 1;
	}

	public HashTable(int initialSize) {
		if (initialSize >= MIN_HASH_SIZE) {
			hashTable = new int[getSuitableHashTableSize(initialSize)];
			nextTable = new int[initialSize];
		} else {
			hashTable = null;
			nextTable = null;
		}
	}

	@Override
	public Object clone() {
		HashTable newTable = null;
		try {
			newTable = (HashTable) super.clone();
		} catch (CloneNotSupportedException e) {
			// Shouldn't happen because object supports clone.
			return null;
		}

		int size = capacity();

		if (hashTable != null) {
			newTable.hashTable = new int[getSuitableHashTableSize(size)];
			newTable.nextTable = new int[size];
			System.arraycopy(hashTable, 0, newTable.hashTable, 0, hashTable.length);
			System.arraycopy(nextTable, 0, newTable.nextTable, 0, nextTable.length);
		}
		newTable.currEntry = currEntry;
		return newTable;
	}

	protected void resize() {
		resize(capacity() << 1);
	}

	public void clear() {
		currEntry = -1;

		// Clear the table.
		if (hashTable == null)
			return;

		Arrays.fill(hashTable, 0);
		Arrays.fill(nextTable, 0);
	}

	protected void rehash() {
		if (nextTable == null)
			return;

		// Clear the table (don't call clear() or else the subclasses stuff will be cleared too).
		Arrays.fill(hashTable, 0);
		Arrays.fill(nextTable, 0);
		// Need to rehash everything.
		for (int i = 0; i <= currEntry; ++i) {
			linkIntoHashTable(i, hash(i));
		}
	}

	protected void resize(int size) {
		if (size >= MIN_HASH_SIZE) {
			hashTable = new int[getSuitableHashTableSize(size)];
			nextTable = new int[size];

			// Need to rehash everything.
			for (int i = 0; i <= currEntry; ++i) {
				linkIntoHashTable(i, hash(i));
			}
		}
	}

	private static int getSuitableHashTableSize(int size) {
		size += (size + 2) / 3;
		if (size < 0)
			return Integer.MAX_VALUE; // Integer overflow. Return the max possible size.
		int low = 0;
		int high = PRIMES.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			int p = PRIMES[mid];
			if (p < size) {
				low = mid + 1;
			} else if (p > size) {
				high = mid;
			} else {
				return p;
			}
		}
		if (low == PRIMES.length)
			return Integer.MAX_VALUE; // Largest prime is not sufficient. Return the max possible size.

		return PRIMES[low];
	}

	protected int hash(int pos) {
		// Return the hash value of the element in the key table.
		throw new UnsupportedOperationException();
	}

	final int hashToOffset(int hash) {
		int offset = hash % hashTable.length;
		if (offset < 0)
			offset += hashTable.length - 1;
		return offset;
	}

	protected final void linkIntoHashTable(int i, int hash) {
		if (nextTable == null)
			return;

		int j = hashTable[hash];
		if (j == 0) {
			hashTable[hash] = i + 1;
		} else {
			// Need to link.
			j--;
			int maxIterationsLeft = nextTable.length;
			for (int k; (k = nextTable[j]) != 0 && k != j + 1; j = k - 1) {
				if (--maxIterationsLeft < 0) {
					throw new IllegalStateException("i = " + i + " hash = " + hash + " j = " + j //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ "\n  nextTable = " + Arrays.toString(nextTable) //$NON-NLS-1$
							+ "\n  hashTable = " + Arrays.toString(hashTable)); //$NON-NLS-1$
				}
			}
			nextTable[j] = i + 1;
		}
	}

	public final int capacity() {
		if (nextTable == null)
			return MIN_HASH_SIZE - 1;
		return nextTable.length;
	}

	protected void removeEntry(int i, int hash) {
		if (nextTable == null) {
			--currEntry;
			return;
		}

		// Remove the hash entry.
		if (hashTable[hash] == i + 1) {
			hashTable[hash] = nextTable[i];
		} else {
			// Find entry pointing to me.
			int j = hashTable[hash] - 1;
			int k;
			while ((k = nextTable[j]) != 0 && k != i + 1) {
				j = k - 1;
			}
			nextTable[j] = nextTable[i];
		}

		if (i < currEntry) {
			// Shift everything over.
			System.arraycopy(nextTable, i + 1, nextTable, i, currEntry - i);

			// Adjust hash and next entries for things that moved.
			for (int j = 0; j < hashTable.length; ++j) {
				int k = hashTable[j] - 1;
				if (k > i)
					hashTable[j] = k;
			}

			for (int j = 0; j < nextTable.length; ++j) {
				int k = nextTable[j] - 1;
				if (k > i)
					nextTable[j] = k;
			}
		}

		// Last entry is now free.
		nextTable[currEntry] = 0;
		--currEntry;
	}

	public final void sort(Comparator<Object> c) {
		if (size() > 1) {
			quickSort(c, 0, size() - 1);
			rehash();
		}
	}

	private void quickSort(Comparator<Object> c, int p, int r) {
		if (p < r) {
			int q = partition(c, p, r);
			if (p < q)
				quickSort(c, p, q);
			if (++q < r)
				quickSort(c, q, r);
		}
	}

	protected int partition(Comparator<Object> c, int p, int r) {
		throw new UnsupportedOperationException();
	}

	/**
	 * For debugging only.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void dumpNexts() {
		if (nextTable == null)
			return;

		for (int i = 0; i < nextTable.length; ++i) {
			if (nextTable[i] == 0)
				continue;

			System.out.print(i);

			for (int j = nextTable[i] - 1; j >= 0; j = nextTable[j] - 1)
				System.out.print(" -> " + j); //$NON-NLS-1$

			System.out.println(""); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the number of collisions.
	 * For debugging only.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public int countCollisions() {
		if (nextTable == null)
			return 0;

		int numCollisions = 0;
		for (int i = 0; i < nextTable.length; ++i) {
			if (nextTable[i] != 0)
				numCollisions++;
		}
		return numCollisions;
	}
}
