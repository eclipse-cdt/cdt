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

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EmptyCodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class PartialWorkingCopyCodeReaderFactory 
        implements ICodeReaderFactory {

    private final IWorkingCopyProvider provider;
	private ICodeReaderCache cache = null;

    /**
     * @param provider
     */
    public PartialWorkingCopyCodeReaderFactory(IWorkingCopyProvider provider) {
        this.provider = provider;
		cache = new EmptyCodeReaderCache();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return CDOM.PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
     */
    public CodeReader createCodeReaderForTranslationUnit(String path) {
        return ((EmptyCodeReaderCache)cache).createReader( path, createWorkingCopyIterator() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
        return cache.get( path );
    }

    /**
     * @return
     */
    protected Iterator createWorkingCopyIterator() {
        if( provider == null ) return EmptyIterator.EMPTY_ITERATOR;
        return Arrays.asList( provider.getWorkingCopies() ).iterator();
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getCodeReaderCache()
     */
	public ICodeReaderCache getCodeReaderCache() {
		return cache;
	}

}
