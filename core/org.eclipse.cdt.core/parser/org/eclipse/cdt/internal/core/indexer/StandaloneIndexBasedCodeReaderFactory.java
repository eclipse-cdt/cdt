/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
 
package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IMacroCollector;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.index.IndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.filesystem.URIUtil;

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
public class StandaloneIndexBasedCodeReaderFactory extends IndexBasedCodeReaderFactory {

	public static class DefaultFallBackFactory implements ICodeReaderFactory {

		public CodeReader createCodeReaderForInclusion(IMacroCollector callback, String path) {
			try {
				if (!new File(path).exists())
					return null;
				return new CodeReader(path);
			} catch (IOException e) {
				return null;
			}
		}

		public CodeReader createCodeReaderForTranslationUnit(String path) {
			try {
				if (!new File(path).exists())
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
	
	public StandaloneIndexBasedCodeReaderFactory(IIndex index) {
		super(null, index);
	}
	
	public StandaloneIndexBasedCodeReaderFactory(IIndex index, ICodeReaderFactory fallbackFactory) {
		super(null, index, new HashMap/*<String,IIndexFileLocation>*/(), fallbackFactory);
	}
	
	public StandaloneIndexBasedCodeReaderFactory(IIndex index, Map iflCache) {
		super(null, index, iflCache, new DefaultFallBackFactory());
	}

	public IIndexFileLocation findLocation(String absolutePath) {
		IIndexFileLocation result = (IIndexFileLocation) getIFLCache().get(absolutePath); 
		if(result==null) {
			//Standalone indexing stores the absolute paths of files being indexed
			result = new IndexFileLocation(URIUtil.toURI(absolutePath),absolutePath); 
			getIFLCache().put(absolutePath, result);
		}
		return result;
	}
}
