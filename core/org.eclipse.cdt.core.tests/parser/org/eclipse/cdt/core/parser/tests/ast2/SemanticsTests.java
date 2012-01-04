/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Directly tests parts of the semantics package
 */
public class SemanticsTests extends AST2BaseTest {

	public SemanticsTests() {}
	public SemanticsTests(String name) { super(name); }
	

	//	class A {};
	//	class B {};
	//
	//	class X {
	//	public:
	//		// unary
	//		A* operator !() { return new A(); } // logical not
	//		B* operator &() { return new B(); } // address of
	//		A* operator ~() { return new A(); } // one's complement
	//		B* operator *() { return new B(); } // ptr deference
	//		A* operator +() { return new A(); } // unary plus
	//		A* operator -() { return new A(); } // unary negation
	//		void operator ->() {} // Member selection
	//		X& operator++();       // Prefix increment operator.
	//	    X operator++(int);     // Postfix increment operator.
	//	    X& operator--();       // Prefix decrement operator.
	//	    X operator--(int);     // Postfix decrement operator.
	//
	//		// binary
	//		void operator ,(int x)  {} // comma
	//		void operator ,(long x) {} // comma (overloaded)
	//		void operator !=(int x) {} // NE
	//		void operator !=(long x){} // NE (overloaded)
	//		void operator %(int x)  {} // modulus
	//		void operator %=(int x) {} // modulus with assignment
	//		void operator &(int x)  {} // bitwise AND
	//		void operator &&(A a)   {} // logical AND
	//		void operator &=(A a)   {} // Bitwise AND/assignment
	//		void operator *(B b)    {} // multiplication
	//		void operator *=(X x)   {} // multiplication with assignment
	//		void operator +(X x)    {} // Addition
	//		A operator +=(int y)    {} // Addition with assignment
	//		B operator -(X x)       {} // Subtraction
	//		void operator -=(int y) {} // Subtraction with assignment	
	//		void operator ->*(int z){} // ptr-to-member selection
	//		void operator /(int x)  {} // division
	//		void operator /=(int y) {} // division with assignment
	//		void operator <(int x)  {} // LT
	//		void operator <<(int x) {} // L shift
	//		void operator <<=(int x){} // L shift assignment
	//		void operator <=(int x) {} // LE
	//		void operator =(int x)  {} // assignment
	//		void operator ==(int y) {} // EQ
	//		void operator >(int x)  {} // GT
	//		void operator >=(int y) {} // GE
	//		void operator >>(int x) {} // R shift
	//		void operator >>=(int x){} // R shift with assignment
	//		void operator ^(int x)  {} // XOR
	//		void operator ^=(int x) {} // XOR assignment
	//		void operator |(int x)  {} // Bitwise OR
	//		void operator |=(int x) {} // Bitwise OR with assignment
	//		void operator ||(int x) {} // logical OR
	//
	//		void operator()(int a, int b, int c) {} // function call
	//
	//		void operator[](int i) {} // subscripting
	//      
	//      operator A(); // conversion
	//      operator B(); // conversion
	//	};
	public void testConversionOperators() throws Exception {
		// Test getDeclaredConversionOperators()
		BindingAssertionHelper ba= new BindingAssertionHelper(getAboveComment(), true);
		ICPPClassType c= ba.assertNonProblem("X {", 1, ICPPClassType.class);
		ICPPMethod[] cops= SemanticUtil.getDeclaredConversionOperators(c);
		assertEquals(2, cops.length);
		Set actual= new HashSet();
		actual.add(cops[0].getName()); actual.add(cops[1].getName());
		Set expected= new HashSet();
		expected.add("operator A"); expected.add("operator B");
		assertEquals(expected, actual);

		// Test isConversionOperator()
		ICPPMethod[] dms= c.getDeclaredMethods();
		assertEquals(48, dms.length);
		
		for(ICPPMethod method : dms) {
			String name= method.getName();
			boolean isConvOp= name.equals("operator A") || name.equals("operator B");
			assertEquals(isConvOp, SemanticUtil.isConversionOperator(method));
		}
	}
}
