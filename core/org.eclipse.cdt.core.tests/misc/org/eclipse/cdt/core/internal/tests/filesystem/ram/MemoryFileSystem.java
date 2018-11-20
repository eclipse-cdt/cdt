/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
 *     Andrew Gvozdev (Quoin Inc.) - contributed to CDT from org.eclipse.core.tests.resources v20090320
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests.filesystem.ram;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A test file system that keeps everything in memory.
 */
public class MemoryFileSystem extends FileSystem {
	public static final String SCHEME_MEMORY = "mem";

	/**
	 * Converts a path to a URI in the memory file system.
	 * @param path
	 * @return
	 */
	public static URI toURI(IPath path) {
		try {
			return new URI(MemoryFileSystem.SCHEME_MEMORY, path.setDevice(null).toPortableString(), null);
		} catch (URISyntaxException e) {
			//should not happen
			throw new RuntimeException(e);
		}
	}

	public MemoryFileSystem() {
		super();
	}

	@Override
	public IFileStore getStore(URI uri) {
		return new MemoryFileStore(Path.fromPortableString(uri.getSchemeSpecificPart()));
	}

	@Override
	public boolean isCaseSensitive() {
		return true;
	}
}
