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
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.IOException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.runtime.CoreException;

/**
 * This is the interface to a cache for CodeReaders.
 *
 * For thread safety the implementations of this interface must ensure that their methods are thread safe.
 *
 * @deprecated
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface ICodeReaderCache {
	/**
	 * Retrieves the CodeReader corresponding to the key specified that represents the
	 * path for that CodeReader.  If no CodeReader is found in the cache then a new CodeReader
	 * is created for the path and then returned.
	 *
	 * @param key the path corresponding to the CodeReader, generally:
	 * fileToParse.getLocation().toOSString()
	 * @return the CodeReader corresponding to the path specified by the key
	 */
	public CodeReader get(String key);

	/**
	 * Retrieves the CodeReader corresponding to the key specified that represents the
	 * path for that CodeReader.  If no CodeReader is found in the cache then a new CodeReader
	 * is created for the ifl and then returned.
	 *
	 * @param key the path corresponding to the CodeReader, generally:
	 * fileToParse.getLocation().toOSString()
	 * @return the CodeReader corresponding to the path specified by the key
	 * @throws IOException
	 * @throws CoreException
	 * @since 5.1
	 */
	public CodeReader get(String key, IIndexFileLocation ifl) throws CoreException, IOException;

	/**
	 * Used to remove the CodeReader corresponding to the path specified by key from the cache.
	 *
	 * @param key the path of the CodeReader to be removed
	 * @return the removed CodeReader or null if not found
	 */
	public CodeReader remove(String key);

	/**
	 * Returns the amount of space that the cache is using.
	 * The units are relative to the implementation of the cache.  It could be
	 * the total number of objects in the cache, or the total space the cache is
	 * using in MB.
	 */
	public int getCurrentSpace();

	/**
	 *
	 */
	public void flush();
}
