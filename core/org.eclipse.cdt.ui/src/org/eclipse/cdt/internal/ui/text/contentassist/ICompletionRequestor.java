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
package org.eclipse.cdt.internal.ui.text.contentassist;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public interface ICompletionRequestor {
	void acceptField(String name, String returnType, ASTAccessVisibility visibility, int completionStart, int completionLength, int relevance);
	void acceptVariable(String name, String returnType, int completionStart, int completionLength, int relevance);
	void acceptLocalVariable(String name, String returnType, int completionStart, int completionLength, int relevance);
	void acceptMethod(String name, String parameterString, String returnType, ASTAccessVisibility visibility, int completionStart, int completionLength, int relevance);
	void acceptFunction(String name, String parameterString, String returnType, int completionStart, int completionLength, int relevance);
	void acceptClass(String name, int completionStart, int completionLength, int relevance);
	void acceptStruct(String name, int completionStart, int completionLength, int relevance);
	void acceptUnion(String name, int completionStart, int completionLength, int relevance);
	void acceptNamespace(String name, int completionStart, int completionLength, int relevance);
	void acceptMacro(String name, int completionStart, int completionLength, int relevance);
	void acceptEnumeration(String name, int completionStart, int completionLength, int relevance);
	void acceptEnumerator(String name, int completionStart, int completionLength, int relevance);
	void acceptKeyword(String name, int completionStart, int completionLength, int relevance);
	void acceptError(IProblem error);
}
