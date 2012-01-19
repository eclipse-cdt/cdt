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
package org.eclipse.cdt.ui.tests.refactoring.extractfunction;

import junit.framework.Test;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Tests for Extract Function refactoring.
 */
public class ExtractFunctionRefactoringTest extends RefactoringTestBase {
	private ExtractFunctionInformation refactoringInfo;
	private String extractedFunctionName = "extracted";
	private String returnValue;
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private boolean virtual;
	private boolean replaceDuplicates = true;

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
	protected Refactoring createRefactoring() {
		refactoringInfo = new ExtractFunctionInformation();
		return new ExtractFunctionRefactoring(getSelectedFile(), getSelection(), refactoringInfo,
				getCProject());
	}

	@Override
	protected void simulateUserInput() {
		refactoringInfo.setMethodName(extractedFunctionName);
		refactoringInfo.setReplaceDuplicates(replaceDuplicates);
		if (refactoringInfo.getMandatoryReturnVariable() == null) {
			if (returnValue != null) {
				for (NameInformation nameInfo : refactoringInfo.getParameterCandidates()) {
					if (returnValue.equals(String.valueOf(nameInfo.getName().getSimpleID()))) {
						refactoringInfo.setReturnVariable(nameInfo);
						nameInfo.setUserSetIsReference(false);
						break;
					}
				}
			}
		}
		refactoringInfo.setVisibility(visibility);
		refactoringInfo.setVirtual(virtual);
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
	public void testVariableDefinedInside() throws Exception {
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
	//void A::extracted(int& i) {
	//	//comment
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithComment() throws Exception {
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
	//void extracted(int& i) {
	//	// Comment
	//	i = 7;
	//}
	//
	//int main() {
	//	int i;
	//	// Comment
	//	extracted(i);
	//	return i;
	//}
	public void testFirstExtractedStatementWithLeadingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	/*$*/i= 7;/*$$*/ // Comment
	//	return i;
	//}
	//====================
	//void extracted(int& i) {
	//	i = 7; // Comment
	//}
	//
	//int main() {
	//	int i;
	//	extracted(i);
	//	return i;
	//}
	public void testLastExtractedStatementWithTraillingComment() throws Exception {
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
	public void testWithTwoVariableDefinedInScope() throws Exception {
		assertRefactoringFailure();
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
	public void testWithNamedTypedField() throws Exception {
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
	public void testWithNamedTypedVariableDefinedInScope() throws Exception {
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
	//#define ZWO 2
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
	//	i += ZWO;
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
	//#define ZWO 2
	//
	//A::A() {
	//}
	//
	//A::~A() {
	//}
	//
	//void A::extracted(int& i) {
	//	++i;
	//	i += ZWO;
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
	public void testWithObjectStyleMacro() throws Exception {
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
	//void A::extracted(int& i) {
	//	++i;
	//	i = ADD(i, 42);
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
	public void testWithFunctionStyleMacro() throws Exception {
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
	//	void extracted(int* i);
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
	//void A::extracted(int* i) {
	//	++*i;
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
	public void testWithPointer() throws Exception {
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
	//	void extracted(int* i);
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
	//	help();
	//	//A end-comment/*$$*/
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
	//void A::extracted(int* i) {
	//	++*i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int* i = new int(2);
	//	extracted(i);
	//	//A end-comment
	//	return *i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithPointerAndCommentAtTheEnd() throws Exception {
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
	//	void extracted(int* i);
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
	//	//A beautiful comment
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
	//void A::extracted(int* i) {
	//	++*i;
	//	help();
	//}
	//
	//int A::foo() {
	//	//A beautiful comment
	//	int* i = new int(2);
	//	extracted(i);
	//	return *i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithPointerAndComment() throws Exception {
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
	public void testWithReturnValue() throws Exception {
		returnValue = "i";
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
	public void testWithReturnValueAndRefParameter() throws Exception {
		returnValue = "i";
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
	//	int extracted(int i, B* b, int y, float x);
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
	//int A::extracted(int i, B* b, int y, float x) {
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
	//	i = extracted(i, b, y, x);
	//	++x;
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithReturnValueAndRefParameterAndSomeMoreNotUsedAfterwards() throws Exception {
		returnValue = "i";
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
	//	float extracted(int& i, B* b, int y, float x);
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
	//float A::extracted(int& i, B* b, int y, float x) {
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
	//	x = extracted(i, b, y, x);
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
	public void testWithReturnValueTakeTheSecondAndRefParameterAndSomeMoreNotUsedAferwards() throws Exception {
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
	//	int extracted(int i, B* b, int y, float x);
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
	//int A::extracted(int i, B* b, int y, float x) {
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
	//	i = extracted(i, b, y, x);
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
	public void testWithReturnValueAndALotRefParameter() throws Exception {
		returnValue = "i";
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
	//	B* extracted(int& i, B* b, int y, float x);
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
	//B* A::extracted(int& i, B* b, int y, float x) {
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
	//	b = extracted(i, b, y, x);
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
	public void testWithReturnValueTakeTheSecondAndRefParameter() throws Exception {
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
	//	void extracted(int& i);
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
	public void testWithProtectedVisibility() throws Exception {
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
	//	void extracted(int& i);
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
	public void testWithPublicVisibility() throws Exception {
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
	//	void extracted(int& i) const;
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
	//void A::extracted(int& i) const {
	//	++i;
	//	help();
	//}
	//
	//int A::foo() const {
	//	int i = 2;
	//	extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithConstMethod() throws Exception {
		assertRefactoringSuccess();
	}

	//main.h
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
	public void testDontReturnVariablesThatArentUsed() throws Exception {
		extractedFunctionName = "loop";
		assertRefactoringSuccess();
	}

	//main.h
	//void method() {
	//	/*$*/if (true)
	//		return;/*$$*/
	//	//unreachable
	//}
	public void testDontExtractCodeThatContainsReturn() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//void function() {
	//	for (int var = 0; var < 100; ++var) {
	//		/*$*/if (var < 50)
	//			continue;/*$$*/
	//	}
	//}
	public void testTestIfWeDontAllowToExtractContinue() throws Exception {
		assertRefactoringFailure();
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
	public void testExtractFunctionWithAMacroCallInSelectedCodeForgetsTheMacro() throws Exception {
		extractedFunctionName = "runTest";
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
	public void testWithCommentsExtractFunctionWithAMacroCallInSelectedCodeForgetsTheMacro() throws Exception {
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
	public void testStringArrayProblemInExtractFunction() throws Exception {
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
	//	void extracted(int& a, int b, int c);
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
	//void A::extracted(int& a, int b, int c) {
	//	a = b + c;
	//}
	//
	//int A::foo(int& a) {
	//	int b = 7;
	//	int c = 8;
	//	extracted(a, b, c);
	//	return a;
	//}
	public void testBug239059DoubleAmpersandInSignatureOfExtractedFunctions() throws Exception {
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
	public void testBug241717TypedefCausesVoidAsReturnType() throws Exception {
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
	public void testBug248238ExtractMethodProducesWrongReturnTypeOrJustFailsClasstype() throws Exception {
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
	public void testBug248238ExtractMethodProducesWrongReturnTypeOrJustFailsTypedef() throws Exception {
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
	//const char endTag(test::string name) {
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
	public void testBug248622ExtractFunctionFailsToExtractSeveralExpressionsSelectionAtTheEnd() throws Exception {
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
	//const char extracted() {
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
	public void testBug248622ExtractFunctionFailsToExtractSeveralExpressionsSelectionInTheMiddle() throws Exception {
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
	public void testBug262000ExtractFunctionMisinterpretsArtificialBlocks() throws Exception {
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
	public void testBug264712ExtractFunctionDeletesCommentsInHeader() throws Exception {
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
	//void extracted(int& a) {
	//	try {
	//		a = myFunc();
	//	} catch (const int&) {
	//		a = 3;
	//	}
	//}
	//
	//int main() {
	//	int a = 0;
	//	extracted(a);
	//	return a;
	//}
	public void testBug281564ExtractFunctionFailsWhenCatchingAnUnnamedException() throws Exception {
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
	public void testBug282004ExtractFunctionInCProjectNotCPPWontExtractParameters() throws Exception {
		assertRefactoringSuccess();
	}

	//main.c
	//int main() {
	//	int a, b;
	//	/*$*/a = b * 2;/*$$*/
	//	return a;
	//}
	//====================
	//void extracted(int* a, int b) {
	//	a = b * 2;
	//}
	//
	//int main() {
	//	int a, b;
	//	extracted(a, b);
	//	return a;
	//}
	public void testBug288268CRefactoringCreatesCPPParameters() throws Exception {
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
	//	virtual void extracted(int& i);
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
	//void A::extracted(int& i) {
	//	//comment
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	extracted(i);
	//	return i;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testWithVirtual() throws Exception {
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
	//	extracted(i);
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
	public void testWithDuplicates() throws Exception {
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
	//	int oo = 99;
	//	++oo;
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
	//	int oo = 99;
	//	extracted(oo);
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
		returnValue = "j";
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
		returnValue = "j";
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
	public void testDuplicatesWithDifferentNamesAndReturnType() throws Exception {
		returnValue = "i";
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
	public void testDuplicatesWithALotOfDifferentNamesAnVariableNotUsedAfterwardsInTheDuplicate() throws Exception {
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
	public void testWithDuplicateNameUsedAfterwardsInDuplicateButNotInOriginalSelectionThisIsNoDuplicate() throws Exception {
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
	//	int extracted(int i, B* b, int y, float x);
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
	//	i = extracted(i, b, y, x);
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::extracted(int i, B* b, int y, float x) {
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
	//	i = extracted(i, b, y, x);
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
	public void testWithReturnValueAndALotRefParameterAndAMethodCall() throws Exception {
		returnValue = "i";
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
	//	int extracted(int i, B* b, int y, float x);
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
	//	i = extracted(i, b, y, x);
	//	b->hello(y);
	//	++x;
	//	i++;
	//}
	//
	//int A::extracted(int i, B* b, int y, float x) {
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
	//	i = extracted(i, b, y, x);
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
	public void testWithReturnValueAndALotRefParameterAndAMethodCallDuplicateIsSimilar() throws Exception {
		returnValue = "i";
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
	//	extracted(i);
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
	public void testWithDuplicatesAndComments() throws Exception {
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
	//	void extracted(int& ii);
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
	//void A::extracted(int& ii) {
	//	++ii;
	//	ii = ADD(ii, 42);
	//	help();
	//}
	//
	//int A::foo() {
	//	int ii = 2;
	//	extracted(ii);
	//	return ii;
	//}
	//
	//int A::help() {
	//	return 42;
	//}
	public void testExtractFunctionRefactoringTestWithFunctionStyleMacro2() throws Exception {
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
	public void testReturnValueAssignedToMacroCall() throws Exception {
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
	public void testWithMultipleMacros() throws Exception {
		returnValue = "bb";
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
	public void testExtractFunctionHistoryRefactoringTestVariableDefinedInScope() throws Exception {
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
	//void A::extracted(int& i) {
	//	//comment
	//	++i;
	//	help();
	//}
	//
	//int A::foo() {
	//	int i = 2;
	//	//comment
	//	extracted(i);
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
	public void testExtractFunctionHistoryRefactoringTest() throws Exception {
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
	//void extracted(int& i) {
	//	// Comment
	//	i = 7;
	//}
	//
	//int main() {
	//	int i;
	//	// Comment
	//	extracted(i);
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
	public void testHistoryFirstExtractedStatementWithLeadingComment() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i;
	//	i = 7; // Comment
	//	return i;
	//}
	//====================
	//void extracted(int& i) {
	//	i = 7; // Comment
	//}
	//
	//int main() {
	//	int i;
	//	extracted(i);
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
	public void testHistoryExtractedStatementWithTrailingComment() throws Exception {
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
	//	extracted(oo);
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

	//refactoringScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Create method extracted" description="Extract Method Refactoring"
	// fileName="file:${projectPath}/A.cpp" flags="4" id="org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring"
	// name="extracted" project="RegressionTestProject" replaceDuplicates="true" selection="99,13" visibility="private"/>
	//</session>
	public void testExtractFunctionRefactoringTestDuplicatesWithDifferentNamesHistoryTest() throws Exception {
		assertRefactoringSuccess();
	}
}
