/**********************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.prefix;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;

public class BasicCompletionTest extends CompletionTestBase {

	private void testVar(IASTCompletionNode node) throws Exception {
		IASTName[] names = node.getNames();
		assertEquals(1, names.length);
		IBinding[] bindings = names[0].getCompletionContext().findBindings(
				names[0], true);
		assertEquals(1, bindings.length);
		IVariable var = (IVariable)bindings[0];
		assertEquals("blah", var.getName());
	}
	
	public void testVar() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("int blah = 4;");
		code.append("int two = bl");
		testVar(getGPPCompletionNode(code.toString()));
		testVar(getGCCCompletionNode(code.toString()));
	}

	public void testFunction() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("void func(int x) { }");
		code.append("void func2() { fu");
		
		// C++
		IASTCompletionNode node = getGPPCompletionNode(code.toString());
		IASTName[] names = node.getNames();
		// There are three names, one as an expression, one that isn't connected, one as a declaration
		assertEquals(3, names.length);
		// The expression points to our functions
		IBinding[] bindings = names[0].getCompletionContext().findBindings(
				names[0], true);
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
		// The second name shouldn't be hooked up
		assertNull(names[1].getTranslationUnit());
		// The third name shouldn't be hooked up either
		assertNull(names[2].getTranslationUnit());

		// C
		node = getGCCCompletionNode(code.toString());
		names = node.getNames();
		// There are two names, one as an expression, one as a declaration
		assertEquals(2, names.length);
		// The expression points to our functions
		bindings = sortBindings(names[0].getCompletionContext().findBindings(
				names[0], true));
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
		// The second name shouldn't be hooked up
		assertNull(names[1].getTranslationUnit());
	}

	public void testTypedef() throws Exception {
		StringBuffer code = new StringBuffer();
		code.append("typedef int blah;");
		code.append("bl");
		
		// C++
		IASTCompletionNode node = getGPPCompletionNode(code.toString());
		IASTName[] names = node.getNames();
		assertEquals(2, names.length);
		assertNull(names[0].getTranslationUnit());
		IBinding[] bindings = names[1].getCompletionContext().findBindings(
				names[1], true);
		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef)bindings[0]).getName());
		
		// C
		node = getGCCCompletionNode(code.toString());
		names = node.getNames();
		assertEquals(1, names.length);
		bindings = names[0].getCompletionContext().findBindings(names[0], true);
		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef)bindings[0]).getName());
	}
	
}
