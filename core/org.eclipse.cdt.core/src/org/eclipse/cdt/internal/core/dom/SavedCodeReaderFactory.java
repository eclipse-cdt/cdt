/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;

/**
 * @author jcamelon
 */
public class SavedCodeReaderFactory implements ICodeReaderFactory {

	private ICodeReaderCache cache = null;
	
    public static SavedCodeReaderFactory getInstance()
    {
        return instance;
    }
    
    private static SavedCodeReaderFactory instance = new SavedCodeReaderFactory();
    
    private SavedCodeReaderFactory()
    {
		int size=0;
		if (CCorePlugin.getDefault() == null || CCorePlugin.getDefault().getPluginPreferences() == null)
			size = CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB;
		else
			size = CCorePlugin.getDefault().getPluginPreferences().getInt(CodeReaderCache.CODE_READER_BUFFER);
		 
		if (size >= 0)
			cache = new CodeReaderCache(size);
		else
			cache = new CodeReaderCache(CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return CDOM.PARSE_SAVED_RESOURCES;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
     */
    public CodeReader createCodeReaderForTranslationUnit(String path) {
		return cache.get(path);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
		return cache.get(path);
    }
	
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
	public ICodeReaderCache getCodeReaderCache() {
		return cache;
	}
}
