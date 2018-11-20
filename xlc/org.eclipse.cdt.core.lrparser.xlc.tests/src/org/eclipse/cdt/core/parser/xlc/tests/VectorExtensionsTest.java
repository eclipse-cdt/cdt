/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class VectorExtensionsTest extends XlcTestBase {

	public VectorExtensionsTest() {
	}

	public VectorExtensionsTest(String name) {
		super(name);
	}

	public void testVector1() {
		String code = "int test() {  \n" + "   vector unsigned int a = {1,2,3,4};  \n"
				+ "   vector unsigned int b = {2,4,6,8};  \n" + "   vector unsigned int c = a + b;      \n"
				+ "   int e = b > a;                      \n" + "   int f = a[2];                       \n"
				+ "   vector unsigned int d = ++a;        \n" + "}\n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorDeclarations() {
		String code = "vector unsigned char a;	  \n" + "vector signed char b;  \n" + "vector bool char c;	  \n"
				+ "vector unsigned short d; 	  \n" + "vector unsigned short int e;  \n"
				+ "vector signed short f;  \n" + "vector signed short int g;  \n" + "vector bool short h;	  \n"
				+ "vector bool short int i;  \n" + "vector unsigned int j;  \n" + "vector unsigned long k;  \n"
				+ "vector unsigned long int l;  \n" + "vector signed int m;  \n" + "vector signed long n;  \n"
				+ "vector signed long int o;  \n" + "vector bool int p;  \n" + "vector bool long q;  \n"
				+ "vector bool long int r;  \n" + "vector float s;  \n" + "vector pixel t;  \n"
				+ "__vector __pixel u;  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	// these are only treated as keywords inside a vector declaration
	public void testReservedWords() {
		String code = "int pixel; " + "int bool;  ";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorLiterals() {
		String code = "int test() {" + "    (vector unsigned int)(10); "
				+ "    (vector unsigned int)(14, 82, 73, 700); " + "    (vector pixel)(14, 82, 73, 700); "
				+ "    (vector bool int)(10); " + "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorInitializers() {
		String code = "int test() {" + "    vector unsigned int v3 = {1,2,3,4}; " + "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorTypedefs() {
		String code = "int test() {" + "     typedef vector pixel vint16; " + "     vint16 v1;" + "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorCompoundLiterals() {
		String code = "int test() {" + "    (vector unsigned int){10}; "
				+ "    (vector unsigned int){14, 82, 73, 700}; " + "    (vector pixel){14, 82, 73, 700}; "
				+ "    (vector bool int){10}; " + "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorAlignof() {
		String code = "int test() {" + "   vector unsigned int v1 = (vector unsigned int)(10);  \n"
				+ "   vector unsigned int *pv1 = &v1;  \n" + "   __alignof__(v1); \n" + "   __alignof__(&v1);  \n"
				+ "   __alignof__(*pv1);  \n" + "   __alignof__(pv1);  \n" + "   __alignof__(vector signed char);  \n"
				+ "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void testVectorTypeof() {
		String code = "int test() {" + "   vector unsigned int v1 = (vector unsigned int)(10);  \n"
				+ "   vector unsigned int *pv1 = &v1;  \n" + "   __typeof__(v1); \n" + "   __typeof__(&v1);  \n"
				+ "   __typeof__(*pv1);  \n" + "   __typeof__(pv1);  \n" + "   __typeof__(vector signed char);  \n"
				+ "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}

	public void _testOverloads() {
		String code = "void foo(int); \n" + "void foo(vector unsigned int); \n" + "void foo(vector pixel) \n"
				+ "int test() { \n" + "    int x; \n" + "    vector unsigned int y; \n" + "    vector pixel z; \n"
				+ "    foo(x); \n" + "    foo(y); \n" + "    foo(z); \n" + "} \n";

		IASTTranslationUnit tu = parse(code, getCPPLanguage(), true);

		IASTDeclaration[] decls = tu.getDeclarations();
		IASTName foo1 = ((IASTSimpleDeclaration) decls[0]).getDeclarators()[0].getName();
		IASTName foo2 = ((IASTSimpleDeclaration) decls[1]).getDeclarators()[0].getName();
		IASTName foo3 = ((IASTSimpleDeclaration) decls[2]).getDeclarators()[0].getName();

		IASTFunctionDefinition func = (IASTFunctionDefinition) decls[4];
		IASTStatement[] stats = ((IASTCompoundStatement) func.getBody()).getStatements();

		IASTName fooCall1 = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTExpressionStatement) stats[3])
				.getExpression()).getFunctionNameExpression()).getName();
		IASTName fooCall2 = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTExpressionStatement) stats[4])
				.getExpression()).getFunctionNameExpression()).getName();
		IASTName fooCall3 = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTExpressionStatement) stats[5])
				.getExpression()).getFunctionNameExpression()).getName();

		assertSame(foo1.resolveBinding(), fooCall1.resolveBinding());
		assertSame(foo2.resolveBinding(), fooCall2.resolveBinding());
		assertSame(foo3.resolveBinding(), fooCall3.resolveBinding());
	}
}
