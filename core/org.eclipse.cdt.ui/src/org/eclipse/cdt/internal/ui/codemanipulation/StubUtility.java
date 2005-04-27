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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;

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

	/**
	 * Examines a string and returns the first line delimiter found.
	 */
	public static String getLineDelimiterUsed(ICElement elem) throws CModelException {
        if (elem == null) return ""; //$NON-NLS-1$
        
		ITranslationUnit cu= (ITranslationUnit) elem.getAncestor(ICElement.C_UNIT);
		if (cu != null && cu.exists()) {
			IBuffer buf= cu.getBuffer();
			int length= buf.getLength();
			for (int i= 0; i < length; i++) {
				char ch= buf.getChar(i);
				if (ch == SWT.CR) {
					if (i + 1 < length) {
						if (buf.getChar(i + 1) == SWT.LF) {
							return "\r\n"; //$NON-NLS-1$
						}
					}
					return "\r"; //$NON-NLS-1$
				} else if (ch == SWT.LF) {
					return "\n"; //$NON-NLS-1$
				}
			}
		}
		return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}


}
