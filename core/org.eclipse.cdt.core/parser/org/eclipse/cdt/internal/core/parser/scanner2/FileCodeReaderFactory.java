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
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;

/**
 * @author jcamelon
 */
public class FileCodeReaderFactory implements ICodeReaderFactory {

    private static FileCodeReaderFactory instance;

    private FileCodeReaderFactory()
    {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    public int getUniqueIdentifier() {
        return 3;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
     */
    public CodeReader createCodeReaderForTranslationUnit(String path) {
        return InternalParserUtil.createFileReader(path);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    public CodeReader createCodeReaderForInclusion(String path) {
        return InternalParserUtil.createFileReader(path);
    }

    /**
     * @return
     */
    public static FileCodeReaderFactory getInstance() {
        if( instance == null )
            instance = new FileCodeReaderFactory();
        return instance;
    }

}
