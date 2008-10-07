/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
 
package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * A factory for CodeReaders construction.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public class StandaloneIndexerFallbackReaderFactory implements ICodeReaderFactory {

	public CodeReader createCodeReaderForInclusion(String path) {
		try {
			if (!new File(path).isFile())
				return null;
			return new CodeReader(path);
		} catch (IOException e) {
			return null;
		}
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		try {
			if (!new File(path).isFile())
				return null;
			return new CodeReader(path);
		} catch (IOException e) {
			return null;
		}
	}

	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}

	public int getUniqueIdentifier() {
		return 0;
	}
}
