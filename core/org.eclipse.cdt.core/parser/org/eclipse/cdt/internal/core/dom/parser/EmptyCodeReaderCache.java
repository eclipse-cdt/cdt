/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.io.IOException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * This is an empty implementation of the ICodeReaderCache interface.  It is used to implement a 
 * cache for the interface that isn't actually a cache, but rather always creates new CodeReaders
 * every time a CodeReader is retrieved. 
 * 
 * This cache is not optimized to be run from within Eclipse (i.e. it ignores IResources).
 */
public class EmptyCodeReaderCache implements ICodeReaderCache {

	/**
	 * Creates a new CodeReader for the given file location.
	 */
	public CodeReader get(String location) {
		try {
			return new CodeReader(location);
		} catch (IOException e) {
		}
		return null;
	}
	
	public CodeReader get(String key, IIndexFileLocation ifl) throws CoreException, IOException {
		return InternalParserUtil.createCodeReader(ifl, null);
	}

	/**
	 * Returns null.
	 */
	public CodeReader remove(String key) {
		return null;
	}

	/**
	 * Returns 0.
	 */
	public int getCurrentSpace() {
		return 0;
	}

	public void flush() {
		// nothing to do
		
	}
}
