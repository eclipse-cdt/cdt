/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

/**
 * A cache for checking whether a file exists. The cache shall be used for a limited amount of time,
 * only (e.g. one indexer task). It uses as much memory as it needs. To protect against OutOfMemory
 * situations, a soft reference is used.
 * @since 5.0
 */
public final class FileExistsCache {
	private static final Content EMPTY_STRING_ARRAY= new Content(new String[0]);
	private static boolean BYPASS_CACHE= Boolean.getBoolean("CDT_INDEXER_BYPASS_FILE_EXISTS_CACHE"); //$NON-NLS-1$

	private static class Content {
		public Content(String[] names) {
			fNames= names;
			fIsFile= new BitSet(names.length * 2);
		}
		public String[] fNames;
		public BitSet fIsFile;
	}

	private Reference<Map<String, Content>> fCache;
	private final boolean fCaseInSensitive;

	public FileExistsCache(boolean caseInsensitive) {
		fCaseInSensitive= caseInsensitive;
		Map<String, Content> cache = new HashMap<>();
		// Before running out of memory the entire map will be thrown away.
		fCache= new SoftReference<>(cache);
	}

	public boolean isFile(String path) {
		String parent;
		String name;
		File file = null;
		IFileStore parentStore = null;
		IFileStore fileStore = null;

		if (UNCPathConverter.isUNC(path)) {
			try {
				URI uri = UNCPathConverter.getInstance().toURI(path);
				fileStore = EFS.getStore(uri);
				if (BYPASS_CACHE) {
					return fileStore != null && !fileStore.fetchInfo().isDirectory();
				}
				parentStore = fileStore.getParent();
				if (parentStore == null) {
					parentStore = fileStore;
				}
				parent = parentStore.toURI().toString();
				name = fileStore.getName();
			} catch (CoreException e) {
				return false;
			}
		} else {
			file= new File(path);
			if (BYPASS_CACHE) {
				return file.isFile();
			}

			parent= file.getParent();
			if (parent == null)
				return false;

			name= file.getName();
		}
		if (fCaseInSensitive)
			name= name.toUpperCase();

		Content avail= getExistsCache().get(parent);
		if (avail == null) {
			String[] files = null;
			try {
				files = parentStore == null ? new File(parent).list() : parentStore.childNames(EFS.NONE, null);
			} catch (CoreException e) {
				// Ignore
			}
			if (files == null || files.length == 0) {
				avail= EMPTY_STRING_ARRAY;
			} else {
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
		if (isFileBitset.get(idx + 1))
			return false;

		if ((file != null && file.isFile()) || (fileStore != null && !fileStore.fetchInfo().isDirectory())) {
			isFileBitset.set(idx);
			return true;
		}
		isFileBitset.set(idx + 1);
		return false;
	}

	private Map<String, Content> getExistsCache() {
		Map<String, Content> cache= fCache.get();
		if (cache == null) {
			cache= new HashMap<>();
			// Before running out of memory the entire map will be thrown away.
			fCache= new SoftReference<>(cache);
		}
		return cache;
	}
}
