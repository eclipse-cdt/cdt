/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.extractconstant;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantInfo;
import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;

import junit.framework.Test;

/**
 * Tests for Extract Constant refactoring.
 */
public class ExtractConstantRefactoringTest extends RefactoringTestBase {
	private String extractedConstantName;
	private VisibilityEnum visibility;
	private boolean replaceAllLiterals;
	private ExtractConstantRefactoring refactoring;

	public ExtractConstantRefactoringTest() {
		super();
	}

	public ExtractConstantRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(ExtractConstantRefactoringTest.class);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		extractedConstantName = "EXTRACTED";
		visibility = VisibilityEnum.v_private;
		replaceAllLiterals = true;
		super.setUp();
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ExtractConstantRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		ExtractConstantInfo info = refactoring.getRefactoringInfo();
		info.setName(extractedConstantName);
		info.setVisibility(visibility);
		info.setReplaceAllLiterals(replaceAllLiterals);
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
	//	void bar();
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
	//	void bar();
	//
	//private:
	//	static const int EXTRACTED = 42;
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
	//	return /*$*/42/*$$*/; //Hello
	//}
	//
	//void A::bar() {
	//	int a = 42;
	//	int b = 42;
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
	//	return EXTRACTED; //Hello
	//}
	//
	//void A::bar() {
	//	int a = EXTRACTED;
	//	int b = EXTRACTED;
	//}
	public void testExtractConstantInt() throws Exception {
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
	//	void bar();
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
	//	void bar();
	//
	//private:
	//	static const int EXTRACTED = 42;
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
	//	//Hello
	//	return /*$*/42/*$$*/;
	//}
	//
	//void A::bar() {
	//	int a = 42;
	//	int b = 42;
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
	//	//Hello
	//	return EXTRACTED;
	//}
	//
	//void A::bar() {
	//	int a = EXTRACTED;
	//	int b = EXTRACTED;
	//}
	public void testExtractConstantInt2() throws Exception {
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
	//	float foo();
	//	void bar();
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
	//	float foo();
	//	void bar();
	//
	//private:
	//	static const float EXTRACTED = 42.0f;
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
	//float A::foo() {
	//	return /*$*/42.0f/*$$*/;
	//}
	//
	//void A::bar() {
	//	float a = 42.0f;
	//	float b = 42.0f;
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
	//float A::foo() {
	//	return EXTRACTED;
	//}
	//
	//void A::bar() {
	//	float a = EXTRACTED;
	//	float b = EXTRACTED;
	//}
	public void testExtractConstantFloat() throws Exception {
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
	//	double foo();
	//	void bar();
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
	//	double foo();
	//	void bar();
	//
	//private:
	//	static const double EXTRACTED = 42.0;
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
	//double A::foo() {
	//	return /*$*/42.0/*$$*/;
	//}
	//
	//void A::bar() {
	//	double a = 42.0;
	//	double b = 42.0;
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
	//double A::foo() {
	//	return EXTRACTED;
	//}
	//
	//void A::bar() {
	//	double a = EXTRACTED;
	//	double b = EXTRACTED;
	//}
	public void testExtractConstantDouble() throws Exception {
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
	//	static const int a = 42;
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
	//	static const int a = 42;
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
	//	return 42;
	//}
	//
	//int bar() {
	//	return /*$*/42/*$$*/;
	//}
	//====================
	//#include "A.h"
	//
	//namespace {
	//
	//const int EXTRACTED = 42;
	//
	//}
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//int A::foo() {
	//	return EXTRACTED;
	//}
	//
	//int bar() {
	//	return EXTRACTED;
	//}
	public void testExtractConstantStaticInt() throws Exception {
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
	//
	//protected:
	//	static const int EXTRACTED = 42;
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
	//	return /*$*/42/*$$*/;
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
	//	return EXTRACTED;
	//}
	public void testReplaceNumberProtected() throws Exception {
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
	//	static const int EXTRACTED = 42;
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
	//	return /*$*/42/*$$*/;
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
	//	return EXTRACTED;
	//}
	public void testReplaceNumberPrivate() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class X {
	//	void method() {
	//		int a = /*$*/42/*$$*/;
	//	}
	//};
	//====================
	//class X {
	//public:
	//	static const int EXTRACTED = 42;
	//
	//private:
	//	void method() {
	//		int a = EXTRACTED;
	//	}
	//};
	public void testExtractConstantFromInlinedMethod_Bug246062() throws Exception {
		visibility = VisibilityEnum.v_public;
		assertRefactoringSuccess();
	}

	//A.h
	//class X {
	//	void method() {
	//		char *a = /*$*/"sometext"/*$$*/;
	//	}
	//
	//	void method2() {
	//		const char *b = "sometext";
	//	}
	//};
	//====================
	//class X {
	//	void method() {
	//		char *a = EXTRACTED;
	//	}
	//
	//	void method2() {
	//		const char *b = EXTRACTED;
	//	}
	//
	//	static const char *EXTRACTED = "sometext";
	//};
	public void testString() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class X {
	//	void method() {
	//		wchar_t *a = /*$*/L"sometext"/*$$*/;
	//	}
	//
	//	void method2() {
	//		const wchar_t *b = L"sometext";
	//		const char *c = "sometext";
	//	}
	//};
	//====================
	//class X {
	//	void method() {
	//		wchar_t *a = EXTRACTED;
	//	}
	//
	//	void method2() {
	//		const wchar_t *b = EXTRACTED;
	//		const char *c = "sometext";
	//	}
	//
	//	static const wchar_t *EXTRACTED = L"sometext";
	//};
	public void testExtractConstantWideString() throws Exception {
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
	//	void bar();
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
	//	void bar();
	//
	//	static const int EXTRACTED = 42;
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
	//	return 42; // Hello
	//}
	//
	//void A::bar() {
	//	int a = 42;
	//	int b = 42;
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
	//	return EXTRACTED; // Hello
	//}
	//
	//void A::bar() {
	//	int a = EXTRACTED;
	//	int b = EXTRACTED;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create constant for 42" description="Extract Constant Refactoring"
	//fileName="file:${projectPath}/A.cpp" flags="4"
	//id="org.eclipse.cdt.ui.refactoring.extractconstant.ExtractConstantRefactoring" name="EXTRACTED"
	//project="RegressionTestProject" selection="64,2" visibility="public" replaceAll="true"/>
	//</session>
	//
	public void testHistoryExtractConstantInt() throws Exception {
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
	//
	//protected:
	//	static const int EXTRACTED = 42;
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
	//int A::foo() {
	//	return EXTRACTED;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create constant for 42" description="Extract Constant Refactoring"
	//fileName="file:${projectPath}/A.cpp" flags="4"
	//id="org.eclipse.cdt.ui.refactoring.extractconstant.ExtractConstantRefactoring" name="EXTRACTED"
	//project="RegressionTestProject" selection="64,2" visibility="protected"/>
	//</session>
	public void testHistoryReplaceNumberProtected() throws Exception {
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
	//
	//private:
	//	static const int EXTRACTED = 42;
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
	//int A::foo() {
	//	return EXTRACTED;
	//}

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create constant for 42" description="Extract Constant Refactoring"
	//fileName="file:${projectPath}/A.cpp" flags="4"
	//id="org.eclipse.cdt.ui.refactoring.extractconstant.ExtractConstantRefactoring" name="EXTRACTED"
	//project="RegressionTestProject" selection="64,2" visibility="private"/>
	//</session>
	public void testHistoryReplaceNumberPrivate() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	int a = -(/*$*/+(~(10))/*$$*/);
	//	int b = -(+(~(10)));
	//	int c = -(+(~(11)));
	//	int d = +(~(10));
	//	int e = (~10);
	//	int f = -(+(-(10)));
	//}
	//=
	//A.cpp
	//namespace {
	//
	//const int EXTRACTED = +(~(10));
	//
	//}
	//
	//void foo() {
	//	int a = -(EXTRACTED);
	//	int b = -(EXTRACTED);
	//	int c = -(+(~(11)));
	//	int d = EXTRACTED;
	//	int e = (~10);
	//	int f = -(+(-(10)));
	//}
	public void testExtractionOfUnaryExpressions() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	int i = /*$*/2 * 21/*$$*/;
	//}
	//=
	//namespace {
	//
	//const int EXTRACTED = 2 * 21;
	//
	//}
	//
	//void foo() {
	//	int i = EXTRACTED;
	//}
	public void testSingleBinaryExpression() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo(int) {
	//	foo(/*$*/2 * 21/*$$*/);
	//}
	//=
	//namespace {
	//
	//const int EXTRACTED = 2 * 21;
	//
	//}
	//
	//void foo(int) {
	//	foo(EXTRACTED);
	//}
	public void testBinaryExpressionInFunctionCall() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo(int, int) {
	//	foo(/*$*/2, 21/*$$*/);
	//}
	public void testExtractTwoIndependentLiterals() throws Exception {
		assertRefactoringFailure();
	}

	//A.cpp
	//void foo(int, int) {
	//	/*$*/foo(2, 21)/*$$*/;
	//}
	public void testExtractFunctionCall() throws Exception {
		assertRefactoringFailure();
	}

	//A.cpp
	//void foo() {
	//	int i = 42;
	//}
	public void testNoSelection() throws Exception {
		assertRefactoringFailure();
	}

	//A.cpp
	//void foo() {
	//	int i = 15;
	//	int j = /*$*/i/*$$*/;
	//}
	public void testExtractIdentifier() throws Exception {
		assertRefactoringFailure();
	}

	//A.cpp
	//void foo() {
	//	int i = 4/*$*//*$$*/2;
	//}
	//=
	//namespace {
	//
	//const int EXTRACTED = 42;
	//
	//}
	//
	//void foo() {
	//	int i = EXTRACTED;
	//}
	public void testCarretInLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void foo() {
	//	int i = 42/*$*//*$$*/;
	//}
	//=
	//namespace {
	//
	//const int EXTRACTED = 42;
	//
	//}
	//
	//void foo() {
	//	int i = EXTRACTED;
	//}
	public void testCarretAtLiteral() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//int i = /*$*/42/*$$*/;
	public void testDefaultNameForIntegerLiteral() throws Exception {
		runUpToInitialConditions();
		ExtractConstantInfo refactoringInfo = refactoring.getRefactoringInfo();
		assertEquals("_42", refactoringInfo.getName());
	}

	// A.cpp
	// namespace NS_1 {
	// int i_in_NS1, j_in_NS1;
	// struct S_in_NS1;
	// }
	// namespace NS_2 {
	// int i_in_NS2;
	// struct S_in_NS2;
	// }
	// using NS_1::j_in_NS1;
	// using namespace NS_2;
	// int global_variable;
	// void free_function();
	// struct S {
	// void function(int parameter) {
	// int local_before;
	// int local_at = /*$*/42/*$$*/;
	// {
	// int nested;
	// }
	// int local_after;
	// }
	// int member_variable;
	// void member_function();
	// };
	public void testOccupiedAndFreeNamesInContext() throws Exception {
		runUpToInitialConditions();
		ExtractConstantInfo refactoringInfo = refactoring.getRefactoringInfo();

		String[] expectedOccupiedNames = { "free_function", "function", "global_variable", "i_in_NS2", "j_in_NS1",
				"local_after", "local_at", "local_before", "member_function", "member_variable", "parameter", "NS_1",
				"NS_2", "S", "S_in_NS2" };
		String[] expectedFreeNames = { "_42", "i_in_NS1", "nested", "S_in_NS1" };
		String[] allNames = combine(expectedOccupiedNames, expectedFreeNames);
		String[] usedNames = Arrays.stream(allNames).filter(refactoringInfo::isNameUsed).toArray(String[]::new);
		String[] freeNames = Arrays.stream(allNames).filter(not(refactoringInfo::isNameUsed)).toArray(String[]::new);

		assertEquals(Arrays.toString(expectedOccupiedNames), Arrays.toString(usedNames));
		assertEquals(Arrays.toString(expectedFreeNames), Arrays.toString(freeNames));
	}

	private <T> Predicate<? super T> not(Predicate<? super T> predicate) {
		return predicate.negate()::test;
	}

	private String[] combine(String[] array1, String[] array2) {
		String[] result = new String[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	private void runUpToInitialConditions() throws CoreException {
		createRefactoring();
		refactoring.setContext(new CRefactoringContext(refactoring));
		refactoring.checkInitialConditions(npm());
	}

	//A.cpp
	//int h = 42;
	//void foo() {
	//	int j = 42;
	//	int i = /*$*/42/*$$*/;
	//}
	//=
	//namespace {
	//
	//const int EXTRACTED = 42;
	//
	//}
	//
	//int h = 42;
	//void foo() {
	//	int j = 42;
	//	int i = EXTRACTED;
	//}
	public void testExtractOnlyOneOccurrence() throws Exception {
		replaceAllLiterals = false;
		assertRefactoringSuccess();
	}
}
