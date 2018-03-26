/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.inlinelocalvariable;

import junit.framework.Test;

import org.junit.Before;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable.InlineLocalVariableRefactoring;

/**
 * Tests for Inline Local Variable refactoring.
 */
public class InlineLocalVariableRefactoringTest extends RefactoringTestBase {;
	private InlineLocalVariableRefactoring refactoring;

	public InlineLocalVariableRefactoringTest() {
		super();
	}

	public InlineLocalVariableRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(InlineLocalVariableRefactoringTest.class);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new InlineLocalVariableRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	if (/*$*/i/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (5) {
	//		;
	//	}
	//}
	public void testInlineLocalVariable() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Price {
	//	int basePrice() {
	//		return 12;
	//	};
	//};
	//
	//int main() {
	//	Price anOrder;
	//	int basePrice = anOrder.basePrice();
	//	return (/*$*/basePrice/*$$*/ > 1000);
	//}
	//====================
	//struct Price {
	//	int basePrice() {
	//		return 12;
	//	};
	//};
	//
	//int main() {
	//	Price anOrder;
	//	return (anOrder.basePrice() > 1000);
	//}
	public void testInlineLocalVariableWithStruct() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	int r { };
	//	if (/*$*/i/*$$*/) {
	//		for (int i; i > 100; i++) {
	//			r += i;
	//		}
	//	}
	//}
	//====================
	//int main() {
	//	int r { };
	//	if (5) {
	//		for (int i; i > 100; i++) {
	//			r += i;
	//		}
	//	}
	//}
	public void testInlineLocalVariableWithSameNameUsedInsideForStatement() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	i++;
	//	if (i) {
	//		;
	//	}
	//}
	//
	public void testInlineLocalVariableModifiedAfterDefinition() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	if (/*$*/i/*$$*/) {
	//		int a { i };
	//	}
	//	int b { i };
	//}
	//====================
	//int main() {
	//	if (5) {
	//		int a { 5 };
	//	}
	//	int b { 5 };
	//}
	public void testInlineLocalVariableMultipleOccurences_1() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	if (i) {
	//		int a { /*$*/i/*$$*/ };
	//	}
	//	int b { i };
	//}
	//====================
	//int main() {
	//	if (5) {
	//		int a { 5 };
	//	}
	//	int b { 5 };
	//}
	public void testInlineLocalVariableMultipleOccurences_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { 5 };
	//	if (/*$*/i/*$$*/) {
	//		int a { i };
	//		for (int i = 0; i < 100; i++) {
	//			int c { i };
	//		}
	//	}
	//	int b { i };
	//}
	//====================
	//int main() {
	//	if (5) {
	//		int a { 5 };
	//		for (int i = 0; i < 100; i++) {
	//			int c { i };
	//		}
	//	}
	//	int b { 5 };
	//}
	public void testInlineLocalVariableMultipleOccurences_3() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	struct A{
	//		int b;
	//	}a;
	//	/*$*/a/*$$*/.b = 10;
	//}
	public void testInlineLocalVariableFieldReference() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	int i { 12 + 48 };
	//	if (/*$*/i/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (12 + 48) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableInitializerListInt() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	bool b { true };
	//	if (/*$*/b/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (true) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableInitializerListBool() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	bool b { };
	//	if (/*$*/b/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (bool { }) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableInitializerListEmpty() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	bool b;
	//	if (b) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableUninitialized() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	bool const b { true };
	//	if (/*$*/b/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (true) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableConst() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	int i { };
	//	if (/*$*/i/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (int { }) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableConstDefault() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	constexpr int i { 10 };
	//	if (/*$*/i/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	if (10) {
	//		;
	//	}
	//}
	public void testInlineLocalVariableConstExprDefault() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	struct Banana {
	//		int c;
	//};
	//	int o = banana.c;
	//	if (/*$*/o/*$$*/) {
	//		;
	//	}
	//}
	//====================
	//int main() {
	//	struct Banana {
	//		int c;
	//};
	//	if (banana.c) {
	//		;
	//	}
	//}
	public void testInlineLocalCustomTypeMember() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana b { 19 };
	//	Banana c = /*$*/b/*$$*/;
	//	std::cout << b.c << '\n';
	//}
	//====================
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana c = Banana { 19 };
	//	std::cout << Banana { 19 }.c << '\n';
	//}
	public void testInlineLocalCustomTypeInitializer() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana b { 22 };
	//	Banana c {/*$*/b/*$$*/};
	//}
	//====================
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana c { Banana { 22 } };
	//}
	public void testInlineLocalCustomTypeInitializer_1() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana b { };
	//	Banana c = /*$*/b/*$$*/;
	//	std::cout << b.c << '\n';
	//}
	//====================
	//#include <iostream>
	//int main() {
	//	struct Banana{
	//		int c;
	//	};
	//	Banana c = Banana { };
	//	std::cout << Banana { }.c << '\n';
	//}
	public void testInlineLocalCustomTypeDefaultInitializer() throws Exception {
		assertRefactoringSuccess();
	}
}
