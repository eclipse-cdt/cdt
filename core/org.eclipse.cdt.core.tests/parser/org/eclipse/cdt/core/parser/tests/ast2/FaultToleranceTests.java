/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ParserLanguage;

import junit.framework.TestSuite;

/**
 * Testcases related to recovery from invalid syntax.
 */
public class FaultToleranceTests extends AST2TestBase {

	public static TestSuite suite() {
		return suite(FaultToleranceTests.class);
	}

	public FaultToleranceTests() {
		super();
	}

	public FaultToleranceTests(String name) {
		super(name);
	}

	// typedef int tint;
	// struct X {
	//    int a;
	// }
	// tint b;
	public void testCompositeTypeWithoutSemi() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTCompositeTypeSpecifier def = getCompositeType(tu, 1);
			IASTProblemDeclaration pdecl = getDeclaration(tu, 2);
			IASTSimpleDeclaration sdecl = getDeclaration(tu, 3);
		}
	}

	// typedef int tint;
	// struct X {
	//    int a;
	// } c
	// tint b;
	public void testCompositeTypeWithDtorWithoutSemi() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTSimpleDeclaration sdecl = getDeclaration(tu, 1);
			assertInstance(sdecl.getDeclSpecifier(), IASTCompositeTypeSpecifier.class);
			assertEquals(1, sdecl.getDeclarators().length);
			IASTProblemDeclaration pdecl = getDeclaration(tu, 2);
			sdecl = getDeclaration(tu, 3);
		}
	}

	// typedef int tint;
	// int a
	// tint b;
	public void testVariableWithoutSemi() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTSimpleDeclaration sdecl = getDeclaration(tu, 1);
			assertEquals("int a", sdecl.getRawSignature());
			IASTProblemDeclaration pdecl = getDeclaration(tu, 2);
			sdecl = getDeclaration(tu, 3);
		}
	}

	// typedef int tint;
	// int a()
	// tint b;
	public void testPrototypeWithoutSemi() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTSimpleDeclaration sdecl = getDeclaration(tu, 1);
			assertEquals("int a()", sdecl.getRawSignature());
			IASTProblemDeclaration pdecl = getDeclaration(tu, 2);
			sdecl = getDeclaration(tu, 3);
		}
	}

	// void f() {
	//   int a= 1
	// 	 f()
	// }
	public void testExpressionWithoutSemi_314593() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTFunctionDefinition fdef = getDeclaration(tu, 0);
			IASTStatement stmt = getStatement(fdef, 0);
			assertEquals("int a= 1", stmt.getRawSignature());
			IASTProblemStatement pstmt = getStatement(fdef, 1);
			stmt = getStatement(fdef, 2);
			assertEquals("f()", stmt.getRawSignature());
			pstmt = getStatement(fdef, 3);
		}
	}

	// struct X {
	//   int a;
	public void testIncompleteCompositeType() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTCompositeTypeSpecifier comp = getCompositeType(tu, 0);
			IASTProblemDeclaration pdecl = getDeclaration(tu, 1);

			IASTSimpleDeclaration sdecl = getDeclaration(comp, 0);
		}
	}

	// void func() {
	//   int a;
	public void testIncompleteFunctionDefinition() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTFunctionDefinition fdef = getDeclaration(tu, 0);
			IASTProblemDeclaration pdecl = getDeclaration(tu, 1);

			IASTDeclarationStatement sdecl = getStatement(fdef, 0);
		}
	}

	// namespace ns {
	//   int a;
	public void testIncompleteNamespace() throws Exception {
		final String comment = getAboveComment();
		IASTTranslationUnit tu = parse(comment, ParserLanguage.CPP, false, false);
		ICPPASTNamespaceDefinition ns = getDeclaration(tu, 0);
		IASTProblemDeclaration pdecl = getDeclaration(tu, 1);

		IASTSimpleDeclaration sdecl = getDeclaration(ns, 0);
	}

	// extern "C" {
	//   int a;
	public void testIncompleteLinkageSpec() throws Exception {
		final String comment = getAboveComment();
		IASTTranslationUnit tu = parse(comment, ParserLanguage.CPP, false, false);
		ICPPASTLinkageSpecification ls = getDeclaration(tu, 0);
		IASTProblemDeclaration pdecl = getDeclaration(tu, 1);

		IASTSimpleDeclaration sdecl = getDeclaration(ls, 0);
	}

	// void test() {
	//    int a= offsetof(struct mystruct, singlechar);
	// }
	public void testRangeOfProblemNode_Bug238151() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTFunctionDefinition fdef = getDeclaration(tu, 0);
			IASTProblemStatement pdecl = getStatement(fdef, 0);
			assertEquals("int a= offsetof(struct mystruct, singlechar);", pdecl.getRawSignature());
		}
	}

	// int f(){
	//    if( 12 A )
	//       return -1;
	//    int v;
	// }
	public void testProblemInIfExpression_Bug100321() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTFunctionDefinition fdef = getDeclaration(tu, 0);
			IASTIfStatement ifstmt = getStatement(fdef, 0);
			assertInstance(ifstmt.getConditionExpression(), IASTProblemExpression.class);
			assertEquals("12 A", ifstmt.getConditionExpression().getRawSignature());
			assertInstance(ifstmt.getThenClause(), IASTReturnStatement.class);
		}
	}

	// _MYMACRO_ myType foo();
	// _MYMACRO_ myType foo() {}
	// extern void foo2() _MYMACRO_;
	public void testUndefinedMacrosInFunctionDeclarations_Bug234085() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTProblemDeclaration pd = getDeclaration(tu, 0);
			assertEquals("_MYMACRO_", pd.getRawSignature());
			IASTSimpleDeclaration sdecl = getDeclaration(tu, 1);
			assertEquals("myType foo();", sdecl.getRawSignature());

			pd = getDeclaration(tu, 2);
			assertEquals("_MYMACRO_", pd.getRawSignature());
			IASTFunctionDefinition fdef = getDeclaration(tu, 3);
			assertEquals("myType foo() {}", fdef.getRawSignature());

			sdecl = getDeclaration(tu, 4);
			assertEquals("extern void foo2()", sdecl.getRawSignature());
			pd = getDeclaration(tu, 5); // the missing semicolon

			if (lang == ParserLanguage.CPP) {
				pd = getDeclaration(tu, 6);
				assertEquals("_MYMACRO_;", pd.getRawSignature());
			} else {
				sdecl = getDeclaration(tu, 6);
				assertEquals("_MYMACRO_;", sdecl.getRawSignature());
			}
		}
	}

	// enum _T { I J, K }; // missing comma
	// int i;
	public void testEnumProblem() throws Exception {
		final String comment = getAboveComment();
		for (ParserLanguage lang : ParserLanguage.values()) {
			IASTTranslationUnit tu = parse(comment, lang, false, false);
			IASTSimpleDeclaration e = getDeclaration(tu, 0);
			IASTProblemDeclaration p = getDeclaration(tu, 1);
			assertEquals("J, K };", p.getRawSignature());
			IASTSimpleDeclaration s = getDeclaration(tu, 2);
			assertEquals("int i;", s.getRawSignature());
		}
	}

	// class A {
	//    enum _T { I J, K }; // missing comma
	//    int i;
	// };
	public void testEnumError_Bug72685() throws Exception {
		final String comment = getAboveComment();
		IASTTranslationUnit tu = parse(comment, ParserLanguage.CPP, false, false);
		IASTCompositeTypeSpecifier ct = getCompositeType(tu, 0);
		IASTSimpleDeclaration e = getDeclaration(ct, 0);
		IASTProblemDeclaration p = getDeclaration(ct, 1);
		assertEquals("J, K };", p.getRawSignature());
		IASTSimpleDeclaration s = getDeclaration(ct, 2);
		assertEquals("int i;", s.getRawSignature());
	}

	//	#define XX() .
	//	int c;
	//	XX(
	//	);
	//	int d;
	public void testErrorRecovery_273759() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), ParserLanguage.C, false, false);
		IASTSimpleDeclaration s = getDeclaration(tu, 0);
		IASTProblemDeclaration p = getDeclaration(tu, 1);
		s = getDeclaration(tu, 2);

		tu = parse(getAboveComment(), ParserLanguage.CPP, false, false);
		s = getDeclaration(tu, 0);
		p = getDeclaration(tu, 1);
		s = getDeclaration(tu, 2);
	}

	//	    TINT* f(TINT* p) {
	//	    TINT* f1(TINT* p) {
	//	    TINT* f2(TINT* p) {
	//	    TINT* f3(TINT* p) {
	//	    TINT* f4(TINT* p) {
	//	    TINT* f5(TINT* p) {
	//	    TINT* f6(TINT* p) {
	//	    TINT* f7(TINT* p) {
	//	    TINT* f8(TINT* p) {
	//	    TINT* f9(TINT* p) {
	//	    TINT* f10(TINT* p) {
	//	    TINT* f11(TINT* p) {
	//	    TINT* f12(TINT* p) {
	//	    TINT* f13(TINT* p) {
	//	    TINT* f14(TINT* p) {
	//	    TINT* f15(TINT* p) {
	//	    TINT* f16(TINT* p) {
	//	    TINT* f17(TINT* p) {
	//	    TINT* f18(TINT* p) {
	//	    TINT* f19(TINT* p) {
	//	    TINT* f20(TINT* p) {
	//	    TINT* f21(TINT* p) {
	//	    TINT* f22(TINT* p) {
	//	    TINT* f23(TINT* p) {
	//	    TINT* f24(TINT* p) {
	//	    TINT* f25(TINT* p) {
	//	    TINT* f26(TINT* p) {
	//	    TINT* f27(TINT* p) {
	//	    TINT* f28(TINT* p) {
	//	    TINT* f29(TINT* p) {
	//	    }
	public void testPerformanceIssue_364108() throws Exception {
		final String comment = getAboveComment();
		parse(comment, ParserLanguage.CPP, false, false);
		parse(comment, ParserLanguage.C, false, false);
	}
}
