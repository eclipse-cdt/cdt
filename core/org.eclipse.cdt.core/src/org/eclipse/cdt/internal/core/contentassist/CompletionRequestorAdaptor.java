/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.contentassist;

import org.eclipse.cdt.core.ICompletionRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompletionRequestorAdaptor implements ICompletionRequestor {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptField(java.lang.String, java.lang.String)
	 */
	public void acceptField(String name, String returnType, ASTAccessVisibility visibility, int completionStart, int completionLength, int relevance) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptClass(java.lang.String)
	 */
	public void acceptClass(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptEnumeration(java.lang.String)
	 */
	public void acceptEnumeration(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptFunction(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void acceptFunction(
		String name,
		String parameterString,
		String returnType, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptMacro(java.lang.String)
	 */
	public void acceptMacro(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptMethod(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void acceptMethod(
		String name,
		String parameterString,
		String returnType, ASTAccessVisibility visibility, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptStruct(java.lang.String)
	 */
	public void acceptStruct(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptUnion(java.lang.String)
	 */
	public void acceptUnion(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptVariable(java.lang.String, java.lang.String)
	 */
	public void acceptVariable(String name, String returnType, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptLocalVariable(java.lang.String, java.lang.String)
	 */
	public void acceptLocalVariable(String name, String returnType, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptNamespace(java.lang.String)
	 */
	public void acceptNamespace(String name, int completionStart, int completionLength, int relevance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptEnumerator(java.lang.String, int, int, int)
	 */
	public void acceptEnumerator(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
		// TODO Auto-generated method stub

	}

}
