/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class AccessControlTests extends AST2BaseTest {
	
	protected class AccessAssertionHelper extends BindingAssertionHelper {
		AccessAssertionHelper(String contents) throws ParserException {
			super(contents, true);
		}
		
		void assertAccessible(String section, int len) {
			IASTName name = findName(section, len);
			IBinding binding = name.resolveBinding();
    		assertNotNull(binding);
			assertTrue(AccessContext.isAccessible(binding, name));
		}
		
		void assertNotAccessible(String section, int len) {
			IASTName name = findName(section, len);
			IBinding binding = name.resolveBinding();
    		assertNotNull(binding);
			assertFalse(AccessContext.isAccessible(binding, name));
		}
	}
	
	public AccessControlTests() {
	}
	
	public AccessControlTests(String name) {
		super(name);
	}
	
	public static TestSuite suite() {
		return suite(AccessControlTests.class);
	}

	//	class A {
	//	public:
	//	  int a;
	//	};
	//	class B : private A {
	//    friend void test();
	//    int b;
	//	};
	//  class C : protected B {
	//  };
	//	void test() {
	//    class D : public C {
	//      void m() {
	//        a; //1
	//        b; //1
	//      }
	//	  };
	//    B b;
	//    b.a; //2
	//    b.b; //2
	//    C c;
	//    c.a; //3
	//    c.b; //3
	//	}
	public void testFriends() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, ParserLanguage.CPP);
		AccessAssertionHelper ah= new AccessAssertionHelper(code);
		ah.assertAccessible("a; //1", 1);
		ah.assertAccessible("b; //1", 1);
		ah.assertAccessible("a; //2", 1);
		ah.assertAccessible("b; //2", 1);
		ah.assertNotAccessible("a; //3", 1);
		ah.assertNotAccessible("b; //3", 1);
	}

	//	class A {
	//	public:
	//	  int a;
	//	};
	//
	//	class B : public A {
	//	private:
	//	  int a;
	//	};
	//
	//	void test(B x) {
	//	  x.a = 0;
	//	}
	public void testHiddenMember() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, ParserLanguage.CPP);
		AccessAssertionHelper ah= new AccessAssertionHelper(code);
		ah.assertNotAccessible("a = 0", 1);
	}
}
