/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.BitSet;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;

import junit.framework.TestSuite;

/**
 * Testcases for inactive code in ast.
 */
public class ASTInactiveCodeTests extends AST2TestBase {

	public static TestSuite suite() {
		return suite(ASTInactiveCodeTests.class);
	}

	public ASTInactiveCodeTests() {
		super();
	}

	public ASTInactiveCodeTests(String name) {
		super(name);
	}

	@Override
	protected void configureScanner(IScanner scanner) {
		super.configureScanner(scanner);
		scanner.setProcessInactiveCode(true);
	}

	//	#if %
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	public void testIfBranches() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.C, i);
		}
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i);
		}
	}

	private void testBranches(String codeTmpl, ParserLanguage lang, int bits) throws Exception {
		testBranches(codeTmpl, lang, bits, 0);
	}

	private void testBranches(String codeTmpl, ParserLanguage lang, int bits, int level) throws Exception {
		BitSet bs = convert(bits);
		char[] chars = codeTmpl.toCharArray();
		int pos = codeTmpl.indexOf('%', 0);
		int i = 0;
		while (pos >= 0) {
			chars[pos] = bs.get(i++) ? '1' : '0';
			pos = codeTmpl.indexOf('%', pos + 1);
		}
		IASTDeclarationListOwner tu = parse(new String(chars), lang);
		while (level-- > 0) {
			final IASTDeclaration decl = tu.getDeclarations(true)[0];
			if (decl instanceof IASTSimpleDeclaration) {
				tu = (IASTDeclarationListOwner) ((IASTSimpleDeclaration) decl).getDeclSpecifier();
			} else {
				tu = (IASTDeclarationListOwner) decl;
			}
		}

		IASTDeclaration[] decl = tu.getDeclarations(true);
		assertEquals(8, decl.length);
		assertEquals(bs.get(0), decl[0].isActive());
		assertEquals(!bs.get(0) && bs.get(1), decl[1].isActive());
		assertEquals(!bs.get(0) && bs.get(1) && bs.get(2), decl[2].isActive());
		assertEquals(!bs.get(0) && bs.get(1) && !bs.get(2) && bs.get(3), decl[3].isActive());
		assertEquals(!bs.get(0) && bs.get(1) && !bs.get(2) && !bs.get(3), decl[4].isActive());
		assertEquals(!bs.get(0) && bs.get(1), decl[5].isActive());
		assertEquals(!bs.get(0) && !bs.get(1), decl[6].isActive());
		assertEquals(true, decl[7].isActive());
	}

	private BitSet convert(int bits) {
		BitSet result = new BitSet(32);
		for (int i = 0; i < 32; i++) {
			if ((bits & (1 << i)) != 0) {
				result.set(i);
			}
		}
		return result;
	}

	//  #define A1
	//	#ifdef A%
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	public void testIfdefBranches() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.C, i);
		}
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i);
		}
	}

	//  #define A0
	//	#ifndef A%
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	public void testIfndefBranches() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.C, i);
		}
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i);
		}
	}

	// struct S {
	//	#if %
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	// };
	public void testStructs() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.C, i, 1);
		}
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i, 1);
		}
	}

	// extern "C" {
	//	#if %
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	// };
	public void testExternC() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i, 1);
		}
	}

	// namespace ns {
	//	#if %
	//	   int a0;
	//	#elif %
	//	   int a1;
	//	   #if %
	//	      int a2;
	//	   #elif %
	//	      int a3;
	//	   #else
	//	      int a4;
	//	   #endif
	//	   int a5;
	//	#else
	//	   int a6;
	//	#endif
	//	int a7;
	// }
	public void testNamespace() throws Exception {
		String codeTmpl = getAboveComment();
		for (int i = 0; i < (1 << 4); i++) {
			testBranches(codeTmpl, ParserLanguage.CPP, i, 1);
		}
	}

	// typedef int TInt;
	// const int value= 12;
	// #if 0
	//    int f(TInt);
	//    int g(value);
	// #endif
	public void testAmbiguity() throws Exception {
		String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, ParserLanguage.CPP);
		IASTDeclaration[] decls = tu.getDeclarations(true);
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) decls[2];
		assertTrue(decl.getDeclarators()[0] instanceof IASTFunctionDeclarator);
		decl = (IASTSimpleDeclaration) decls[3];
		assertFalse(decl.getDeclarators()[0] instanceof IASTFunctionDeclarator);
	}

	// int a; // 1
	// #if 0
	//    int a; // 2
	//    #if 0
	//       int a; // 3
	//    #endif
	//    int b; // 1
	// #endif
	// int b; // 2
	public void testDuplicateDefinition() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, false);
		bh.assertNonProblem("a; // 1", 1);
		bh.assertNonProblem("a; // 2", 1);
		bh.assertNonProblem("a; // 3", 1);
		bh.assertNonProblem("b; // 1", 1);
		bh.assertNonProblem("b; // 2", 1);

		bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("a; // 1", 1);
		bh.assertNonProblem("a; // 2", 1);
		bh.assertNonProblem("a; // 3", 1);
		bh.assertNonProblem("b; // 1", 1);
		bh.assertNonProblem("b; // 2", 1);

		parseAndCheckBindings(code, ParserLanguage.C);
		parseAndCheckBindings(code, ParserLanguage.CPP);
	}

	// struct S {
	// #if 0
	//   int a;
	// };
	// #else
	//   int b;
	// };
	// #endif
	public void testInactiveClosingBrace() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, false);
		IField a = bh.assertNonProblem("a;", 1);
		IField b = bh.assertNonProblem("b;", 1);
		assertSame(a.getOwner(), b.getOwner());

		bh = new AST2AssertionHelper(code, true);
		a = bh.assertNonProblem("a;", 1);
		b = bh.assertNonProblem("b;", 1);
		assertSame(a.getOwner(), b.getOwner());
	}

	// struct S
	// #if 1
	//   {
	//     int a;
	// #else
	//   int b;
	// #endif
	//   int c;
	//   #if 0
	//      int d;
	//   #endif
	// };
	public void testOpenBraceInActiveBranch() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, false);
		IField a = bh.assertNonProblem("a;", 1);
		bh.assertNoName("b;", 1);
		IField c = bh.assertNonProblem("c;", 1);
		IField d = bh.assertNonProblem("d;", 1);
		assertSame(a.getOwner(), c.getOwner());
		assertSame(a.getOwner(), d.getOwner());

		bh = new AST2AssertionHelper(code, true);
		a = bh.assertNonProblem("a;", 1);
		bh.assertNoName("b;", 1);
		c = bh.assertNonProblem("c;", 1);
		d = bh.assertNonProblem("d;", 1);
		assertSame(a.getOwner(), c.getOwner());
		assertSame(a.getOwner(), d.getOwner());
	}

	// #if 0
	//    struct S {
	//    #if 1
	//      int a;
	//    #else
	//      int b;
	//    #endif
	// #elif 0
	//    int c;
	// #endif
	// int d;
	public void testOpenBraceInInactiveBranch() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, false);
		IField a = bh.assertNonProblem("a;", 1);
		IField b = bh.assertNonProblem("b;", 1);
		IVariable c = bh.assertNonProblem("c;", 1); // part of a different non-nested branch
		IVariable d = bh.assertNonProblem("d;", 1);
		assertSame(a.getOwner(), b.getOwner());
		assertNull(c.getOwner());
		assertNull(d.getOwner());

		bh = new AST2AssertionHelper(code, true);
		a = bh.assertNonProblem("a;", 1);
		b = bh.assertNonProblem("b;", 1);
		c = bh.assertNonProblem("c;", 1); // part of a different non-nested branch
		d = bh.assertNonProblem("d;", 1);
		assertSame(a.getOwner(), b.getOwner());
		assertNull(c.getOwner());
		assertNull(d.getOwner());
	}

	//	 #if 0
	//	 void f() {
	//	   #if 1
	//		 int a;
	//	   #elif 0
	//		 int b;
	//	   #endif
	//	 }
	//	 #endif
	public void testUnexpectedBranchesInInactiveCode() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, false);
		IFunction f = bh.assertNonProblem("f()", 1);
		bh.assertNoName("a;", 1);
		bh.assertNoName("b;", 1);

		bh = new AST2AssertionHelper(code, true);
		f = bh.assertNonProblem("f()", 1);
		bh.assertNoName("a;", 1);
		bh.assertNoName("b;", 1);
	}
}
