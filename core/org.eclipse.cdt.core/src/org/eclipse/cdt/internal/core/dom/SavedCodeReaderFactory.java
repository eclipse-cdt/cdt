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

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class SavedCodeReaderFactory implements ICodeReaderFactory {

    public static SavedCodeReaderFactory getInstance()
    {
        return instance;
    }
    
    private static SavedCodeReaderFactory instance = new SavedCodeReaderFactory();
    
    private SavedCodeReaderFactory()
    {
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
        return ParserUtil.createReader( path, EmptyIterator.EMPTY_ITERATOR );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
        return ParserUtil.createReader( path, EmptyIterator.EMPTY_ITERATOR );
    }

}
