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

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;

/**
 * This is the mechanism that represents a parser service in the CDT.
 * 
 * IASTTranslationUnits and ASTCompletionNodes are artifacts that this service returns.
 * 
 * @author jcamelon
 */
public interface IASTServiceProvider {
    
    /**
     * This exception is thrown when there is not a service provider that can handle
     * the request due to dialect mis-match.  
     * 
     * @author jcamelon
     */
    public static class UnsupportedDialectException extends Exception 
    {
    	
    }

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question	
     * @return syntactical parse tree
     * @throws UnsupportedDialectException 
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse) throws UnsupportedDialectException;
    
    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator  )throws UnsupportedDialectException;

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @param configuration parser configuration provided rather than discovered by service
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration )throws UnsupportedDialectException;

    /**
     * Returns a parse tree that represents the content provided as parameters.
     * 
     * @param fileToParse the file in question
     * @param offset the offset at which you require completion at
     * @param fileCreator @see CDOM#getCodeReaderFactory(int)
     * @return syntactical parse tree
     * @throws UnsupportedDialectException
     */
    public ASTCompletionNode getCompletionNode( IFile fileToParse, int offset, ICodeReaderFactory fileCreator) throws UnsupportedDialectException;
    
}
