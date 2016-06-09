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
package org.eclipse.cdt.ui.tests.refactoring.extractconstant;

import junit.framework.Test;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantInfo;
import org.eclipse.cdt.internal.ui.refactoring.extractconstant.ExtractConstantRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Tests for Extract Constant refactoring.
 */
public class ExtractConstantRefactoringTest extends RefactoringTestBase {
	private String extractedConstantName = "EXTRACTED";
	private VisibilityEnum visibility = VisibilityEnum.v_private;
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
	protected CRefactoring createRefactoring() {
		refactoring = new ExtractConstantRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		ExtractConstantInfo info = refactoring.getRefactoringInfo();
		info.setName(extractedConstantName);
		info.setVisibility(visibility);
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
	//		char* a = /*$*/"sometext"/*$$*/;
	//	}
	//
	//	void method2() {
	//		const char* b = "sometext";
	//	}
	//};
	//====================
	//class X {
	//	void method() {
	//		char* a = EXTRACTED;
	//	}
	//
	//	void method2() {
	//		const char* b = EXTRACTED;
	//	}
	//
	//	static const char* EXTRACTED = "sometext";
	//};
	public void testString() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class X {
	//	void method() {
	//		wchar_t* a = /*$*/L"sometext"/*$$*/;
	//	}
	//
	//	void method2() {
	//		const wchar_t* b = L"sometext";
	//		const char* c = "sometext";
	//	}
	//};
	//====================
	//class X {
	//	void method() {
	//		wchar_t* a = EXTRACTED;
	//	}
	//
	//	void method2() {
	//		const wchar_t* b = EXTRACTED;
	//		const char* c = "sometext";
	//	}
	//
	//	static const wchar_t* EXTRACTED = L"sometext";
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
	//project="RegressionTestProject" selection="64,2" visibility="public"/>
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
}
