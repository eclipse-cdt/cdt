/*******************************************************************************
 * Copyright (c) 2005 Rational Software Corporation and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v0.5 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: Rational Software - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;

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
	 * Creates a new CodeReader and returns it.
	 * @param key
	 * @return
	 */
	public CodeReader get(String key) {
		CodeReader ret = null;
		ret = InternalParserUtil.createFileReader(key);
		return ret;
	}
	
	/**
	 * This provides support for PartialWorkingCopyCodeReaderFactory.
	 * @param finalPath
	 * @param workingCopies
	 * @return
	 */
	public CodeReader createReader( String finalPath, Iterator workingCopies ) {
		return InternalParserUtil.createFileReader(finalPath);
	}

	/**
	 * Returns null.
	 * @param key
	 * @return
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

}
