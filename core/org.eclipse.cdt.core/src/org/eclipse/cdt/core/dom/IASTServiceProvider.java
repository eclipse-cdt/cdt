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
 * @author jcamelon
 */
public interface IASTServiceProvider {
    
    public static class UnsupportedDialectException extends Exception 
    {
    }

    public IASTTranslationUnit getTranslationUnit( IFile fileToParse) throws UnsupportedDialectException;
    
    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator  )throws UnsupportedDialectException;

    public IASTTranslationUnit getTranslationUnit( IFile fileToParse, ICodeReaderFactory fileCreator, IParserConfiguration configuration )throws UnsupportedDialectException;

    public ASTCompletionNode getCompletionNode( IFile fileToParse, int offset, ICodeReaderFactory fileCreator) throws UnsupportedDialectException;
    
}
