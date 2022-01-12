/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.internal.core.util.OverflowingLRUCache;

/**
 * This class is a wrapper/implementor class for OverflowingLRUCache.
 *
 * It uses CodeReaderCacheEntry (which implements ILRUCacheable) to specify that the size of
 * the cache should be relative to the size of the entries and not the number of entries.
 * @deprecated
 */
@Deprecated
public class CodeReaderLRUCache extends OverflowingLRUCache<String, CodeReaderCacheEntry> {

	/**
	 * Creates a new CodeReaderLRUCache with a specified initial maximum size.
	 * @param size the maximum size of the cache in terms of MB
	 */
	public CodeReaderLRUCache(int size) {
		super(); // need to initialize the LRUCache with super() so that the size of the hashtable isn't relative to the size in MB
		this.setSpaceLimit(size);
	}

	// must be overloaded, required to remove entries from the cache
	@Override
	protected boolean close(LRUCacheEntry<String, CodeReaderCacheEntry> entry) {
		Object obj = remove(entry._fKey);

		if (obj != null)
			return true;

		return false;
	}

	@Override
	protected OverflowingLRUCache<String, CodeReaderCacheEntry> newInstance(int size, int overflow) {
		return null;
	}

	/**
	 * Removes an entry from the cache and returns the entry that was removed if found.
	 * Otherwise null is returned.
	 */
	@Override
	public CodeReader remove(String key) {
		Object removed = removeKey(key);

		if (removed instanceof CodeReaderCacheEntry)
			return ((CodeReaderCacheEntry) removed).getCodeReader();

		return null;
	}

	/**
	 * Puts a CodeReader into the cache by wrapping it with a CodeReaderCacheEntry first.
	 * This way the proper size of the element in the cache can be determined
	 * via the CodeReaderCacheEntry.
	 */
	public CodeReader put(String key, CodeReader value) {
		CodeReaderCacheEntry entry = new CodeReaderCacheEntry(value);
		CodeReaderCacheEntry ret = put(key, entry);
		if (ret != null)
			return ret.getCodeReader();

		return null;
	}

	/**
	 * Retrieves a CodeReader from the cache corresponding to the path specified by the key.
	 */
	public CodeReader get(String key) {
		CodeReaderCacheEntry obj = peek(key);
		if (obj != null)
			return obj.getCodeReader();

		return null;
	}

}