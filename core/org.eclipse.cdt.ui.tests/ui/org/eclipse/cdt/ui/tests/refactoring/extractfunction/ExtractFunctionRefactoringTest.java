/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
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
package org.eclipse.cdt.ui.tests.refactoring.extractfunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

import junit.framework.Test;

/**
 * Tests for Extract Function refactoring.
 */
public class ExtractFunctionRefactoringTest extends RefactoringTestBase {
	private static final String NO_RETURN_VALUE = "";
	private String extractedFunctionName = "extracted";
	private String returnValue;
	// Map from old names to new ones.
	private final Map<String, String> parameterRename = new HashMap<>();
	// New positions of parameters, or null.
	private int[] parameterOrder;
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private boolean virtual;
	private final boolean replaceDuplicates = true;
	private ExtractFunctionRefactoring refactoring;

	public ExtractFunctionRefactoringTest() {
		super();
	}

	public ExtractFunctionRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(ExtractFunctionRefactoringTest.class);
	}

	@Override
	public void setUp() throws Exception {
		setIncludeFolder("resources/includes/");
		super.setUp();
	}

	@Override
	protected void resetPreferences() {
		super.resetPreferences();
		getPreferenceStore().setToDefault(PreferenceConstants.FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT);
		getPreferenceStore().setToDefault(PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER);
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ExtractFunctionRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		ExtractFunctionInformation refactoringInfo = refactoring.getRefactoringInfo();
		refactoringInfo.setMethodName(extractedFunctionName);
		refactoringInfo.setReplaceDuplicates(replaceDuplicates);
		if (refactoringInfo.getMandatoryReturnVariable() == null) {
			if (returnValue != null) {
				for (NameInformation nameInfo : refactoringInfo.getParameters()) {
					nameInfo.setReturnValue(returnValue.equals(getName(nameInfo)));
				}
			}
		}
		if (!parameterRename.isEmpty()) {
			for (NameInformation nameInfo : refactoringInfo.getParameters()) {
				String newName = parameterRename.get(getName(nameInfo));
				if (newName != null)
					nameInfo.setNewName(newName);
			}
		}
		if (parameterOrder != null) {
			List<NameInformation> parameters = refactoringInfo.getParameters();
			NameInformation[] originalParameters = parameters.toArray(new NameInformation[parameters.size()]);
			for (int i = 0; i < parameterOrder.length; i++) {
				parameters.set(parameterOrder[i], originalParameters[i]);
			}
		}
		refactoringInfo.setVisibility(visibility);
		refactoringInfo.setVirtual(virtual);
	}

	private String getName(NameInformation nameInfo) {
		return String.valueOf(nameInfo.getName().getSimpleID());
	}

	private void allowNameComputation() {
		CPPASTNameBase.sAllowNameComputation = true;
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted();
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
	//	/*$*/int i = 2;
	//	++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted() {
	//	int i = 2;
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = extracted();
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testLocalVariableDeclaration_1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	void foo();
	//	B b;
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	void foo();
	//	B b;
	//
	//private:
	//	int help();
	//	void extracted();
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
	//void A::foo() {
	//	/*$*/b = new B();
	//	help();/*$$*/
	//}
	//
	//int A::help() {
	//	return 42;
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
	//void A::extracted() {
	//	b = new B();
	//	help();
	//}
	//
	//void A::foo() {
	//	extracted();
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//};
	//
	//#endif /*B_H_*/
	public void testLocalVariableDeclaration_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//class A {
	//public:
	//	explicit A(const char*);
	//	void m1() const;
	//	void m2() const;
	//};
	//
	//void main() {
	//  /*$*/A a("");
	//  a.m1();/*$$*/
    //		A b(a); // nonstandard indent to check that it is preserved
	//}
	//====================
	//class A {
	//public:
	//	explicit A(const char*);
	//	void m1() const;
	//	void m2() const;
	//};
	//
	//A extracted() {
	//	A a("");
	//	a.m1();
	//	return a;
	//}
	//
	//void main() {
	//	A a = extracted();
    //		A b(a); // nonstandard indent to check that it is preserved
	//}
	public void testLocalVariableDeclaration_3() throws Exception {
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
	//
	//private:
	//	int help();
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
	//	/*$*/int o = 1;
	//	int i = 2;
	//	++i;
	//	o++;
	//	help();/*$$*/
	//	o++;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testTwoLocalVariables() throws Exception {
		assertRefactoringFailure();
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
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
	//	int i = 2;
	//	//comment
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) {
	//	//comment
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testComment() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	// Comment
	//	/*$*/i= 7;/*$$*/
	//	return i;
	//}
	//====================
	//int extracted(int i) {
	//	// Comment
	//	i = 7;
	//	return i;
	//}
	//
	//int main() {
	//	int i;
	//	// Comment
	//	i = extracted(i);
	//	return i;
	//}
	public void testLeadingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	/*$*/i= 7;/*$$*/ // Comment
	//	return i;
	//}
	//====================
	//int extracted(int i) {
	//	i = 7; // Comment
	//	return i;
	//}
	//
	//int main() {
	//	int i;
	//	i = extracted(i);
	//	return i;
	//}
	public void testTraillingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	void foo();
	//	B b;
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	void foo();
	//	B b;
	//
	//private:
	//	int help();
	//	void extracted();
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
	//void A::foo() {
	//	/*$*/b = new B();
	//	help();/*$$*/
	//}
	//
	//int A::help() {
	//	return 42;
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
	//void A::extracted() {
	//	b = new B();
	//	help();
	//}
	//
	//void A::foo() {
	//	extracted();
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//};
	//
	//#endif /*B_H_*/
	public void testNamedTypedField() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	void foo();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	void foo();
	//
	//private:
	//	ns1::ns2::B extracted(ns1::ns2::B b);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//#include "B.h"
	//
	//using ns1::ns2::B;
	//
	//void A::foo() {
	//	B b;
	//	/*$*/b.m();/*$$*/
	//	b.n();
	//}
	//====================
	//#include "A.h"
	//#include "B.h"
	//
	//using ns1::ns2::B;
	//
	//B A::extracted(B b) {
	//	b.m();
	//	return b;
	//}
	//
	//void A::foo() {
	//	B b;
	//	b = extracted(b);
	//	b.n();
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//namespace ns1 { namespace ns2 {
	//struct B {
	//  void m();
	//  void n() const;
	//};
	//}}
	//
	//#endif /*B_H_*/
	public void testUsingDeclaration() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, true);
		assertRefactoringSuccess();
	}

	//A.cpp
	//void test() {
	//	for (int i = 0; i < 2; i++) {
	//		/*$*/for (int j = 0; j < i; j++) {
	//			for (int k = 0; k < j; k++) {
	//			}
	//		}/*$$*/
	//	}
	//}
	//====================
	//void extracted(int i) {
	//	for (int j = 0; j < i; j++) {
	//		for (int k = 0; k < j; k++) {
	//		}
	//	}
	//}
	//
	//void test() {
	//	for (int i = 0; i < 2; i++) {
	//		extracted(i);
	//	}
	//}
	public void testNestedLoops_Bug424876() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//#define TWO 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	/*$*/++i;
	//	i += TWO;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//#define TWO 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::extracted(int i) {
	//	++i;
	//	i += TWO;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testObjectStyleMacro() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	void extracted(int* j);
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
	//	int* i = new int(2);
	//	/*$*/++*i;
	//	help();/*$$*/
	//	return *i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//void A::extracted(int* j) {
	//	++*j;
	//	help();
	//}
	//
	//int A::foo() {
	//	int* i = new int(2);
	//	extracted(i);
	//	return *i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testRenamedParameter() throws Exception {
		parameterRename.put("i", "j");
		assertRefactoringSuccess();
	}

	//A.c
	//struct A {
	//	int i;
	//	int j;
	//};
	//
	//int test() {
	//	struct A a = { 1, 2 };
	//	return /*$*/a.i + a.j/*$$*/;
	//}
	//====================
	//struct A {
	//	int i;
	//	int j;
	//};
	//
	//int extracted(const struct A* a) {
	//	return a->i + a->j;
	//}
	//
	//int test() {
	//	struct A a = { 1, 2 };
	//	return extracted(&a);
	//}
	public void testInputParameterPassedByPointer() throws Exception {
		assertRefactoringSuccess();
	}

	//A.c
	//int test() {
	//	int i = 0;
	//	int j = 1;
	//	/*$*/int k = i;
	//	i = j;
	//	j = k;/*$$*/
	//	return i - j;
	//}
	//====================
	//void swap(int* i, int* j) {
	//	int k = *i;
	//	*i = *j;
	//	*j = k;
	//}
	//
	//int test() {
	//	int i = 0;
	//	int j = 1;
	//	swap(&i, &j);
	//	return i - j;
	//}
	public void testOutputParameterPassedByPointer() throws Exception {
		extractedFunctionName = "swap";
		returnValue = NO_RETURN_VALUE;
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//public:
	//	int method();
	//  int const_method() const;
	//};

	//A.cpp
	//#include "A.h"
	//
	//int test() {
	//	A a, b;
	//	return /*$*/a.method() + b.const_method()/*$$*/ + a.const_method();
	//}
	//====================
	//#include "A.h"
	//
	//int extracted(A b, A* a) {
	//	return a->method() + b.const_method();
	//}
	//
	//int test() {
	//	A a, b;
	//	return extracted(b, &a) + a.const_method();
	//}
	public void testOutputParameterWithMethodCall() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, true);
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//public:
	//	A(int i, const char* s);
	//	int method();
	//};

	//A.cpp
	//#include "A.h"
	//
	//void test(int i, const char* s) {
	//	/*$*/A a(i, s);/*$$*/
	//	if (i != 0)
	//		a.method();
	//}
	//====================
	//#include "A.h"
	//
	//A extracted(int i, const char* s) {
	//	A a(i, s);
	//	return a;
	//}
	//
	//void test(int i, const char* s) {
	//	A a = extracted(i, s);
	//	if (i != 0)
	//		a.method();
	//}
	public void testReturnValueWithCtorInitializer() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	void extracted(int& i);
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
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//void A::extracted(int& i) {
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithoutReturnValue() throws Exception {
		returnValue = NO_RETURN_VALUE;
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i, int b);
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
	//	int i = 2;
	//	int b = i;
	//	/*$*/++i;
	//	i = i + b;
	//	help();/*$$*/
	//	++b;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i, int b) {
	//	++i;
	//	i = i + b;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int b = i;
	//	i = extracted(i, b);
	//	++b;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testReturnValueSelection_1() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i, int y, float x, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	/*$*/++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();/*$$*/
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i, int y, float x, B* b) {
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	i = extracted(i, y, x, b);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testReturnValueSelection_2() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	bool extracted(bool y, float x, int& i, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	bool y = false;
	//	/*$*/++i;
	//	b->hello(y);
	//	y = !y;
	//	i = i + x;
	//	help();/*$$*/
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//bool A::extracted(bool y, float x, int& i, B* b) {
	//	++i;
	//	b->hello(y);
	//	y = !y;
	//	i = i + x;
	//	help();
	//	return y;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	bool y = false;
	//	y = extracted(y, x, i, b);
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//	void hello(bool y);
	//};
	//
	//#endif /*B_H_*/
	public void testReturnValueSelection_3() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	float extracted(int& i, int y, float x, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	/*$*/++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();/*$$*/
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//float A::extracted(int& i, int y, float x, B* b) {
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	return x;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	x = extracted(i, y, x, b);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//	void hello(float y);
	//};
	//
	//#endif /*B_H_*/
	public void testExplicitlyAssignedReturnValue() throws Exception {
		returnValue = "x";
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	B* extracted(int& i, int y, float x, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	/*$*/++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();/*$$*/
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//B* A::extracted(int& i, int y, float x, B* b) {
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	return b;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	b = extracted(i, y, x, b);
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//	void hello(float y);
	//};
	//
	//#endif /*B_H_*/
	public void testExplicitlyAssignedReturnValueAndOutputParameter() throws Exception {
		returnValue = "b";
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
	//
	//private:
	//	int help();
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
	//
	//protected:
	//	int extracted(int i);
	//
	//private:
	//	int help();
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
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testProtectedVisibility() throws Exception {
		visibility = VisibilityEnum.v_protected;
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
	//
	//private:
	//	int help();
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
	//	int extracted(int i);
	//
	//private:
	//	int help();
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
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testPublicVisibility() throws Exception {
		visibility = VisibilityEnum.v_public;
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
	//	int foo() const;
	//
	//private:
	//	int help();
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
	//	int foo() const;
	//
	//private:
	//	int help();
	//	int extracted(int i) const;
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
	//int A::foo() const {
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) const {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() const {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testConstMethod() throws Exception {
		assertRefactoringSuccess();
	}

	//test.cpp
	//int test(bool param) {
	//	int a = 42;
	//	char b = '0';
	//
	//	if (param) {
	//		/*$*/b += a;
	//		a += b;/*$$*/
	//	} else {
	//		b -= a;
	//		a -= b;
	//	}
	//  return a;
	//}
	//====================
	//int extracted(char b, int a) {
	//	b += a;
	//	a += b;
	//	return a;
	//}
	//
	//int test(bool param) {
	//	int a = 42;
	//	char b = '0';
	//
	//	if (param) {
	//		a = extracted(b, a);
	//	} else {
	//		b -= a;
	//		a -= b;
	//	}
	//  return a;
	//}
	public void testOutputParametersDetectionInIfStatement() throws Exception {
		assertRefactoringSuccess();
	}

	//main.c
	//int main() {
	//	int a = 42;
	//	char b = '0';
	//
	//	switch (a) {
	//	case 0:
	//		/*$*/b += a;
	//		a += b;/*$$*/
	//		break;
	//	case 42:
	//		b -= a;
	//		a -= b;
	//		break;
	//	default:
	//		b ++;
	//		a += b;
	//		break;
	//	}
	//	return b;
	//}
	//====================
	//char extracted(char b, int a) {
	//	b += a;
	//	a += b;
	//	return b;
	//}
	//
	//int main() {
	//	int a = 42;
	//	char b = '0';
	//
	//	switch (a) {
	//	case 0:
	//		b = extracted(b, a);
	//		break;
	//	case 42:
	//		b -= a;
	//		a -= b;
	//		break;
	//	default:
	//		b ++;
	//		a += b;
	//		break;
	//	}
	//	return b;
	//}
	public void testOutputParametersDetectionInSwitchStatement_Bug302406() throws Exception {
		assertRefactoringSuccess();
	}

	//test.c
	//void print(int i, double a, double b);
	//
	//void test(double x) {
	//	double s = 0;
	//	double y = 1;
	//	int i;
	//	for (i = 0; i < 10; i++) {
	//      print(x, s);
	//      /*$*/x *= x;
	//      y *= i;
	//		s += x / y;/*$$*/
	//	}
	//}
	//====================
	//void print(int i, double a, double b);
	//
	//extracted(double x, int i, double* y, double* s) {
	//	x *= x;
	//	*y *= i;
	//	*s += x / *y;
	//	return x;
	//}
	//
	//void test(double x) {
	//	double s = 0;
	//	double y = 1;
	//	int i;
	//	for (i = 0; i < 10; i++) {
	//      print(x, s);
	//		x = extracted(x, i, &y, &s);
	//	}
	//}
	public void testOutputParametersDetectionInForLoop() throws Exception {
		assertRefactoringSuccess();
	}

	//test.c
	//void test() {
	//	int i = 0;
	//	while (i <= 10)
	//		/*$*/i++;/*$$*/
	//}
	//====================
	//int extracted(int i) {
	//	i++;
	//	return i;
	//}
	//
	//void test() {
	//	int i = 0;
	//	while (i <= 10)
	//		i = extracted(i);
	//}
	public void testOutputParametersDetectionInWhileLoop() throws Exception {
		assertRefactoringSuccess();
	}

	//test.c
	//void test() {
	//	int i = 0;
	//loop:
	//	if (i > 10) return;
	//	/*$*/i++;/*$$*/
	//	goto loop;
	//}
	//====================
	//int extracted(int i) {
	//	i++;
	//	return i;
	//}
	//
	//void test() {
	//	int i = 0;
	//loop:
	//	if (i > 10) return;
	//	i = extracted(i);
	//	goto loop;
	//}
	public void testOutputParametersDetectionWithGotoLoopSimple() throws Exception {
		assertRefactoringSuccess();
	}

	//test.c
	//void test() {
	//	int a = 0, b = 0, c = 0, d = 0;
	//loop1:
	//	if (a > 1) return;
	//	goto loop1;
	//loop2:
	//	if (b > 2) return;
	//loop3:
	//	if (c > 3) return;
	//	goto loop2;
	//loop4:
	//	if (d > 4) return;
	//	goto loop3;
	//	/*$*/a++;
	//	b++;
	//	c++;
	//	d++;/*$$*/
	//	goto loop4;
	//}
	//====================
	//int extracted(int a, int b, int* c, int* d) {
	//	a++;
	//	b++;
	//	*c++;
	//	*d++;
	//	return b;
	//}
	//
	//void test() {
	//	int a = 0, b = 0, c = 0, d = 0;
	//loop1:
	//	if (a > 1) return;
	//	goto loop1;
	//loop2:
	//	if (b > 2) return;
	//loop3:
	//	if (c > 3) return;
	//	goto loop2;
	//loop4:
	//	if (d > 4) return;
	//	goto loop3;
	//	b = extracted(a, b, &c, &d);
	//	goto loop4;
	//}
	public void testOutputParametersDetectionWithGotoLoopComplex() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//void method() {
	//	/*$*/for (int var = 0; var < 100; ++var) {
	//		if (var < 50)
	//			continue;
	//	}/*$$*/
	//}
	//====================
	//void loop() {
	//	for (int var = 0; var < 100; ++var) {
	//		if (var < 50)
	//			continue;
	//	}
	//}
	//
	//void method() {
	//	loop();
	//}
	public void testDoNotReturnVariablesThatAreNotUsed() throws Exception {
		extractedFunctionName = "loop";
		assertRefactoringSuccess();
	}

	//main.h
	//void method() {
	//	/*$*/if (true)
	//		return;/*$$*/
	//	//unreachable
	//}
	public void testDoNotExtractCodeContainingReturn() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//void function() {
	//	for (int var = 0; var < 100; ++var) {
	//		/*$*/if (var < 50)
	//			continue;/*$$*/
	//	}
	//}
	public void testDoNotExtractCodeContainingContinue() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//void test() {
	//	int b[10];
	//	/*$*/for (auto a : b) {
	//		if (a == 5)
	//			continue;
	//	}/*$$*/
	//}
	//====================
	//void extracted(int b[10]) {
	//	for (auto a : b) {
	//		if (a == 5)
	//			continue;
	//	}
	//}
	//
	//void test() {
	//	int b[10];
	//	extracted(b);
	//}
	public void testContinueInsideRangeBasedLoop() throws Exception {
		assertRefactoringSuccess();
	}

	//Test.cpp
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond, cond)
	//
	//void testFuerRainer() {
	//	int i = int();
	//	/*$*/++i;
	//	ASSERT(i);
	//	--i;/*$$*/
	//}
	//====================
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond, cond)
	//
	//void runTest(int i) {
	//	++i;
	//	ASSERT(i);
	//	--i;
	//}
	//
	//void testFuerRainer() {
	//	int i = int();
	//	runTest(i);
	//}
	public void testCommentsWithMacroCallInSelectedCodeForgetsTheMacro() throws Exception {
		extractedFunctionName = "runTest";
		assertRefactoringSuccess();
	}

	//Test.cpp
	//#include <string>
	//
	//using namespace std;
	//
	//int const INITIAL_CAPACITY = 10;
	//
	//int main() {
	//	int m_capacity;
	//	/*$*/m_capacity += INITIAL_CAPACITY;
	//	string* newElements = new string[m_capacity];/*$$*/
	//	newElements[0] = "s";
	//}
	//====================
	//#include <string>
	//
	//using namespace std;
	//
	//int const INITIAL_CAPACITY = 10;
	//
	//string* runTest(int m_capacity) {
	//	m_capacity += INITIAL_CAPACITY;
	//	string* newElements = new string[m_capacity];
	//	return newElements;
	//}
	//
	//int main() {
	//	int m_capacity;
	//	string* newElements = runTest(m_capacity);
	//	newElements[0] = "s";
	//}
	public void testStringArray() throws Exception {
		extractedFunctionName = "runTest";
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
	//	int foo(int& a);
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
	//	int foo(int& a);
	//
	//private:
	//	void extracted(int b, int c, int& a);
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
	//int A::foo(int& a) {
	//	int b = 7;
	//	int c = 8;
	//	/*$*/a = b + c;/*$$*/
	//	return a;
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
	//void A::extracted(int b, int c, int& a) {
	//	a = b + c;
	//}
	//
	//int A::foo(int& a) {
	//	int b = 7;
	//	int c = 8;
	//	extracted(b, c, a);
	//	return a;
	//}
	public void testReferenceVariable_Bug239059() throws Exception {
		assertRefactoringSuccess();
	}

	//Test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class RetValueType {
	//};
	//
	//typedef RetValueType RetType;
	//
	//class Test {
	//public:
	//	Test();
	//	virtual ~Test();
	//private:
	//	void test();
	//};
	//
	//#endif /* TEST_H_ */
	//====================
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class RetValueType {
	//};
	//
	//typedef RetValueType RetType;
	//
	//class Test {
	//public:
	//	Test();
	//	virtual ~Test();
	//private:
	//	void test();
	//	RetType extracted();
	//};
	//
	//#endif /* TEST_H_ */

	//Test.cpp
	//#include "Test.h"
	//
	//Test::Test() {
	//}
	//
	//Test::~Test() {
	//}
	//
	//void Test::test() {
	//	RetType v = /*$*/RetType()/*$$*/;
	//}
	//====================
	//#include "Test.h"
	//
	//Test::Test() {
	//}
	//
	//Test::~Test() {
	//}
	//
	//RetType Test::extracted() {
	//	return RetType();
	//}
	//
	//void Test::test() {
	//	RetType v = extracted();
	//}
	public void testTypedef_Bug241717() throws Exception {
		assertRefactoringSuccess();
	}

	//Test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//struct A {
	//	typedef A B;
	//	const B* m(const char* p);
	//};
	//
	//#endif // TEST_H_
	//====================
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//struct A {
	//	typedef A B;
	//	const B* m(const char* p);
	//};
	//
	//#endif // TEST_H_

	//Test.cpp
	//#include "Test.h"
	//
	//void test() {
	//	auto x = new A();
	//	const auto* y = "";
	//	auto r = /*$*/x->m(y)/*$$*/;
	//}
	//====================
	//#include "Test.h"
	//
	//const A::B* extracted(A* x, const char* y) {
	//	return x->m(y);
	//}
	//
	//void test() {
	//	auto x = new A();
	//	const auto* y = "";
	//	auto r = extracted(x, y);
	//}
	public void testAuto_Bug422727() throws Exception {
		assertRefactoringSuccess();
	}

	//testString.h
	//namespace test {
	//
	//class string {
	//public:
	//	friend string operator+(const string& lhs, const string& rhs) {
	//		return rhs;
	//	}
	//
	//	string operator+(const string& rhs) { return rhs; }
	//	string(char* cp) {}
	//	string() {};
	//};
	//
	//}

	//Test.cpp
	//#include "testString.h"
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return /*$*/"<" + name + ">"/*$$*/ + "</" + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "testString.h"
	//
	//test::string startTag(test::string name) {
	//	return "<" + name + ">";
	//}
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return startTag(name) + "</" + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	public void testQualifiedReturnTypeName_Bug248238_1() throws Exception {
		extractedFunctionName = "startTag";
		assertRefactoringSuccess();
	}

	//testString.h
	//
	//namespace test {
	//
	//class string2 {
	//public:
	//	friend string2 operator+(const string2& lhs, const string2& rhs) {
	//		return rhs;
	//	}
	//
	//	string2 operator+(const string2& rhs) { return rhs; }
	//	string2(char* cp) {}
	//	string2() {};
	//};
	//
	//typedef string2 string;
	//
	//}

	//Test.cpp
	//#include "testString.h"
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return /*$*/"<" + name + ">"/*$$*/ + "</" + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "testString.h"
	//
	//test::string startTag(test::string name) {
	//	return "<" + name + ">";
	//}
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return startTag(name) + "</" + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	public void testQualifiedReturnTypeName_Bug248238_2() throws Exception {
		extractedFunctionName = "startTag";
		assertRefactoringSuccess();
	}

	//testString.h
	//
	//namespace test {
	//
	//class string {
	//public:
	//	friend string operator+(const string& lhs, const string& rhs) {
	//		return rhs;
	//	}
	//
	//	string operator+(const string& rhs) { return rhs; }
	//	string(char* cp) {}
	//	string() {};
	//};
	//
	//}

	//Test.cpp
	//#include "testString.h"
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return "<" + name + ">" + /*$*/"</" + name + ">"/*$$*/;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "testString.h"
	//
	//const char* endTag(test::string name) {
	//	return "</" + name + ">";
	//}
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return "<" + name + ">" + endTag(name);
	//}
	//
	//int main() {
	//	return 0;
	//}
	public void testReturnStatementExpression_Bug248622_1() throws Exception {
		extractedFunctionName = "endTag";
		assertRefactoringSuccess();
	}

	//testString.h
	//
	//namespace test {
	//
	//class string {
	//public:
	//	friend string operator+(const string& lhs, const string& rhs) {
	//		return rhs;
	//	}
	//
	//	string operator+(const string& rhs) { return rhs; }
	//	string(char* cp) {}
	//	string() {};
	//};
	//
	//}

	//Test.cpp
	//#include "testString.h"
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return "<" + name + /*$*/">" + "</"/*$$*/ + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "testString.h"
	//
	//const char* extracted() {
	//	return ">" + "</";
	//}
	//
	//test::string toXML() {
	//	test::string name;
	//	name = "hello";
	//	return "<" + name + extracted() + name + ">";
	//}
	//
	//int main() {
	//	return 0;
	//}
	public void testReturnStatementExpression_Bug248622_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main(int argc, char** argv) {
	//	/*$*/int a = 0;
	//	{
	//		a++;
	//	}/*$$*/
	//}
	//====================
	//void extracted() {
	//	int a = 0;
	//	{
	//		a++;
	//	}
	//}
	//
	//int main(int argc, char** argv) {
	//	extracted();
	//}
	public void testBlock_Bug262000() throws Exception {
		assertRefactoringSuccess();
	}

	//classhelper.h
	//// Comment
	////
	//// Comment
	//// Comment
	//#ifndef utils_classhelper_h_seen
	//#define utils_classhelper_h_seen
	//#define IMPORTANT_VALUE 1
	//#endif

	//test.h
	///*
	// * Copyright 2009
	// */
	//#ifndef test_h_seen
	//#define test_h_seen
	//
	//#include "classhelper.h"
	//
	//class Test {
	//  public:
	//	/**
	//	 * Small class with some comments
	//	 */
	//	Test();
	//
	//	/** Calculate important things.
	//	 * @returns the result of the calculation
	//	 */
	//	int calculateStuff();
	//
	//  private:
	//	/**
	//	 * Retain a value for something.
	//	 */
	//	int m_value;
	//};
	//
	//#endif
	//====================
	///*
	// * Copyright 2009
	// */
	//#ifndef test_h_seen
	//#define test_h_seen
	//
	//#include "classhelper.h"
	//
	//class Test {
	//  public:
	//	/**
	//	 * Small class with some comments
	//	 */
	//	Test();
	//
	//	/** Calculate important things.
	//	 * @returns the result of the calculation
	//	 */
	//	int calculateStuff();
	//
	//  private:
	//	/**
	//	 * Retain a value for something.
	//	 */
	//	int m_value;
	//
	//	int extracted();
	//};
	//
	//#endif

	//test.cpp
	//#include "test.h"
	//
	//Test::Test() {}
	//
	//int Test::calculateStuff() {
	//	// refactor these lines to a new method:
	//	/*$*/int result = m_value;
	//	result += 10;
	//	result *= 10;/*$$*/
	//	return result;
	//}
	//====================
	//#include "test.h"
	//
	//Test::Test() {}
	//
	//int Test::extracted() {
	//	// refactor these lines to a new method:
	//	int result = m_value;
	//	result += 10;
	//	result *= 10;
	//	return result;
	//}
	//
	//int Test::calculateStuff() {
	//	// refactor these lines to a new method:
	//	int result = extracted();
	//	return result;
	//}
	public void testCommentsInHeader_Bug264712() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//
	//int myFunc() {
	//	return 5;
	//}
	//
	//int main() {
	//	int a = 0;
	///*$*/try {
	//		a = myFunc();
	//	} catch (const int&) {
	//		a = 3;
	//	}/*$$*/
	//	return a;
	//}
	//====================
	//
	//int myFunc() {
	//	return 5;
	//}
	//
	//int extracted(int a) {
	//	try {
	//		a = myFunc();
	//	} catch (const int&) {
	//		a = 3;
	//	}
	//	return a;
	//}
	//
	//int main() {
	//	int a = 0;
	//	a = extracted(a);
	//	return a;
	//}
	public void testCatchUnnamedException_Bug281564() throws Exception {
		assertRefactoringSuccess();
	}

	//main.c
	//int main() {
	//	int a,b;
	//	/*$*/b=a*2;/*$$*/
	//	return a;
	//}
	//====================
	//void extracted(int b, int a) {
	//	b = a * 2;
	//}
	//
	//int main() {
	//	int a,b;
	//	extracted(b, a);
	//	return a;
	//}
	public void testCFunction_Bug282004() throws Exception {
		assertRefactoringSuccess();
	}

	//main.c
	//int main() {
	//	int a, b;
	//	/*$*/a = b * 2;/*$$*/
	//	return a;
	//}
	//====================
	//int extracted(int a, int b) {
	//	a = b * 2;
	//	return a;
	//}
	//
	//int main() {
	//	int a, b;
	//	a = extracted(a, b);
	//	return a;
	//}
	public void testCFunction_Bug288268() throws Exception {
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
	//
	//private:
	//	int help();
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
	//	virtual int extracted(int i);
	//
	//private:
	//	int help();
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
	//	int i = 2;
	//	//comment
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) {
	//	//comment
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testVirtual() throws Exception {
		visibility = VisibilityEnum.v_public;
		virtual = true;
		assertRefactoringSuccess();
	}

	//main.c
	//int main() {
	//	int f;
	//	/*$*/int a = 0;
	//	int b = 1;
	//
	//	for (int i = 0; i < 10; ++i) {
	//		int c = a + b;
	//		a = b;
	//		b = c;
	//	}/*$$*/
	//
	//	f = b;
	//}
	//====================
	//int fib() {
	//	int a = 0;
	//	int b = 1;
	//	for (int i = 0; i < 10; ++i) {
	//		int c = a + b;
	//		a = b;
	//		b = c;
	//	}
	//	return b;
	//}
	//
	//int main() {
	//	int f;
	//	int b = fib();
	//	f = b;
	//}
	public void testHandlingOfBlankLines() throws Exception {
		extractedFunctionName = "fib";
		assertRefactoringSuccess();
	}

	//A.cpp
	//void test() {
	//}
	//
	//template<typename T>
	//int tempFunct() {
	//	T i;
	//	i = 0;
	//	/*$*/i++;
	//	i += 3;/*$$*/
	//
	//	return 0;
	//}
	//====================
	//void test() {
	//}
	//
	//template<typename T>
	//void extracted(T i) {
	//	i++;
	//	i += 3;
	//}
	//
	//template<typename T>
	//int tempFunct() {
	//	T i;
	//	i = 0;
	//	extracted(i);
	//	return 0;
	//}
	public void testTemplateFunction() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void test() {
	//}
	//
	//template<typename T>
	//int tempFunct(T p) {
	//	/*$*/++p;
	//	p + 4;/*$$*/
	//	return 0;
	//}
	//====================
	//void test() {
	//}
	//
	//template<typename T>
	//void extracted(T p) {
	//	++p;
	//	p + 4;
	//}
	//
	//template<typename T>
	//int tempFunct(T p) {
	//	extracted(p);
	//	return 0;
	//}
	public void testTemplateFunctionWithTemplateParameter() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void test() {
	//}
	//
	//template<typename T>
	//int tempFunct() {
	//	/*$*/T p;
	//	p = 0;
	//	p + 4;/*$$*/
	//	p + 2;
	//	return 0;
	//}
	//====================
	//void test() {
	//}
	//
	//template<typename T>
	//T extracted() {
	//	T p;
	//	p = 0;
	//	p + 4;
	//	return p;
	//}
	//
	//template<typename T>
	//int tempFunct() {
	//	T p = extracted();
	//	p + 2;
	//	return 0;
	//}
	public void testTemplateFunctionWithTemplateTypeDeclaration() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
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
	//	int i = 2;
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int i = 2;
	//	i = extracted(i);
	//}
	//
	//int A::extracted(int i) {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicates() throws Exception {
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
	//	void foo();
	//	int i;
	//
	//private:
	//	int help();
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
	//	void foo();
	//	int i;
	//
	//private:
	//	int help();
	//	int extracted(int j, int& a);
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
	//	int j = 0;
	//	i++;
	//	j++;
	//	help();
	//}
	//
	//void A::foo() {
	//	int j = 0;
	//	int a = 1;
	//	/*$*/j++;
	//	a++;
	//	help();/*$$*/
	//	a++;
	//	j++;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int j = 0;
	//	i = extracted(i, j);
	//}
	//
	//int A::extracted(int j, int& a) {
	//	j++;
	//	a++;
	//	help();
	//	return j;
	//}
	//
	//void A::foo() {
	//	int j = 0;
	//	int a = 1;
	//	j = extracted(j, a);
	//	a++;
	//	j++;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicateWithField() throws Exception {
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
	//	void foo();
	//	int i;
	//	int field;
	//
	//private:
	//	int help();
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
	//	void foo();
	//	int i;
	//	int field;
	//
	//private:
	//	int help();
	//	int extracted(int j);
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
	//	int j = 0;
	//	int a = 1;
	//	a++;
	//	j++;
	//	help();
	//}
	//
	//void A::foo() {
	//	int j = 0;
	//
	//	/*$*/field++;
	//	j++;
	//	help();/*$$*/
	//	field++;
	//	j++;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int j = 0;
	//	int a = 1;
	//	a++;
	//	j++;
	//	help();
	//}
	//
	//int A::extracted(int j) {
	//	field++;
	//	j++;
	//	help();
	//	return j;
	//}
	//
	//void A::foo() {
	//	int j = 0;
	//
	//	j = extracted(j);
	//	field++;
	//	j++;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicateWithFieldInMarkedScope() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i, float& j);
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
	//	int oo = 99;
	//	float blabla = 0;
	//	++oo;
	//	blabla += 1;
	//	help();
	//	blabla += 1;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float j = 8989;
	//	/*$*/++i;
	//	j+=1;
	//	help();/*$$*/
	//	j++;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int oo = 99;
	//	float blabla = 0;
	//	oo = extracted(oo, blabla);
	//	blabla += 1;
	//}
	//
	//int A::extracted(int i, float& j) {
	//	++i;
	//	j += 1;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float j = 8989;
	//	i = extracted(i, j);
	//	j++;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicatesWithDifferentNames() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int j, int i);
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
	//	int aa = 9;
	//	int bb = 99;
	//	aa += bb;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int j = 3;
	//	/*$*/i += j;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int aa = 9;
	//	int bb = 99;
	//	aa = extracted(bb, aa);
	//}
	//
	//int A::extracted(int j, int i) {
	//	i += j;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int j = 3;
	//	i = extracted(j, i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicatesWithDifferentNamesAndReordering() throws Exception {
		parameterOrder = new int[] { 1, 0 };
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	void extracted(int& i, float j);
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
	//	int oo = 99;
	//	float blabla = 0;
	//	++oo;
	//	blabla += 1;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float j = 8989;
	//	/*$*/++i;
	//	j+=1;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int oo = 99;
	//	float blabla = 0;
	//	extracted(oo, blabla);
	//}
	//
	//void A::extracted(int& i, float j) {
	//	++i;
	//	j += 1;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float j = 8989;
	//	extracted(i, j);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicatesWithDifferentNamesAndOutputParameter() throws Exception {
		returnValue = NO_RETURN_VALUE;
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
	//	void foo();
	//
	//private:
	//	int help();
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
	//	void foo();
	//
	//private:
	//	int help();
	//	void extracted(int i);
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
	//	int i = 2;
	//	++i;// No Duplicate
	//	help();
	//	++i;// this is the reason
	//}
	//
	//void A::foo() {
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int i = 2;
	//	++i;// No Duplicate
	//	help();
	//	++i;// this is the reason
	//}
	//
	//void A::extracted(int i) {
	//	++i;
	//	help();
	//}
	//
	//void A::foo() {
	//	int i = 2;
	//	extracted(i);
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testOutputParameterInDuplicateButNotInOriginal() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	int extracted(int i, int y, float x, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	/*$*/++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();/*$$*/
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	i = extracted(i, y, x, b);
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::extracted(int i, int y, float x, B* b) {
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	i = extracted(i, y, x, b);
	//	b->hello(y);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//	void hello(float y);
	//};
	//
	//#endif /*B_H_*/
	public void testMethodCall() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include "B.h"
	//
	//class A {
	//public:
	//	A();
	//	virtual ~A();
	//	int foo();
	//
	//private:
	//	int help();
	//	int extracted(int i, int y, float x, B* b);
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
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	/*$*/++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();/*$$*/
	//	b->hello(y);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	i = extracted(i, y, x, b);
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::extracted(int i, int y, float x, B* b) {
	//	++i;
	//	b->hello(y);
	//	i = i + x;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	float x = i;
	//	B* b = new B();
	//	int y = x + i;
	//	i = extracted(i, y, x, b);
	//	b->hello(y);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//B.h
	//#ifndef B_H_
	//#define B_H_
	//
	//class B {
	//public:
	//	B();
	//	virtual ~B();
	//	void hello(float y);
	//};
	//
	//#endif /*B_H_*/
	public void testMethodCallWithDuplicate() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
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
	//	int i = 2;
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	/*$*/++i;
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int i = 2;
	//	i = extracted(i);
	//}
	//
	//int A::extracted(int i) {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testDuplicatesAndComments() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//#define ADD(a,b) a + b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	/*$*/++i;
	//	i = ADD(i, 42);
	//	help();/*$$*/
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//#define ADD(a,b) a + b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::extracted(int i) {
	//	++i;
	//	i = ADD(i, 42);
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testFunctionStyleMacro_1() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int ii);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//#define ADD(a,ab) a + ab + 2 + a
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int ii = 2;
	//	/*$*/++ii;
	//	ii = ADD(ii, 42);
	//	help();/*$$*/
	//	return ii;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//#define ADD(a,ab) a + ab + 2 + a
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::extracted(int ii) {
	//	++ii;
	//	ii = ADD(ii, 42);
	//	help();
	//	return ii;
	//}
	//
	//int A::foo() {
	//	int ii = 2;
	//	ii = extracted(ii);
	//	return ii;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testFunctionStyleMacro_2() throws Exception {
		assertRefactoringSuccess();
	}

	//Test.cpp
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond, cond)
	//
	//void testFuerRainer() {
	//	int i = int();
	//	/*$*/++i;
	//	//Leading Comment
	//	ASSERT(i);
	//	//Trailling Comment
	//	--i;/*$$*/
	//}
	//====================
	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond, cond)
	//
	//void runTest(int i) {
	//	++i;
	//	//Leading Comment
	//	ASSERT(i);
	//	//Trailling Comment
	//	--i;
	//}
	//
	//void testFuerRainer() {
	//	int i = int();
	//	runTest(i);
	//}
	public void testFunctionStyleMacro_3() throws Exception {
		extractedFunctionName = "runTest";
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int& i, int b);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//#define ADD(b) b = b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int b = 42;
	//	/*$*/++i;
	//	help();
	//	ADD(b);/*$$*/
	//	b += 2;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//#define ADD(b) b = b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::extracted(int& i, int b) {
	//	++i;
	//	help();
	//	ADD(b);
	//	return b;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int b = 42;
	//	b = extracted(i, b);
	//	b += 2;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testReturnValueAssignedInMacro() throws Exception {
		returnValue = "b";
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int bb);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//#define ADD(b) b = b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int bb = 42;
	//	++i;
	//	/*$*/ADD(bb);
	//	ADD(bb);/*$$*/
	//	bb += 2;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//#define ADD(b) b = b + 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::extracted(int bb) {
	//	ADD(bb);
	//	ADD(bb);
	//	return bb;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	int bb = 42;
	//	++i;
	//	bb = extracted(bb);
	//	bb += 2;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testMultipleMacros() throws Exception {
		assertRefactoringSuccess();
	}

	//test.cpp
	//template<typename T>
	//void p(T p) {}
	//
	//#define TRACE(var) p(__LINE__), p(": "), p(#var), p("="), p(var)
	//
	//void test(int x, int y) {
	//	/*$*/TRACE(x);/*$$*/
	//	TRACE(y);
	//	TRACE(x);
	//}
	//====================
	//template<typename T>
	//void p(T p) {}
	//
	//#define TRACE(var) p(__LINE__), p(": "), p(#var), p("="), p(var)
	//
	//void extracted(int x) {
	//	TRACE(x);
	//}
	//
	//void test(int x, int y) {
	//	extracted(x);
	//	TRACE(y);
	//	extracted(x);
	//}
	public void testLiteralFromMacro() throws Exception {
		assertRefactoringSuccess();
	}

	//test.cpp
	//#define LABEL(a, b) a ## b
	//#define MACRO1(cond1, cond2, var, n) \
	//if (cond1) { \
	//  if (cond2) { \
	//    var++; \
	//    goto LABEL(label, n); \
	//  } \
	//} else LABEL(label, n): \
	//  var--
	//#define MACRO(var) MACRO1(true, false, var, __COUNTER__)
	//
	//void test1(int x, int y) {
	//  MACRO(x);
	//  MACRO(y);
	//  /*$*/MACRO(x);/*$$*/
	//}
	//====================
	//#define LABEL(a, b) a ## b
	//#define MACRO1(cond1, cond2, var, n) \
	//if (cond1) { \
	//  if (cond2) { \
	//    var++; \
	//    goto LABEL(label, n); \
	//  } \
	//} else LABEL(label, n): \
	//  var--
	//#define MACRO(var) MACRO1(true, false, var, __COUNTER__)
	//
	//void extracted(int x) {
	//	MACRO(x);
	//}
	//
	//void test1(int x, int y) {
	//	extracted(x);
	//  MACRO(y);
	//	extracted(x);
	//}
	public void testLabelFromMacro() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted();
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
	//	int i = 2;
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted() {
	//	int i = 2;
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = extracted();
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	//  fileName="file:${projectPath}/A.cpp"
	//  flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	//  name="extracted" project="RegressionTestProject" selection="57,25" visibility="private"/>
	//</session>
	//
	public void testHistoryWithVariableDefinedInScope() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
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
	//	int i = 2;
	//	//comment
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
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
	//int A::extracted(int i) {
	//	//comment
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	// fileName="file:${projectPath}/A.cpp"
	//  flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	//  name="extracted" project="RegressionTestProject" selection="69,24" visibility="private"/>
	//</session>
	public void testHistory() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	// Comment
	//	i = 7;
	//	return i;
	//}
	//====================
	//int extracted(int i) {
	//	// Comment
	//	i = 7;
	//	return i;
	//}
	//
	//int main() {
	//	int i;
	//	// Comment
	//	i = extracted(i);
	//	return i;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	// fileName="file:${projectPath}/main.cpp"
	// flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	// name="extracted" project="RegressionTestProject" selection="34,6" visibility="private"/>
	//</session>
	public void testHistoryWithLeadingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	i = 7; // Comment
	//	return i;
	//}
	//====================
	//int extracted(int i) {
	//	i = 7; // Comment
	//	return i;
	//}
	//
	//int main() {
	//	int i;
	//	i = extracted(i);
	//	return i;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	// fileName="file:${projectPath}/main.cpp"
	// flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	// name="extracted" project="RegressionTestProject" selection="22,6" visibility="private"/>
	//</session>
	public void testHistoryWithTrailingComment() throws Exception {
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
	//
	//private:
	//	int help();
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
	//
	//private:
	//	int help();
	//	int extracted(int i);
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
	//	int oo = 99;
	//	++oo;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//	int oo = 99;
	//	oo = extracted(oo);
	//}
	//
	//int A::extracted(int i) {
	//	++i;
	//	help();
	//	return i;
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	i = extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	// fileName="file:${projectPath}/A.cpp" flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	// name="extracted" project="RegressionTestProject" replaceDuplicates="true" selection="99,13" visibility="private"/>
	//</session>
	public void testHistoryWithDuplicatesWithDifferentNames() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	/*$*/int array[] = { 1, 2, 3 };
	//	int i = array[0];/*$$*/
	//	return i;
	//}
	//====================
	//int extracted() {
	//	int array[] = { 1, 2, 3 };
	//	int i = array[0];
	//	return i;
	//}
	//
	//int main() {
	//	int i = extracted();
	//	return i;
	//}
	public void testExtractArrayInitializer_Bug412380() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	auto var = 1;
	//	/*$*/var;/*$$*/
	//}
	//====================
	//void extracted(int var) {
	//	var;
	//}
	//
	//int main() {
	//	auto var = 1;
	//	extracted(var);
	//}
	public void testExtractWithAutoVar() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int foo() {
	//	return 42;
	//}
	//
	//int main() {
	//	int a = /*$*/foo()/*$$*/;
	//}
	//====================
	//int foo() {
	//	return 42;
	//}
	//
	//int extracted() {
	//	return foo();
	//}
	//
	//int main() {
	//	int a = extracted();
	//}
	public void testExtractFunctionCallExpression_Bug396338() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int foo() {
	//	return 42;
	//}
	//
	//int main() {
	//	int a = /*$*/foo/*$$*/();
	//}
	public void testExtractFunctionName_Bug396338() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int foo(int a) {
	//	return a + 5;
	//}
	//
	//int main() {
	//	int a = foo(/*$*/2/*$$*/);
	//}
	//====================
	//int foo(int a) {
	//	return a + 5;
	//}
	//
	//constexpr int extracted() {
	//	return 2;
	//}
	//
	//int main() {
	//	int a = foo(extracted());
	//}
	public void testExtractFunctionParameter_Bug396338() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	char c { 'A' };
	//	switch (c) {
	//	case /*$*/'A'/*$$*/:
	//		break;
	//	}
	//}
	//====================
	//constexpr char extracted() {
	//	return 'A';
	//}
	//
	//int main() {
	//	char c { 'A' };
	//	switch (c) {
	//	case extracted():
	//		break;
	//	}
	//}
	public void testExtractLiteralExpressionInCaseStatement_Bug396351() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	char c = 'A';
	//	switch (c) {
	//	/*$*/case 'A':/*$$*/
	//		break;
	//	}
	//}
	//
	public void testExtractCaseStatement_Bug396353() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	char c { 'A' };
	//	switch (c) {
	//	/*$*/case 'A':
	//		break;/*$$*/
	//	}
	//}
	//
	public void testExtractCaseAndBreakStatement_Bug396353() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	char c { 'A' };
	//	/*$*/switch (c) {
	//	case 'A':
	//		break;
	//	}/*$$*/
	//}
	//====================
	//void extracted(char c) {
	//	switch (c) {
	//	case 'A':
	//		break;
	//	}
	//}
	//
	//int main() {
	//	char c { 'A' };
	//	extracted(c);
	//}
	public void testExtractWholeSwitchStatement_Bug396353() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	/*$*/loop: ++n;
	//	if (n < 10)
	//		goto loop;/*$$*/
	//}
	//====================
	//void extracted(int n) {
	//	loop: ++n;
	//	if (n < 10)
	//		goto loop;
	//}
	//
	//int main() {
	//	int n = 0;
	//	extracted(n);
	//}
	public void testExtractLabelStatementAndCorrespondingGotoStatement_Bug396354() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	loop: ++n;
	//	if (n < 10)
	//		/*$*/goto loop;/*$$*/
	//}
	public void testExtractGotoStatementWithoutLabelStatement_Bug396354() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	int c = 1;
	//	/*$*/loop:
	//	if (n < 10) {
	//		n++;
	//		goto loop;
	//	}/*$$*/
	//	if (c < 10) {
	//		c++;
	//		goto loop;
	//	}
	//}
	//
	public void testExtractLabelStatementButNotAllGotoStatements_Bug396354() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	int c = 1;
	//	/*$*/loop:
	//	if (n < 10) {
	//		n++;
	//		goto loop;
	//	}
	//	if (c < 10) {
	//		c++;
	//		goto loop;
	//	}/*$$*/
	//}
	//====================
	//void extracted(int n, int c) {
	//	loop: if (n < 10) {
	//		n++;
	//		goto loop;
	//	}
	//	if (c < 10) {
	//		c++;
	//		goto loop;
	//	}
	//}
	//
	//int main() {
	//	int n = 0;
	//	int c = 1;
	//	extracted(n, c);
	//}
	public void testExtractLabelStatementAndAllGotoStatements_Bug396354() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	int c = 1;
	//	loop1:
	//	/*$*/loop2:
	//	if (n < 10) {
	//		n++;
	//		goto loop2;
	//	}
	//	if (c < 10) {
	//		c++;
	//		goto loop1;
	//	}/*$$*/
	//}
	public void testExtractOneCompleteOneWithoutLabelStatement_Bug396354() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int n = 0;
	//	int c = 1;
	//	/*$*/loop1:
	//	loop2:
	//	if (n < 10) {
	//		n++;
	//		goto loop2;
	//	}/*$$*/
	//	if (c < 10) {
	//		c++;
	//		goto loop1;
	//	}
	//}
	public void testExtractBothLabelsButOnlyOneWithAllGotoStatements_Bug396354() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int loop1 = 0;
	//	int loop2 = 1;
	//	/*$*/loop1:
	//	loop2:
	//	if (loop1 < 10) {
	//		loop1++;
	//		goto loop2;
	//	}
	//	if (loop2 < 10) {
	//		loop2++;
	//		goto loop1;
	//	}/*$$*/
	//}
	//====================
	//void extracted(int loop1, int loop2) {
	//	loop1: loop2: if (loop1 < 10) {
	//		loop1++;
	//		goto loop2;
	//	}
	//	if (loop2 < 10) {
	//		loop2++;
	//		goto loop1;
	//	}
	//}
	//
	//int main() {
	//	int loop1 = 0;
	//	int loop2 = 1;
	//	extracted(loop1, loop2);
	//}
	public void testExtractAllGotosAndLabelStatments_Bug396354() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//   auto lambda = /*$*/[] () {std::cout << "hello\n";};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted() {
	//	return []() {std::cout << "hello\n";};
	//}
	//
	//int main() {
	//	auto lambda = extracted();
	//}
	public void testExtractLambdaExpressionNoCaptureNoParameter_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//   auto lambda = /*$*/[] (auto a, auto b) {std::cout << a + b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted() {
	//	return [](auto a, auto b) {std::cout << a + b << '\n';};
	//}
	//
	//int main() {
	//	auto lambda = extracted();
	//}
	public void testExtractLambdaExpressionParametersByValue_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//   auto lambda = /*$*/[] (auto &a, auto &b) {std::cout << ++a << " " << ++b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted() {
	//	return [](auto& a, auto& b) {std::cout << ++a << " " << ++b << '\n';};
	//}
	//
	//int main() {
	//	auto lambda = extracted();
	//}
	public void testExtractLambdaExpressionParametersByReference_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a{5};
	//	int b{4};
	//	auto lambda = /*$*/[a, b] () {std::cout << a + b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a, int& b) {
	//	return [a, b]() {std::cout << a + b << '\n';};
	//}
	//
	//int main() {
	//	int a{5};
	//	int b{4};
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionValuesCaptured_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a{5};
	//	int b{4};
	//	auto lambda = /*$*/[&a, &b] () {std::cout << ++a << " " << ++b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a, int& b) {
	//	return [&a, &b]() {std::cout << ++a << " " << ++b << '\n';};
	//}
	//
	//int main() {
	//	int a{5};
	//	int b{4};
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionReferencesCaptured_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[=] () {std::cout << a + b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a, int& b) {
	//	return [=]() {std::cout << a + b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionEverythingCapturedByValue_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[&] () {std::cout << ++a << " " << ++b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a, int& b) {
	//	return [&]() {std::cout << ++a << " " << ++b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionEverythingCapturedByReference_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[=, &b] () {std::cout << a << " " << ++b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& b, int& a) {
	//	return [=, &b]() {std::cout << a << " " << ++b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(b, a);
	//}
	public void testExtractLambdaExpressionCapturedByReferenceEverythingElseByValue_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[&, b] () {std::cout << ++a << " " << b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& b, int& a) {
	//	return [&, b]() {std::cout << ++a << " " << b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(b, a);
	//}
	public void testExtractLambdaExpressionCapturedByValueEverythingElseByReference_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	auto lambda = /*$*/[a] (auto b) {std::cout << a+b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a) {
	//	return [a](auto b) {std::cout << a + b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	auto lambda = extracted(a);
	//}
	public void testExtractLambdaExpressionCapturedByValueAndOneParameter_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[=] () mutable {std::cout << ++a << " " << ++b << '\n';};/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//auto extracted(int& a, int& b) {
	//	return [=]() mutable {std::cout << ++a << " " << ++b << '\n';};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionEverythingCapturedByValueAndMutable_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = /*$*/[=] (int c) {return a + b + c;};/*$$*/
	//}
	//====================
	//auto extracted(int& a, int& b) {
	//	return [=](int c) {return a + b + c;
	//	};
	//}
	//
	//int main() {
	//	int a { 5 };
	//	int b { 3 };
	//	auto lambda = extracted(a, b);
	//}
	public void testExtractLambdaExpressionWithReturnStatement_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//Example.cpp
	//#include "Example.h"
	//#include <iostream>
	//
	//void Example::foo() {
	//	int a { 4 };
	//	auto lambda = /*$*/[=] (int b) {std::cout << a + b;};/*$$*/
	//}
	//====================
	//#include "Example.h"
	//#include <iostream>
	//
	//auto Example::extracted(int& a) {
	//	return [=](int b) {std::cout << a + b;};
	//}
	//
	//void Example::foo() {
	//	int a { 4 };
	//	auto lambda = extracted(a);
	//}

	//Example.h
	//class Example {
	//public:
	//	void foo();
	//};
	//====================
	//class Example {
	//public:
	//	void foo();
	//
	//private:
	//	auto extracted(int& a);
	//};
	public void testExtractLambdaExpressionInsideMemberFunction_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//Example.cpp
	//#include "Example.h"
	//#include <iostream>
	//
	//void Example::foo() {
	//	int a { 4 };
	//	auto lambda = /*$*/[=] (int b) {std::cout << a + b + c;};/*$$*/
	//}
	//====================
	//#include "Example.h"
	//#include <iostream>
	//
	//auto Example::extracted(int& a) {
	//	return [=](int b) {std::cout << a + b + c;};
	//}
	//
	//void Example::foo() {
	//	int a { 4 };
	//	auto lambda = extracted(a);
	//}

	//Example.h
	//class Example {
	//public:
	//	void foo();
	//
	//private:
	//	int c { 3 };
	//};
	//====================
	//class Example {
	//public:
	//	void foo();
	//
	//private:
	//	int c { 3 };
	//
	//	auto extracted(int& a);
	//};
	public void testExtractLambdaExpressionInsideMemberFunctionThatUsesMembers_Bug491274() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { };
	//	/*$*/a++;/*$$*/
	//	std::cout << a;
	//}
	//====================
	//#include <iostream>
	//
	//int extracted(int a) {
	//	a++;
	//	return a;
	//}
	//
	//int main() {
	//	int a { };
	//	a = extracted(a);
	//	std::cout << a;
	//}
	public void testExtractVariableUsedInBinaryExpressionAfter_Bug509060() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { };
	//	/*$*/a++;/*$$*/
	//	std::cout << a << '\n';
	//}
	//====================
	//#include <iostream>
	//
	//int extracted(int a) {
	//	a++;
	//	return a;
	//}
	//
	//int main() {
	//	int a { };
	//	a = extracted(a);
	//	std::cout << a << '\n';
	//}
	public void testExtractVariableUsedInNestedBinaryExpressionAfter_Bug509060() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { };
	//	int b { };
	//	/*$*/a++;
	//	b++;/*$$*/
	//	std::cout << a << b;
	//}
	//====================
	//#include <iostream>
	//
	//int extracted(int a, int& b) {
	//	a++;
	//	b++;
	//	return a;
	//}
	//
	//int main() {
	//	int a { };
	//	int b { };
	//	a = extracted(a, b);
	//	std::cout << a << b;
	//}
	public void testExtractTwoVariablesUsedInNestedBinaryExpressionAfter_Bug509060() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//class A {
	//public:
	//	void foo();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//class A {
	//public:
	//	void foo();
	//
	//private:
	//	std::string extracted(std::string string);
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//#include <iostream>
	//
	//void A::foo() {
	//	std::string string { "Hello" };
	//	/*$*/string += " World";
	//	string += "!\n";/*$$*/
	//	std::cout << string;
	//}
	//====================
	//#include "A.h"
	//#include <iostream>
	//
	//std::string A::extracted(std::string string) {
	//	string += " World";
	//	string += "!\n";
	//	return string;
	//}
	//
	//void A::foo() {
	//	std::string string { "Hello" };
	//	string = extracted(string);
	//	std::cout << string;
	//}
	public void testAdditionalNamespace_Bug507113() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <string>
	//
	//void foo() {
	//	std::string somestr;
	//	/*$*/somestr += "a";
	//	somestr += "a";
	//	somestr += "a";/*$$*/
	//}
	//====================
	//#include <string>
	//
	//void extracted(std::string somestr) {
	//	somestr += "a";
	//	somestr += "a";
	//	somestr += "a";
	//}
	//
	//void foo() {
	//	std::string somestr;
	//	extracted(somestr);
	//}
	public void testExtractStringInFunction_Bug507113() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//void A::foo() {
	//	std::string somestr;
	//	/*$*/somestr += "a";
	//	somestr += "a";
	//	somestr += "a";/*$$*/
	//}
	//====================
	//#include "A.h"
	//
	//void A::extracted(std::string somestr) {
	//	somestr += "a";
	//	somestr += "a";
	//	somestr += "a";
	//}
	//
	//void A::foo() {
	//	std::string somestr;
	//	extracted(somestr);
	//}

	//A.h
	//#include <string>
	//
	//class A {
	//public:
	//	void foo();
	//};
	//====================
	//#include <string>
	//
	//class A {
	//public:
	//	void foo();
	//
	//private:
	//	void extracted(std::string somestr);
	//};
	public void testExtractStringInMemberFunction_Bug507113() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int x;
	//	int *px;
	//	px = /*$*/&x/*$$*/;
	//}
	//====================
	//int* extracted(int& x) {
	//	return &x;
	//}
	//
	//int main() {
	//	int x;
	//	int *px;
	//	px = extracted(x);
	//}
	public void testExtractUnaryExpression_Bug396355() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main(){
	//	int a{};
	//	/*$*/a/*$$*/ = 2;
	//}
	//====================
	//int& extracted(int& a) {
	//	return a;
	//}
	//
	//int main(){
	//	int a{};
	//	extracted(a) = 2;
	//}
	public void testExtractIdExpression_Bug396336() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a[12];
	//	/*$*/a[2]/*$$*/ = 2;
	//}
	//====================
	//int& extracted(int a[12]) {
	//	return a[2];
	//}
	//
	//int main() {
	//	int a[12];
	//	extracted(a) = 2;
	//}
	public void testExtractArraySubscriptExpression_Bug396336() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int& getRef(int& a){
	//	return a;
	//}
	//
	//int main(){
	//	int a{3};
	//	/*$*/getRef(a)/*$$*/ = 2;
	//}
	//====================
	//int& getRef(int& a){
	//	return a;
	//}
	//
	//int& extracted(int& a) {
	//	return getRef(a);
	//}
	//
	//int main(){
	//	int a{3};
	//	extracted(a) = 2;
	//}
	public void testExtractFunctionCallExpression_Bug396336() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main() {
	//	std::vector<int> v { 1, 2, 3 };
	//	/*$*/*(v.begin() + 1)/*$$*/ = 2;
	//}
	//====================
	//#include <vector>
	//
	//int& extracted(std::vector<int>& v) {
	//	return *(v.begin() + 1);
	//}
	//
	//int main() {
	//	std::vector<int> v { 1, 2, 3 };
	//	extracted(v) = 2;
	//}
	public void testExtractUnaryExpression_Bug396336() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main() {
	//	std::vector<int> v { 1, 2, 3 };
	//	/*$*/v[2]/*$$*/ = 2;
	//}
	//====================
	//#include <vector>
	//
	//int& extracted(std::vector<int>& v) {
	//	return v[2];
	//}
	//
	//int main() {
	//	std::vector<int> v { 1, 2, 3 };
	//	extracted(v) = 2;
	//}
	public void testExtractArraySubscriptExpressionVector_Bug396336() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//template<class T> class Calc {
	//public:
	//	Calc(int id){ID = id;};
	//	T add(T a, T b){return a + b;};
	//	int ID;
	//};
	//
	//int main() {
	//	Calc<int> calc(5);
	//	Calc<int> calc2(4);
	//	/*$*/calc/*$$*/ = calc2;
	//}
	//====================
	//template<class T> class Calc {
	//public:
	//	Calc(int id){ID = id;};
	//	T add(T a, T b){return a + b;};
	//	int ID;
	//};
	//
	//auto&& extracted(Calc<int>& calc) {
	//	return calc;
	//}
	//
	//int main() {
	//	Calc<int> calc(5);
	//	Calc<int> calc2(4);
	//	extracted(calc) = calc2;
	//}
	public void testExtractIdExpressionTemplate_Bug396336() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	char c = 'A';
	//	switch (c) {
	//	case 'A':
	//		break;
	//	/*$*/default:/*$$*/
	//		break;
	//	}
	//}
	public void testExtractDefaultStatement() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	char c = 'A';
	//	/*$*/switch (c) {
	//	case 'A':
	//		break;
	//	default:
	//		break;
	//	}/*$$*/
	//}
	//====================
	//void extracted(char c) {
	//	switch (c) {
	//	case 'A':
	//		break;
	//	default:
	//		break;
	//	}
	//}
	//
	//int main() {
	//	char c = 'A';
	//	extracted(c);
	//}
	public void testSwitchWithDefaultStatement() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		if (b < 10){
	//			/*$*/return 12;/*$$*/
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return 14;
	//}
	//====================
	//int extracted() {
	//	return 12;
	//}
	//
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		if (b < 10){
	//			return extracted();
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return 14;
	//}
	public void testExtractSingleReturnStatement_1() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		if (b < 10){
	//			return 12;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	/*$*/return 14;/*$$*/
	//}
	//====================
	//int extracted() {
	//	return 14;
	//}
	//
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		if (b < 10){
	//			return 12;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return extracted();
	//}
	public void testExtractSingleReturnStatement_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		/*$*/if (b < 10) {
	//			return 12;
	//		} else {
	//			return 18;
	//		}/*$$*/
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return 14;
	//}
	//====================
	//int extracted(int b) {
	//	if (b < 10) {
	//		return 12;
	//	} else {
	//		return 18;
	//	}
	//}
	//
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	if (a < 20) {
	//		return extracted(b);
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return 14;
	//}
	public void testExtractIfElse() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	/*$*/if (a < 20) {
	//		if (b < 10) {
	//			return 12;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//	return 14;/*$$*/
	//}
	//====================
	//int extracted(int a, int b) {
	//	if (a < 20) {
	//		if (b < 10) {
	//			return 12;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}
	//
	//	return 14;
	//}
	//
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	return extracted(a, b);
	//}
	public void testExtractReturnOutsideIfElse_1() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	/*$*/if (a < 20) {
	//		if (b < 10) {
	//			b = 10;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		a = 56;
	//	}
	//	return 14;/*$$*/
	//}
	//====================
	//int extracted(int a, int b) {
	//	if (a < 20) {
	//		if (b < 10) {
	//			b = 10;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		a = 56;
	//	}
	//
	//	return 14;
	//}
	//
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	return extracted(a, b);
	//}
	public void testExtractReturnOutsideIfElse_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	/*$*/if (a < 20) {
	//		if (b < 10) {
	//			return 12;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		a = 56;
	//	}/*$$*/
	//}
	public void testExtractIfElseThatDoesntReturn() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	/*$*/if (a < 20) {
	//		if (b < 10) {
	//			b = 10;
	//		} else {
	//			return 18;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}/*$$*/
	//}
	public void testExtractIfElseThatDoesntReturnInInnerIf() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 10 };
	//	int b { 14 };
	//	/*$*/if (a < 20) {
	//		if (b < 10) {
	//			return 12;
	//		} else {
	//			b = 10;
	//		}
	//	} else if (a > 56) {
	//		return 42;
	//	}/*$$*/
	//}
	public void testExtractIfElseThatDoesntReturnInInnerElse() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	if (a > 4) {
	//		return 1;
	//	}
	//	/*$*/if (a < 100) {
	//		return 10;
	//	}
	//	return 100;/*$$*/
	//}
	//====================
	//int extracted(int a) {
	//	if (a < 100) {
	//		return 10;
	//	}
	//	return 100;
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 4) {
	//		return 1;
	//	}
	//	return extracted(a);
	//}
	public void testExtractIfWithReturnOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <stdexcept>
	//
	//int main() {
	//	int a { 14 };
	//	/*$*/if (a >= 0) {
	//		return 12;
	//	} else {
	//		throw std::invalid_argument("negative value");
	//	}/*$$*/
	//}
	//====================
	//#include <stdexcept>
	//
	//int extracted(int a) {
	//	if (a >= 0) {
	//		return 12;
	//	} else {
	//		throw std::invalid_argument("negative value");
	//	}
	//}
	//
	//int main() {
	//	int a { 14 };
	//	return extracted(a);
	//}
	public void testExtractReturnAndThrow() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <cstdlib>
	//
	//int main() {
	//	int a { 14 };
	//	/*$*/if (a >= 0) {
	//		return 12;
	//	} else {
	//		std::abort();
	//	}/*$$*/
	//}
	//====================
	//#include <cstdlib>
	//
	//int extracted(int a) {
	//	if (a >= 0) {
	//		return 12;
	//	} else {
	//		std::abort();
	//	}
	//}
	//
	//int main() {
	//	int a { 14 };
	//	return extracted(a);
	//}
	public void testExtractReturnAndNoReturnAttribute() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Clock {
	//	unsigned int hours;
	//	unsigned int minutes;
	//};
	//
	//Clock functionWithCustomType(int a) {
	//	/*$*/Clock clock;
	//	if (a < 20) {
	//		clock.hours = 12;
	//	} else {
	//		clock.hours = 15;
	//	}
	//	return clock;/*$$*/
	//}
	//
	//int main() {
	//	int a { 10 };
	//	Clock c = functionWithCustomType(a);
	//}
	//====================
	//struct Clock {
	//	unsigned int hours;
	//	unsigned int minutes;
	//};
	//
	//Clock extracted(int a) {
	//	Clock clock;
	//	if (a < 20) {
	//		clock.hours = 12;
	//	} else {
	//		clock.hours = 15;
	//	}
	//	return clock;
	//}
	//
	//Clock functionWithCustomType(int a) {
	//	return extracted(a);
	//}
	//
	//int main() {
	//	int a { 10 };
	//	Clock c = functionWithCustomType(a);
	//}
	public void testExtractReturnCustomType() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 20 };
	//	/*$*/for (int i { 0 }; i < 10; i++){
	//		a += i;
	//		if (a > 150) {
	//			return 150;
	//		}
	//	}/*$$*/
	//}
	public void testExtractReturnInFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 20 };
	//	int i { };
	//	/*$*/while (i < 10){
	//		a += i;
	//		if (i > 150) {
	//			return 150;
	//		}
	//		i++;
	//	}/*$$*/
	//}
	public void testExtractReturnInWhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 20 };
	//	int i { };
	//	/*$*/do {
	//		a += i;
	//		if (i > 150) {
	//			return 150;
	//		}
	//		i++;
	//	} while (i < 10);/*$$*/
	//}
	public void testExtractReturnInDoWhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 20 };
	//	std::vector<int> v {1, 2, 3};
	//	/*$*/for (int i : v) {
	//		a += i;
	//		if (a > 150) {
	//          return 150;
	//		}
	//	}/*$$*/
	//}
	public void testExtractReturnInRangeBasedFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	default:
	//		return 1000;
	//	}/*$$*/
	//}
	//====================
	//int extracted(int a) {
	//	switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	default:
	//		return 1000;
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	return extracted(a);
	//}
	public void testExtractSwitchReturnInAllCases() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//	case 2:
	//		return 100;
	//	default:
	//		return 1000;
	//	}/*$$*/
	//}
	//====================
	//int extracted(int a) {
	//	switch (a) {
	//	case 1:
	//	case 2:
	//		return 100;
	//	default:
	//		return 1000;
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	return extracted(a);
	//}
	public void testExtractSwitchFallThroughReturnInFollowingCase() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//		break;
	//	default:
	//		return 1000;
	//	}/*$$*/
	//}
	//====================
	//int extracted(int a) {
	//	switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//		break;
	//	default:
	//		return 1000;
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	return extracted(a);
	//}
	public void testExtractSwitchBreakAfterReturn() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	}/*$$*/
	//}
	public void testExtractSwitchWithoutDefault() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		break;
	//	default:
	//		return 1000;
	//	}/*$$*/
	//}
	public void testExtractSwitchOneCaseDoesntReturn() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//	case 2:
	//		break;
	//	default:
	//		return 1000;
	//	}/*$$*/
	//}
	public void testExtractSwitchWithFallThroughButNoReturnInCaseAfter() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	default:
	//	}/*$$*/
	//}
	public void testExtractSwitchWithDefaultThatDoesntReturn() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	default:
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	}/*$$*/
	//}
	//====================
	//int extracted(int a) {
	//	switch (a) {
	//	default:
	//	case 1:
	//		return 10;
	//	case 2:
	//		return 100;
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	return extracted(a);
	//}
	public void testExtractSwitchWithDefaulFirst() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int a { 5 };
	//	/*$*/switch (a) {
	//	default:
	//	case 1:
	//		return 10;
	//	case 2:
	//	}/*$$*/
	//}
	public void testExtractSwithWithDefaultFirstAndCaseWithoutReturn() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (int i = 0; /*$*/i < 100;/*$$*/ i++)
	//		sum += i;
	//}
	//====================
	//bool extracted(int i) {
	//	return i < 100;
	//}
	//
	//int main() {
	//	int sum { };
	//	for (int i = 0; extracted(i); i++)
	//		sum += i;
	//}
	public void testExtractStatementInForHead() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (/*$*/int i = 0; i < 100;/*$$*/ i++)
	//		sum += i;
	//}
	public void testExtractMoreThanOneStatementInForHead_1() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (int i = 0; /*$*/i < 100; i++/*$$*/)
	//		sum += i;
	//}
	public void testExtractMoreThanOneStatementInForHead_2() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (/*$*/int i = 0; i < 100; i++/*$$*/)
	//		sum += i;
	//}
	public void testExtractMoreThanOneStatementInForHead_3() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	/*$*/int sum { };
	//	for (int i = 0;/*$$*/ i < 100; i++)
	//		sum += i;
	//}
	public void testExtractStatementInForHeadOneBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (int i = 0; i < 100; /*$*/i++)
	//		sum += i;/*$$*/
	//}
	public void testExtractStatementInForHeadOneInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (int i = 0; i < 100; i++)
	//		/*$*/sum += i;/*$$*/
	//}
	//====================
	//int extracted(int sum, int i) {
	//	sum += i;
	//	return sum;
	//}
	//
	//int main() {
	//	int sum { };
	//	for (int i = 0; i < 100; i++)
	//		sum = extracted(sum, i);
	//}
	public void testExtractStatementInsideFor() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	for (int i = 0; i < 100; i++)
	//		/*$$*/sum += i;
	//	++sum;/*$*/
	//}
	public void testExtractStatementInsideForOneOutside() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { }, fac { 1 };
	//	/*$*/for (int i = 0; i < 100; i++) {
	//		sum += i;/*$$*/
	//		fac *= i;
	//	}
	//}
	public void testExtractIncompleteFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	/*$*/for (int i = 0; i < 100; i++)
	//		sum += i;/*$$*/
	//	int r { sum };
	//}
	//====================
	//int extracted(int sum) {
	//	for (int i = 0; i < 100; i++)
	//		sum += i;
	//	return sum;
	//}
	//
	//int main() {
	//	int sum { };
	//	sum = extracted(sum);
	//	int r { sum };
	//}
	public void testExtractFor() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int sum { };
	//	/*$*/for (int i = 0; i < 100; i++)
	//		sum += i;
	//	++sum;/*$$*/
	//	int r { sum };
	//}
	//====================
	//int extracted(int sum) {
	//	for (int i = 0; i < 100; i++)
	//		sum += i;
	//	++sum;
	//	return sum;
	//}
	//
	//int main() {
	//	int sum { };
	//	sum = extracted(sum);
	//	int r { sum };
	//}
	public void testExtractForAndOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int productIxJ { };
	//	for (int i = 0; i < 100; /*$*/i++)
	//		for (int j = 0;/*$$*/ j < 100; j++)
	//			++productIxJ;
	//}
	public void testExtractStatementsInDifferentForHeads() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int productIxJ { };
	//	for (int i = 0; i < 100; /*$*/i++)
	//		for (int j = 0; j < 100; j++)
	//			++productIxJ;/*$$*/
	//}
	public void testExtractStatementInForHeadAndInnerfor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int productIxJ { };
	//	for (int i = 0; i < 100; i++) {
	//		/*$*/for (int j = 0; j < 100; j++) {
	//			++productIxJ;
	//		}/*$$*/
	//	}
	//}
	//====================
	//int extracted(int productIxJ) {
	//	for (int j = 0; j < 100; j++) {
	//		++productIxJ;
	//	}
	//	return productIxJ;
	//}
	//
	//int main() {
	//	int productIxJ { };
	//	for (int i = 0; i < 100; i++) {
	//		productIxJ = extracted(productIxJ);
	//	}
	//}
	public void testExtractInnerFor() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int productIxJplusI { };
	//	for (int i = 0; i < 100; i++) {
	//		/*$*/for (int j = 0; j < 100; j++) {
	//			++productIxJplusI;
	//		}
	//		++productIxJplusI;/*$$*/
	//	}
	//}
	//====================
	//int extracted(int productIxJplusI) {
	//	for (int j = 0; j < 100; j++) {
	//		++productIxJplusI;
	//	}
	//	++productIxJplusI;
	//	return productIxJplusI;
	//}
	//
	//int main() {
	//	int productIxJplusI { };
	//	for (int i = 0; i < 100; i++) {
	//		productIxJplusI = extracted(productIxJplusI);
	//	}
	//}
	public void testExtractInnerForAndOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int productIxJplusI { };
	//	/*$*/for (int i = 0; i < 100; i++) {
	//		for (int j = 0; j < 100; j++) {
	//			++productIxJplusI;
	//		}/*$$*/
	//		++productIxJplusI;
	//	}
	//}
	public void testExtractIncompleteForWithInnerFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int productIxJplusI { };
	//	/*$*/for (int i = 0; i < 100; i++)
	//		for (int j = 0; j < 100; j++)
	//			++productIxJplusI;
	//		++productIxJplusI;/*$$*/
	//	int r { productIxJplusI };
	//}
	//====================
	//int extracted(int productIxJplusI) {
	//	for (int i = 0; i < 100; i++)
	//		for (int j = 0; j < 100; j++)
	//			++productIxJplusI;
	//	++productIxJplusI;
	//	return productIxJplusI;
	//}
	//
	//int main() {
	//	int productIxJplusI { };
	//	productIxJplusI = extracted(productIxJplusI);
	//	int r { productIxJplusI };
	//}
	public void testExtractTwoForStatements() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	while (/*$*/i > 0/*$$*/) {
	//		sum += i;
	//	}
	//}
	//====================
	//bool extracted(int i) {
	//	return i > 0;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	while (extracted(i)) {
	//		sum += i;
	//	}
	//}
	public void testExtractExpressionInWhileHead() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	while (i > /*$*/0) {
	//		sum += i;/*$$*/
	//	}
	//}
	public void testExtractLiteralInWhileHeadOneStatementInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	/*$*/int sum { } ;
	//	while (i > 0/*$$*/) {
	//		sum += i;
	//	}
	//}
	public void testExtractExpressionInWhileHeadOneStatementBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	while (i > 0) {
	//		/*$*/sum += i;/*$$*/
	//	}
	//}
	//====================
	//int extracted(int sum, int i) {
	//	sum += i;
	//	return sum;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	while (i > 0) {
	//		sum = extracted(sum, i);
	//	}
	//}
	public void testExtractStatementInsideWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	while (i > 0) {
	//		/*$*/sum += i;
	//	}
	//	++sum;/*$$*/
	//}
	public void testExtractExpressionInsideWhileHeadOneStatementOutside() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } , fak { 1 };
	//	/*$*/while (i > 0) {
	//		sum += i;/*$$*/
	//		fak *= i;
	//	}
	//}
	public void testExtractIncompleteWhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	/*$*/while (i > 0) {
	//		sum += i;
	//	}/*$$*/
	//	int r { sum };
	//}
	//====================
	//int extracted(int i, int sum) {
	//	while (i > 0) {
	//		sum += i;
	//	}
	//	return sum;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	sum = extracted(i, sum);
	//	int r { sum };
	//}
	public void testExtractWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	/*$*/while (i > 0) {
	//		sum += i;
	//	}
	//	int r { sum };/*$$*/
	//	++r;
	//}
	//====================
	//int extracted(int i, int sum) {
	//	while (i > 0) {
	//		sum += i;
	//	}
	//	int r { sum };
	//	return r;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { } ;
	//	int r = extracted(i, sum);
	//	++r;
	//}
	public void testExtractWhileOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (/*$*/i > 0) {
	//		int j { 100 };
	//		while (j > 0)/*$$*/ {
	//			++productIxJ;
	//			j--;
	//		}
	//		i--;
	//	}
	//}
	public void testExtractExpressionsInDifferentWhileHeads() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (/*$*/i > 0) {
	//		int j { 100 };
	//		while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}/*$$*/
	//		i--;
	//	}
	//}
	public void testExtractExpressionInWhileHeadAndInnerwhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (i > 0) {
	//		int j { 100 };
	//		/*$*/while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}/*$$*/
	//		i--;
	//	}
	//}
	//====================
	//int extracted(int j, int& productIxJ) {
	//	while (j > 0) {
	//		++productIxJ;
	//		j--;
	//	}
	//	return j;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (i > 0) {
	//		int j { 100 };
	//		j = extracted(j, productIxJ);
	//		i--;
	//	}
	//}
	public void testExtractInnerWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (i > 0) {
	//		int j { 100 };
	//		/*$*/while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}
	//		i--;/*$$*/
	//	}
	//}
	//====================
	//int extracted(int j, int& productIxJ, int& i) {
	//	while (j > 0) {
	//		++productIxJ;
	//		j--;
	//	}
	//	i--;
	//	return j;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	while (i > 0) {
	//		int j { 100 };
	//		j = extracted(j, productIxJ, i);
	//	}
	//}
	public void testExtractInnerWhileOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	/*$*/while (i > 0) {
	//		int j { 100 };
	//		while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}/*$$*/
	//		i--;
	//	}
	//	++productIxJ;
	//}
	public void testExtractIncompleteWhileWithInnerWhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	/*$*/while (i > 0) {
	//		int j { 100 };
	//		while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}
	//		i--;
	//	}/*$$*/
	//	int r { productIxJ};
	//}
	//====================
	//int extracted(int i, int productIxJ) {
	//	while (i > 0) {
	//		int j { 100 };
	//		while (j > 0) {
	//			++productIxJ;
	//			j--;
	//		}
	//		i--;
	//	}
	//	return productIxJ;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { } ;
	//	productIxJ = extracted(i, productIxJ);
	//	int r { productIxJ};
	//}
	public void testExtractTwoWhileStatements() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (/*$*/a > 100/*$$*/) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	//====================
	//#include <iostream>
	//
	//bool extracted(int a) {
	//	return a > 100;
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (extracted(a)) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractExpressionInsideIfHead() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	/*$*/int a { 5 };
	//	if (a > 100/*$$*/) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractExpressionInsideIfHeadOneStatementBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (/*$*/a > 100) {
	//		std::cout/*$$*/ << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractExpressionInsideIfHeadOneStatementInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		/*$*/std::cout << "> 100\n";/*$$*/
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	//====================
	//#include <iostream>
	//
	//void extracted() {
	//	std::cout << "> 100\n";
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		extracted();
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractStatementInsideIf() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		/*$*/std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";/*$$*/
	//	}
	//}
	public void testExtractStatementInsideElseOneBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		/*$*/std::cout << "<= 50\n";
	//	}
	//	std::cout << "end\n";/*$$*/
	//}
	public void testExtractStatementInsideElseOneOutside() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a  { 2 };
	//	/*$*/if (a > 10) {
	//		std::cout << "a > 10\n";
	//	}/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted(int a) {
	//	if (a > 10) {
	//		std::cout << "a > 10\n";
	//	}
	//}
	//
	//int main() {
	//	int a  { 2 };
	//	extracted(a);
	//}
	public void testExtractIfClauseThatDoesntHaveElseClause() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	/*$*/if (a > 100) {
	//		std::cout << "> 100\n";
	//	}/*$$*/ else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractIfClauseOnly() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	/*$*/if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	}/*$$*/ else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractIfElseIfWithoutElse() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	/*$*/if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted(int a) {
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	extracted(a);
	//}
	public void testExtractIf() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} /*$*/else if (a > 50) {
	//		std::cout << "> 50\n";
	//	}/*$$*/ else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractElseIfWithoutElse() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	}/*$*/ else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted(int a) {
	//	if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else
	//		extracted(a);
	//}
	public void testExtractElseIfElse() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else /*$*/if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted(int a) {
	//	if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else
	//		extracted(a);
	//}
	public void testExtractInnerIf() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else /*$*/if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//	std::cout << "end\n";/*$$*/
	//}
	public void testExtractInnerIfAndStatementOutside() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	/*$*/if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//	std::cout << "end\n";/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted(int a) {
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//
	//	std::cout << "end\n";
	//}
	//
	//int main() {
	//	int a { 5 };
	//	extracted(a);
	//}
	public void testExtractIfAndOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (/*$*/a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50/*$$*/) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}
	//}
	public void testExtractExpressionsInDifferentIfHeads() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (/*$*/a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		std::cout << "<= 50\n";
	//	}/*$$*/
	//}
	public void testExtractExpressionInIfHeadAndElseIf() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} /*$*/else {
	//		std::cout << "<= 50\n";
	//	}/*$$*/
	//}
	//====================
	//#include <iostream>
	//
	//void extracted() {
	//	std::cout << "<= 50\n";
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		extracted();
	//	}
	//}
	public void testExtractElseWithParenthesis() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		/*$*/std::cout << "<= 50\n";/*$$*/
	//	}
	//}
	//====================
	//#include <iostream>
	//
	//void extracted() {
	//	std::cout << "<= 50\n";
	//}
	//
	//int main() {
	//	int a { 5 };
	//	if (a > 100) {
	//		std::cout << "> 100\n";
	//	} else if (a > 50) {
	//		std::cout << "> 50\n";
	//	} else {
	//		extracted();
	//	}
	//}
	public void testExtractElseWithoutParenthesis() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		sum += i;
	//		--i;
	//	} while (/*$*/i > 0/*$$*/);
	//}
	//====================
	//bool extracted(int i) {
	//	return i > 0;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		sum += i;
	//		--i;
	//	} while (extracted(i));
	//}
	public void testExtractExpressionInDoWhileHead() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		sum += i;
	//		/*$*/--i;
	//	} while (i > 0)/*$$*/;
	//}
	public void testExtractExpressionInDoWhileHeadOneStatementInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		/*$*/sum += i;/*$$*/
	//		--i;
	//	} while (i > 0);
	//}
	//====================
	//int extracted(int sum, int i) {
	//	sum += i;
	//	return sum;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		sum = extracted(sum, i);
	//		--i;
	//	} while (i > 0);
	//}
	public void testExtractStatementInsideDoWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do {
	//		sum += i;
	//		/*$*/--i;
	//	} while (i > 0);
	//	int r { sum };/*$$*/
	//}
	public void testExtractStatementInsideDoWhileOneStatementAfter() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	/*$*/int sum { };
	//	do {
	//		sum += i;/*$$*/
	//		--i;
	//	} while (i > 0);
	//}
	public void testExtractStatementInsideDoWhileOneStatementBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	do /*$*/{
	//		sum += i;
	//		--i;
	//	} while (i > 0);/*$$*/
	//}
	public void testExtractDoWhileWithoutDo() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	/*$*/do {
	//		sum += i;
	//		--i;
	//	} while (i > 0);/*$$*/
	//	int r { sum };
	//}
	//====================
	//int extracted(int sum, int i) {
	//	do {
	//		sum += i;
	//		--i;
	//	} while (i > 0);
	//	return sum;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	sum = extracted(sum, i);
	//	int r { sum };
	//}
	public void testExtractDoWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	/*$*/do {
	//		sum += i;
	//		--i;
	//	} while (i > 0);
	//	int r { sum };/*$$*/
	//	++r;
	//}
	//====================
	//int extracted(int sum, int i) {
	//	do {
	//		sum += i;
	//		--i;
	//	} while (i > 0);
	//	int r { sum };
	//	return r;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int sum { };
	//	int r = extracted(sum, i);
	//	++r;
	//}
	public void testExtractDoWhileOneŜtatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		do {
	//			++productIxJ;
	//			--j;
	//		} while (/*$*/j > 0);
	//		--i;
	//	} while (i > 0);/*$$*/
	//}
	public void testExtractExpressionsInDifferentDoWhileHeads() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		/*$*/do {
	//			++productIxJ;
	//			--j;
	//		} while (j > 0);/*$$*/
	//		--i;
	//	} while (i > 0);
	//}
	//====================
	//int extracted(int productIxJ, int& j) {
	//	do {
	//		++productIxJ;
	//		--j;
	//	} while (j > 0);
	//	return productIxJ;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		productIxJ = extracted(productIxJ, j);
	//		--i;
	//	} while (i > 0);
	//}
	public void testExtractInnerDoWhile() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		/*$*/do {
	//			++productIxJ;
	//			--j;
	//		} while (j > 0);
	//		--i;/*$$*/
	//	} while (i > 0);
	//}
	//====================
	//int extracted(int productIxJ, int& j, int& i) {
	//	do {
	//		++productIxJ;
	//		--j;
	//	} while (j > 0);
	//	--i;
	//	return productIxJ;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		productIxJ = extracted(productIxJ, j, i);
	//	} while (i > 0);
	//}
	public void testExtractInnerDoWhileOneStatementOutside() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	do {
	//		int j { 100 };
	//		/*$*/do {
	//			++productIxJ;
	//			--j;
	//		} while (j > 0);
	//		--i;
	//	} while (i > 0);/*$$*/
	//}
	public void testExtractIncompleteDoWhileOneinnerDoWhile() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	/*$*/do {
	//		int j { 100 };
	//		do {
	//			++productIxJ;
	//			--j;
	//		} while (j > 0);
	//		--i;
	//	} while (i > 0);/*$$*/
	//	int r { productIxJ };
	//}
	//====================
	//int extracted(int productIxJ, int i) {
	//	do {
	//		int j { 100 };
	//		do {
	//			++productIxJ;
	//			--j;
	//		} while (j > 0);
	//		--i;
	//	} while (i > 0);
	//	return productIxJ;
	//}
	//
	//int main() {
	//	int i { 100 };
	//	int productIxJ { };
	//	productIxJ = extracted(productIxJ, i);
	//	int r { productIxJ };
	//}
	public void testExtractTwoDoWhileStatements() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	/*$*/struct Banana {
	//		int weight;
	//	};/*$$*/
	//	Banana b1;
	//	Banana b2;
	//	int weightTotal = b1.weight + b2.weight;
	//}
	public void testExtractDeclarationStatementwithCompositeTypeSpecifier() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	struct Banana {
	//		int weight;
	//	};
	//	Banana b1;
	//	Banana b2;
	//	/*$*/int weightTotal = b1.weight + b2.weight;/*$$*/
	//}
	public void testExtractDeclarationStatementWithFieldReference() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	/*$*/enum Color {
	//		green, blue, red
	//	};/*$$*/
	//	Color bg = green;
	//}
	public void testExtractEnumerationSpecifierWithSurroundingDeclarationStatement() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	enum Color {
	//		green, blue, red
	//	};
	//	/*$*/Color bg = green;/*$$*/
	//}
	public void testExtractDeclarationStatementWithNamedTypeSpecifier() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	for (/*$*/int v : v1) {
	//		sum += v;/*$$*/
	//	}
	//}
	public void testExtractRangeBasedForHeadOneStatementInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : /*$*/v1) {
	//		sum += v;/*$$*/
	//	}
	//}
	public void testExtractExpressionInRangeBasedForHeadOneStatementInBody() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	/*$*/std::vector<int> v1 { 1, 2, 3 };
	//	for (int v/*$$*/ : v1) {
	//		sum += v;
	//	}
	//}
	//====================
	//#include <vector>
	//
	//std::vector<int> extracted() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	return v1;
	//}
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 = extracted();
	//	for (int v : v1) {
	//		sum += v;
	//	}
	//}
	public void testExtractDeclarationInRangeBasedForHeadOneStatementBefore() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	/*$*/std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : v1/*$$*/) {
	//		sum += v;
	//	}
	//}
	public void testExtractRangeBasedForHeadOneStatementBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : v1) {
	//		/*$*/sum += v;/*$$*/
	//	}
	//}
	//====================
	//#include <vector>
	//
	//int extracted(int sum, int v) {
	//	sum += v;
	//	return sum;
	//}
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : v1) {
	//		sum = extracted(sum, v);
	//	}
	//}
	public void testExtractStatementInsideRangeBasedFor() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	/*$*/std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : v1) {
	//		sum += v;/*$$*/
	//	}
	//}
	public void testExtractStatementInsideRangeBasedForOneStatementBefore() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	for (int v : v1) {
	//		/*$*/sum += v;
	//	}
	//	int r { sum };/*$$*/
	//}
	public void testExtractStatementInsideRangeBasedForOneStatementAfter() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	int sum1 { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	/*$*/for (int v : v1) {
	//		sum += v;/*$$*/
	//		sum1 += v;
	//	}
	//}
	public void testExtractIncompleteRangeBasedFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	/*$*/for (int v : v1) {
	//		sum += v;
	//	}/*$$*/
	//	int r { sum };
	//}
	//====================
	//#include <vector>
	//
	//int extracted(std::vector<int> v1, int sum) {
	//	for (int v : v1) {
	//		sum += v;
	//	}
	//	return sum;
	//}
	//
	//int main(){
	//	int sum { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	sum = extracted(v1, sum);
	//	int r { sum };
	//}
	public void testExtractRangeBasedFor() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main(){
	//	int sum { };
	//	int sum1 { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	/*$*/for (int v : v1) {
	//		sum += v;
	//	}
	//	int r { sum };/*$$*/
	//	++r;
	//}
	//====================
	//#include <vector>
	//
	//int extracted(std::vector<int> v1, int sum) {
	//	for (int v : v1) {
	//		sum += v;
	//	}
	//	int r { sum };
	//	return r;
	//}
	//
	//int main(){
	//	int sum { };
	//	int sum1 { };
	//	std::vector<int> v1 { 1, 2, 3 };
	//	int r = extracted(v1, sum);
	//	++r;
	//}
	public void testExtractRangeBasedForOneStatementAfter() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : /*$*/v1) {
	//		for (int i2 /*$$*/: v2) {
	//			r.push_back(i1 * i2);
	//		}
	//	}
	//}
	//====================
	//#include <vector>
	//
	//auto&& extracted(std::vector<int>& v1) {
	//	return v1;
	//}
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : extracted(v1)) {
	//		for (int i2 : v2) {
	//			r.push_back(i1 * i2);
	//		}
	//	}
	//}
	public void testExtractExpressionOfRangeBasedForDeclarationFromOther() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : /*$*/v1) {
	//		for (int i2 : v2) {
	//			r.push_back(i1 * i2);
	//		}/*$$*/
	//	}
	//}
	public void testExtractExpressionInRangeBasedForHeadInnerRangeBasedFor() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//#include <vector>
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : v1) {
	//		/*$*/for (int i2 : v2) {
	//			r.push_back(i1 * i2);
	//		}/*$$*/
	//	}
	//}
	//====================
	//#include <vector>
	//
	//void extracted(std::vector<int> v2, std::vector<int> r, int i1) {
	//	for (int i2 : v2) {
	//		r.push_back(i1 * i2);
	//	}
	//}
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : v1) {
	//		extracted(v2, r, i1);
	//	}
	//}
	public void testExtractInnerRangeBasedFor() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//#include <iostream>
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : v1) {
	//		/*$*/for (int i2 : v2) {
	//			r.push_back(i1 * i2);
	//		}
	//		std::cout << "inner end\n";/*$$*/
	//	}
	//}
	//====================
	//#include <vector>
	//#include <iostream>
	//
	//void extracted(std::vector<int> v2, std::vector<int> r, int i1) {
	//	for (int i2 : v2) {
	//		r.push_back(i1 * i2);
	//	}
	//	std::cout << "inner end\n";
	//}
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : v1) {
	//		extracted(v2, r, i1);
	//	}
	//}
	public void testExtractInnerRangeBasedForAndStatementAfter() throws Exception {
		allowNameComputation();
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <vector>
	//#include <iostream>
	//
	//void extracted(int i1, int i2, std::vector<int>& r) {
	//	r.push_back(i1 * i2);
	//	std::cout << "1 completed\n";
	//}
	//
	//int main() {
	//	std::vector<int> v1 { 1, 2, 3 };
	//	std::vector<int> v2 { 1, 2, 3 };
	//	std::vector<int> r { };
	//
	//	for (int i1 : v1) {
	//		for (int i2 : v2) {
	//			extracted(i1, i2, r);
	//		}
	//	}
	//}
	public void testIncompleteRangeBasedForInnerRangebBsedFor() throws Exception {
		assertRefactoringFailure();
	}
}
