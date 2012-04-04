/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractlocalvariable;

import junit.framework.Test;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring;

/**
 * Tests for Extract Local Variable refactoring.
 */
public class ExtractLocalVariableRefactoringTest extends RefactoringTestBase {
	private String extractedVariableName;
	private ExtractLocalVariableRefactoring refactoring;

	public ExtractLocalVariableRefactoringTest() {
		super();
	}

	public ExtractLocalVariableRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(ExtractLocalVariableRefactoringTest.class);
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ExtractLocalVariableRefactoring(getSelectedTranslationUnit(),
				getSelection(),	getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		if (extractedVariableName != null)
			refactoring.getRefactoringInfo().setName(extractedVariableName);
	}

	//A.cpp
	//int foo() {
	//	return /*$*/42/*$$*/;
	//}
	//====================
	//int foo() {
	//	int i = 42;
	//	return i;
	//}
	public void testIntLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//int foo() {
	//	return /*$*/'c'/*$$*/;
	//}
	//====================
	//int foo() {
	//	char c = 'c';
	//	return c;
	//}
	public void testCharLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//const char* foo() {
	//	return /*$*/"Hello World!"/*$$*/;
	//}
	//====================
	//const char* foo() {
	//	const char* helloWorld = "Hello World!";
	//	return helloWorld;
	//}
	public void testStringLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//double foo() {
	//	return /*$*/42.0f/*$$*/;
	//}
	//====================
	//double foo() {
	//	float f = 42.0f;
	//	return f;
	//}
	public void testFloatLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//double foo() {
	//	return /*$*/42./*$$*/;
	//}
	//====================
	//double foo() {
	//	double f = 42.;
	//	return f;
	//}
	public void testDoubleLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	return /*$*/(42)/*$$*/;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int i = 42;
	//	return i;
	//}
	public void testParentheses() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int x = 3;
	//	return /*$*/(x + 2)/*$$*/ * 15;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int x = 3;
	//	int i = x + 2;
	//	return i * 15;
	//}
	public void testSuggestedNameInScope() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	for (int n = /*$*/5 + 2/*$$*/; n < 10; ++n)
	//		;
	//}
	//====================
	//void foo() {
	//	int i = 5 + 2;
	//	for (int n = i; n < 10; ++n)
	//		;
	//}
	public void testForStatement_Bug277065() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	int a = 0;
	//	float b = 0.1f;
	//	double c = /*$*/(a + b)/*$$*/ * 0.2;
	//}
	//====================
	//void foo() {
	//	int a = 0;
	//	float b = 0.1f;
	//	float a0 = a + b;
	//	double c = a0 * 0.2;
	//}
	public void testExpression() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	int a[2];
	//	int b = */*$*/(a + 1)/*$$*/;
	//}
	//====================
	//void foo() {
	//	int a[2];
	//	int* i = a + 1;
	//	int b = *i;
	//}
	public void testPointer() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//const volatile int* k;
	//
	//void foo() {
	//	/*$*/k;/*$$*/
	//}
	//====================
	//const volatile int* k;
	//
	//void foo() {
	//	const volatile int* k0 = k;
	//	k0;
	//}
	public void testQualifiers() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//class K {
	//public:
	//	bool operator+(int b) { return true; }
	//	float operator+(unsigned u) { return 1.0f; }
	//};
	//void foo() {
	//	K k;
	//	/*$*/k+3u/*$$*/;
	//}
	//====================
	//class K {
	//public:
	//	bool operator+(int b) { return true; }
	//	float operator+(unsigned u) { return 1.0f; }
	//};
	//void foo() {
	//	K k;
	//	float i = k + 3u;
	//	i;
	//}
	public void testOverloadedOperators() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//
	//void func() {
	//	int* (*a)[2];
	//	/*$*/a/*$$*/;
	//}
	//====================
	//
	//void func() {
	//	int* (*a)[2];
	//	int* (*a0)[2] = a;
	//	a0;
	//}
	public void testArrayOfFunctionPointers_Bug318784() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//template<class T>
	//class Foo {
	//};
	//
	//Foo<int> getFoo();
	//
	//int main() {
	//	/*$*/getFoo()/*$$*/;
	//	return 0;
	//}
	//====================
	//template<class T>
	//class Foo {
	//};
	//
	//Foo<int> getFoo();
	//
	//int main() {
	//	Foo<int> foo = getFoo();
	//	foo;
	//	return 0;
	//}
	public void testTemplateTypeParameters_Bug331963() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//namespace bar {
	//
	//template<class T>
	//class Foo {
	//};
	//
	//}
	//
	//bar::Foo<int> getFoo();
	//
	//int main() {
	//	/*$*/getFoo()/*$$*/;
	//	return 0;
	//}
	//====================
	//namespace bar {
	//
	//template<class T>
	//class Foo {
	//};
	//
	//}
	//
	//bar::Foo<int> getFoo();
	//
	//int main() {
	//	bar::Foo<int> foo = getFoo();
	//	foo;
	//	return 0;
	//}
	public void testTemplateTypeParametersWithNamespace_Bug331963() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//
	//struct Foo {
	//	int getVarWithLongName();
	//};
	//
	//void bar() {
	//	Foo f;
	//	/*$*/f.getVarWithLongName()/*$$*/;
	//}
	//====================
	//
	//struct Foo {
	//	int getVarWithLongName();
	//};
	//
	//void bar() {
	//	Foo f;
	//	int varWithLongName = f.getVarWithLongName();
	//	varWithLongName;
	//}
	public void testSuggestedName_Bug330693_1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//
	//struct Foo {
	//	int get();
	//};
	//
	//void bar() {
	//	Foo f;
	//	/*$*/f.get()/*$$*/;
	//}
	//====================
	//
	//struct Foo {
	//	int get();
	//};
	//
	//void bar() {
	//	Foo f;
	//	int get = f.get();
	//	get;
	//}
	public void testSuggestedName_Bug330693_2() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//
	//int getA() {
	//	return 0;
	//};
	//
	//int getB(int a) {
	//	return a;
	//}
	//
	//void bar() {
	//	/*$*/getB(getA())/*$$*/;
	//}
	//====================
	//
	//int getA() {
	//	return 0;
	//};
	//
	//int getB(int a) {
	//	return a;
	//}
	//
	//void bar() {
	//	int b = getB(getA());
	//	b;
	//}
	public void testSuggestedName_Bug335202_1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//
	//int getA() {
	//	return 0;
	//};
	//
	//int getB(int a) {
	//	return a;
	//}
	//
	//int getC(int a) {
	//	return a;
	//}
	//
	//void bar() {
	//	getB(/*$*/getC(getA())/*$$*/);
	//}
	//====================
	//
	//int getA() {
	//	return 0;
	//};
	//
	//int getB(int a) {
	//	return a;
	//}
	//
	//int getC(int a) {
	//	return a;
	//}
	//
	//void bar() {
	//	int c = getC(getA());
	//	getB(c);
	//}
	public void testSuggestedName_Bug335202_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//void foo() {
	//	for (int n = 5 + 2; n < 10; ++n)
	//		;
	//}
	//====================
	//void foo() {
	//	int i = 5 + 2;
	//	for (int n = i; n < 10; ++n)
	//		;
	//}

	//refScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Extract 5 + 2" description="Extract Local Variable Refactoring"
	// fileName="file:${projectPath}/main.cpp" flags="4"
	// id="org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.ExtractLocalVariableRefactoring"
	// name="i" project="RegressionTestProject" selection="27,5"/>
	//</session>
	public void testLocalVariableFromForLoop() throws Exception {
		assertRefactoringSuccess();
	}
}
