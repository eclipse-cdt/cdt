/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser;

/**
 * @author dschaefe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CharArrayMap {

	private char[][] keyTable;
	private Object[] valueTable;
	private int[] hashTable;
	private int[] nextTable;

	private int currEntry;
	
	public CharArrayMap(int initialSize) {
		// Make sure size is a power of two
		int size = 1;
		while (size < initialSize)
			size <<= 1;
		createTables(size);
	}
	
	private final void createTables(int size) {
		keyTable = new char[size][];
		valueTable = new Object[size];
		nextTable = new int[size];
		hashTable = new int[size << 1];
		currEntry = 0;
	}
	
	private final int hash(char[] key) {
		return CharArrayUtils.hash(key) & (hashTable.length - 1);
	}

	private final int add(char[] key, Object value) {
		keyTable[currEntry] = key;
		valueTable[currEntry] = value;
		return ++currEntry;
	}
	
	// returns the overwritten object if there was one
	public Object put(char[] key, Object value) {
		try {
			int hash = hash(key);
			int i = hashTable[hash] - 1;
			if (i < 0) {
				// Nobody here
				hashTable[hash] = add(key, value);
			} else {
				// See if the key is already defined
				int last = i;
				while (i >= 0) {
					if (CharArrayUtils.equals(key, keyTable[i])) {
						Object oldvalue = valueTable[i];
						valueTable[i] = value;
						// Nothing left to do, escape...
						return oldvalue;
					}
					last = i;
					i = nextTable[i] - 1;
				}
				
				// Not there, time to add
				nextTable[last] = add(key, value);
			}
			
			return null;
		} catch (IndexOutOfBoundsException e) {
			// Oops, too many, resize and try again
			char[][] oldKeyTable = keyTable;
			Object[] oldValueTable = valueTable;
			
			int newSize = hashTable.length << 1;
			createTables(newSize);
			for (int i = 0; i < oldKeyTable.length; ++i)
				put(oldKeyTable[i], oldValueTable[i]);
			
			return put(key, value);
		}
	}
	
	public Object get(char[] key) {
		int hash = hash(key);
		int i = hashTable[hash] - 1;
		while (i >= 0) {
			if (CharArrayUtils.equals(key, keyTable[i]))
				return valueTable[i];
			i = nextTable[i] - 1;
		}
		return null;
	}
}
