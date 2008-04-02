/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.util.WeakHashSet;

/**
 * A limited LRU cache for looking up files in an include search path.
 * @since 5.0
 */
public final class IncludeFileResolutionCache {
	public static class ISPKey  {
		private String[] fISP;
		private int fHashCode;

		private ISPKey(String[] isp) {
			fISP= isp;
			fHashCode= Arrays.hashCode(isp);
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			return obj != null && Arrays.equals(fISP, ((ISPKey) obj).fISP);
		}

		public int hashCode() {
			return fHashCode;
		}
	}
	
	public static class LookupKey {
		private ISPKey fCanonicISP;
		private char[] fName;
		private int fHashCode;

		private LookupKey(ISPKey ispKey, char[] include) {
			fCanonicISP= ispKey;
			fName= include;
			fHashCode= Arrays.hashCode(include) * 31 + ispKey.hashCode();
		}
		
		public int hashCode() {
			return fHashCode;
		}

		public boolean equals(Object obj) {
			LookupKey other= (LookupKey) obj;
			if (fCanonicISP != other.fCanonicISP)
				return false;
			if (!Arrays.equals(fName, other.fName))
				return false;
			return true;
		}
	}

	private WeakHashSet fCanonicISPs;
	private LinkedHashMap fCache;
		
	/**
	 * Creates a cache for include file resolution using up to the given amount of memory
	 * @param maxSizeKBytes the maximum size of the cache in kilobytes
	 */
	public IncludeFileResolutionCache(final int maxSizeKBytes) {
		final int size= maxSizeKBytes*1024/72; // HashEntry 32 bytes, Key 16 bytes, Name 16 bytes, Integer 8 bytes 
		fCache= new LinkedHashMap(size, 0.75f, true) {
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > size;
			}
		};
		fCanonicISPs= new WeakHashSet();
	}
	
	public ISPKey getKey(String[] isp) {
		return (ISPKey) fCanonicISPs.add(new ISPKey(isp));
	}

	public LookupKey getKey(ISPKey ispKey, char[] filename) {
		return new LookupKey(ispKey, filename);
	}

	public Integer getCachedPathOffset(LookupKey key) {
        return (Integer) fCache.get(key);
	}

	public void putCachedPathOffset(LookupKey key, Integer offset) {
		fCache.put(key, offset);
	}
}
