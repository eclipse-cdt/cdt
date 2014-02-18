package org.eclipse.cdt.ui.tests.refactoring.inlinetemp;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.inlinetemp.InlineTempRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.inlinetemp.InlineTempSettings;


public class InlineTempTest extends RefactoringTestBase {

	private InlineTempRefactoring refactoring;
	private InlineTempSettings settings;
	
	
	@Override
	protected Refactoring createRefactoring() {
		this.refactoring = new InlineTempRefactoring(getSelectedTranslationUnit(), 
				getSelection(), getCProject());
		
		return this.refactoring;
	}

	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.settings = new InlineTempSettings();
	}
	
	
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.settings = null;
	}
	
	
	
	@Override
	protected void simulateUserInput() {
		super.simulateUserInput();
		if (this.settings != null) {
			final InlineTempSettings target = this.refactoring.getSettings();
			
			target.setAlwaysAddParenthesis(this.settings.isAlwaysAddParenthesis());
			target.setInlineAll(this.settings.isInlineAll());
			target.setRemoveDeclaration(this.settings.isRemoveDeclaration());
		}
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//  	const int /*$*/a/*$$*/ = 10;
	//		int b = a;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//  	const int a = 10;
	//		int b = 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testDeclarationSelection() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(false);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//  	const int /*$*/a/*$$*/ = 10;
	//		int b = a;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int b = 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testRemoveDeclaration() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1,/*$*/a/*$$*/ = 10;
	//		int b = a;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1;
	//		int b = 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testRemoveWithMultipleDeclarators() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a = 10;
	//		int b = a;
	//		int c = /*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a = 10;
	//		int b = a;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testRemoveSingleUsage() throws Exception {
		this.settings.setInlineAll(false);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(false);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a = 10;
	//		int b = a;
	//		int c = /*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1;
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testRemoveAll() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a = 10 + 10;
	//		int b = a;
	//		int c = /*$*/a/*$$*/ * 10;
	//		int x = -a;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1;
	//		int b = 10 + 10;
	//		int c = (10 + 10) * 10;
	//		int x = -(10 + 10);
	//  }
	//};
	//#endif /* A_H_ */
	public void testIntelligentParenthesis() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a = 10 + 10;
	//		int b = a;
	//		int c = /*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1;
	//		int b = (10 + 10);
	//		int c = (10 + 10) * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testAlwaysAddParenthesis() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(true);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1, a(10 + 10);
	//		int b = a;
	//		int c = /*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int c = 1;
	//		int b = int(10 + 10);
	//		int c = int(10 + 10) * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testInlineConstructorExpression() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int x = 10;
	//		int* const a = &x;
	//		int b = *a;
	//		int c = */*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int x = 10;
	//		int b = *(&x);
	//		int c = *(&x) * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testInlineConstPointer() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int x = int(10+10);
	//		int* const a = &x;
	//		int b = *a;
	//		int c = */*$*/a/*$$*/ * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int x = int(10+10);
	//		int b = *(&x);
	//		int c = *(&x) * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testInlineConstPointerConstructor() throws Exception {
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int a = /*$*/10/*$$*/;
	//		int b = a;
	//		int c = a * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int a = 10;
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testAutoInlineAll() throws Exception {
		this.settings.setInlineAll(false); // this should be changed to true automatically, causing a warning
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(false);
		this.expectedFinalWarnings = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		const int a = /*$*/10/*$$*/;
	//		int b = a;
	//		int c = a * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testFailRemoveDeclarator() throws Exception {
		// fail because removing declarator and not inlining all references
		this.settings.setInlineAll(false); 
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = /*$*/10/*$$*/;
	//		modify(a);
	//		int b = a;
	//		int c = a * 10;
	//  }
	//	void modify(int a) {
	//		a++;
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		modify(10);
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//	void modify(int a) {
	//		a++;
	//	}
	//};
	//#endif /* A_H_ */
	public void testNoModificationCall() throws Exception {
		// HINT: CPPVariableReadWriteFlags does not distinguish whether the variable is
		//		 passed as reference
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.expectedInitialErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = /*$*/10/*$$*/;
	//		modify(a);
	//		int b = a;
	//		int c = a * 10;
	//  }
	//	void modify(int& a) {
	//		a++;
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = 10;
	//		modify(10);
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//	void modify(int& a) {
	//		a++;
	//	}
	//};
	//#endif /* A_H_ */
	public void testFailModificationCall() throws Exception {
		// fail because a is passed as reference to a function
		this.settings.setInlineAll(true);
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(false);
		this.expectedInitialErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = /*$*/10/*$$*/;
	//		int b = 10 * a++;
	//		int c = a * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int b = 10 * 10++;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testFailModificationUnary() throws Exception {
		// fail because a is being modified within unary expr
		this.settings.setInlineAll(true); 
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.expectedInitialErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = /*$*/10/*$$*/;
	//		int b = (a += 17) * 10;
	//		int c = a * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = 10;
	//		int b = (10 += 17) * 10;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testFailModificationBinary() throws Exception {
		// fail because a is being modified within binary expr
		this.settings.setInlineAll(true); 
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(false);
		this.expectedInitialErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int a = /*$*/10/*$$*/;
	//		int b = a;
	//		int c = a * 10;
	//  }
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testFailNoConstGuarantee() throws Exception {
		// fail because inlined var is not declared const
		this.settings.setInlineAll(true); 
		this.settings.setAlwaysAddParenthesis(false);
		this.settings.setRemoveDeclaration(true);
		this.expectedInitialErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  int /*$*/foo/*$$*/ = 0;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Test {
	//  private:
	//  void test() {
	//		int b = 10;
	//		int c = 10 * 10;
	//  }
	//};
	//#endif /* A_H_ */
	public void testFailAttributeSelected() throws Exception {
		this.assertRefactoringFailure();
	}
}
