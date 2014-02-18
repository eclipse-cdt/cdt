package org.eclipse.cdt.ui.tests.refactoring.pullup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownInformation;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PushDownRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.pushdown.ui.PushDownMemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class PushDownRefactoringTest extends RefactoringTestBase {
	
	
	private static class MemberSettings {
		String memberName;
		String targetAction;
		String targetClassName;
		String targetClassAction;
		VisibilityEnum targetVisibility;
		
		public MemberSettings(String memberName, String targetAction, String targetClassName,
				String targetClassAction, VisibilityEnum targetVisibility) {
			this.memberName = memberName;
			this.targetAction = targetAction;
			this.targetClassName = targetClassName;
			this.targetClassAction = targetClassAction;
			this.targetVisibility = targetVisibility;
		}
	}
	

	private PushDownRefactoring refactoring;
	private List<MemberSettings> selection;
	
	
	
	@Override
	protected Refactoring createRefactoring() {
		this.refactoring = new PushDownRefactoring(this.getSelectedTranslationUnit(), 
				this.getSelection(), this.getCProject());
		
		return this.refactoring;
	}

	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.selection = new ArrayList<>();
	}
	
	
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.selection.clear();
		this.selection = null;
	}
	
	
	
	@Override
	protected void simulateUserInput() {
		final PushDownInformation infos = this.refactoring.getInformation();
		
		nextMember: for (final MemberSettings ms : this.selection) {
			for (final PushDownMemberTableEntry mte : infos.getAllMembers()) {
				if (mte.getMember().getName().equals(ms.memberName)) {
					mte.setSelectedAction(ms.targetAction);
					mte.setTargetVisibility(ms.targetVisibility);
					for (final InheritanceLevel lvl : infos.getTargets()) {
						if (lvl.getClazz().getName().equals(ms.targetClassName)) {
							mte.setActionForClass(lvl, ms.targetClassAction);
							continue nextMember;
						}
					}
				}
			}
		}
	}
	
	
	
	protected void setAction(String memberName, String targetAction, 
			String targetClassName, String targetClassAction, 
			VisibilityEnum targetVisibility) {
		
		this.selection.add(new MemberSettings(memberName, targetAction, targetClassName, 
				targetClassAction, targetVisibility));
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//	int i;
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//#endif /* A_H_ */
	public void testNoSubclass() throws Exception {
		this.assertRefactoringFailure();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int /*$*/field/*$$*/;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	int field;
	//};
	//#endif /* A_H_ */
	public void testPushDownFieldIntoSingleClass() throws Exception {
		this.setAction("field", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int /*$*/field/*$$*/;
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	int field;
	//};
	//class Derived2 : Base {
	//public:
	//	int field;
	//};
	//#endif /* A_H_ */
	public void testPushDownFieldIntoMultipleClass() throws Exception {
		this.setAction("field", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("field", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int /*$*/field/*$$*/;
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//	int field;
	//};
	//#endif /* A_H_ */
	public void testPushDownFieldExists() throws Exception {
		this.setAction("field", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("field", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	void foo();
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDeclarationIntoSingleClass() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	void foo() {
	//	}
	//};
	//#endif /* A_H_ */
	public void testPushDownCreateStubIntoSingleClass() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.METHOD_STUB, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	void foo();
	//};
	//class Derived2 : Base {
	//public:
	//	void foo();
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDeclarationIntoMultipleClass() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	virtual void foo() = 0;
	//};
	//class Derived : Base {
	//public:
	//	void foo();
	//};
	//class Derived2 : Base {
	//public:
	//	void foo();
	//};
	//#endif /* A_H_ */
	public void testLeaveAbstractDownMethodDeclarationIntoMultipleClass() throws Exception {
		this.setAction("foo", TargetActions.LEAVE_VIRTUAL, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("foo", TargetActions.LEAVE_VIRTUAL, "Derived2", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/() {
	//		int i = 0;
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	void foo() {
	//		int i = 0;
	//	}
	//};
	//class Derived2 : Base {
	//public:
	//	void foo() {
	//	}
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDefinitionIntoMultipleClass() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.METHOD_STUB, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//public:
	//};
	//class Derived2 : Base {
	//public:
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//};
	//class Derived : Base {
	//public:
	//	void foo();
	//};
	//class Derived2 : Base {
	//public:
	//	void foo();
	//};
	//#endif /* A_H_ */
	
	//A.cpp
	//#include "A.h"
	//
	//void Base::foo() {
	//	int i = 0;
	//}
	//====================
	//#include "A.h"
	//
	//void Derived::foo() {
	//	int i = 0;
	//}
	//
	//void Derived2::foo() {
	//}
	public void testPushDownMethodDefinitionIntoMultipleClassWithCpp() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.METHOD_STUB, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//	int i;
	//public:
	//	void /*$*/foo/*$$*/() {
	//		i = 0;
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDefinitionWithDependency() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//	void bar();
	//public:
	//	void /*$*/foo/*$$*/() {
	//		bar();
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDefinitionWithDependency2() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//	void bar() {
	//		foo();
	//	}
	//	void /*$*/foo/*$$*/() {
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDefinitionWithDependency3() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//	void bar();
	//public:
	//	void /*$*/foo/*$$*/() {
	//		bar();
	//	}
	//};
	//class Derived : Base {
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//private:
	//
	//public:
	//};
	//class Derived : Base {
	//private:
	//	void bar();
	//
	//public:
	//	void foo() {
	//		bar();
	//	}
	//};
	//#endif /* A_H_ */
	public void testPushDownMethodDefinitionWithDependencySuccess() throws Exception {
		this.setAction("bar", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_private);
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/() {
	//	}
	//};
	//class SomeClass {
	//public:
	//	void doSomething() {
	//		Base b = Base();
	//		b.foo();
	//	}
	//};
	//class Derived : Base {
	//};
	//#endif /* A_H_ */
	public void testMethodDefinitionStillUsed() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/() {
	//	}
	//};
	//class Derived : Base {
	//};
	//class Derived2 : Derived {
	//};
	//class SomeClass {
	//public:
	//	void doSomething() {
	//		Derived d = Derived();
	//		d.foo();
	//	}
	//};
	//#endif /* A_H_ */
	public void testMethodDefinitionStillUsed2() throws Exception {
		this.setAction("foo", TargetActions.PUSH_DOWN, "Derived2", 
				TargetActions.EXISTING_DEFINITION, VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/() {
	//	}
	//};
	//class Derived : Base {
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//
	//protected:
	//	virtual void foo() = 0;
	//};
	//class Derived : Base {
	//protected:
	//	void foo() {
	//	}
	//};
	//#endif /* A_H_ */
	public void testLeaveAbstractChangeVisibilityDefinition() throws Exception {
		this.setAction("foo", TargetActions.LEAVE_VIRTUAL, "Derived", 
				TargetActions.METHOD_STUB, VisibilityEnum.v_protected);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Derived : Base {
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//
	//protected:
	//	virtual void foo() = 0;
	//};
	//class Derived : Base {
	//protected:
	//	void foo() {
	//	}
	//};
	//#endif /* A_H_ */
	public void testLeaveAbstractChangeVisibilityDeclaration() throws Exception {
		this.setAction("foo", TargetActions.LEAVE_VIRTUAL, "Derived", 
				TargetActions.METHOD_STUB, VisibilityEnum.v_protected);
		this.assertRefactoringSuccess();
	}
}
