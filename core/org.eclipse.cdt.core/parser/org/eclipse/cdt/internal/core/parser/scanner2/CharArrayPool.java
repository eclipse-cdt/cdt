/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author dschaefe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CharArrayPool {

	private int currEntry = -1;
	
	// Hash table is twice the size of the others
	private int[] hashTable;
	private int[] nextTable;
	private char[][] stringTable;
	
	public CharArrayPool(int tableSize) {
		// make sure size is a power of 2
		int size = 1;
		while (size < tableSize)
			size <<= 1;
		
		hashTable = new int[size << 1];
		nextTable = new int[tableSize];
		stringTable = new char[tableSize][];
	}
	
	private final int hash(char[] source, int start, int length) {
		return CharArrayUtils.hash(source, start, length) & (hashTable.length - 1);
	}

	private static final boolean equals(char[] str1, int start, int length, char[] str2) {
		if (str2.length != length)
			return false;
		
		int curr = start;
		for (int i = 0; i < length; ++i)
			if (str1[curr++] != str2[i])
				return false;
		
		return true;
	}
	
	private final char[] find(char[] source, int start, int length, int hash) {
		int i = hashTable[hash] - 1;
		
		do {
			char[] str = stringTable[i];
			if (equals(source, start, length, str))
				return str;
			i = nextTable[i] - 1;
		} while (i >= 0);
		
		return null;
	}

	// Removes the current entry
	private final void remove() {
		int hash = hash(stringTable[currEntry], 0, stringTable[currEntry].length);
		int i = hashTable[hash] - 1;
		if (i == currEntry)
			// make my next the hash entry
			hashTable[hash] = nextTable[currEntry];
		else {
			// remove me from the next list
			int last;
			do {
				last = i;
				i = nextTable[i] - 1;
			} while (i != currEntry);
			
			nextTable[last] = nextTable[currEntry];
		}
		
		stringTable[currEntry] = null;
		nextTable[currEntry] = 0;
	}
	
	private final void addHashed(char[] str, int hash) {
		// First remove the existing string if there is one
		if (++currEntry == stringTable.length)
			currEntry = 0;
		
		if (stringTable[currEntry] != null)
			remove();
		
		stringTable[currEntry] = str;

		// Now add it to the hash table, insert into next entries as necessary
		if (hashTable[hash] != 0)
			nextTable[currEntry] = hashTable[hash];
		hashTable[hash] = currEntry + 1;
	}
	
	public final char[] add(char[] source, int start, int length) {
		// do we have it already?
		int hash = hash(source, start, length);
		char[] result = null;
		if (hashTable[hash] > 0)
			result = find(source, start, length, hash);

		// if not, add it
		if (result == null) {
			System.arraycopy(source, 0, result = new char[length], 0, length);
			addHashed(result, hash);
		}
		
		return result;
	}
	
	public final char[] add(char[] source) {
		return add(source, 0, source.length);
	}
}
