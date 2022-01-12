/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
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
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.hidemethod;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoring;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import junit.framework.Test;

/**
 * Tests for Extract Local Variable refactoring.
 */
public class HideMethodRefactoringTest extends RefactoringTestBase {

	public HideMethodRefactoringTest() {
		super();
	}

	public HideMethodRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(HideMethodRefactoringTest.class);
	}

	@Override
	protected CRefactoring createRefactoring() {
		return new HideMethodRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testSimple() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	//Comment
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	//Comment
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testLineComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*Comment*/
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*Comment*/
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testBlockComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*///Comment
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2(); //Comment
	//};
	//
	//#endif /*A_H_*/
	public void testLineCommentBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*//*Comment*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2(); /*Comment*/
	//};
	//
	//#endif /*A_H_*/
	public void testBlockCommentBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//
	//	//Comment
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	//Comment
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testLineCommentWithSpace() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//
	//	/*Comment*/
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*Comment*/
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testBlockCommentWithSpace() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*///Comment
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2(); //Comment
	//};
	//
	//#endif /*A_H_*/
	public void testLineCommentWithSpaceBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*//*Comment*/
	//
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2(); /*Comment*/
	//};
	//#endif /*A_H_*/
	public void testBlockCommentWithSpaceBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*
	//	 * Comment
	//	 */
	//	/*$*/void method2();/*$$*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*
	//	 * Comment
	//	 */
	//	void method2();
	//};
	//#endif /*A_H_*/
	public void testBigBlockComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*$*/void method2();/*$$*/		/*
	//							 * Comment
	//							 */
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2(); /*
	//	 * Comment
	//	 */
	//};
	//
	//#endif /*A_H_*/
	public void testBigBlockCommentBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*
	//	 * Davor
	//	 */
	//	/*$*/void method2();/*$$*/		/*
	//							 * Comment
	//							 */
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*
	//	 * Davor
	//	 */
	//	void method2(); /*
	//	 * Comment
	//	 */
	//};
	//
	//#endif /*A_H_*/
	public void testBigBlockCommentBeforeAndBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*123*/
	//	/*$*/void method2();/*$$*///TEST
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*123*/
	//	void method2(); //TEST
	//};
	//
	//#endif /*A_H_*/
	public void testMixedCommentBeforeAndAfter() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	/*123*/
	//	/*$*/void method2();/*$$*//*TEST*/
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	/*123*/
	//	void method2(); /*TEST*/
	//};
	//#endif /*A_H_*/
	public void testBlockCommentBeforeAndBehind() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//private:
	//	/*$*/void method2();/*$$*/
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//private:
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testNoChange() throws Exception {
		expectedInitialErrors = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	/*$*/void method1();/*$$*/
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	void method2();
	//
	//private:
	//	void method1();
	//};
	//
	//#endif /*A_H_*/
	public void testTwoMethodsDifferentLine() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	/*$*/void method1();/*$$*/void method2();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	void method2();
	//
	//private:
	//	void method1();
	//};
	//
	//#endif /*A_H_*/
	public void testTwoMethodsSameLine() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	//Comment
	//	/*$*/void method1();/*$$*/
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	void method2();
	//
	//private:
	//	//Comment
	//	void method1();
	//};
	//
	//#endif /*A_H_*/
	public void testTwoMethodsDifferentLineWithComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	//Comment
	//	/*$*/void method1();/*$$*/void method2();
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	void method2();
	//
	//private:
	//	//Comment
	//	void method1();
	//};
	//
	//#endif /*A_H_*/
	public void testTwoMethodsSameLineWithComment() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	void method2();
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2();
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//void A::/*$*/method2/*$$*/()
	//{
	//}
	public void testSimpleImplementationFile() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	//TEST 1
	//	void method2(); //TEST 2
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	//TEST 1
	//	void method2(); //TEST 2
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//void A::/*$*/method2/*$$*/()
	//{
	//}
	public void testSimpleImplementationFileWithCommentsBUG60() throws Exception {
		assertRefactoringSuccess();
	}

	//HideMethod.h
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void /*$*/method2/*$$*/();
	//
	//	void method3() {
	//		method2();
	//	}
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	//====================
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//
	//	void method3() {
	//		method2();
	//	}
	//
	//private:
	//	void method2();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	public void testReferences1() throws Exception {
		assertRefactoringSuccess();
	}

	//HideMethod.h
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void /*$*/method2/*$$*/();
	//	void method3();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	//====================
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void method3();
	//
	//private:
	//	void method2();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */

	//HideMethod.cpp
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::method2() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	method2();
	//}
	//====================
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::method2() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	method2();
	//}
	public void testReferences2() throws Exception {
		assertRefactoringSuccess();
	}

	//HideMethod.h
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	void /*$*/method2/*$$*/();
	//	void method3();
	//};
	//
	//class test {
	//public:
	//	void call() {
	//		HideMethod hm;
	//		hm.method2();
	//	}
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	//====================
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	void method3();
	//
	//private:
	//	void method2();
	//};
	//
	//class test {
	//public:
	//	void call() {
	//		HideMethod hm;
	//		hm.method2();
	//	}
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	public void testReferences3() throws Exception {
		expectedFinalWarnings = 1;
		assertRefactoringSuccess();
	}

	//HideMethod.h
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void /*$*/method2/*$$*/();
	//	void method3();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	//====================
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void method3();
	//
	//private:
	//	void method2();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */

	//HideMethod.cpp
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::method2() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	//do nothing
	//}
	//
	//int main() {
	//	HideMethod hm;
	//	hm.method2();
	//}
	//====================

	//HideMethod.cpp
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::method2() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	//do nothing
	//}
	//
	//int main() {
	//	HideMethod hm;
	//	hm.method2();
	//}
	public void testReferences4() throws Exception {
		expectedFinalWarnings = 1;
		assertRefactoringSuccess();
	}

	//HideMethod.h
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void method2();
	//	void method3();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */
	//====================
	//#ifndef HIDEMETHOD_H_
	//#define HIDEMETHOD_H_
	//
	//class HideMethod {
	//public:
	//	HideMethod();
	//	virtual ~HideMethod();
	//	void method3();
	//
	//private:
	//	void method2();
	//};
	//
	//#endif /* HIDEMETHOD_H_ */

	//HideMethod.cpp
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::/*$*/method2/*$$*/() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	method2();
	//}
	//====================
	//#include "HideMethod.h"
	//
	//HideMethod::HideMethod() {
	//}
	//
	//HideMethod::~HideMethod() {
	//}
	//
	//void HideMethod::method2() {
	//	//do nothing
	//}
	//
	//void HideMethod::method3() {
	//	method2();
	//}
	public void testCPPFileSelection() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	/*$*/void method2();/*$$*/
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//
	//private:
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testChangeToDefaultVisibilityClass1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//	/*$*/void method2();/*$$*/
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testChangeToDefaultVisibilityClass2() throws Exception {
		expectedInitialErrors = 1;
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//struct A {
	//	/*$*/void method2();/*$$*/
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//struct A {
	//private:
	//	void method2();
	//};
	//
	//#endif /*A_H_*/
	public void testChangeToDefaultVisibilityStruct() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void /*$*/set/*$$*/(bool b) {}
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//};
	//====================
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//
	//private:
	//	void set(bool b) {
	//	}
	//};
	public void testCheckIfPrivateBug1() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void set(bool b) {
	//	}
	//
	//	void test() {
	//		other o;
	//		this->/*$*/set/*$$*/(o.value());
	//	}
	//};
	//====================
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//
	//private:
	//	void set(bool b) {
	//	}
	//};
	public void testCheckIfPrivateBug2() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//struct other {
	//	bool /*$*/value/*$$*/() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void set(bool b) {}
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//};
	//====================
	//struct other {
	//private:
	//	bool value() {
	//		return true;
	//	}
	//};
	//
	//class Class {
	//public:
	//	void set(bool b) {}
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//};
	public void testCheckIfPrivateBug3() throws Exception {
		expectedFinalWarnings = 1;
		assertRefactoringSuccess();
	}

	//A.cpp
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void set(bool b) {
	//	}
	//
	//	void /*$*/test/*$$*/() {
	//		other o;
	//		this->set(o.value());
	//	}
	//};
	//====================
	//struct other {
	//	bool value() { return true; }
	//};
	//
	//class Class {
	//public:
	//	void set(bool b) {
	//	}
	//
	//private:
	//	void test() {
	//		other o;
	//		this->set(o.value());
	//	}
	//};
	public void testCheckIfPrivateBug4() throws Exception {
		assertRefactoringSuccess();
	}

	//A.cpp
	//int /*$*/main/*$$*/() {
	//	int i = 2;
	//	i++;
	//	return 0;
	//}
	//====================
	//int main() {
	//	int i = 2;
	//	i++;
	//	return 0;
	//}
	public void testStandaloneFunction() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//class Class {
	//public:
	//	void /*$*/to_move()/*$$*/;
	//
	//private:
	//	void just_private();
	//};
	//====================
	//class Class {
	//public:
	//
	//private:
	//	void just_private();
	//	void to_move();
	//};
	public void testEmptyPublicSections() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//class Class {
	//public:
	//	void /*$*/to_move()/*$$*/;
	//
	//private:
	//	void just_private();
	//
	//private:
	//};
	//====================
	//class Class {
	//public:
	//
	//private:
	//	void just_private();
	//	void to_move();
	//
	//private:
	//};
	public void testSeveralPrivateSections() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	void method2();
	//	std::string toString();
	//
	//private:
	//	int i;
	//};
	//
	//#endif /*A_H_*/
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#include <iostream>
	//
	//class A {
	//public:
	//	A();
	//	std::string toString();
	//
	//private:
	//	int i;
	//
	//	void method2();
	//};
	//
	//#endif /*A_H_*/

	//refScript.xml
	//<?xml version="1.0" encoding="UTF-8"?>
	//<session version="1.0">
	//<refactoring comment="Hide Method method2" description="Hide Method Refactoring"
	// fileName="file:${projectPath}/A.h" flags="2"
	// id="org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoring"
	// project="RegressionTestProject" selection="78,7"/>
	//</session>
	public void testHistorySimple() throws Exception {
		assertRefactoringSuccess();
	}
}
