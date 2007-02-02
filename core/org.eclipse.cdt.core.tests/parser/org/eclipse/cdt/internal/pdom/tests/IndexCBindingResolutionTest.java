/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.core.runtime.Path;

/**
 * For testing PDOM binding C language resolution
 */
/*
 * aftodo - once we have non-problem bindings working, each test should
 * additionally check that the binding obtained has characteristics as
 * expected (type,name,etc..)
 */
public class IndexCBindingResolutionTest extends IndexBindingResolutionTestBase {

	public static TestSuite suite() {
		return suite(IndexCBindingResolutionTest.class);
	}

	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCProject("ResolveBindingTestsC", "bin", IPDOMManager.ID_NO_INDEXER);
		header = new Path("header.h");
		references = new Path("references.c");
		super.setUp();
	}
	
	// // header file
	// struct S {}; union U {}; enum E {ER1,ER2,ER3};
	// int var1; S var2; S *var3; void func1(E); void func2(S);
	// typedef int Int; typedef int *IntPtr;
	// void func3(int** ppi); void func4(int);

	// // referencing file
	// #include "header.h"
	// void references() {
	// 	struct S s; /*s*/ union U u; /*u*/ E e; /*e*/
	//  var1 = 1; /*var1*/ var2 = s; /*var2*/ var3 = &s; /*var3*/
	//  func1(e); /*func1*/ func1(var1); /*func2*/ func2(s); /*func3*/
	//  Int a; /*a*/
	//  IntPtr b = &a; /*b*/
	//  func3(*b); /*func4*/ func4(a); /*func5*/
	// }
	public void testSimpleGlobalBindings() throws IOException {
		IBinding b2 = getBindingFromASTName("S s;", 1);
		IBinding b3 = getBindingFromASTName("s;", 1);
		IBinding b4 = getBindingFromASTName("U u;", 1);
		IBinding b5 = getBindingFromASTName("u; ", 1);
		IBinding b6 = getBindingFromASTName("E e; ", 1);
		IBinding b7 = getBindingFromASTName("e; ", 1);
		IBinding b8 = getBindingFromASTName("var1 = 1;", 4);
		IBinding b9 = getBindingFromASTName("var2 = s;", 4);
		IBinding b10 = getBindingFromASTName("var3 = &s;", 4);
		IBinding b11 = getBindingFromASTName("func1(e);", 5);
		IBinding b12 = getBindingFromASTName("func1(var1);", 5);
		IBinding b13 = getBindingFromASTName("func2(s);", 5);
		IBinding b14 = getBindingFromASTName("Int a; ", 3);
		IBinding b15 = getBindingFromASTName("a; ", 1);
		IBinding b16 = getBindingFromASTName("IntPtr b = &a; ", 6);
		IBinding b17 = getBindingFromASTName("b = &a; /*b*/", 1);
		IBinding b18 = getBindingFromASTName("func3(*b);", 5);	
		IBinding b19 = getBindingFromASTName("b); /*func4*/", 1);
		IBinding b20 = getBindingFromASTName("func4(a);", 5);
		IBinding b21 = getBindingFromASTName("a); /*func5*/", 1);
	}

	// typedef struct S {int a;} S;
	// typedef enum E {A,B} E;
	
	// struct A {
	//    S *s;
	//    E *e;
	// };
	public void testTypedef() {
		IBinding b1 = getBindingFromASTName("S", 1);
		assertTrue(b1 instanceof ICompositeType);
		IBinding b2 = getBindingFromASTName("E", 1);
		assertTrue(b2 instanceof IEnumeration);
	}
	
	public void _testEnumeratorInFileScope() {fail("todo");}
	public void _testEnumeratorInStructScope() {fail("todo");}
	public void _testEnumeratorInUnionScope() {fail("todo");}
	public void _testFieldReference() {fail("todo");}
	public void _testMemberAccess() {fail("todo");}
	
	//	 // header file
	//		struct S {struct S* sp;};
	//		struct S foo1(struct S s);
	//		struct S* foo2(struct S* s);
	//		int foo3(int i);
	//		int foo4(int i, struct S s);

	//	 /* aftodo - latter cases need review */
	//	 // referencing content
	//		void references() {
	//			struct S s, *sp;
	//			foo1/*a*/(sp[1]);                       // IASTArraySubscriptExpression
	//			foo2/*b*/(sp+1);                        // IASTBinaryExpression
	//			foo2/*c*/((struct S*) sp);/*1*/         // IASTCastExpression
	//			foo1/*d*/(1==1 ? s : s);/*2*/           // IASTConditionalExpression
	//			foo4/*e*/(5, s);/*3*/                   // IASTExpressionList
	//			foo2/*f*/(s.sp);/*4*/ foo2(sp->sp);/*5*/// IASTFieldReference
	//			foo1/*g*/(foo1(s));/*6*/                // IASTFunctionCallExpression
	//			foo1/*h*/(s);/*7*/                      // IASTIdExpression
	//			foo3/*i*/(23489);                       // IASTLiteralExpression
	//			foo3/*j*/(sizeof(struct S));/*8*/       // IASTTypeIdExpression
	//			foo1/*k*/(*sp);/*9*/                    // IASTUnaryExpression
	//			/* not applicable */                    // ICPPASTDeleteExpression
	//			/* not applicable */                    // ICPPASTNewExpression
	//			// ?? foo/*n*/();                       // ICPPASTSimpleTypeConstructorExpression
	//			// ?? foo/*o*/();                       // ICPPASTTypenameExprssion
	//			// foo/*p*/(MADE_UP_SYMBOL);            // ICPPASTTypenameExprssion
	//		}
	public void _testExpressionKindForFunctionCalls() {
		IBinding b0 = getBindingFromASTName("foo1/*a*/", 4);
		IBinding b0a = getBindingFromASTName("ap[1]", 2);
		
		IBinding b1 = getBindingFromASTName("foo2/*b*/", 4);
		IBinding b1a = getBindingFromASTName("ap+1);", 2);
		
		IBinding b2 = getBindingFromASTName("foo2/*c*/", 4);
		IBinding b2a = getBindingFromASTName("ap);/*1*/", 2);
		
		IBinding b3 = getBindingFromASTName("foo1/*d*/", 4);
		IBinding b3a = getBindingFromASTName("a : a);/*2*/", 1);
		IBinding b3b = getBindingFromASTName("a);/*2*/", 1);
		
		IBinding b4 = getBindingFromASTName("foo4/*e*/", 4);
		IBinding b4a = getBindingFromASTName("a);/*3*/", 1);
		
		IBinding b5 = getBindingFromASTName("foo2/*f*/", 4);
		IBinding b5a = getBindingFromASTName("a.ap);/*4*/", 1);
		IBinding b5b = getBindingFromASTName("ap);/*4*/", 2);
		IBinding b5c = getBindingFromASTName("sp->sp);/*5*/", 2);
		IBinding b5d = getBindingFromASTName("sp);/*5*/", 2);
		
		IBinding b6 = getBindingFromASTName("foo1/*g*/", 4);
		IBinding b6a = getBindingFromASTName("foo1(s));/*6*/", 4);
		IBinding b6b = getBindingFromASTName("s));/*6*/", 1);
		
		IBinding b7 = getBindingFromASTName("foo1/*h*/", 4);
		IBinding b7a = getBindingFromASTName("s);/*7*/", 1);
		
		IBinding b8 = getBindingFromASTName("foo3/*i*/", 4);
		
		IBinding b9 = getBindingFromASTName("foo3/*j*/", 4);
		IBinding b9a = getBindingFromASTName("S));/*8*/", 1);
		
		IBinding b10 = getBindingFromASTName("foo1/*k*/", 4);
		IBinding b10a = getBindingFromASTName("sp);/*9*/ ", 2);
	}
}
