/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

import java.util.List;

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
		IBinding[] bindings = names[0].getCompletionContext().findBindings(names[0], true);
		assertEquals(1, bindings.length);
		IVariable var = (IVariable)bindings[0];
		assertEquals("blah", var.getName());
	}
	
	public void testVar() throws Exception {
		String code = 
			"int blah = 4;" +
			"int two = bl";
		
		testVar(getGPPCompletionNode(code));
		testVar(getGCCCompletionNode(code));
	}

	public void testFunction() throws Exception {
		String code =
			"void func(int x) { }" +
			"void func2() { fu";
		
		// C++
		IASTCompletionNode node = getGPPCompletionNode(code);
		IASTName[] names = node.getNames();
		// There are two names, one as an expression, one that isn't connected, one as a declaration
		assertTrue(names.length > 1);
		// The expression points to our functions
		IBinding[] bindings = names[0].getCompletionContext().findBindings(names[0], true);
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
		// The other names shouldn't be hooked up
		for (int i = 1; i < names.length; i++) {
			assertNull(names[i].getTranslationUnit());
		}

		// C
		node = getGCCCompletionNode(code);
		names = node.getNames();
		// There are two names, one as an expression, one as a declaration
		assertTrue(names.length > 1);
		// The expression points to our functions
		bindings = sortBindings(names[0].getCompletionContext().findBindings(names[0], true));
		// There should be two since they both start with fu
		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction)bindings[0]).getName());
		assertEquals("func2", ((IFunction)bindings[1]).getName());
		// The other names shouldn't be hooked up
		for (int i = 1; i < names.length; i++) {
			assertNull(names[i].getTranslationUnit());
		}
	}

	public void testTypedef() throws Exception {
		String code = 
			"void test() {typedef int blah;" +
			"bl";
		
		// C++
		IASTCompletionNode node = getGPPCompletionNode(code);
		IASTName[] names = node.getNames();
		assertEquals(2, names.length);
		assertNull(names[1].getTranslationUnit());
		IBinding[] bindings = names[0].getCompletionContext().findBindings(names[0], true);
		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef)bindings[0]).getName());
		
		// C
		node = getGCCCompletionNode(code);
		names = node.getNames();
		assert(names.length > 0);
		bindings = names[0].getCompletionContext().findBindings(names[0], true);
		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef)bindings[0]).getName());
	}
	
	public void testBug181624() throws Exception {
		String code = 
			"void foo() {" +
			"  switch (";
		
		// C++
		IASTCompletionNode node = getGPPCompletionNode(code);
		assertNotNull(node);
		
		// C
		node = getGCCCompletionNode(code);
		assertNotNull(node);
		
		code = 
			"void foo() {" +
			"  while (";
		
		// C++
		node = getGPPCompletionNode(code);
		assertNotNull(node);
		
		// C
		node = getGCCCompletionNode(code);
		assertNotNull(node);
	}
	
	//	template <typename T> class CT {};
	//	template <typename T> class B: public A<T> {
	//	public: 
	//       void doit(){}
	//	};
	//	int main() {
	//	   B<int> b;
	//	   b.
	public void testBug267911() throws Exception {
		String code = getAboveComment();
		IASTCompletionNode node = getGPPCompletionNode(code);
		assertNotNull(node);
		List<IBinding> bindings= proposeBindings(node);
		String[] names= getSortedNames(bindings);
		assertEquals("B", names[0]);
		assertEquals("doit", names[1]);
	}
}
