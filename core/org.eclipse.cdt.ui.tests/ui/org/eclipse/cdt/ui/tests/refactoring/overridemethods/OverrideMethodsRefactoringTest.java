/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.overridemethods;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.overridemethods.Method;
import org.eclipse.cdt.internal.ui.refactoring.overridemethods.OverrideMethodsRefactoring;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import junit.framework.Test;

/**
 * Tests for override methods
 */
public class OverrideMethodsRefactoringTest extends RefactoringTestBase {

	private String[] selectedMethods;
	private OverrideMethodsRefactoring refactoring;
	private boolean addOverride = false;
	private boolean preserveVirtual = true;

	public OverrideMethodsRefactoringTest() {
		super();
	}

	public OverrideMethodsRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(OverrideMethodsRefactoringTest.class);
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new OverrideMethodsRefactoring(getSelectedTranslationUnit(), getSelection(), getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		if (selectedMethods != null) {
			Map<ICPPClassType, List<Method>> map = refactoring.getMethodContainer().getInitialInput();
			for (Map.Entry<ICPPClassType, List<Method>> entry : map.entrySet()) {
				List<Method> methods = entry.getValue();
				for (Method m : methods) {
					for (String name : selectedMethods) {
						if (m.toString().equals(name))
							refactoring.getPrintData().addMethod(m);
					}
				}
			}
		}
		refactoring.getOptions().setAddOverride(addOverride);
		refactoring.getOptions().setPreserveVirtual(preserveVirtual);
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const;
	//};
	//
	//inline void X::baseFunc() const {
	//}
	public void testWithHeaderOnly() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const;
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void X::baseFunc() const {
	//}
	public void testWithHeaderAndSource() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//namespace FIRST {
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc(Base *ptr) const = 0;
	//};
	//};
	//namespace SECOND {
	//class X: public FIRST::Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//};
	//====================
	//namespace FIRST {
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc(Base *ptr) const = 0;
	//};
	//};
	//namespace SECOND {
	//class X: public FIRST::Base {
	//public:
	//	X();
	//	virtual void baseFunc(FIRST::Base *ptr) const;
	//};
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void SECOND::X::baseFunc(FIRST::Base *ptr) const {
	//}
	public void testWithMixedNamespaceHeaderAndSource() throws Exception {
		selectedMethods = new String[] { "baseFunc(FIRST::Base *)const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	void baseFunc() const;
	//};
	//
	//inline void X::baseFunc() const {
	//}
	public void testIgnoringVirtual() throws Exception {
		preserveVirtual = false;
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const override;
	//};
	//
	//inline void X::baseFunc() const {
	//}
	public void testAddingOverrideVirtual() throws Exception {
		addOverride = true;
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//template<class T>
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//template<class T>
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const;
	//};
	//
	//template<class T>
	//inline void X<T>::baseFunc() const {
	//}
	public void testWithTemplateClass() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//template<class T>
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc(T *t) const = 0;
	//};
	//class X: public Base<int> {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//template<class T>
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc(T *t) const = 0;
	//};
	//class X: public Base<int> {
	//public:
	//	X();
	//	virtual void baseFunc(int *t) const;
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void X::baseFunc(int *t) const {
	//}
	public void testWithTemplateBaseClass() throws Exception {
		selectedMethods = new String[] { "baseFunc(int *) {#0,0: int}" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X {
	//public:
	//	X();
	//	class Internal: public Base {
	//  public:
	//	/*$*//*$$*/
	//	};
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X {
	//public:
	//	X();
	//	class Internal: public Base {
	//  public:
	//		virtual void baseFunc() const;
	//	};
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void X::Internal::baseFunc() const {
	//}
	public void testWithNestedClass() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//};
	///*$*//*$$*/
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//};
	public void testWithNoSelection() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringFailure();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	void baseFunc() const;
	//};
	//class X: public Base {
	//public:
	//	X();
	///*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	void baseFunc() const;
	//};
	//class X: public Base {
	//public:
	//	X();
	//};
	public void testWithNoMethods() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringFailure();
	}

	//A.h
	//class X {
	//public:
	//	X();
	///*$*//*$$*/
	//};
	//====================
	//class X {
	//public:
	//	X();
	//};
	public void testWithNoBaseClass() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const throw	(int) = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const throw	(int) = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const throw (int);
	//};
	//
	//inline void X::baseFunc() const throw (int) {
	//}
	public void testWithThrowNoEmpty() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const throw	() = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const throw	() = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const throw ();
	//};
	//
	//inline void X::baseFunc() const throw () {
	//}
	public void testWithThrowEmpty() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const noexcept = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const noexcept = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const noexcept;
	//};
	//
	//inline void X::baseFunc() const {
	//}
	public void testWithNoExcept() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc1() const = 0;
	//	virtual void baseFunc2() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc1() const = 0;
	//	virtual void baseFunc2() const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc2() const;
	//	virtual void baseFunc1() const;
	//};
	//
	//inline void X::baseFunc2() const {
	//}
	//
	//inline void X::baseFunc1() const {
	//}
	public void testWithMultipleMethods() throws Exception {
		selectedMethods = new String[] { "baseFunc1()const", "baseFunc2()const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() && = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() && = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() &&;
	//};
	//
	//inline void X::baseFunc() && {
	//}
	public void testWithRefQualifier() throws Exception {
		selectedMethods = new String[] { "baseFunc()&&" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void* baseFunc(void *ptr) const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void* baseFunc(void *ptr) const = 0;
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void* baseFunc(void *ptr) const;
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void* X::baseFunc(void *ptr) const {
	//}
	public void testWithPointers() throws Exception {
		selectedMethods = new String[] { "baseFunc(void *)const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void* baseFunc(void *ptr) const = 0, method2();
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void* baseFunc(void *ptr) const = 0, method2();
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void* baseFunc(void *ptr) const;
	//};

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//void* X::baseFunc(void *ptr) const {
	//}
	public void testWithMultipleMethodsOnSameLine() throws Exception {
		selectedMethods = new String[] { "baseFunc(void *)const" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const {
	//	}
	//};
	//class X: public Base {
	//public:
	//	X();
	//	/*$*//*$$*/
	//};
	//====================
	//class Base {
	//public:
	//	virtual ~Base();
	//	virtual void baseFunc() const {
	//	}
	//};
	//class X: public Base {
	//public:
	//	X();
	//	virtual void baseFunc() const;
	//};
	//
	//inline void X::baseFunc() const {
	//}
	public void testWithHeaderOnlyImpl_Bug548138() throws Exception {
		selectedMethods = new String[] { "baseFunc()const" };
		assertRefactoringSuccess();
	}
}
