/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.codemanipulation;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class StubUtility {
	
	/**
	 * @see org.eclipse.jdt.ui.CodeGeneration#getTypeComment(ICompilationUnit, String, String)
	 */	
	public static String getHeaderFileContent(ITranslationUnit cu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {
		return ""; //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.jdt.ui.CodeGeneration#getTypeComment(ICompilationUnit, String, String)
	 */	
	public static String getBodyFileContent(ITranslationUnit cu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {
		return ""; //$NON-NLS-1$
	}
	/*
	 * @see org.eclipse.jdt.ui.CodeGeneration#getTypeComment(ICompilationUnit, String, String)
	 */		
	public static String getClassComment(ITranslationUnit cu, String typeQualifiedName, String lineDelim) throws CoreException {
		return ""; //$NON-NLS-1$
	}	
}
