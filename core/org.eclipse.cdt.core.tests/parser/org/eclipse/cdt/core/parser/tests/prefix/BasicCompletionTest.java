/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.prefix;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;

public class BasicCompletionTest extends CompletionTestBase {

	public void testVar() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("int blah = 4;");
		code.append("int two = bl");
		ASTCompletionNode node = getGPPCompletionNode(code.toString());
		IASTName[] names = node.getNames();
		assertEquals(1, names.length);
		IBinding[] bindings = names[0].resolvePrefix();
		assertEquals(1, bindings.length);
		IVariable var = (IVariable)bindings[0];
		assertEquals("blah", var.getName());
	}

	public void testFunction() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("void func(int x);");
		code.append("void func2() { fu");
		ASTCompletionNode node = getGPPCompletionNode(code.toString());
		IASTName[] names = node.getNames();
		// There are two names, one as an expression, one as a declaration
		assertEquals(2, names.length);
		// The declaration name is not hooked up to the TU due to backtrack
		assertNull(names[1].getTranslationUnit());
		// The points to our functions
		IBinding[] bindings = names[0].resolvePrefix();
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
	}

}
