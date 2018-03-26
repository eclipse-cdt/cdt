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
package org.eclipse.cdt.ui.tests.refactoring.movetype;

import junit.framework.Test;

import org.junit.Before;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.movetype.MoveTypeRefactoring;

/**
 * Tests for Extract Constant refactoring.
 */
public class MoveTypeRefactoringTest extends RefactoringTestBase {
	private MoveTypeRefactoring refactoring;

	public MoveTypeRefactoringTest() {
		super();
	}

	public MoveTypeRefactoringTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(MoveTypeRefactoringTest.class);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected CRefactoring createRefactoring() {
		refactoring = new MoveTypeRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}

	//main.cpp
	//int main() {
	//	struct Banana {
	//		/*$*/int weight;/*$$*/
	//	};
	//	Banana b1;
	//	Banana b2;
	//}
	public void testMoveTypeInvalidSelection_1() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	struct Banana {
	//		int weight;
	//	};
	//	/*$*/Banana b1/*$$*/;
	//	Banana b2;
	//}
	public void testMoveTypeInvalidSelection_2() throws Exception {
		assertRefactoringFailure();
	}

	//main.cpp
	//int main() {
	//	/*$*/struct Banana {
	//		int weight;
	//	};/*$$*/
	//	Banana b1;
	//	Banana b2;
	//}
	//====================
	//struct Banana {
	//	int weight;
	//};
	//
	//int main() {
	//	Banana b1;
	//	Banana b2;
	//}
	public void testMoveTypeStruct() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Banana {
	//	int color;
	//};
	//
	//int main() {
	//	/*$*/struct Banana {
	//		int weight;
	//	};/*$$*/
	//	Banana b1;
	//	Banana b2;
	//}
	//====================
	//struct Banana {
	//	int color;
	//};
	//struct Banana_0 {
	//	int weight;
	//};
	//
	//int main() {
	//	Banana_0 b1;
	//	Banana_0 b2;
	//}
	public void testMoveTypeStructWithUsedName_1() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Banana {
	//	int color;
	//};
	//
	//int main() {
	//	/*$*/struct Banana {
	//		int weight;
	//	};/*$$*/
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana b1;
	//	Banana b2;
	//}
	//====================
	//struct Banana {
	//	int color;
	//};
	//struct Banana_1 {
	//	int weight;
	//};
	//
	//int main() {
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana_1 b1;
	//	Banana_1 b2;
	//}
	public void testMoveTypeStructWithUsedName_2() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Banana {
	//	int color;
	//};
	//
	//int main() {
	//	/*$*/struct Banana {
	//		Banana() {
	//			weight = 4;
	//		}
	//		int weight;
	//	};/*$$*/
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana b1;
	//	Banana b2;
	//}
	//====================
	//struct Banana {
	//	int color;
	//};
	//struct Banana_1 {
	//	Banana_1() {
	//		weight = 4;
	//	}
	//
	//	int weight;
	//};
	//
	//int main() {
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana_1 b1;
	//	Banana_1 b2;
	//}
	public void testMoveTypeStructWithConstructor() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//struct Banana {
	//	int color;
	//};
	//
	//int main() {
	//	/*$*/struct Banana {
	//		Banana() {
	//			weight = 4;
	//		}
	//		int weight;
	//	} b3, b4;/*$$*/
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana b1;
	//	Banana b2;
	//}
	//====================
	//struct Banana {
	//	int color;
	//};
	//struct Banana_1 {
	//	Banana_1() {
	//		weight = 4;
	//	}
	//
	//	int weight;
	//};
	//
	//int main() {
	//	Banana_1 b3, b4;
	//	struct Banana_0 {
	//		int length;
	//	};
	//	Banana_1 b1;
	//	Banana_1 b2;
	//}
	public void testMoveTypeStructWithDeclarators() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//int main() {
	//	/*$*/enum Color {
	//		green, blue, red
	//	};/*$$*/
	//	Color bg = green;
	//	Color fg = blue;
	//}
	//====================
	//enum Color {
	//	green, blue, red
	//};
	//
	//int main() {
	//	Color bg = green;
	//	Color fg = blue;
	//}
	public void testMoveTypeEnum() throws Exception {
		assertRefactoringSuccess();
	}

	//main.cpp
	//enum Color {
	//	red, yellow
	//};
	//
	//int main() {
	//	/*$*/enum Color {
	//		green, blue, red
	//	}fg = blue, bg = green;/*$$*/
	//	enum Color_0 {
	//		brown, gold
	//	};
	//}
	//====================
	//enum Color {
	//	red, yellow
	//};
	//enum Color_1 {
	//	green, blue, red
	//};
	//
	//int main() {
	//	Color_1 fg = blue, bg = green;
	//	enum Color_0 {
	//		brown, gold
	//	};
	//}
	public void testMoveTypeEnumWithDeclaratorsAndUsedName() throws Exception {
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
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//class A {
	//public:
	//	void foo();
	//
	//private:
	//	struct Banana {
	//		int c;
	//	};
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//void A::foo() {
	//	/*$*/struct Banana{
	//		int c;
	//	};/*$$*/
	//	Banana banana;
	//	banana.c = 3;
	//}
	//====================
	//A.cpp
	//#include "A.h"
	//
	//void A::foo() {
	//	Banana banana;
	//	banana.c = 3;
	//}
	public void testMoveStructInClass() throws Exception {
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//class A {
	//public:
	//	void foo();
	//private:
	//	struct Banana {
	//		int c;
	//	};
	//};
	//
	//#endif /*A_H_*/
	//====================
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//class A {
	//public:
	//	void foo();
	//private:
	//	struct Banana {
	//		int c;
	//	};
	//	struct Banana_0 {
	//		int c;
	//	};
	//};
	//
	//#endif /*A_H_*/

	//A.cpp
	//#include "A.h"
	//
	//void A::foo() {
	//	/*$*/struct Banana{
	//		int c;
	//	};/*$$*/
	//	Banana banana;
	//	banana.c = 3;
	//}
	//====================
	//A.cpp
	//#include "A.h"
	//
	//void A::foo() {
	//	Banana_0 banana;
	//	banana.c = 3;
	//}
	public void testMoveStructInClassUsedName() throws Exception {
		assertRefactoringSuccess();
	}
}
