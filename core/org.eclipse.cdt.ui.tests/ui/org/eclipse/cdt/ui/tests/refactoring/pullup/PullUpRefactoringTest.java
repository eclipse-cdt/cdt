package org.eclipse.cdt.ui.tests.refactoring.pullup;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpInformation;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.PullUpMemberTableEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.SubClassTreeEntry;
import org.eclipse.cdt.internal.ui.refactoring.pullup.ui.TargetActions;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class PullUpRefactoringTest extends RefactoringTestBase {

	
	private PullUpRefactoring refactoring;
	private Map<String, String> selectedMembers;
	private Map<String, VisibilityEnum> visibility;
	private Map<String, Collection<String>> remove;
	private String targetClassName;
	private boolean createStubs;
	private boolean pullIntoAbstract;
	
	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.selectedMembers = new HashMap<String, String>();
		this.visibility = new HashMap<String, VisibilityEnum>();
		this.remove = new HashMap<String, Collection<String>>();
		this.createStubs = false;
	}
	
	
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.visibility.clear();
		this.visibility = null;
		this.selectedMembers.clear();
		this.selectedMembers = null;
		this.remove.clear();
		this.remove = null;
		this.targetClassName = null;
		this.createStubs = false;
		this.pullIntoAbstract = false;
	}
	
	
	
	@Override
	protected Refactoring createRefactoring() {
		this.refactoring = new PullUpRefactoring(this.getSelectedTranslationUnit(), 
				this.getSelection(), this.getCProject());
		return this.refactoring;
	}
	
	
	
	private void pullup(String member, VisibilityEnum visibility) {
		this.selectedMembers.put(member, TargetActions.PULL_UP);
		this.visibility.put(member, visibility);
	}
	
	
	
	private void declareVirtual(String member, VisibilityEnum visibility) {
		this.selectedMembers.put(member, TargetActions.DECLARE_VIRTUAL);
		this.visibility.put(member, visibility);
	}
	
	
	
	private void selectTarget(String className) {
		this.targetClassName = className;
	}
	
	
	
	private void createStubs() {
		this.createStubs = true;
	}
	
	
	
	private void allowPullIntoAbstract() {
		this.pullIntoAbstract = true;
	}
	
	
	private void removeFromSubclass(String className, String memberName) {
		Collection<String> members = this.remove.get(className);
		if (members == null) {
			members = new HashSet<String>();
			this.remove.put(className, members);
		}
		members.add(memberName);
	}
	
	
	
	@Override
	protected void simulateUserInput() {
		final PullUpInformation infos = this.refactoring.getInformation();
		
		infos.setDoInsertMethodStubs(this.createStubs);
		infos.setPullIntoPureAbstract(this.pullIntoAbstract);
		for (final Entry<String, String> e : this.selectedMembers.entrySet()) {
			for (final PullUpMemberTableEntry mte : infos.getAllMembers()) {
				if (mte.getMember().getName().equals(e.getKey())) {
					mte.setSelectedAction(e.getValue());
					final VisibilityEnum v = this.visibility.get(e.getKey());
					mte.setTargetVisibility(v);
					break;
				}
			}
		}
		
		
		for (final InheritanceLevel lvl : infos.getTargets()) {
			if (lvl.getClazz().getName().equals(this.targetClassName)) {
				infos.setSelectedTarget(lvl);
				break;
			}
		}
		
		
		final Map<InheritanceLevel, List<SubClassTreeEntry>> tree = infos.generateTree();
		for (Entry<InheritanceLevel, List<SubClassTreeEntry>> e : tree.entrySet()) {
			for (final SubClassTreeEntry scte : e.getValue()) {
				final Collection<String> members = this.remove.get(e.getKey().getClazz().getName());
				
				if (members != null && members.contains(scte.getMember().getMember().getName())) {
					infos.toggleRemove(scte, true);
				}
			}
		}
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int field;
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/field/*$$*/;
	//};
	//#endif /* A_H_ */
	public void testPullUpFieldExists() throws Exception {
		this.selectTarget("Base");
		this.pullup("field", VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int foo(int bar);
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/foo/*$$*/(int bar);
	//};
	//#endif /* A_H_ */
	public void testPullUpMethodExists() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int foo(int bar);
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/foo/*$$*/(int bar) {
	//	}
	//};
	//#endif /* A_H_ */
	public void testPullUpMethodDefinitionExists() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//	void foobar() = 0;
	//};
	//class Derived : Base {
	//public:
	//	int foo(int bar);
	//	int /*$*/bar/*$$*/() {
	//		return foo(0);
	//	}
	//};
	//#endif /* A_H_ */
	public void testPullUpMethodIntoAbstract() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_private);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void foobar() = 0;
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/bar/*$$*/() {
	//		return 0;
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int bar() {
	//		return 0;
	//	}
	//
	//	void foobar() = 0;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpMethodIntoAbstractIgnoreError() throws Exception {
		this.selectTarget("Base");
		this.pullup("bar", VisibilityEnum.v_public);
		this.allowPullIntoAbstract();
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//	void foobar() = 0;
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/foo/*$$*/;
	//};
	//#endif /* A_H_ */
	public void testPullUpFieldIntoAbstract() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_private);
		this.expectedFinalErrors = 1;
		this.assertRefactoringSuccess();
	}
	

	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void foo();
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void foo();
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	
	//A.cpp
	//#include "A.h"
	//
	//void Derived::/*$*/foo/*$$*/() {
	//}
	//====================
	//#include "A.h"
	//
	//void Base::foo() {
	//}
	public void testSelectInCpp() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/field/*$$*/;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int field;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpSimpleField() throws Exception {
		this.selectTarget("Base");
		this.pullup("field", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int baseField;
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/field1/*$$*/;
	//	int field2;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int field1;
	//	int field2;
	//	int baseField;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpSimpleFieldsIntoExistingVisibilty() throws Exception {
		this.selectTarget("Base");
		this.pullup("field1", VisibilityEnum.v_public);
		this.pullup("field2", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int baseField;
	//};
	//class Derived : Base {
	//public:
	//	int /*$*/field1/*$$*/;
	//	int field2;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	int field1;
	//	int baseField;
	//
	//private:
	//	int field2;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpSimpleFieldsIntoDistinctVisibilties() throws Exception {
		this.selectTarget("Base");
		this.pullup("field1", VisibilityEnum.v_public);
		this.pullup("field2", VisibilityEnum.v_private);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a);
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a);
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpSimpleMethodDeclaration() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//		a += 10;
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testPullUpMethodDefinitionInHeader() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Base::myMethod(int a) {
	//	a += 10;
	//}
	public void testPullUpMethodDefinitionInCpp() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	//Leading comment
	//	void myMethod(int a);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	//Leading comment
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Base::myMethod(int a) {
	//	a += 10;
	//}
	public void testPullUpMethodDefinitionInCppWithComment() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a);
	//};
	//#endif /* BASE_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Base::myMethod(int a) {
	//	a += 10;
	//}
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	
	//Derived.cpp
	//#include "Derived.h"
	//void Derived::myMethod(int a) {
	//	a += 10;
	//}
	//====================
	//#include "Derived.h"
	public void testPullUpMethodDefinitionAndDeclaration() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a);
	//	int anotherMethod(int a, float b);
	//	void methodWithDefInHeader();
	//	int privateMethod();
	//};
	//#endif /* BASE_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Base::myMethod(int a) {
	//	a += 10;
	//}
	//
	//void Base::methodWithDefInHeader() {
	//}
	//
	//int Base::privateMethod() {
	//	return 10;
	//}
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a);
	//	int anotherMethod(int a, float b);
	//	void methodWithDefInHeader() {
	//	}
	//private:
	//	int privateMethod();
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//
	//private:
	//};
	//#endif /* DERIVED_H_ */
	
	//Derived.cpp
	//#include "Derived.h"
	//void Derived::myMethod(int a) {
	//	a += 10;
	//}
	//
	//int Derived::privateMethod() {
	//	return 10;
	//}
	//====================
	//#include "Derived.h"
	public void testPullUpMultiple() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.pullup("anotherMethod", VisibilityEnum.v_public);
		this.pullup("methodWithDefInHeader", VisibilityEnum.v_public);
		this.pullup("privateMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	template<typename T>
	//	void myMethod(T a) {
	//	}
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	template <typename T>
	//	void /*$*/myMethod/*$$*/(T a) {
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testPullUpTemplateDefinition() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	template<typename T> void myMethod(T a);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	template<typename T> void /*$*/myMethod/*$$*/(T a);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testPullUpTemplateDeclaration() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	template<typename T> void myMethod(T a);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(T a);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testPullUpFromTemplateClass() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	template<typename T> template<typename V> void myMethod(T a, V y);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//	template<typename V>
	//	void /*$*/myMethod/*$$*/(T a, V y);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testPullUpComplexTemplateDeclaration() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	template<typename T>
	//	template<typename V>
	//	void myMethod(T a, V y) {
	//	}
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//	template<typename V>
	//	void /*$*/myMethod/*$$*/(T a, V y) {
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//template<typename T>
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testPullUpComplexTemplateDefinition() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a);
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//protected:
	//	virtual void myMethod(int a) = 0;
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	
	//A.cpp
	//#include "A.h"
	//
	//void Derived::myMethod(int a) {
	//	a += 10;
	//}
	public void testDeclareVirtualSimple() throws Exception {
		this.selectTarget("Base");
		this.declareVirtual("myMethod", VisibilityEnum.v_protected);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//class Child : Base {
	//public:
	//	void foo();
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	virtual void myMethod(int a) = 0;
	//};
	//class Child : Base {
	//public:
	//	void foo();
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void myMethod(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Child::myMethod(int a) {
	//}
	public void testInsertStubs() throws Exception {
		this.selectTarget("Base");
		this.declareVirtual("myMethod", VisibilityEnum.v_public);
		this.createStubs();
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//class Child : Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Child2 : Base {
	//public:
	//	void bar();
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void foo();
	//};
	//class Child : Base {
	//public:
	//};
	//class Child2 : Base {
	//public:
	//};
	//#endif /* BASE_H_ */
	public void testRemoveAdditional() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.removeFromSubclass("Child2", "bar");
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//class Child : Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Child2 : Base {
	//public:
	//	void bar();
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void foo();
	//};
	//class Child : Base {
	//public:
	//};
	//class Child2 : Base {
	//public:
	//};
	//#endif /* BASE_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//
	//void Child2::bar() {
	//}
	//====================
	//#include "Base.h"
	public void testRemoveAdditionalComplex() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.removeFromSubclass("Child2", "bar");
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//class Child : Base {
	//public:
	//	void /*$*/foo/*$$*/();
	//};
	//class Child2 : Base {
	//public:
	//	template<typename V>
	//	void bar(V i);
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void foo();
	//};
	//class Child : Base {
	//public:
	//};
	//class Child2 : Base {
	//public:
	//};
	//#endif /* BASE_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//template<typename V>
	//void Child2::bar(V i) {
	//}
	//====================
	//#include "Base.h"
	public void testRemoveAdditionalTemplate() throws Exception {
		this.selectTarget("Base");
		this.pullup("foo", VisibilityEnum.v_public);
		this.removeFromSubclass("Child2", "bar");
		this.assertRefactoringSuccess();
	}
	
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//class Child : Base {
	//public:
	//	void foo() = 0; // child class is pure abstract
	//};
	//class ChildOfChild : Child {
	//public:
	// void bar();
	//};
	//class AnotherChild : Base {
	//public:
	//	void foooo() {;
	//	}
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	virtual void myMethod(int a) = 0;
	//};
	//class Child : Base {
	//public:
	//	void foo() = 0; // child class is pure abstract
	//};
	//class ChildOfChild : Child {
	//public:
	// void bar();
	//};
	//class AnotherChild : Base {
	//public:
	//	void foooo() {;
	//	}
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void myMethod(int a) {
	//		a += 10;
	//	}
	//};
	//#endif /* DERIVED_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void AnotherChild::myMethod(int a) {
	//}
	//
	//void ChildOfChild::myMethod(int a) {
	//}
	public void testInsertStubsComplex() throws Exception {
		this.selectTarget("Base");
		this.declareVirtual("myMethod", VisibilityEnum.v_public);
		this.createStubs();
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		// comment in body
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//		// comment in body
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testInlineCommentFreestanding() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		// comment in body leading to statement
	//		a = 0;
	//		// another set of inline
	//		// comments spanning two lines leading to a stmt
	//		a++;
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//		// comment in body leading to statement
	//		a = 0;
	//		// another set of inline
	//		// comments spanning two lines leading to a stmt
	//		a++;
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testInlineCommentLeading() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a = 0;
	//		// comment in body trailing to statement
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//		a = 0;
	//		// comment in body trailing to statement
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testInlineCommentTrailing() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a);
	//};
	//#endif /* BASE_H_ */
	
	//Base.cpp
	//#include "Base.h"
	//====================
	//#include "Base.h"
	//
	//void Base::myMethod(int a) {
	//	//body comment
	//	a += 10;
	//}
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	
	//Derived.cpp
	//#include "Derived.h"
	//void Derived::myMethod(int a) {
	//	//body comment
	//	a += 10;
	//}
	//====================
	//#include "Derived.h"
	public void testInlineCommentInImplementation() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//		a = 0; // comment in body trailing to statement
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//		a = 0; // comment in body trailing to statement
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testInlineCommentTrailingToStatement() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	// leading comments
	//	// multi line
	//	void /*$*/myMethod/*$$*/(int a) {
	//	}
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	// leading comments
	//	// multi line
	//	void myMethod(int a) {
	//	}
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testLeadingComment() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//Base.h
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//};
	//#endif /* BASE_H_ */
	//====================
	//#ifndef BASE_H_
	//#define BASE_H_
	//
	//class Base {
	//public:
	//	// leading comment before template
	//	template<typename T> void myMethod(T a);
	//};
	//#endif /* BASE_H_ */
	
	//Derived.h
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//	// leading comment before template
	//	template <typename T> void /*$*/myMethod/*$$*/(T a);
	//};
	//#endif /* DERIVED_H_ */
	//====================
	//#ifndef DERIVED_H_
	//#define DERIVED_H_
	//
	//#include "Base.h"
	//
	//class Derived : Base {
	//public:
	//};
	//#endif /* DERIVED_H_ */
	public void testLeadingCommentToTemplate() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a) {
	//	} // trailing comment to method
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a) {
	//	} // trailing comment to method
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testTrailingComment() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
	
	
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//};
	//class Derived : Base {
	//public:
	//	void /*$*/myMethod/*$$*/(int a); // trailing comment to declaration
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Base {
	//public:
	//	void myMethod(int a); // trailing comment to declaration
	//};
	//class Derived : Base {
	//public:
	//};
	//#endif /* A_H_ */
	public void testTrailingCommentToDeclaration() throws Exception {
		this.selectTarget("Base");
		this.pullup("myMethod", VisibilityEnum.v_public);
		this.assertRefactoringSuccess();
	}
}