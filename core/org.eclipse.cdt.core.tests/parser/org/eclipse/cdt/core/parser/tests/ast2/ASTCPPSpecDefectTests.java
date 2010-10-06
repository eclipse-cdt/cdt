/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.Iterator;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.parser.ParserLanguage;

public class ASTCPPSpecDefectTests extends AST2BaseTest {
	
	public ASTCPPSpecDefectTests() {
	}
	
	public ASTCPPSpecDefectTests(String name) {
		super(name);
	}
	
	public static TestSuite suite() {
		return suite(ASTCPPSpecDefectTests.class);
	}
		
	protected IASTTranslationUnit parseAndCheckBindings(String code) throws Exception {
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
		CPPNameCollector col = new CPPNameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
		return tu;
	}	
	
	private IASTTranslationUnit parseAndCheckBindings() throws Exception {
		String code= getAboveComment();
		return parseAndCheckBindings(code);
	}
	
	protected void assertNoProblemBindings(CPPNameCollector col) {
		Iterator i = col.nameList.iterator();
		while (i.hasNext()) {
			IASTName n = (IASTName) i.next();
			assertFalse(n.resolveBinding() instanceof IProblemBinding);
		}
	}
		
	//  // C++ defect #33
	//	namespace ns {
	//		struct S {};
	//		void fp(void (*)(int));
	//	}
	//	void f0(ns::S);
	//	void f0(int);
	//
	//	void test() {
	//		fp(f0);  
	//	}
	public void test33_ADLForOverloadSet_324842() throws Exception {
		parseAndCheckBindings();
	}
	
	//  // C++ defect #38
	//	template<typename T> T operator+(T&);
	//	struct A {
	//	  friend A operator + <>(A&);
	//	};
	public void test38_templateArgForOperator() throws Exception {
		parseAndCheckBindings();
	}
	
	//	template <class T1, class ...Z> class S; // #1
	//	template <class T1, class ...Z> class S<T1, const Z&...> {}; // #2
	//	template <class T1, class T2> class S<T1, const T2&> {};; // #3
	//	S<int, const int&> s; // both #2 and #3 match; #3 is more specialized
	public void test692_partialOrdering() throws Exception {
		parseAndCheckBindings();
	}

}
