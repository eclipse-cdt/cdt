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
package org.eclipse.cdt.core.dom;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.InternalASTServiceProvider;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.core.resources.IFile;

/**
 * @author jcamelon
 * 
 * This class serves as the manager of the AST/DOM mechanisms for the CDT.
 * It should be eventually added to CCorePlugin for startup.  
 */
public class CDOM implements IASTServiceProvider {
    
    private CDOM() 
    {
    }
    
    private static CDOM instance = new CDOM();
    public static CDOM getInstance()
    {
        return instance;
    }
    
    private IASTServiceProvider defaultService = new InternalASTServiceProvider();    

    
    public IASTServiceProvider getASTService() {
        return this;
    }
    

    public static final int PARSE_SAVED_RESOURCES = 0; 
    public static final int PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS = 1;
    public static final int PARSE_WORKING_COPY_WHENEVER_POSSIBLE = 2;
    
    public ICodeReaderFactory getCodeReaderFactory( int key )
    {
        switch( key )
        {
        	case PARSE_SAVED_RESOURCES: 
        	    return SavedCodeReaderFactory.getInstance();
        	case PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS:
        	    return null; //TODO
        	case PARSE_WORKING_COPY_WHENEVER_POSSIBLE:
        	    return null; //TODO
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile, org.eclipse.cdt.core.dom.ICodeReaderFactory)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse, fileCreator );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.IASTServiceProvider#getTranslationUnit(org.eclipse.core.resources.IFile, org.eclipse.cdt.core.dom.ICodeReaderFactory, org.eclipse.cdt.core.dom.IParserConfiguration)
     */
    public IASTTranslationUnit getTranslationUnit(IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration) throws UnsupportedDialectException {
        return defaultService.getTranslationUnit(fileToParse, fileCreator, configuration );
    }

}
