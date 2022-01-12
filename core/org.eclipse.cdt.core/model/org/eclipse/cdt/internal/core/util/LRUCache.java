/*******************************************************************************
 * Copyright (c) 2002, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The <code>LRUCache</code> is a hash table that stores a finite number of elements.
 * When an attempt is made to add values to a full cache, the least recently used values
 * in the cache are discarded to make room for the new values as necessary.
 *
 * <p>The data structure is based on the LRU virtual memory paging scheme.
 *
 * <p>Objects can take up a variable amount of cache space by implementing
 * the <code>ILRUCacheable</code> interface.
 *
 * <p>This implementation is NOT thread-safe.  Synchronization wrappers would
 * have to be added to ensure atomic insertions and deletions from the cache.
 *
 * @see ILRUCacheable
 *
 * This class is similar to the JDT LRUCache class.
 */
public class LRUCache<K, T> implements Cloneable {

	/**
	 * This type is used internally by the LRUCache to represent entries
	 * stored in the cache.
	 * It is static because it does not require a pointer to the cache
	 * which contains it.
	 *
	 * @see LRUCache
	 */
	protected static class LRUCacheEntry<K, T> {
		/**
		 * Hash table key
		 */
		public K _fKey;

		/**
		 * Hash table value (an LRUCacheEntry object)
		 */
		public T _fValue;

		/**
		 * Time value for queue sorting
		 */
		public int _fTimestamp;

		/**
		 * Cache footprint of this entry
		 */
		public int _fSpace;

		/**
		 * Previous entry in queue
		 */
		public LRUCacheEntry<K, T> _fPrevious;

		/**
		 * Next entry in queue
		 */
		public LRUCacheEntry<K, T> _fNext;

		/**
		 * Creates a new instance of the receiver with the provided values
		 * for key, value, and space.
		 */
		public LRUCacheEntry(K key, T value, int space) {
			_fKey = key;
			_fValue = value;
			_fSpace = space;
		}

		/**
		 * Returns a String that represents the value of this object.
		 */
		@Override
		public String toString() {
			return "LRUCacheEntry [" + _fKey + "-->" + _fValue + "]"; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Amount of cache space used so far
	 */
	protected int fCurrentSpace;

	/**
	 * Maximum space allowed in cache
	 */
	protected int fSpaceLimit;

	/**
	 * Counter for handing out sequential timestamps
	 */
	protected int fTimestampCounter;

	/**
	 * Hash table for fast random access to cache entries
	 */
	protected Hashtable<K, LRUCacheEntry<K, T>> fEntryTable;

	/**
	 * Start of queue (most recently used entry)
	 */
	protected LRUCacheEntry<K, T> fEntryQueue;

	/**
	 * End of queue (least recently used entry)
	 */
	protected LRUCacheEntry<K, T> fEntryQueueTail;

	/**
	 * Default amount of space in the cache
	 */
	protected static final int DEFAULT_SPACELIMIT = 100;

	/**
	 * Creates a new cache.  Size of cache is defined by
	 * <code>DEFAULT_SPACELIMIT</code>.
	 */
	public LRUCache() {
		this(DEFAULT_SPACELIMIT);
	}

	/**
	 * Creates a new cache.
	 * @param size Size of Cache
	 */
	public LRUCache(int size) {
		fTimestampCounter = fCurrentSpace = 0;
		fEntryQueue = fEntryQueueTail = null;
		fEntryTable = new Hashtable<>(size);
		fSpaceLimit = size;
	}

	/**
	 * Returns a new cache containing the same contents.
	 *
	 * @return New copy of object.
	 */
	@Override
	public Object clone() {
		LRUCache<K, T> newCache = newInstance(fSpaceLimit);
		LRUCacheEntry<K, T> qEntry;

		/* Preserve order of entries by copying from oldest to newest */
		qEntry = this.fEntryQueueTail;
		while (qEntry != null) {
			newCache.privateAdd(qEntry._fKey, qEntry._fValue, qEntry._fSpace);
			qEntry = qEntry._fPrevious;
		}
		return newCache;
	}

	/**
	 * Flushes all entries from the cache.
	 */
	public void flush() {
		fCurrentSpace = 0;
		LRUCacheEntry<K, T> entry = fEntryQueueTail; // Remember last entry
		fEntryTable = new Hashtable<>(); // Clear it out
		fEntryQueue = fEntryQueueTail = null;
		while (entry != null) { // send deletion notifications in LRU order
			privateNotifyDeletionFromCache(entry);
			entry = entry._fPrevious;
		}
	}

	/**
	 * Flushes the given entry from the cache.  Does nothing if entry does not
	 * exist in cache.
	 *
	 * @param key Key of object to flush
	 */
	public void flush(Object key) {
		LRUCacheEntry<K, T> entry = fEntryTable.get(key);
		/* If entry does not exist, return */
		if (entry == null) {
			return;
		}

		this.privateRemoveEntry(entry, false);
	}

	/**
	 * Answers the value in the cache at the given key.
	 * If the value is not in the cache, returns null
	 *
	 * @param key Hash table key of object to retrieve
	 * @return Retrieved object, or null if object does not exist
	 */
	public Object get(Object key) {
		LRUCacheEntry<K, T> entry = fEntryTable.get(key);
		if (entry == null) {
			return null;
		}

		this.updateTimestamp(entry);
		return entry._fValue;
	}

	/**
	 * Returns the amount of space that is current used in the cache.
	 */
	public int getCurrentSpace() {
		return fCurrentSpace;
	}

	/**
	 * Returns the maximum amount of space available in the cache.
	 */
	public int getSpaceLimit() {
		return fSpaceLimit;
	}

	/**
	 * Returns an Enumeration of the keys currently in the cache.
	 */
	public Enumeration<K> keys() {
		return fEntryTable.keys();
	}

	/**
	 * Tests if this cache is empty.
	 */
	public boolean isEmpty() {
		return fEntryTable.isEmpty();
	}

	/**
	 * Ensures there is the specified amount of free space in the receiver,
	 * by removing old entries if necessary.  Returns true if the requested space was
	 * made available, false otherwise.
	 *
	 * @param space Amount of space to free up
	 */
	protected boolean makeSpace(int space) {
		int limit = this.getSpaceLimit();

		/* if space is already available */
		if (fCurrentSpace + space <= limit) {
			return true;
		}

		/* if entry is too big for cache */
		if (space > limit) {
			return false;
		}

		/* Free up space by removing oldest entries */
		while (fCurrentSpace + space > limit && fEntryQueueTail != null) {
			this.privateRemoveEntry(fEntryQueueTail, false);
		}
		return true;
	}

	/**
	 * Returns a new LRUCache instance
	 */
	protected LRUCache<K, T> newInstance(int size) {
		return new LRUCache<>(size);
	}

	/**
	 * Adds an entry for the given key/value/space.
	 */
	protected void privateAdd(K key, T value, int space) {
		LRUCacheEntry<K, T> entry = new LRUCacheEntry<>(key, value, space);
		this.privateAddEntry(entry, false);
	}

	/**
	 * Adds the given entry from the receiver.
	 * @param shuffle Indicates whether we are just shuffling the queue
	 * (i.e., the entry table is left alone).
	 */
	protected void privateAddEntry(LRUCacheEntry<K, T> entry, boolean shuffle) {
		if (!shuffle) {
			fEntryTable.put(entry._fKey, entry);
			fCurrentSpace += entry._fSpace;
		}

		entry._fTimestamp = fTimestampCounter++;
		entry._fNext = this.fEntryQueue;
		entry._fPrevious = null;

		if (fEntryQueue == null) {
			/* this is the first and last entry */
			fEntryQueueTail = entry;
		} else {
			fEntryQueue._fPrevious = entry;
		}

		fEntryQueue = entry;
	}

	/**
	 * An entry has been removed from the cache, for example because it has
	 * fallen off the bottom of the LRU queue.
	 * Subclasses could over-ride this to implement a persistent cache below the LRU cache.
	 */
	protected void privateNotifyDeletionFromCache(LRUCacheEntry<K, T> entry) {
		// Default is NOP.
	}

	/**
	 * Removes the entry from the entry queue.
	 * @param shuffle indicates whether we are just shuffling the queue
	 * (i.e., the entry table is left alone).
	 */
	protected void privateRemoveEntry(LRUCacheEntry<K, T> entry, boolean shuffle) {
		LRUCacheEntry<K, T> previous = entry._fPrevious;
		LRUCacheEntry<K, T> next = entry._fNext;

		if (!shuffle) {
			fEntryTable.remove(entry._fKey);
			fCurrentSpace -= entry._fSpace;
			privateNotifyDeletionFromCache(entry);
		}

		/* if this was the first entry */
		if (previous == null) {
			fEntryQueue = next;
		} else {
			previous._fNext = next;
		}

		/* if this was the last entry */
		if (next == null) {
			fEntryQueueTail = previous;
		} else {
			next._fPrevious = previous;
		}
	}

	/**
	 * Sets the value in the cache at the given key. Returns the value.
	 *
	 * @param key Key of object to add.
	 * @param value Value of object to add.
	 * @return added value.
	 */
	public T put(K key, T value) {
		int oldSpace, newTotal;

		/* Check whether there's an entry in the cache */
		int newSpace = spaceFor(key, value);
		LRUCacheEntry<K, T> entry = fEntryTable.get(key);

		if (entry != null) {
			/*
			 * Replace the entry in the cache if it would not overflow
			 * the cache.  Otherwise flush the entry and re-add it so as
			 * to keep cache within budget
			 */
			oldSpace = entry._fSpace;
			newTotal = getCurrentSpace() - oldSpace + newSpace;
			if (newTotal <= getSpaceLimit()) {
				updateTimestamp(entry);
				entry._fValue = value;
				entry._fSpace = newSpace;
				this.fCurrentSpace = newTotal;
				return value;
			}
			privateRemoveEntry(entry, false);
		}
		if (makeSpace(newSpace)) {
			privateAdd(key, value, newSpace);
		}
		return value;
	}

	/**
	 * Removes and returns the value in the cache for the given key.
	 * If the key is not in the cache, returns null.
	 *
	 * @param key Key of object to remove from cache.
	 * @return Value removed from cache.
	 */
	public T removeKey(K key) {
		LRUCacheEntry<K, T> entry = fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		T value = entry._fValue;
		this.privateRemoveEntry(entry, false);
		return value;
	}

	/**
	 * Sets the maximum amount of space that the cache can store
	 *
	 * @param limit Number of units of cache space
	 */
	public void setSpaceLimit(int limit) {
		if (limit < fSpaceLimit) {
			makeSpace(fSpaceLimit - limit);
		}
		fSpaceLimit = limit;
	}

	/**
	 * Returns the space taken by the given key and value.
	 */
	protected int spaceFor(Object key, Object value) {
		if (value instanceof ILRUCacheable) {
			return ((ILRUCacheable) value).getCacheFootprint();
		}
		return 1;
	}

	/**
	 * Returns a String that represents the value of this object.  This method
	 * is for debugging purposes only.
	 */
	@Override
	public String toString() {
		return "LRUCache " + (fCurrentSpace * 100.0 / fSpaceLimit) + "% full\n" + //$NON-NLS-1$ //$NON-NLS-2$
				this.toStringContents();
	}

	/**
	 * Returns a String that represents the contents of this object.  This method
	 * is for debugging purposes only.
	 */
	protected String toStringContents() {
		StringBuilder result = new StringBuilder();
		int length = fEntryTable.size();
		Object[] unsortedKeys = new Object[length];
		String[] unsortedToStrings = new String[length];
		Enumeration<K> e = this.keys();
		for (int i = 0; i < length; i++) {
			Object key = e.nextElement();
			unsortedKeys[i] = key;
			unsortedToStrings[i] = (key instanceof org.eclipse.cdt.internal.core.model.CElement)
					? ((org.eclipse.cdt.internal.core.model.CElement) key).getElementName()
					: key.toString();
		}
		ToStringSorter sorter = new ToStringSorter();
		sorter.sort(unsortedKeys, unsortedToStrings);
		for (int i = 0; i < length; i++) {
			String toString = sorter.sortedStrings[i];
			Object value = this.get(sorter.sortedObjects[i]);
			result.append(toString);
			result.append(" -> "); //$NON-NLS-1$
			result.append(value);
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}

	/**
	 * Updates the timestamp for the given entry, ensuring that the queue is
	 * kept in correct order.  The entry must exist
	 */
	protected void updateTimestamp(LRUCacheEntry<K, T> entry) {
		entry._fTimestamp = fTimestampCounter++;
		if (fEntryQueue != entry) {
			this.privateRemoveEntry(entry, true);
			this.privateAddEntry(entry, true);
		}
		return;
	}
}
