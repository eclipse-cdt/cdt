/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.togglefunction;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleRefactoring;

/**
 * Tests for ToggleRefactoring for C++ projects.
 */
public class ToggleRefactoringTest extends RefactoringTestBase {
	private ToggleRefactoring refactoring;

	public ToggleRefactoringTest() {
		super();
	}

	public ToggleRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = suite(ToggleRefactoringTest.class);
		suite.addTestSuite(ToggleRefactoringCTest.class);
		suite.addTestSuite(ToggleNodeHelperTest.class);
		return suite;
	}

	@Override
	public void setUp() throws Exception {
		createEmptyFiles = false;
		super.setUp();
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new ToggleRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		refactoring.getContext().setSettedDefaultAnswer(true);
		refactoring.getContext().setDefaultAnswer(true);
	}

	//A.h
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//====================
	//void freefunction();

	//A.cpp
	//====================
	//#include "A.h"
	//
	//void freefunction() {
	//	return;
	//}
	public void testFileCreationFreeFunctionFromHeaderToImpl() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//====================
	//#include "A.h"
	//

	//A.h
	//====================
	//void freefunction() {
	//	return;
	//}
	public void testFileCreationFromImplToHeader() throws Exception {
		createEmptyFiles = false;
		assertRefactoringSuccess();
	}

	//A.h
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//====================
	//void freefunction();

	//A.cpp
	//====================
	//#include "A.h"
	//
	//void freefunction() {
	//	return;
	//}
	public void testFreeFunctionFromHeaderToImpl() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//====================
	//#include "A.h"
	//

	//A.h
	//====================
	//void freefunction() {
	//	return;
	//}
	public void testFreeFunctionFromImplToHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	void foo() {
	//	}
	//
	//private:
	//	int /*$*/x/*$$*/;
	//};
	public void testTestNotSupportedVariableSelection() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//		void /*$*/foo/*$$*/();
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	public void testTestNotSupportedNoDefinition() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	public void testTestNotSupportedNoTranslationunit() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void /*$*/foo/*$$*/();
	//	void foo();
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	public void testTestMultipleDeclarations() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void foo();
	//	void /*$*/foo/*$$*/() {
	//		return;
	//	}
	//};
	//
	//void blah() {
	//}
	//
	//inline void A::foo() {
	//	return;
	//}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	public void testTestMultipledefinitions() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//class A {
	//	void foo() {
	//		void /*$*/bar/*$$*/() {
	//		}
	//	}
	//};
	public void testTestNotSupportedNestedFunctions() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//class A {
	//	void me/*$*//*$$*/mber() {
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestZeroLengthSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void m/*$*/e/*$$*/mber() {
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestSubstringSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void member() {
	//		r/*$*//*$$*/eturn;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestBodySelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void member() {
	//		int /*$*/abcd/*$$*/ = 42;
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	int abcd = 42;
	//	return;
	//}
	public void testTestBodySelectionWithConfusingName() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	/*$*//*$$*/void member() {
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestLeftBorderSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void member() {
	//		return;
	//	}/*$*//*$$*/
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestRightBorderSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	vo/*$*/id member() {
	//		ret/*$$*/urn;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testTestOverlappingSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	int /*$*/function/*$$*/() {
	//		return 0;
	//	}
	//
	//private:
	//	int a;
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//public:
	//	int function();
	//
	//private:
	//	int a;
	//};
	//
	//inline int A::function() {
	//	return 0;
	//}
	public void testTestSimpleFunctionInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	int function();
	//
	//private:
	//	int a;
	//};
	//
	//inline int A::/*$*/function/*$$*/() {
	//	return 0;
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//public:
	//	int function();
	//
	//private:
	//	int a;
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int A::function() {
	//	return 0;
	//}
	public void testTestSimpleFunctionInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int A::/*$*/function/*$$*/() {
	//	return 0;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	int function();
	//
	//private:
	//	int a;
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//public:
	//	int function() {
	//		return 0;
	//	}
	//
	//private:
	//	int a;
	//};
	public void testTestSimpleFunctionInImplementationToInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//MyClass.cpp
	//#include "MyClass.h"
	//
	//myClass::/*$*/myClass/*$$*/(int implname) :
	//		fVal(implname) {
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "MyClass.h"
	//
	//int main() {
	//	return 0;
	//}

	//MyClass.h
	//
	//struct myClass {
	//	int fVal;
	//	myClass(int headername);
	//};
	//====================
	//
	//struct myClass {
	//	int fVal;
	//	myClass(int implname) :
	//			fVal(implname) {
	//	}
	//};
	public void testTestDifferentParameterNames() throws Exception {
		assertRefactoringSuccess();
	}

	//MyClass.cpp
	//#include "MyClass.h"
	//
	//myClass::/*$*/myClass/*$$*/(int implname) :
	//		fVal(implname) {
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "MyClass.h"
	//
	//int main() {
	//	return 0;
	//}

	//MyClass.h
	//struct myClass {
	//	int fVal;
	//	myClass(int);
	//};
	//====================
	//struct myClass {
	//	int fVal;
	//	myClass(int implname) :
	//			fVal(implname) {
	//	}
	//};
	public void testTestMissingParameterNames() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//template <typename T, typename U>
	//class A {
	//	class B {
	//		T /*$*/member/*$$*/() {
	//			return T();
	//		}
	//	};
	//};
	//====================
	//#include <iostream>
	//
	//template <typename T, typename U>
	//class A {
	//	class B {
	//		T member();
	//	};
	//};
	//
	//template<typename T, typename U>
	//inline T A<T, U>::B::member() {
	//	return T();
	//}
	public void testTestTemplateFunctionInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//	class B {
	//		T member();
	//	};
	//};
	//
	//template<typename T>
	//inline T A<T>::B::/*$*/member/*$$*/() {
	//	return T();
	//}
	//====================
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//	class B {
	//		T member() {
	//			return T();
	//		}
	//	};
	//};
	public void testTestTemplateFunctionInHeaderToInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	tem/*$*/plate/*$$*/<typename T>
	//	T foo() {
	//		return T();
	//	}
	//};
	//====================
	//class A {
	//	template<typename T>
	//	T foo();
	//};
	//
	//template<typename T>
	//inline T A::foo() {
	//	return T();
	//}
	public void testTestTemplateFunctionInHeaderToInClassWithTemplateSelected() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T, typename S>
	//class A {
	//public:
	//	template<typename U, typename V>
	//	void /*$*/foo/*$$*/(const U& u, const V& v) {
	//		return;
	//	}
	//};
	//====================
	//template<typename T, typename S>
	//class A {
	//public:
	//	template<typename U, typename V>
	//	void foo(const U& u, const V& v);
	//};
	//
	//template<typename T, typename S>
	//template<typename U, typename V>
	//inline void A<T, S>::foo(const U& u, const V& v) {
	//	return;
	//}
	public void testTestComplexTemplateFunctionFromInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T, typename S>
	//class A {
	//public:
	//	template<typename U, typename V>
	//	void /*$*/foo/*$$*/(const U& u, const V& v);
	//};
	//
	//template<typename T, typename S>
	//template<typename U, typename V>
	//inline void A<T,S>::foo(const U& u, const V& v) {
	//	return;
	//}
	//====================
	//template<typename T, typename S>
	//class A {
	//public:
	//	template<typename U, typename V>
	//	void foo(const U& u, const V& v) {
	//		return;
	//	}
	//};
	public void testTestComplexTemplateFunctionFromInHeaderToInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void /*$*/foo/*$$*/() {
	//		return;
	//	}
	//};
	//
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//inline void A::foo() {
	//	return;
	//}
	//
	//}
	public void testTestSimpleNamespaceInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//inline void A::/*$*/foo/*$$*/() {
	//	return;
	//}
	//
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//namespace N {
	//
	//void A::foo() {
	//	return;
	//}
	//
	//}
	public void testTestSimpleNamespaceInHeaderToImplementationWithinNamespaceDefinition() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//inline void A::/*$*/foo/*$$*/() {
	//	return;
	//}
	//
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//namespace N {
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//namespace N {
	//
	//void A::foo() {
	//	return;
	//}
	//
	//}
	public void testTestSimpleNamespaceInHeaderToImplementationWithNamespaceDefinitionInImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}
	//
	//inline void /*$*/N::A::foo/*$$*/() {
	//	return;
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//namespace N {
	//
	//void A::foo() {
	//	return;
	//}
	//
	//}
	public void testTestSimpleNamespaceInHeaderToImplementationWithNamespaceQualifiedName() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//void /*$*/N::A::foo/*$$*/() {
	//	return;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}
	//====================
	//#include <iostream>
	//
	//namespace N {
	//
	//class A {
	//	void foo() {
	//		return;
	//	}
	//};
	//
	//}
	public void testTestSimpleNamespaceFromImplementationToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//namespace N {
	//
	//void /*$*/A::foo/*$$*/() {
	//	return;
	//}
	//
	//}
	//====================
	//#include "A.h"

	//A.h
	//#include <iostream>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}
	//====================
	//#include <iostream>
	//
	//namespace N {
	//
	//class A {
	//	void foo() {
	//		return;
	//	}
	//};
	//
	//}
	public void testTestRemoveEmptyNamespaceFromImplentation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void /*$*/member/*$$*/(int a, int b)
	//	try {
	//		return;
	//	}
	//	catch (std::exception& e1){
	//		return;
	//	}
	//};
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};
	//
	//inline void A::member(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	public void testTestTryCatchFromInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};
	//
	//inline void /*$*/A::member/*$$*/(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::member(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	public void testTestTryCatchFromInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include <exception>
	//#include "A.h"
	//
	//void A::/*$*/member/*$$*/()
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include <exception>
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void member();
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	void member()
	//	try {
	//		return;
	//	}
	//	catch (std::exception& e1) {
	//		return;
	//	}
	//};
	public void testTestTryCatchFromInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void /*$*/member/*$$*/(int a, int b)
	//	try {
	//		return;
	//	}
	//	catch (std::exception& e1) {
	//		return;
	//	}
	//	catch (std::exception& e2) {
	//		return;
	//	}
	//};
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};
	//
	//inline void A::member(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//catch (std::exception& e2) {
	//	return;
	//}
	public void testTestMultipleTryCatchFromInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};
	//
	//inline void /*$*/A::member/*$$*/(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//catch (std::exception& e2) {
	//	return;
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//class A {
	//	void member(int a, int b);
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::member(int a, int b)
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//catch (std::exception& e2) {
	//	return;
	//}
	public void testTestMultipleTryCatchFromInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include <exception>
	//#include "A.h"
	//
	//void A::/*$*/member/*$$*/()
	//try {
	//	return;
	//}
	//catch (std::exception& e1) {
	//	return;
	//}
	//catch (std::exception& e2) {
	//	return;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include <exception>
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void member();
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	void member()
	//	try {
	//		return;
	//	}
	//	catch (std::exception& e1) {
	//		return;
	//	}
	//	catch (std::exception& e2) {
	//		return;
	//	}
	//};
	public void testTestMultipleTryCatchFromInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void /*$*/member/*$$*/(int a = 0, int b = 0) {
	//		return;
	//	}
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	void member(int a = 0, int b = 0);
	//};
	//
	//inline void A::member(int a, int b) {
	//	return;
	//}
	public void testTestDefaultParameterInitializerInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void member(int a = 0, int b = 0);
	//};
	//
	//inline void /*$*/A::member/*$$*/(int a, int b) {
	//	return;
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//	void member(int a = 0, int b = 0);
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::member(int a, int b) {
	//	return;
	//}
	public void testTestDefaultParameterInitializerInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//void A::/*$*/member/*$$*/(int a, int b) {
	//	return;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	void member(int a = 0, int b = 0);
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	void member(int a = 0, int b = 0) {
	//		return;
	//	}
	//};
	public void testTestDefaultParameterInitializerInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	/*$*/A/*$$*/(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y);
	//	~A() {
	//	}
	//};
	//
	//inline A::A(int x, int y) :
	//		a(x), b(y) {
	//}
	public void testTestConstructorToggleInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y);
	//	~A() {
	//	}
	//};
	//
	//inline A::/*$*/A/*$$*/(int x, int y) :
	//		a(x), b(y) {
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y);
	//	~A() {
	//	}
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//A::A(int x, int y) :
	//		a(x), b(y) {
	//}
	public void testTestConstructorToggleInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//A::/*$*/A/*$$*/(int x, int y) :
	//		a(x), b(y) {
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	A(int x, int y);
	//	~A() {
	//	}
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	public void testTestConstructorToggleInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	/*$*/~A/*$$*/() {
	//	}
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	~A();
	//};
	//
	//inline A::~A() {
	//}
	public void testTestDestructorToggleInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	~A();
	//};
	//
	//inline /*$*/A::~A/*$$*/() {
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	~A();
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//A::~A() {
	//}
	public void testTestDestructorToggleInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	///*$*/A::~A/*$$*/() {
	//	int x;
	//	int y;
	//	return;
	//}
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A();
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//		int x;
	//		int y;
	//		return;
	//	}
	//};
	public void testTestDestructorToggleInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void /*$*/member/*$$*/(int a, int b) {
	//			return;
	//		}
	//	};
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void member(int a, int b);
	//	};
	//};
	//
	//inline void A::B::member(int a, int b) {
	//	return;
	//}
	public void testTestNestedClassInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void member(int a, int b);
	//	};
	//};
	//
	//inline void A::B::/*$*/member/*$$*/(int a, int b) {
	//	return;
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void member(int a, int b);
	//	};
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::B::member(int a, int b) {
	//	return;
	//}
	public void testTestNestedClassInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::B::/*$*/member/*$$*/(int a, int b) {
	//	return;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void member(int a, int b);
	//	};
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//	class B {
	//		void member(int a, int b) {
	//			return;
	//		}
	//	};
	//};
	public void testTestNestedClassInImplementationToClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void N::A::/*$*/foo/*$$*/() {
	//	return;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo();
	//};
	//
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo() {
	//		return;
	//	}
	//};
	//
	//}
	public void testTestImplementationToClassWithDefintionSelected() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void /*$*/foo/*$$*/();
	//};
	//
	//}
	//====================
	//#include <iostream>
	//#include <exception>
	//
	//namespace N {
	//
	//class A {
	//	void foo() {
	//		return;
	//	}
	//};
	//
	//}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void N::A::foo() {
	//	return;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	public void testTestImplementationToClassWithDeclarationSelected() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//
	//int /*$*/freeFunction/*$$*/(int* a, int& b) {
	//	return 42;
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//
	//int freeFunction(int* a, int& b);

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int freeFunction(int* a, int& b) {
	//	return 42;
	//}
	public void testTestFreeFunctionToggleFromHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int /*$*/freeFunction/*$$*/(int* a, int& b) {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//
	//int freeFunction(int* a, int& b);
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//
	//int freeFunction(int* a, int& b) {
	//	return 42;
	//}
	public void testTestFreeFunctionToggleFromImplementationToHeaderWithDeclaration() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int /*$*/freeFunction/*$$*/(int* a, int& b)
	//try {
	//	return 42;
	//}
	//catch (std::exception& e) {
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}

	//A.h
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//====================
	//#include <iostream>
	//
	//class A {
	//private:
	//	int a;
	//	int b;
	//
	//public:
	//	A(int x, int y) :
	//			a(x), b(y) {
	//	}
	//	~A() {
	//	}
	//};
	//
	//int freeFunction(int* a, int& b)
	//try {
	//	return 42;
	//}
	//catch (std::exception& e) {
	//}
	public void testTestFreeFunctionToggleFromImplementationToHeaderWithOutDeclaration() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//int /*$*/freeFunction/*$$*/() {
	//	return 42;
	//}
	//====================
	//int freeFunction();

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//int freeFunction() {
	//	return 42;
	//}
	public void testTestFreeFunction() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//int /*$*/A::freefunction/*$$*/() {
	//	return 42;
	//}
	//====================
	//#include "A.h"
	//
	//int A::freefunction() {
	//	return 42;
	//}
	public void testTestQualifiedNameToggle() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//namespace N {
	//
	//void /*$*/freefunction/*$$*/() {
	//	return;
	//}
	//
	//}
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//namespace N {
	//
	//void freefunction();
	//
	//}
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//namespace N {
	//
	//void freefunction() {
	//	return;
	//}
	//
	//}
	public void testTestNamespacedFreeFunction() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//
	//class A {
	//	virtual int /*$*/foo/*$$*/() {
	//		return 0;
	//	}
	//};
	//====================
	//
	//class A {
	//	virtual int foo();
	//};
	//
	//inline int A::foo() {
	//	return 0;
	//}
	public void testTestRemoveVirtualSpecifierFromClassToInheader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	virtual int /*$*/foo/*$$*/();
	//};
	//
	//inline int A::foo() {
	//	return 0;
	//}
	//====================
	//class A {
	//	virtual int foo();
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int A::foo() {
	//	return 0;
	//}
	public void testTestVirtualSpecifierFromInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	virtual int /*$*/foo/*$$*/();
	//};
	//====================
	//class A {
	//	virtual int foo() {
	//		return 0;
	//	}
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//int A::foo() {
	//	return 0;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	public void testTestVirtualSpecifierFromImplementationToHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	void func1();
	//	void /*$*/func2/*$$*/() {
	//	}
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//inline void A::func1() {
	//}
	//
	//inline void A::func3() {
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//public:
	//	void func1();
	//	void func2();
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//inline void A::func1() {
	//}
	//
	//inline void A::func2() {
	//}
	//
	//inline void A::func3() {
	//}
	public void testTestCorrectOrderingInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//class A {
	//public:
	//	void func1();
	//	void func2();
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//inline void A::/*$*/func2/*$$*/() {
	//	return;
	//}
	//====================
	//#include <iostream>
	//
	//class A {
	//public:
	//	void func1();
	//	void func2();
	//	void func3();
	//	void func4() {
	//	}
	//};

	//A.cpp
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::func1() {
	//	return;
	//}
	//
	//void A::func3() {
	//	return;
	//}
	//====================
	//#include "A.h"
	//
	//int main() {
	//	return 0;
	//}
	//
	//void A::func1() {
	//	return;
	//}
	//
	//void A::func2() {
	//	return;
	//}
	//
	//void A::func3() {
	//	return;
	//}
	public void testTestCorrectOrderingInHeaderToImplementation() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//public:
	//	void func1();
	//	void /*$*/func2/*$$*/() {
	//	}
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//template<typename T>
	//inline void A<T>::func1() {
	//}
	//
	//template<typename T>
	//inline void A<T>::func3() {
	//}
	//====================
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//public:
	//	void func1();
	//	void func2();
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//template<typename T>
	//inline void A<T>::func1() {
	//}
	//
	//template<typename T>
	//inline void A<T>::func2() {
	//}
	//
	//template<typename T>
	//inline void A<T>::func3() {
	//}
	public void testTestCorrectTemplateOrderingInClassToInHeader() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//public:
	//	void func1();
	//	void func2();
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//template<typename T>
	//inline void A<T>::func1() {
	//}
	//
	//template<typename T>
	//inline void A<T>::/*$*/func2/*$$*/() {
	//}
	//
	//template<typename T>
	//inline void A<T>::func3() {
	//}
	//====================
	//#include <iostream>
	//
	//template <typename T>
	//class A {
	//public:
	//	void func1();
	//	void func2() {
	//	}
	//	void func3();
	//	void func4() {
	//	}
	//};
	//
	//template<typename T>
	//inline void A<T>::func1() {
	//}
	//
	//template<typename T>
	//inline void A<T>::func3() {
	//}
	public void testTestCorrectTemplateOrderingInHeaderToInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/() {
	//		// return comment
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	// return comment
	//	return;
	//}
	public void testClassToHeaderBodyComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	// First Top Comment
	//	// Second Top Comment
	//	void /*$*/member/*$$*/() {
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	// First Top Comment
	//	// Second Top Comment
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testClassToHeaderTopCommentOrder() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/() try
	//	{
	//		return;
	//	}
	//	catch (int i) {
	//		// catch comment
	//	}
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member()
	//try {
	//	return;
	//}
	//catch (int i) {
	//	// catch comment
	//}
	public void testClassToHeaderCatchComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	// Top comment
	//	void /*$*/member/*$$*/() {
	//		return;
	//	}
	//};
	//====================
	//class A {
	//	// Top comment
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//}
	public void testClassToHeaderTopComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	// Top comment
	//	template<typename T>
	//	T /*$*/member/*$$*/() {
	//		return T();
	//	}
	//};
	//====================
	//class A {
	//	// Top comment
	//	template<typename T>
	//	T member();
	//};
	//
	//// Top comment
	//template<typename T>
	//inline T A::member() {
	//	return T();
	//}
	public void testClassToHeaderTemplateTopComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/() {
	//		return;
	//	} // Trailing comment
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member() {
	//	return;
	//} // Trailing comment
	public void testClassToHeaderTrailingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/()
	//	try {
	//		return;
	//	}
	//	catch (int e) {
	//	} // Trailing comment
	//};
	//====================
	//class A {
	//	void member();
	//};
	//
	//inline void A::member()
	//try {
	//	return;
	//}
	//catch (int e) {
	//}
	//// Trailing comment
	public void testClassToHeaderTrailingCommentWithTryBlock() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	int /*$*/member/*$$*/()
	//	try {
	//		// aans
	//	} /* one */ catch (int i) {
	//		// zwaa
	//	} /* two */ catch (int j) {
	//		// draa
	//	} /* three */
	//};
	//====================
	//class A {
	//	int member();
	//};
	//
	//inline int A::member()
	//try {
	//	// aans
	//} /* one */
	//catch (int i) {
	//	// zwaa
	//}
	///* two */catch (int j) {
	//	// draa
	//}
	///* three */
	public void testClassToHeaderTrailingMultipleCommentsInTryBlock() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T>
	//class A {
	//	T /*$*/member/*$$*/();
	//};
	//
	//template<typename T>
	//inline T A<T>::member() {
	//	// body comment
	//	return T();
	//}
	//====================
	//template<typename T>
	//class A {
	//	T member() {
	//		// body comment
	//		return T();
	//	}
	//};
	public void testHeaderToClassBodyComment1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T>
	//class A {
	//	// First comment
	//	// Second comment
	//	T /*$*/member/*$$*/();
	//};
	//
	//// Third comment
	//// Fourth comment
	//template<typename T>
	//inline T A<T>::member() {
	//	return T();
	//}
	//====================
	//template<typename T>
	//class A {
	//	// First comment
	//	// Second comment
	//	// Third comment
	//	// Fourth comment
	//	T member() {
	//		return T();
	//	}
	//};
	public void testHeaderToClassRetainTopComments() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T>
	//class A {
	//	T /*$*/member/*$$*/();
	//};
	//
	//template<typename T>
	//inline T A<T>::member()
	//try {
	//	// body comment
	//	return T();
	//}
	//catch (int e) {
	//	// Catch 1
	//}
	//catch (int e) {
	//	// Catch 2
	//}
	//====================
	//template<typename T>
	//class A {
	//	T member()
	//	try {
	//		// body comment
	//		return T();
	//	}
	//	catch (int e) {
	//		// Catch 1
	//	}
	//	catch (int e) {
	//		// Catch 2
	//	}
	//};
	public void testHeaderToClassTryCatchComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename S, typename T>
	//class A {
	//	// Top Comment
	//	template<typename U, typename V>
	//	T /*$*/member/*$$*/();
	//};
	//
	//// 2nd Top Comment
	//template<typename S, typename T>
	//template<typename U, typename V>
	//inline T A<S, T>::member() {
	//	// body comment
	//	return T();
	//}
	//====================
	//template<typename S, typename T>
	//class A {
	//	// Top Comment
	//	template<typename U, typename V>
	//	T member() {
	//		// body comment
	//		return T();
	//	}
	//};
	public void testHeaderToClassMultiTemplateComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//template<typename T>
	//class A {
	//	T /*$*/member/*$$*/();
	//};
	//
	//// Top comment
	//template<typename T>
	//inline T A<T>::member() {
	//	return T();
	//}
	//====================
	//template<typename T>
	//class A {
	//	// Top comment
	//	T member() {
	//		return T();
	//	}
	//};
	public void testHeaderToClassBodyComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/();
	//};
	//
	//inline void A::member() {
	//	// body comment
	//	return;
	//}
	//====================
	//class A {
	//	void member();
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void A::member() {
	//	// body comment
	//	return;
	//}
	public void testHeaderToImplBodyComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/();
	//};
	//
	//inline void A::member() try {
	//	// body comment
	//	return;
	//} catch /*1*/ (int e) { /*2*/ }
	//catch /*3*/ (int e) { /*4*/ }
	//====================
	//class A {
	//	void member();
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void A::member()
	//try {
	//	// body comment
	//	return;
	//}
	//catch (/*1*/int e) {
	//	/*2*/
	//}
	//catch (/*3*/int e) {
	//	/*4*/
	//}
	public void testHeaderToImplTryCatchComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class A {
	//	void /*$*/member/*$$*/();
	//};
	//
	//// Top comment
	//inline void A::member() {
	//	// body comment
	//	return;
	//}
	//====================
	//class A {
	//	void member();
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//// Top comment
	//void A::member() {
	//	// body comment
	//	return;
	//}
	public void testHeaderToImplTopComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//// Definition comment
	//void /*$*/member/*$$*/() {
	//	return;
	//}
	//====================
	//// Definition comment
	//void member();

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//// Definition comment
	//void member() {
	//	return;
	//}
	public void testHeaderToImplFreeFuncTopComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//void A::/*$*/member/*$$*/() {
	//	// body comment
	//	return;
	//}
	//====================
	//#include "A.h"

	//A.h
	//class A {
	//	void member();
	//};
	//====================
	//class A {
	//	void member() {
	//		// body comment
	//		return;
	//	}
	//};
	public void testImplToHeaderBodyComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//// Definition comment
	//void A::/*$*/member/*$$*/() {
	//	return;
	//}
	//====================
	//#include "A.h"

	//A.h
	//class A {
	//	void member();
	//};
	//====================
	//class A {
	//	// Definition comment
	//	void member() {
	//		return;
	//	}
	//};
	public void testImplToHeaderTopComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//// Definition comment
	//void A::/*$*/member/*$$*/() try {
	//	return;
	//} /*1*/ catch (int e) { /*2*/ } /*3*/ catch (int e) { /*4*/ }
	//====================
	//#include "A.h"

	//A.h
	//class A {
	//	void member();
	//};
	//====================
	//class A {
	//	// Definition comment
	//	void member()
	//	try {
	//		return;
	//	} /*1*/
	//	catch (int e) {
	//		/*2*/
	//	}
	//	/*3*/catch (int e) {
	//		/*4*/
	//	}
	//};
	public void testImplToHeaderTryCatchComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//void /*$*/member/*$$*/() {
	//	// body comment
	//	return;
	//}
	//====================
	//#include "A.h"

	//A.h
	//
	//====================
	//void member() {
	//	// body comment
	//	return;
	//}
	public void testImplToHeaderBodyCommentWithoutDeclaration() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//#include "A.h"
	//
	//// Top comment
	//void /*$*/member/*$$*/() {
	//	// body comment
	//	return;
	//}
	//====================
	//#include "A.h"

	//A.h
	//
	//====================
	//// Top comment
	//void member() {
	//	// body comment
	//	return;
	//}
	public void testImplToHeaderTopCommentWithoutDeclaration() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#define MACRO 1
	//int /*$*/freefunction/*$$*/() {
	//	return MACRO;
	//}
	//====================
	//#define MACRO 1
	//int freefunction();

	//A.cpp
	//====================
	//#include "A.h"
	//
	//int freefunction() {
	//	return MACRO;
	//}
	public void testFunctionWithMacroReference_399215() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//void /*$*/foo/*$$*/() {
	//}
	//}
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//void foo();
	//}
	//}

	//A.cpp
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//void foo() {
	//}
	//
	//}
	//
	//}
	public void testFunctionInNestedNamespaceToSource_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//void /*$*/foo/*$$*/();
	//}
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//void foo() {
	//}
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//void foo() {
	//}
	//
	//}
	//
	//}
	//====================
	//#include "A.h"

	public void testFunctionInNestedNamespaceToHeader_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//void /*$*/foo/*$$*/();
	//}
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//void foo() {
	//}
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace outer {
	//
	//void bar() {
	//}
	//
	//namespace inner {
	//
	//void foo() {
	//}
	//
	//}
	//
	//}
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//void bar() {
	//}
	//
	//}

	public void testFunctionInNestedNamespaceToHeaderLeaveNonemptyNamespace_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void /*$*/foo/*$$*/();
	//};
	//}
	//}
	//void outer::inner::S::foo() {
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void foo();
	//};
	//}
	//}

	//A.cpp
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//void S::foo() {
	//}
	//
	//}
	//
	//}
	public void testQualifiedFunctionInNestedNamespaceToSource_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void /*$*/foo/*$$*/();
	//};
	//}
	//}
	//void outer::inner::S::foo() {
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void foo();
	//};
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//}
	//
	//}
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//void S::foo() {
	//}
	//
	//}
	//
	//}
	public void testQualifiedFunctionInNestedNamespaceToSourceWithInnerNamespace_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//namespace outer {
	//struct S {
	//	void /*$*/foo/*$$*/();
	//};
	//}
	//}
	//}
	//void outer::inner::outer::S::foo() {
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//namespace outer {
	//struct S {
	//	void foo();
	//};
	//}
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//}
	//
	//}
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//namespace outer {
	//
	//void S::foo() {
	//}
	//
	//}
	//
	//}
	//
	//}
	public void testQualifiedFunctionInRecurringNestedNamespaceToSourceWithInnerNamespace_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//namespace outer {
	//struct S {
	//	void /*$*/foo/*$$*/();
	//};
	//}
	//}
	//}
	//void outer::inner::outer::S::foo() {
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//namespace outer {
	//struct S {
	//	void foo();
	//};
	//}
	//}
	//}

	//A.cpp
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//namespace outer {
	//
	//void S::foo() {
	//}
	//
	//}
	//
	//}
	//
	//}
	public void testQualifiedFunctionInRecurringNestedNamespaceToNewSource_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void /*$*/foo/*$$*/();
	//};
	//}
	//}
	//void outer::inner::S::foo() {
	//}
	//====================
	//namespace outer {
	//namespace inner {
	//struct S {
	//	void foo();
	//};
	//}
	//}

	//A.cpp
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//namespace outer {
	//}
	//
	//}
	//
	//}
	//====================
	//#include "A.h"
	//namespace outer {
	//
	//namespace inner {
	//
	//namespace outer {
	//}
	//
	//void S::foo() {
	//}
	//
	//}
	//
	//}
	public void testQualifiedFunctionInNestedNamespaceToSourceWithRecurringNamespaceName_464102() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//typedef unsigned long uL;
	//uL f();
	//uL /*$*/f/*$$*/() {
	//	ulong a;
	//	return a;
	//}
	//====================
	//typedef unsigned long uL;
	//uL f();

	//A.c
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//uL f() {
	//	ulong a;
	//	return a;
	//}
	public void testToggleFunctionWithTypedefReturntypeToSource_399217() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//typedef unsigned long uL;
	//uL f();
	//====================
	//typedef unsigned long uL;
	//uL f() {
	//	ulong a;
	//	return a;
	//}

	//A.c
	//#include "A.h"
	//
	//uL /*$*/f/*$$*/() {
	//	ulong a;
	//	return a;
	//}
	//====================
	//#include "A.h"
	public void testToggleFunctionWithTypedefReturntypeToHeader_399217() throws Exception {
		assertRefactoringSuccess();
	}

	//A.c
	//typedef unsigned long uL;
	//uL /*$*/f/*$$*/() {
	//	ulong a;
	//	return a;
	//}
	//====================
	//#include "A.h"
	//
	//typedef unsigned long uL;

	//A.h
	//====================
	public void testToggleFunctionWithTypedefReturntypeSourceToSource_399217() throws Exception {
		assertRefactoringSuccess();
	}
}
