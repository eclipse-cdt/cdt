/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.internal.core.util.ILRUCacheable;

/**
 * This is a wrapper for entries to put into the OverflowingLRUCache (required to determine the
 * size of entries relative to the CodeReader's file size).
 * 
 * Although the size of the CodeReaderCache is specified in terms of MB, the actual granularity of
 * the cache is KB.
 * @deprecated
 */
@Deprecated
class CodeReaderCacheEntry implements ILRUCacheable {

	private static final double CHAR_TO_KB_FACTOR = 1024;
	CodeReader reader = null;
	int size = 0; // used to specify the size of the CodeReader in terms of KB

	public CodeReaderCacheEntry(CodeReader value) {
		this.reader = value;
		size = (int)Math.ceil(reader.buffer.length / CHAR_TO_KB_FACTOR); // get the size of the file in terms of KB 
	}

	@Override
	public int getCacheFootprint() {
		return size;
	}
	
	public CodeReader getCodeReader() {
		return reader;
	}
}