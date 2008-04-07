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
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache for checking whether a file exists. The cache shall be used for a limited amount of time, only (e.g. one 
 * indexer task). It uses as much memory as it needs. To protect against OutOfMemory situations, a soft reference is
 * used.
 * @since 5.0
 */
public final class FileExistsCache {
	private static final String[] EMPTY_STRING_ARRAY= {};
	private static final boolean CASE_INSENSITIVE = new File("a").equals(new File("A")); //$NON-NLS-1$ //$NON-NLS-2$
	private static boolean BYPASS_CACHE= Boolean.getBoolean("CDT_INDEXER_BYPASS_FILE_EXISTS_CACHE"); //$NON-NLS-1$

	private Reference<Map<String,String[]>> fCache= null;
		
	public FileExistsCache() {
		fCache= new SoftReference<Map<String,String[]>>(new HashMap<String, String[]>());	// before running out of memory the entire map will be thrown away.
	}
	
	public boolean exists(String path) {
		File file= new File(path);
		if (BYPASS_CACHE) {
			return file.exists();
		}
		
		String parent= file.getParent();
		String name= file.getName();
		if (CASE_INSENSITIVE)
			name= name.toUpperCase();
		
		String[] avail= getExistsCache().get(parent); 
		if (avail == null) {
			avail= new File(parent).list();
			if (avail == null || avail.length == 0) {
				avail= EMPTY_STRING_ARRAY;
			}
			else {
				if (CASE_INSENSITIVE) {
					for (int i = 0; i < avail.length; i++) {
						avail[i]= avail[i].toUpperCase();
					}
				}
				Arrays.sort(avail);
			}
			getExistsCache().put(parent, avail);
		}
		return Arrays.binarySearch(avail, name) >= 0;
	}

	private Map<String, String[]> getExistsCache() {
		Map<String, String[]> cache= fCache.get();
		if (cache == null) {
			cache= new HashMap<String, String[]>();
			fCache= new SoftReference<Map<String, String[]>>(cache); // before running out of memory the entire map will be thrown away.
		}
		return cache;
	}
}
