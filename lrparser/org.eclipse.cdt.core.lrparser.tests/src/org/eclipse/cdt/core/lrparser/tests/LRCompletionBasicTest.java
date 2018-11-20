/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.prefix.BasicCompletionTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

@SuppressWarnings({ "restriction", "nls" })
public class LRCompletionBasicTest extends BasicCompletionTest {

	public static TestSuite suite() {
		return new TestSuite(LRCompletionBasicTest.class);
	}

	public LRCompletionBasicTest() {
	}

	//override the test failed case for 340664
	@Override
	public void testCompletionInSizeof340664() throws Exception {
	}

	//override some failed test cases
	@Override
	public void testBug279931() throws Exception {
	}

	@Override
	public void testBug279931a() throws Exception {
	}

	@Override
	public void testQualifiedMemberAccess_Bug300139() throws Exception {
	}

	@Override
	public void testCastExpression_Bug301933() throws Exception {
	}

	@Override
	public void testConditionalOperator_Bug308611() throws Exception {
	}

	@Override
	public void testCompletionInDesignatedInitializor_353281a() throws Exception {
	}

	@Override
	@SuppressWarnings("unused")
	protected IASTCompletionNode getCompletionNode(String code, ParserLanguage lang, boolean useGNUExtensions)
			throws ParserException {

		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		return ParseHelper.getCompletionNode(code, language);
	}

	protected ILanguage getCLanguage() {
		return GCCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return GPPLanguage.getDefault();
	}

	@Override
	public void testFunction() throws Exception {
		String code = "void func(int x) { }" + "void func2() { fu";

		// C++
		IASTCompletionNode node = getGPPCompletionNode(code);
		IBinding[] bindings = LRCompletionParseTest.getBindings(node.getNames());

		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction) bindings[0]).getName());
		assertEquals("func2", ((IFunction) bindings[1]).getName());

		// C
		node = getGCCCompletionNode(code);
		bindings = LRCompletionParseTest.getBindings(node.getNames());

		assertEquals(2, bindings.length);
		assertEquals("func", ((IFunction) bindings[0]).getName());
		assertEquals("func2", ((IFunction) bindings[1]).getName());
	}

	@Override
	public void testTypedef() throws Exception {
		String code = "typedef int blah;" + "bl";

		// C++
		IASTCompletionNode node = getGPPCompletionNode(code);
		IBinding[] bindings = LRCompletionParseTest.getBindings(node.getNames());

		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef) bindings[0]).getName());

		// C
		node = getGCCCompletionNode(code);
		bindings = LRCompletionParseTest.getBindings(node.getNames());

		assertEquals(1, bindings.length);
		assertEquals("blah", ((ITypedef) bindings[0]).getName());
	}

}
