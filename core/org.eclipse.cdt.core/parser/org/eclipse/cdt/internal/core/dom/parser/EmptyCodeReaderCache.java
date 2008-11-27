/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * This is an empty implementation of the ICodeReaderCache interface.  It is used to implement a 
 * cache for the interface that isn't actually a cache, but rather always creates new CodeReaders
 * everytime a CodeReader is retrieved. 
 * 
 * This cache is not optimized to be run from within Eclipse (i.e. it ignores IResources).
 * 
 * @author dsteffle
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
