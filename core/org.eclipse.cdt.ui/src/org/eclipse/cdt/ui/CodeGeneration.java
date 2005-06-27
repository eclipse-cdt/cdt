/*******************************************************************************
 * Copyright (c) 2001, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.codemanipulation.StubUtility;
import org.eclipse.core.runtime.CoreException;

/**
 * Class that offers access to the templates contained in the 'code generation' preference page.
 * 
 * @since 2.1
 */
public class CodeGeneration {

	private CodeGeneration() {
	}

	/**
	 * Returns the content for a new compilation unit using the 'new Java file' code template.
	 * @param cu The compilation to create the source for. The compilation unit does not need to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains a ${typecomment} variable. Can be <code>null</code> if
	 * no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getHeaderFileContent(ITranslationUnit tu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {	
		return StubUtility.getHeaderFileContent(tu, typeComment, typeContent, lineDelimiter);
	}

	/**
	 * Returns the content for a new compilation unit using the 'new Java file' code template.
	 * @param cu The compilation to create the source for. The compilation unit does not need to exist.
	 * @param typeComment The comment for the type to created. Used when the code template contains a ${typecomment} variable. Can be <code>null</code> if
	 * no comment should be added.
	 * @param typeContent The code of the type, including type declaration and body.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the template is undefined or empty.
	 * @throws CoreException
	 */
	public static String getBodyFileContent(ITranslationUnit tu, String typeComment, String typeContent, String lineDelimiter) throws CoreException {	
		return StubUtility.getBodyFileContent(tu, typeComment, typeContent, lineDelimiter);
	}

	/**
	 * Returns the content for a new type comment using the 'typecomment' code template. The returned content is unformatted and is not indented.
	 * @param cu The compilation where the type is contained. The compilation unit does not need to exist.
	 * @param typeQualifiedName The name of the type to which the comment is added. For inner types the name must be qualified and include the outer
	 * types names (dot separated). See {@link org.eclipse.jdt.core.IType#getTypeQualifiedName(char)}.
	 * @param lineDelimiter The line delimiter to be used.
	 * @return Returns the new content or <code>null</code> if the code template is undefined or empty. The returned content is unformatted and is not indented.
	 * @throws CoreException
	 */	
	public static String getClassComment(ITranslationUnit tu, String typeQualifiedName, String lineDelimiter) throws CoreException {
		return StubUtility.getClassComment(tu, typeQualifiedName, lineDelimiter);
	}
	
}
