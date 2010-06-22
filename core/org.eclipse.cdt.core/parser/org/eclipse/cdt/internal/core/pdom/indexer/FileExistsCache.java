/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache for checking whether a file exists. The cache shall be used for a limited amount of time, only (e.g. one 
 * indexer task). It uses as much memory as it needs. To protect against OutOfMemory situations, a soft reference is
 * used.
 * @since 5.0
 */
public final class FileExistsCache {
	private static final Content EMPTY_STRING_ARRAY= new Content(new String[0]);
	private static final boolean CASE_INSENSITIVE = new File("a").equals(new File("A")); //$NON-NLS-1$ //$NON-NLS-2$
	private static boolean BYPASS_CACHE= Boolean.getBoolean("CDT_INDEXER_BYPASS_FILE_EXISTS_CACHE"); //$NON-NLS-1$

	private static class Content {
		public Content(String[] names) {
			fNames= names;
			fIsFile= new BitSet(names.length*2);
		}
		public String[] fNames;
		public BitSet fIsFile;
	}
	private Reference<Map<String,Content>> fCache= null;
	private final boolean fCaseInSensitive;

	public FileExistsCache() {
		this(CASE_INSENSITIVE);
	}

	public FileExistsCache(boolean caseInsensitive) {
		fCaseInSensitive= caseInsensitive;
		fCache= new SoftReference<Map<String,Content>>(new HashMap<String, Content>());	// before running out of memory the entire map will be thrown away.
	}
	
	public boolean isFile(String path) {
		File file= new File(path);
		if (BYPASS_CACHE) {
			return file.isFile();
		}
		
		String parent= file.getParent();
		if (parent == null)
			return false;
		
		String name= file.getName();
		if (fCaseInSensitive)
			name= name.toUpperCase();
		
		Content avail= getExistsCache().get(parent); 
		if (avail == null) {
			String[] files= new File(parent).list();
			if (files == null || files.length == 0) {
				avail= EMPTY_STRING_ARRAY;
			}
			else {
				if (fCaseInSensitive) {
					for (int i = 0; i < files.length; i++) {
						files[i]= files[i].toUpperCase();
					}
				}
				Arrays.sort(files);
				avail= new Content(files);
			}
			getExistsCache().put(parent, avail);
		}
		int idx= Arrays.binarySearch(avail.fNames, name);
		if (idx < 0)
			return false;
		idx *= 2;
		
		final BitSet isFileBitset = avail.fIsFile;
		if (isFileBitset.get(idx))
			return true;
		if (isFileBitset.get(idx+1))
			return false;
		
		if (file.isFile()) {
			isFileBitset.set(idx);
			return true;
		}
		isFileBitset.set(idx+1);
		return false;
	}

	private Map<String, Content> getExistsCache() {
		Map<String, Content> cache= fCache.get();
		if (cache == null) {
			cache= new HashMap<String, Content>();
			fCache= new SoftReference<Map<String, Content>>(cache); // before running out of memory the entire map will be thrown away.
		}
		return cache;
	}
}
