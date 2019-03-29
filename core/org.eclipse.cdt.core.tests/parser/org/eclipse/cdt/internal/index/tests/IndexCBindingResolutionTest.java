/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;

import junit.framework.TestSuite;

/**
 * For testing PDOM binding C language resolution
 */
/*
 * aftodo - once we have non-problem bindings working, each test should
 * additionally check that the binding obtained has characteristics as
 * expected (type,name,etc..)
 */
public class IndexCBindingResolutionTest extends IndexBindingResolutionTestBase {

	public static class SingleProject extends IndexCBindingResolutionTest {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	public static class ProjectWithDepProj extends IndexCBindingResolutionTest {
		public ProjectWithDepProj() {
			setStrategy(new ReferencedProject(false));
		}

		public static TestSuite suite() {
			return suite(ProjectWithDepProj.class);
		}
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(SingleProject.suite());
		suite.addTest(ProjectWithDepProj.suite());
	}

	public IndexCBindingResolutionTest() {
		setStrategy(new SinglePDOMTestStrategy(false));
	}

	// int (*f)(int);
	// int g(int n){return n;}

	// void foo() {
	//    f= g;
	// }
	public void testPointerToFunction() throws Exception {
		IBinding b0 = getBindingFromASTName("f= g;", 1);
		IBinding b1 = getBindingFromASTName("g;", 1);

		assertInstance(b0, IVariable.class);
		IVariable v0 = (IVariable) b0;
		assertInstance(v0.getType(), IPointerType.class);
		IPointerType p0 = (IPointerType) v0.getType();
		assertInstance(p0.getType(), IFunctionType.class);
		IFunctionType f0 = (IFunctionType) p0.getType();
		assertInstance(f0.getReturnType(), IBasicType.class);
		assertEquals(1, f0.getParameterTypes().length);
		assertInstance(f0.getParameterTypes()[0], IBasicType.class);

		assertInstance(b1, IFunction.class);
		IFunctionType f1 = ((IFunction) b1).getType();
		assertInstance(f1.getReturnType(), IBasicType.class);
		assertEquals(1, f1.getParameterTypes().length);
		assertInstance(f1.getParameterTypes()[0], IBasicType.class);
	}

	//	// header file
	//	struct S {int x;};
	//	union U {int x;};
	//	enum E {ER1,ER2,ER3};
	//	void func1(enum E e) {}
	//	void func2(struct S s) {}
	//	void func3(int** ppi) {}
	//	void func4(int n) {}
	//
	//	int var1;
	//	struct S var2;
	//	struct S *var3;
	//	typedef int Int;
	//	typedef int *IntPtr;

	//	// referencing file
	//	void references() {
	//	  struct S s; /*s*/
	//	  union U u; /*u*/
	//	  enum E e; /*e*/
	//	  Int a; /*a*/
	//	  IntPtr b = &a; /*b*/
	//	  func3(&b); /*func4*/
	//    func4(a); /*func5*/
	//
	//	  var1 = 1; /*var1*/
	//	  var2 = s; /*var2*/
	//	  var3 = &s; /*var3*/
	//	  func1(e); /*func1*/
	//	  func1(var1); /*func2*/
	//	  func2(s); /*func3*/
	//	}
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
		IBinding b18 = getBindingFromASTName("func3(&b);", 5);
		IBinding b19 = getBindingFromASTName("b); /*func4*/", 1);
		IBinding b20 = getBindingFromASTName("func4(a);", 5);
		IBinding b21 = getBindingFromASTName("a); /*func5*/", 1);
	}

	// // empty

	// typedef struct S {int a;} S;
	// typedef enum E {A,B} E;
	// struct A {
	//    S *s;
	//    E *e;
	// };
	public void testTypedefA() throws Exception {
		IBinding b1 = getBindingFromASTName("S {", 1);
		IBinding b2 = getBindingFromASTName("S;", 1);
		IBinding b3 = getBindingFromASTName("E {", 1);
		IBinding b4 = getBindingFromASTName("E;", 1);
		IBinding b5 = getBindingFromASTName("S *s", 1);
		IBinding b6 = getBindingFromASTName("E *e", 1);

		assertInstance(b1, ICompositeType.class);
		assertInstance(b2, ITypedef.class);

		assertInstance(b3, IEnumeration.class);
		assertInstance(b4, ITypedef.class);

		assertInstance(b5, ITypedef.class);
		ITypedef t5 = (ITypedef) b5;
		assertInstance(t5.getType(), ICompositeType.class);
		assertEquals(ICompositeType.k_struct, ((ICompositeType) t5.getType()).getKey());

		assertInstance(b6, ITypedef.class);
		ITypedef t6 = (ITypedef) b6;
		assertInstance(t6.getType(), IEnumeration.class);
	}

	// typedef struct S {int a;} S;
	// typedef enum E {A,B} E;

	// struct A {
	//    S *s;
	//    E *e;
	// };
	public void testTypedefB() throws Exception {
		IBinding b1 = getBindingFromASTName("S *s", 1);
		IBinding b2 = getBindingFromASTName("E *e", 1);

		assertInstance(b1, ITypedef.class);
		ITypedef t1 = (ITypedef) b1;
		assertInstance(t1.getType(), ICompositeType.class);
		assertEquals(ICompositeType.k_struct, ((ICompositeType) t1.getType()).getKey());

		assertInstance(b2, ITypedef.class);
		ITypedef t2 = (ITypedef) b2;
		assertInstance(t2.getType(), IEnumeration.class);
	}

	// union U {
	//    int x;
	//    int y;
	// };
	// struct S {
	//    union U u;
	//    int z;
	// };
	// typedef struct S TS;

	// void refs() {
	//    union U b1;
	//    struct S b2;
	//    union U *b3 = &b1;
	//    struct S *b4 = &b2;
	//    TS b5;
	//    TS *b6 = &b5;
	//
	//    b1.x = 0;
	//    b1.y = 1;
	//    b2.u.x = 2;
	//    b2.u.y = 3;
	//    b3->x = 4;
	//    b3->y = 5;
	//    b4->u.x = 6;
	//    b4->u.y = 7;
	//    b4->z = 8;
	//    b5.u.x = 9;
	//    b5.u.y = 10;
	//    b6->u.x = 11;
	//    b6->u.y = 12;
	//    b6->z = 13;
	// }
	public void testFieldReference() throws Exception {
		IBinding b01 = getBindingFromASTName("b1;", 2);
		assertVariable(b01, "b1", ICompositeType.class, "U");
		IBinding b02 = getBindingFromASTName("b2;", 2);
		assertVariable(b02, "b2", ICompositeType.class, "S");
		IBinding b03 = getBindingFromASTName("b3 =", 2);
		assertVariable(b03, "b3", IPointerType.class, null);
		assertTypeContainer(((IVariable) b03).getType(), null, IPointerType.class, ICompositeType.class, "U");
		IBinding b04 = getBindingFromASTName("b4 =", 2);
		assertVariable(b04, "b4", IPointerType.class, null);
		assertTypeContainer(((IVariable) b04).getType(), null, IPointerType.class, ICompositeType.class, "S");
		IBinding b05 = getBindingFromASTName("b5;", 2);
		assertVariable(b05, "b5", ITypedef.class, null);
		assertTypeContainer(((IVariable) b05).getType(), null, ITypedef.class, ICompositeType.class, "S");
		IBinding b06 = getBindingFromASTName("b6 =", 2);
		assertVariable(b06, "b6", IPointerType.class, null);
		assertTypeContainer(((IVariable) b06).getType(), null, IPointerType.class, ITypedef.class, "TS");
		assertTypeContainer(((IPointerType) ((IVariable) b06).getType()).getType(), null, ITypedef.class,
				ICompositeType.class, "S");
		IBinding b07 = getBindingFromASTName("x = 0", 1);
		assertVariable(b07, "x", IBasicType.class, null);
		IBinding b08 = getBindingFromASTName("y = 1", 1);
		assertVariable(b08, "y", IBasicType.class, null);
		IBinding b09 = getBindingFromASTName("x = 0", 1);
		assertVariable(b09, "x", IBasicType.class, null);
		IBinding b10 = getBindingFromASTName("y = 1", 1);
		assertVariable(b08, "y", IBasicType.class, null);
		IBinding b11 = getBindingFromASTName("u.x = 2", 1);
		assertVariable(b11, "u", ICompositeType.class, "U");
		IBinding b12 = getBindingFromASTName("x = 2", 1);
		assertVariable(b12, "x", IBasicType.class, null);
		IBinding b13 = getBindingFromASTName("u.y = 3", 1);
		assertVariable(b13, "u", ICompositeType.class, "U");
		IBinding b14 = getBindingFromASTName("y = 3", 1);
		assertVariable(b08, "y", IBasicType.class, null);
		IBinding b15 = getBindingFromASTName("x = 4", 1);
		assertVariable(b15, "x", IBasicType.class, null);
		IBinding b16 = getBindingFromASTName("y = 5", 1);
		assertVariable(b16, "y", IBasicType.class, null);
		IBinding b17 = getBindingFromASTName("u.x = 6", 1);
		assertVariable(b17, "u", ICompositeType.class, "U");
		IBinding b18 = getBindingFromASTName("x = 6", 1);
		assertVariable(b18, "x", IBasicType.class, null);
		IBinding b19 = getBindingFromASTName("u.y = 7", 1);
		assertVariable(b19, "u", ICompositeType.class, "U");
		IBinding b20 = getBindingFromASTName("y = 7", 1);
		assertVariable(b20, "y", IBasicType.class, null);
		IBinding b21 = getBindingFromASTName("z = 8", 1);
		assertVariable(b21, "z", IBasicType.class, null);
		IBinding b22 = getBindingFromASTName("x = 9", 1);
		assertVariable(b22, "x", IBasicType.class, null);
		IBinding b23 = getBindingFromASTName("y = 10", 1);
		assertVariable(b23, "y", IBasicType.class, null);
		IBinding b24 = getBindingFromASTName("u.x = 11", 1);
		assertVariable(b24, "u", ICompositeType.class, "U");
		IBinding b25 = getBindingFromASTName("x = 11", 1);
		assertVariable(b25, "x", IBasicType.class, null);
		IBinding b26 = getBindingFromASTName("u.y = 12", 1);
		assertVariable(b26, "u", ICompositeType.class, "U");
		IBinding b27 = getBindingFromASTName("y = 12", 1);
		assertVariable(b27, "y", IBasicType.class, null);
		IBinding b28 = getBindingFromASTName("z = 13", 1);
		assertVariable(b28, "z", IBasicType.class, null);
	}

	//	 // header file
	//		struct S {struct S* sp;};
	//		struct S foo1(struct S s);
	//		struct S* foo2(struct S* s);
	//		int foo3(int i);
	//		int foo4(int i, struct S s);

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
	//		}
	public void testExpressionKindForFunctionCalls() {
		IBinding b0 = getBindingFromASTName("foo1/*a*/", 4);
		IBinding b0a = getBindingFromASTName("sp[1]", 2);

		IBinding b1 = getBindingFromASTName("foo2/*b*/", 4);
		IBinding b1a = getBindingFromASTName("sp+1);", 2);

		IBinding b2 = getBindingFromASTName("foo2/*c*/", 4);
		IBinding b2a = getBindingFromASTName("sp);/*1*/", 2);

		IBinding b3 = getBindingFromASTName("foo1/*d*/", 4);
		IBinding b3a = getBindingFromASTName("s : s);/*2*/", 1);
		IBinding b3b = getBindingFromASTName("s);/*2*/", 1);

		IBinding b4 = getBindingFromASTName("foo4/*e*/", 4);
		IBinding b4a = getBindingFromASTName("s);/*3*/", 1);

		IBinding b5 = getBindingFromASTName("foo2/*f*/", 4);
		IBinding b5a = getBindingFromASTName("s.sp);/*4*/", 1);
		IBinding b5b = getBindingFromASTName("sp);/*4*/", 2);
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

	// // header file
	// struct myStruct {
	//    int a;
	// };
	// union myUnion {
	//    int b;
	// };

	// // referencing content
	// struct myStruct;
	// union myUnion;
	// void test() {
	//    struct myStruct* u;
	//    union myUnion* v;
	//    u->a= 1;  // since we include the definition, we may use the type.
	//    v->b= 1;  // since we include the definition, we may use the type.
	// }
	public void testTypeDefinitionWithFwdDeclaration() {
		getBindingFromASTName("a= 1", 1);
		getBindingFromASTName("b= 1", 1);
	}

	// int a= 1+2-3*4+10/2; // -4
	// int b= a+4;
	// int* c= &b;
	// enum X {e0, e4=4, e5, e2=2, e3};

	// void ref() {
	// a; b; c; e0; e2; e3; e4; e5;
	// }
	public void testValues() throws Exception {
		IVariable v = (IVariable) getBindingFromASTName("a;", 1);
		checkValue(v.getInitialValue(), -4);
		v = (IVariable) getBindingFromASTName("b;", 1);
		assertEquals(v.getInitialValue(), IntegralValue.UNKNOWN);
		v = (IVariable) getBindingFromASTName("c;", 1);
		assertNull(v.getInitialValue().numberValue());

		IEnumerator e = (IEnumerator) getBindingFromASTName("e0", 2);
		checkValue(e.getValue(), 0);
		e = (IEnumerator) getBindingFromASTName("e2", 2);
		checkValue(e.getValue(), 2);
		e = (IEnumerator) getBindingFromASTName("e3", 2);
		checkValue(e.getValue(), 3);
		e = (IEnumerator) getBindingFromASTName("e4", 2);
		checkValue(e.getValue(), 4);
		e = (IEnumerator) getBindingFromASTName("e5", 2);
		checkValue(e.getValue(), 5);
	}

	private void checkValue(IValue initialValue, int i) {
		assertNotNull(initialValue);
		final Number numericalValue = initialValue.numberValue();
		assertNotNull(numericalValue);
		assertEquals(i, numericalValue.intValue());
	}

	//	extern char TableValue[10];

	//	char TableValue[sizeof TableValue];
	public void testNameLookupFromArrayModifier_435075() throws Exception {
		checkBindings();
	}

	//	static union {
	//	    int a;
	//	    int b;
	//	};

	//	int waldo = a;
	public void testAnonymousUnion_377409() {
		checkBindings();
	}
}
