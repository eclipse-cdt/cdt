/*******************************************************************************
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

import junit.framework.Test;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionInformation;
import org.eclipse.cdt.internal.ui.refactoring.extractfunction.ExtractFunctionRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Tests for Extract Function refactoring.
 */
public class ExtractFunctionRefactoringTest extends RefactoringTestBase {
	private static final String NO_RETURN_VALUE = "";
	private String extractedFunctionName = "extracted";
	private String returnValue;
	// Map from old names to new ones.
	private Map<String, String> parameterRename = new HashMap<String, String>();
	// New positions of parameters, or null.
	private int[] parameterOrder;
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private boolean virtual;
	private boolean replaceDuplicates = true;
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
	public void testDontExtractCodeContainingReturn() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//void function() {
	//	for (int var = 0; var < 100; ++var) {
	//		/*$*/if (var < 50)
	//			continue;/*$$*/
	//	}
	//}
	public void testDontExtractCodeContainingContinue() throws Exception {
		assertRefactoringFailure();
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
}
