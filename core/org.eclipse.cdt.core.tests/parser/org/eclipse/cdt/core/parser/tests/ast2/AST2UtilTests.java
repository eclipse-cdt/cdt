/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.parser.scanner.ExpressionEvaluator;

/**
 * @author dsteffle
 */
public class AST2UtilTests extends AST2TestBase {
	public AST2UtilTests() {
	}

	public AST2UtilTests(String name) {
		super(name);
	}

	public void testSimpleSignature() throws Exception {
		StringBuilder buff = new StringBuilder();
		buff.append("int l, m, n=0;\n"); //$NON-NLS-1$
		buff.append("int j = l ? m : n;\n"); //$NON-NLS-1$
		buff.append("int i = l^m;\n"); //$NON-NLS-1$
		buff.append("int g = i<<=j;\n"); //$NON-NLS-1$
		buff.append("int f = sizeof( int );\n"); //$NON-NLS-1$
		buff.append("int e = ~f;\n"); //$NON-NLS-1$
		buff.append("int d = ++e;\n"); //$NON-NLS-1$
		buff.append("int b = d++;\n"); //$NON-NLS-1$
		buff.append("int c = sizeof b;\n"); //$NON-NLS-1$
		buff.append("int a = b + c;\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
		IASTDeclaration[] d = tu.getDeclarations();

		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[2].getInitializer())
						.getInitializerClause(),
				"0"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[1]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"l ? m : n"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[2]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"l ^ m"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[3]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"i <<= j"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[4]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"sizeof(int)"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[5]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"~f"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[6]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"++e"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[7]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"d++"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[8]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"sizeof b"); //$NON-NLS-1$
		isExpressionStringEqual(
				((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[9]).getDeclarators()[0].getInitializer())
						.getInitializerClause(),
				"b + c"); //$NON-NLS-1$
	}

	public void testSimpleParameter() throws Exception {
		StringBuilder buff = new StringBuilder();
		buff.append("int a(int x);\n"); //$NON-NLS-1$
		buff.append("int * b(char y, int x);\n"); //$NON-NLS-1$
		buff.append("void c(int * z, float **b);\n"); //$NON-NLS-1$
		buff.append("static int d(int a[restrict]);\n"); //$NON-NLS-1$
		buff.append("void e(const char* const);\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
		IASTDeclaration[] d = tu.getDeclarations();

		isParameterSignatureEqual(((IASTSimpleDeclaration) d[0]).getDeclarators()[0], "(int)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[1]).getDeclarators()[0], "(char, int)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[2]).getDeclarators()[0], "(int*, float**)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[3]).getDeclarators()[0], "(int[])"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[4]).getDeclarators()[0], "(const char* const)"); //$NON-NLS-1$

		isSignatureEqual(((IASTSimpleDeclaration) d[0]).getDeclarators()[0], "int(int)"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[1]).getDeclarators()[0], "int*(char, int)"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[2]).getDeclarators()[0], "void(int*, float**)"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[3]).getDeclarators()[0], "int(int[])"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[4]).getDeclarators()[0], "void(const char* const)"); //$NON-NLS-1$

		isSignatureEqual(((IASTSimpleDeclaration) d[0]).getDeclSpecifier(), "int"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[1]).getDeclSpecifier(), "int"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[2]).getDeclSpecifier(), "void"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[3]).getDeclSpecifier(), "int"); //$NON-NLS-1$
		isSignatureEqual(((IASTSimpleDeclaration) d[4]).getDeclSpecifier(), "void"); //$NON-NLS-1$

		isTypeEqual(((IASTSimpleDeclaration) d[0]).getDeclarators()[0], "int (int)"); //$NON-NLS-1$
		isTypeEqual(((IASTSimpleDeclaration) d[1]).getDeclarators()[0], "int * (char, int)"); //$NON-NLS-1$
		isTypeEqual(((IASTSimpleDeclaration) d[2]).getDeclarators()[0], "void (int *, float * *)"); //$NON-NLS-1$
		isTypeEqual(((IASTSimpleDeclaration) d[3]).getDeclarators()[0], "int (int * restrict)"); //$NON-NLS-1$
		isTypeEqual(((IASTSimpleDeclaration) d[4]).getDeclarators()[0], "void (const char * const)"); //$NON-NLS-1$

		isTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"int (int)"); //$NON-NLS-1$
		isTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[1]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"int * (char, int)"); //$NON-NLS-1$
		isTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[2]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"void (int *, float * *)"); //$NON-NLS-1$
		isTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[3]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"int (int * restrict)"); //$NON-NLS-1$
		isTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[4]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"void (const char * const)"); //$NON-NLS-1$

		isParameterTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"(int)"); //$NON-NLS-1$
		isParameterTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[1]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"(char, int)"); //$NON-NLS-1$
		isParameterTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[2]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"(int *, float * *)"); //$NON-NLS-1$
		isParameterTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[3]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"(int * restrict)"); //$NON-NLS-1$
		isParameterTypeEqual(
				((IFunction) ((IASTSimpleDeclaration) d[4]).getDeclarators()[0].getName().resolveBinding()).getType(),
				"(const char * const)"); //$NON-NLS-1$
	}

	public void testSimpleCParameterSignature() throws Exception {
		StringBuilder buff = new StringBuilder();
		buff.append("int a(int x);\n"); //$NON-NLS-1$
		buff.append("int * b(char y, int x);\n"); //$NON-NLS-1$
		buff.append("void c(int * z, float **b);\n"); //$NON-NLS-1$
		buff.append("static int d(int a[restrict]);\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
		IASTDeclaration[] d = tu.getDeclarations();

		isParameterSignatureEqual(((IASTSimpleDeclaration) d[0]).getDeclarators()[0], "(int)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[1]).getDeclarators()[0], "(char, int)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[2]).getDeclarators()[0], "(int*, float**)"); //$NON-NLS-1$
		isParameterSignatureEqual(((IASTSimpleDeclaration) d[3]).getDeclarators()[0], "(int[])"); //$NON-NLS-1$
	}

	public void testSimpleTypeId() throws Exception {
		StringBuilder buff = new StringBuilder();
		buff.append("int x = sizeof( int );\n"); //$NON-NLS-1$
		buff.append("union Squaw { int x; double u; };\n"); //$NON-NLS-1$
		buff.append("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		buff.append("return sizeof( union Squaw );\n}\n"); //$NON-NLS-1$
		buff.append("typedef short Z; typedef Z jc;\n"); //$NON-NLS-1$
		buff.append("int y = 4;\n"); //$NON-NLS-1$
		buff.append("jc myJc = (jc)y;\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
		IASTDeclaration[] d = tu.getDeclarations();

		// verify signatures
		isSignatureEqual(
				((IASTTypeIdExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0]
						.getInitializer()).getInitializerClause()).getTypeId(),
				"int"); //$NON-NLS-1$
		isSignatureEqual(
				((IASTTypeIdExpression) ((IASTReturnStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) d[2])
						.getBody()).getStatements()[0]).getReturnValue()).getTypeId(),
				"union Squaw"); //$NON-NLS-1$
		isSignatureEqual(
				((IASTCastExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[6]).getDeclarators()[0]
						.getInitializer()).getInitializerClause()).getTypeId(),
				"jc"); //$NON-NLS-1$

		// verify types
		isTypeEqual(((IASTTypeIdExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[0]).getDeclarators()[0]
				.getInitializer()).getInitializerClause()).getTypeId(), "int"); //$NON-NLS-1$
		isTypeEqual(
				((IASTTypeIdExpression) ((IASTReturnStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) d[2])
						.getBody()).getStatements()[0]).getReturnValue()).getTypeId(),
				"Squaw"); //$NON-NLS-1$
		isTypeEqual(((IASTCastExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) d[6]).getDeclarators()[0]
				.getInitializer()).getInitializerClause()).getTypeId(), "short int"); //$NON-NLS-1$
	}

	public void testKnRC() throws Exception {
		StringBuilder buff = new StringBuilder();
		buff.append("int foo(x, y) char x; int y; {}\n"); //$NON-NLS-1$
		buff.append("int foo2(char x, int y) {}\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C, true);
		IASTDeclaration[] d = tu.getDeclarations();

		String fooSignature = ASTStringUtil.getSignatureString(((IASTFunctionDefinition) d[0]).getDeclarator());
		String foo2Signature = ASTStringUtil.getSignatureString(((IASTFunctionDefinition) d[1]).getDeclarator());

		assertEquals(fooSignature, foo2Signature);
	}

	public void testParseIntegral() throws Exception {
		assertEquals(0, ExpressionEvaluator.getNumber("0".toCharArray()));
		assertEquals(0, ExpressionEvaluator.getNumber("0x0".toCharArray()));
		assertEquals(0, ExpressionEvaluator.getNumber("00".toCharArray()));
		assertEquals(0, ExpressionEvaluator.getNumber("000".toCharArray()));
		assertEquals(0, ExpressionEvaluator.getNumber("0L".toCharArray()));
		assertEquals(0, ExpressionEvaluator.getNumber("0LL".toCharArray()));

		assertEquals(1, ExpressionEvaluator.getNumber("1".toCharArray()));
		assertEquals(1, ExpressionEvaluator.getNumber("01".toCharArray()));
		assertEquals(1, ExpressionEvaluator.getNumber("0x1".toCharArray()));

		assertEquals(10, ExpressionEvaluator.getNumber("10".toCharArray()));
		assertEquals(8, ExpressionEvaluator.getNumber("010".toCharArray()));
		assertEquals(16, ExpressionEvaluator.getNumber("0x10".toCharArray()));

		assertEquals(10, ExpressionEvaluator.getNumber("10LLL".toCharArray()));
		assertEquals(8, ExpressionEvaluator.getNumber("010LLL".toCharArray()));
		assertEquals(16, ExpressionEvaluator.getNumber("0x10LLL".toCharArray()));
	}
}
