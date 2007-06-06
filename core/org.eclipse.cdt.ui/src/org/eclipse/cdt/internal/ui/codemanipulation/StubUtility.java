/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.codemanipulation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class StubUtility {
	
	/**
	 * Not implemented, returns <code>""</code>.
	 */	
	public static String getHeaderFileContent(ITranslationUnit cu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {
		return ""; //$NON-NLS-1$
	}
	/**
	 * Not implemented, returns <code>""</code>.
	 */	
	public static String getBodyFileContent(ITranslationUnit cu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {
		return ""; //$NON-NLS-1$
	}
	/**
	 * Not implemented, returns <code>""</code>.
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

	/**
	 * Get the default task tag for the given project.
	 * 
	 * @param project
	 * @return the default task tag
	 */
	public static String getTodoTaskTag(ICProject project) {
		String markers= null;
		if (project == null) {
			markers= CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_TAGS);
		} else {
			markers= project.getOption(CCorePreferenceConstants.TODO_TASK_TAGS, true);
		}
		
		if (markers != null && markers.length() > 0) {
			int idx= markers.indexOf(',');
			if (idx == -1) {
				return markers;
			} else {
				return markers.substring(0, idx);
			}
		}
		return CCorePreferenceConstants.DEFAULT_TASK_TAG;
	}
	
}
