/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Nathan Ridge
 *     Thomas Corbat (IFS)
 *	   Michael Woski
 *	   Mohamed Azab (Mentor Graphics) - Bug 438549. Add mechanism for parameter guessing.
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.CONTEXT;
import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.DISPLAY;
import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.ID;
import static org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest.CompareType.REPLACEMENT;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

import junit.framework.Test;

/**
 * A collection of code completion tests.
 *
 * @since 4.0
 */
public class CompletionTests extends CompletionTestBase {
	private static final String DISTURB_FILE_NAME = "DisturbWith.cpp";

	//	{DisturbWith.cpp}
	//	int gTemp;
	//	void gFunc();
	//	typedef struct {
	//		int mem;
	//	} gStruct;
	//	class gClass {};
	//	namespace gns {
	//		int gnsTemp;
	//		void gnsFunc();
	//		typedef struct {
	//			int mem;
	//		} gnsStruct;
	//		class gnsClass {};
	//	};

	public CompletionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return BaseTestCase.suite(CompletionTests.class, "_");
	}

	//void gfunc() {C1 v; v.m/*cursor*/
	public void testLocalVariable() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {C1 v; v.fMySelf->m/*cursor*/
	public void testLocalVariable_MemberVariable() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {C1 v; v.m12()->m/*cursor*/
	public void testLocalVariable_MemberFunction() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {gfC1()->m/*cursor*/
	public void testGlobalFunction() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::self() {m/*cursor*/
	public void testOwnMember() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m1private(void)", "m1protected(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::self() {this->m/*cursor*/
	public void testOwnMemberViaThis() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m1private(void)", "m1protected(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {try{int bla;}catch(C1 v) {v.fMySelf->m/*cursor*/
	public void testCatchBlock1() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {try{int bla;}catch(C2 c){} catch(C1 v) {v.fMySelf->m/*cursor*/
	public void testCatchBlock2() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {gC/*cursor*/
	public void testGlobalVariables_GlobalScope() throws Exception {
		final String[] expected = { "gC1", "gC2", "gfC1(void)", "gfC2(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::f() {gC/*cursor*/
	public void testGlobalVariables_MethodScope() throws Exception {
		final String[] expected = { "gC1", "gC2", "gfC1(void)", "gfC2(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_GlobalScope() throws Exception {
		final String[] expected = { "cLocal1", "cLocal2" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_MethodScope() throws Exception {
		final String[] expected = { "cLocal1", "cLocal2" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* cLocal1; cLocal1->f/*cursor*/
	public void testDataMembers_GlobalScope() throws Exception {
		final String[] expected = { "fMySelf" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {while(true) {f/*cursor*/
	public void testDataMembers_MethodScope() throws Exception {
		final String[] expected = { "fMySelf" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {gf/*cursor*/
	public void testGlobalFunctions_GlobalScope() throws Exception {
		final String[] expected = { "gfC1(void)", "gfC2(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {gf/*cursor*/
	public void testGlobalFunctions_MethodScope() throws Exception {
		final String[] expected = { "gfC1(void)", "gfC2(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* l1; l1->m/*cursor*/
	public void testMethods_GlobalScope() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {m/*cursor*/
	public void testMethods_MethodScope() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m3private(void)", "m3protected(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C/*cursor*/
	public void testTypes_GlobalScope() throws Exception {
		final String[] expected = { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//class _friend_class { C3* x; void m() {x->m/*cursor*/
	public void testTypes_FriendClass() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m2private(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns { class _friend_class { C3* x; void m() {x->m/*cursor*/  // Not a friend due to namespace
	public void testTypes_FakeFriendClass() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m23(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void _friend_function(C3* x) { x->m/*cursor*/
	public void testTypes_FriendFunction() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m2private(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void _friend_function(C2* x) { x->m/*cursor*/  // Not a friend due to parameter type mismatch
	public void testTypes_FakeFriendFunction() throws Exception {
		final String[] expected = { "m123(void)", "m12(void)", "m13(void)", "m23(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {T/*cursor*/
	public void testTypes_MethodScope() throws Exception {
		final String[] expected = { "T1", "T2", "T3", "TClass<typename T>" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void nsfunc(){C/*cursor*/
	public void testTypes_NamespaceScope() throws Exception {
		final String[] expected = { "C1", "C2", "C3", "CNS" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void gfunc(){::C/*cursor*/
	public void testTypes_GlobalQualification() throws Exception {
		final String[] expected = { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {e/*cursor*/
	public void testEnums_GlobalScope() throws Exception {
		final String[] expected = { "e11", "e12", "E1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {e/*cursor*/
	public void testEnums_MethodScope() throws Exception {
		final String[] expected = { "e11", "e12", "e21", "e22", "E1", "E2" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C/*cursor*/
	public void testQualificationForAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "C3", "C2", "C1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* l1; l1->C/*cursor*/
	public void testQualificationForAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "C2", "C1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C3::fMySelf->iam/*cursor*/
	public void testQualifiedAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "iam3(void)", "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C2::fMySelf->iam/*cursor*/
	public void testQualifiedAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C1::fMySelf->iam/*cursor*/
	public void testQualifiedAccess3() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T3::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "iam3(void)", "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T2::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected = { "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T1::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier3() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1().iam/*cursor*/
	public void testTemporaryObject() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c)->iam/*cursor*/
	public void testAddressOf() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (*c).iam/*cursor*/
	public void testDereferencingOperator1() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; (**c).iam/*cursor*/
	public void testDereferencingOperator2() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; (*c)->iam/*cursor*/
	public void testDereferencingOperator3() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c[0].iam/*cursor*/
	public void testArrayAccessOperator1() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; c[0][1].iam/*cursor*/
	public void testArrayAccessOperator2() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; c[0]->iam/*cursor*/
	public void testArrayAccessOperator3() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (&c[0])->iam/*cursor*/
	public void testArrayAccessOperator4() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {void* c; ((C1*)c)->iam/*cursor*/
	public void testCasts1() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void g(int a) {}; void f() {void* c; g(((C1*)c)->iam/*cursor*/
	public void testCasts2() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c++->iam/*cursor*/
	public void testPointerArithmetic1() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (*++c).iam/*cursor*/
	public void testPointerArithmetic2() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c--->iam/*cursor*/
	public void testPointerArithmetic3() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c+1)->iam/*cursor*/
	public void testPointerArithmetic4() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c-1)->iam/*cursor*/
	public void testPointerArithmetic5() throws Exception {
		final String[] expected = { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {int localVar=0; if (*cond && somefunc(&local/*cursor*/
	public void testNestedCalls() throws Exception {
		final String[] expected = { "localVar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//int a[] = {1,2}; void f(int _0306_b) {_0306_b/*cursor*/
	public void testCuttingInput1() throws Exception {
		final String[] expected = { "_0306_b" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//int a[] = {1,2}; void f(int b) {int _0306_b[] = {2,3}; _0306_b/*cursor*/
	public void testCuttingInput2() throws Exception {
		final String[] expected = { "_0306_b" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//enum EnumType function() {int _031209_v; _031209/*cursor*/
	public void testDisturbingMacros() throws Exception {
		final String[] expected = { "_031209_v" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void x() {NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember1() throws Exception {
		final String[] expected = { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void ns::CNS::mcns(){NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember2() throws Exception {
		final String[] expected = { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//#i/*cursor*/
	public void testCompletePreprocessorDirective() throws Exception {
		final String[] expected = { "#if", "#ifdef", "#ifndef", "#include" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//#  d/*cursor*/
	public void testCompletePreprocessorDirective2() throws Exception {
		final String[] expected = { "define " };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//#  if d/*cursor*/
	public void testCompletePreprocessorDirective3() throws Exception {
		final String[] expected = { "defined" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	int waldo;
	//	void foo() {
	//	#ifdef SOME_UNDEFINED_MACRO
	//		wald/*cursor*/
	//	#endif
	//	}
	public void testInactiveCodeBlock_72809() throws Exception {
		assertCompletionResults(new String[] { "waldo" });
	}

	//void gfunc(){TClass<int> t(0); t.a/*cursor*/
	public void testTemplateClassMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected = { "add(int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc(){C3 c3; c3.t/*cursor*/
	public void testTemplateMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected = { "tConvert(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// void f(){T1::~/*cursor*/
	public void testTypedefSyntheticMembers_415495() throws Exception {
		final String[] expected = {};
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void f(){A1::~/*cursor*/
	public void testAliasSyntheticMembers_415495() throws Exception {
		final String[] expected = {};
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class BaseTest : Spec/*cursor*/
	public void testBaseClassIsStruct_434446() throws Exception {
		final String[] expected = { "Specialization<>" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class BaseTest : Alias/*cursor*/
	public void testBaseClassIsTemplateAlias_434446() throws Exception {
		final String[] expected = { "AliasForSpecialization<typename T1, typename T2>",
				"AliasForTemplateAlias<typename T1, typename T2>" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct S {};
	//	template <typename T>
	//	using waldo = S;
	//	class B : wald/*cursor*/
	public void testAliasTemplate_455797() throws Exception {
		final String[] expected = { "waldo<typename T>" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// template<typename TP_Param>
	// class BaseTest : TP/*cursor*/
	public void testBaseClassIsTemplateParameter() throws Exception {
		final String[] expected = { "TP_Param" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	template<typename T>
	//	struct Parent {
	//	protected:
	//		struct Nested {
	//		protected:
	//			using TParam = T;
	//		};
	//	};
	//
	//	struct NestingTest: Parent<int> {
	//		struct A : Nested {
	//			TP/*cursor*/
	//		};
	//	};
	public void testNestedBaseTemplateMembers_422401() throws Exception {
		final String[] expected = { "TParam" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template<typename T>
	//	class Parent {
	//		struct NestedHidden {};
	//	protected:
	//		struct NestedProtected {};
	//	public:
	//		struct NestedPublic {};
	//	};
	//
	//	template<typename T>
	//	class NestingTest: Parent<T> {
	//		Parent<T>::/*cursor*/
	//	};
	public void testNestedBaseTemplateMembersFromUnknownScope_456752() throws Exception {
		final String[] expected = { "NestedProtected", "NestedPublic" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// template <typename TPA>
	// struct A {
	//   struct AA {
	//     static int i;
	//   };
	// };
	//
	// template <typename TPB>
	// void test()
	// {
	//   A<TPB>::AA::/*cursor*/
	// }
	public void testUnknownMemberClassAccessContext_520783() throws Exception {
		assertCompletionResults(new String[] { "i" });
	}

	// namespace bug521016 {
	// void test() {
	//   B::Test::AAA::/*cursor*/
	// }
	// }
	public void testNestedAliasTemplateSpecialization_521016() throws Exception {
		assertCompletionResults(new String[] { "test()" });
	}

	// template <typename TPA>
	// struct A {
	//   enum class AA {
	//     Test
	//   };
	// };
	//
	// template <typename TPB>
	// void test()
	// {
	//   A<TPB>::AA::/*cursor*/
	// }
	public void testHeuristicEnumScopeResolution_520805() throws Exception {
		assertCompletionResults(new String[] { "Test" });
	}

	//	template <typename T>
	//	struct A {
	//		template <typename U>
	//		struct AA {
	//			template <typename V>
	//			struct AAA {
	//			};
	//		};
	//	};
	//
	//	struct B : A<B> {
	//		AA<B>::/*cursor*/
	//	};
	public void testMemebersForDeeplyNestedTemplates_459389() throws Exception {
		final String[] expected = { "AAA<typename V>" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	struct A {};
	//
	//	template<typename T>
	//	struct B {};
	//
	//	template<typename U>
	//	struct C {
	//	  using D = U;
	//
	//	  template<typename T>
	//	  using E = B<T>;
	//	};
	//
	//	void test() {
	//	  C<A>::/*cursor*/
	//	}
	public void testAliasTemplate_418479() throws Exception {
		final String[] expected = { "D", "E<typename T>" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//using namespace ns;void gfunc(){NSC/*cursor*/
	public void testUsingDirective() throws Exception {
		final String[] expected = { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc(){n/*cursor*/
	public void testAutoColons() throws Exception {
		final String[] expected = { "ns::" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//using namespace n/*cursor*/
	public void testAutoColons2() throws Exception {
		final String[] expected = { "ns" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//// to_be_replaced_
	//void gfunc(){aNew/*cursor*/
	public void testGlobalVariableBeforeSave_180883() throws Exception {
		String replace = "// to_be_replaced_";
		String globalVar = "int aNewGlobalVar;";
		IDocument doc = getDocument();
		int idx = doc.get().indexOf(replace);
		doc.replace(idx, replace.length(), globalVar);

		// succeeds when buffer is saved
		//		fEditor.doSave(new NullProgressMonitor());
		//		EditorTestHelper.joinBackgroundActivities((AbstractTextEditor)fEditor);

		final String[] expected = { "aNewGlobalVar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void Printer::InitPrinter(unsigned char port) {
	//	Printer::/*cursor*/
	public void testPrivateStaticMember_109480() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=109480
		final String[] expected = { "InitPrinter(port)", "port" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class vector3 {
	// public:
	//   void blah(const vector3& v) { x += v./*cursor*/; }
	//   float x;
	// };
	public void testForwardMembersInInlineMethods_103857a() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=103857
		final String[] expected = { "x" };
		assertMinimumCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	struct S {
	//		int mem;
	//	};
	//	class X {
	//		void test() {
	//			T t;
	//			t.m/*cursor*/;
	//		}
	//		typedef S T;
	//	};
	public void testForwardMembersInInlineMethods_103857b() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=185652
		final String[] expected = { "mem" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void Pri/*cursor*/
	public void testMethodDefinitionClassName_190296() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=190296
		final String[] expected = { "Printer::" };
		assertMinimumCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	struct S {
	//		void method();
	//		int datamem;
	//	};
	//
	//	template <typename F>
	//	void f(F);
	//
	//	int main() {
	//		f(&S::/*cursor*/);
	//	}
	public void testAddressOfClassQualifiedNonstaticMember_395562() throws Exception {
		final String[] expected = { "method", "datamem" };
		assertMinimumCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	struct Waldo {
	//		void find();
	//	};
	//	void Waldo::f/*cursor*/
	public void testRegression_395562() throws Exception {
		// Should include parentheses in replacement string when completing
		// out-of-line method definition.
		assertCompletionResults(new String[] { "find()" });
	}

	// typedef struct {
	//    int sx;
	// } my_struct;
	//
	// void func(my_struct s);
	//
	// void test() {
	//    fun/*cursor*/
	public void testFunctionWithTypedefToAnonymousType_bug192787() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=192787
		final String[] expected = { "func(my_struct s) : void" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	// namespace gns {
	//   void test() {
	//      g/*cursor*/
	public void testBindingsWithoutDeclaration() throws Exception {
		// gC1all, gStruct, gnsClass, gnsStruct: fix for 214146, type from a source file is not proposed.
		final String[] expected = { "gC1", "gC2", "gfC1()", "gfC2()", "gns::", "gnsFunc()", "gnsTemp", "gFunc()",
				"gTemp" };
		final String[] expected2 = { "gC1", "gC2", "gfC1()", "gfC2()", "gns::" };
		String disturbContent = readTaggedComment(DISTURB_FILE_NAME);
		IFile dfile = createFile(fProject, DISTURB_FILE_NAME, disturbContent);
		waitForIndexer(fCProject);
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

		dfile.delete(true, npm());
		waitForIndexer(fCProject);
		assertCompletionResults(fCursorOffset, expected2, REPLACEMENT);
	}

	// struct Struct/*cursor*/
	public void testElaboratedTypeSpecifierStruct_bug208710() throws Exception {
		final String[] expected = { "Struct1", "Struct2" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// struct Union/*cursor*/
	public void testElaboratedTypeSpecifierNotStruct_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// struct C/*cursor*/
	public void testElaboratedTypeSpecifierNotStruct2_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union Union/*cursor*/
	public void testElaboratedTypeSpecifierUnion_bug208710() throws Exception {
		final String[] expected = { "Union1", "Union2" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union Struct/*cursor*/
	public void testElaboratedTypeSpecifierNotUnion_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union C/*cursor*/
	public void testElaboratedTypeSpecifierNotUnion2_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class C/*cursor*/
	public void testElaboratedTypeSpecifierClass_bug208710() throws Exception {
		final String[] expected = { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class Struct/*cursor*/
	public void testElaboratedTypeSpecifierNotClass_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	//    C1::/*cursor*/
	public void testEnumInClass_bug199598() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199598
		final String[] expected = { "E2", "e21", "e22" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// class Union/*cursor*/
	public void testElaboratedTypeSpecifierNotClass2_bug208710() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void func() {float a; a= 1./*cursor*/}
	public void testCompletionInFloatingPointLiteral_193464() throws Exception {
		final String[] expected = new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// #ifdef __cplusplus__
	// extern "C" {
	// #endif
	// void c_linkage();
	// #ifdef __cplusplus__
	// }
	// #endif

	// #include "header191315.h"

	// #include "header191315.h"
	// void xxx() { c_lin/*cursor*/
	public void testExternC_bug191315() throws Exception {
		CharSequence[] content = getContentsForTest(3);
		createFile(fProject, "header191315.h", content[0].toString());
		createFile(fProject, "source191315.c", content[1].toString());
		createFile(fProject, "source191315.cpp", content[1].toString());
		waitForIndexer(fCProject);
		final String[] expected = { "c_linkage()" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//#include "/*cursor*/
	public void testInclusionProposals_bug113568() throws Exception {
		File tempRoot = new File(System.getProperty("java.io.tmpdir"));
		File tempDir = new File(tempRoot, "cdttest_113568");
		tempDir.mkdir();
		try {
			createIncludeFiles(tempDir, new String[] { "h1/inc1.h", "h1/sub1/inc11.h", "h2/inc2.h" });
			String[] expected = { "\"inc1.h\"", "\"sub1/\"", "\"inc2.h\"" };
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset++, 0, "i");
			expected = new String[] { "\"inc1.h\"", "\"inc2.h\"" };
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset, 0, "\"");
			expected = new String[] { "\"inc1.h", "\"inc2.h" };
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			createFile(fProject, "inc113568.h", "");
			expected = new String[] { "\"inc1.h", "\"inc113568.h", "\"inc2.h" };
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset - 1, 1, "sub1/");
			expected = new String[] { "\"sub1/inc11.h" };
			assertCompletionResults(fCursorOffset += 4, expected, REPLACEMENT);

			// bug 278967
			getDocument().replace(fCursorOffset - 5, 5, "../");
			expected = new String[] { "\"../h1/", "\"../h2/", };
			assertCompletionResults(fCursorOffset -= 2, expected, REPLACEMENT);
		} finally {
			deleteDir(tempDir);
		}
	}

	//#include "/*cursor*/
	public void testHeaderFileWithNoExtension_292229() throws Exception {
		File tempRoot = new File(System.getProperty("java.io.tmpdir"));
		File tempDir = new File(tempRoot, "cdttest_292229");
		tempDir.mkdir();
		try {
			createIncludeFiles(tempDir, new String[] { "h1/bar", "h1/foo.hpp" });
			// A file like h1/bar which is not known to be a header should appear
			// in the proposal list, but below files that are known to be headers
			// like h1/foo.hpp.
			String[] expected = { "\"foo.hpp\"", "\"bar\"" };
			assertOrderedCompletionResults(expected);
		} finally {
			deleteDir(tempDir);
		}
	}

	public static void deleteDir(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				file.delete();
			}
		}
		dir.delete();
	}

	private static void createIncludeFiles(File dir, String[] files) throws IOException {
		Set<String> includeDirs = new HashSet<>();
		for (String file2 : files) {
			File file = new File(dir, file2);
			final File parentFile = file.getParentFile();
			if (parentFile.getName().startsWith("sub")) {
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
			} else if (includeDirs.add(parentFile.getAbsolutePath())) {
				parentFile.mkdirs();
			}
			file.createNewFile();
		}
		TestScannerProvider.sIncludes = includeDirs.toArray(new String[includeDirs.size()]);
	}

	// void test() {
	// int local;
	// switch(loc/*cursor*/
	public void testSwitchStatement() throws Exception {
		final String[] expected = { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// while(loc/*cursor*/
	public void testWhileStatement() throws Exception {
		final String[] expected = { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(loc/*cursor*/
	public void testForStatement1() throws Exception {
		final String[] expected = { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(int i=0;i<loc/*cursor*/
	public void testForStatement2() throws Exception {
		final String[] expected = { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(int i=0;i<local;loc/*cursor*/
	public void testForStatement3() throws Exception {
		final String[] expected = { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	#define INIT_PTR(PtrName)   (PtrName) = 0;
	//	class CCApp {
	//	public:
	//	        int pIShell;
	//	};
	//
	//	int main(void) {
	//	   CCApp *pThis = 0;
	//
	//	   INIT_PTR(pTh/*cursor*/);
	//	}
	public void testCompletionInMacroArguments1_200208() throws Exception {
		final String[] expected = { "pThis" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	#define INIT_PTR(PtrName)   (PtrName) = 0;
	//	#define COPY_PTR(pTarget, pSource)   (pTarget) = (pSource)
	//
	//	class CCApp {
	//	public:
	//	        int pIShell;
	//	};
	//
	//	int main(void) {
	//	   CCApp *pThis = 0;
	//
	//	   INIT_PTR(pThis);
	//     COPY_PTR(pThis->pIShell, pThis->pI/*cursor*/)
	//	}
	public void testCompletionInMacroArguments2_200208() throws Exception {
		final String[] expected = { "pIShell" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	void test() {
	//		int alocal, blocal;
	//		if (alocal < bl/*cursor*/
	public void testCompletionAfterLessThan_229062() throws Exception {
		final String[] expected = { "blocal" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	enum {enum0, enum1, enum2};
	//	typedef struct {
	//	   int byte1;
	//	   int byte2;
	//	} MYSTRUCT_TYPE;
	//	static const MYSTRUCT_TYPE myArrayOfStructs[] = {{enum/*cursor*/
	public void testCompletionInInitializerList_230389() throws Exception {
		final String[] expected = { "enum0", "enum1", "enum2" };
		assertCompletionResults(expected);
	}

	// void test() {
	//    C2 c2;
	//    c2(1)->iam/*cursor*/
	public void testUserdefinedCallOperator_231277() throws Exception {
		final String[] expected = { "iam1()" };
		assertCompletionResults(expected);
	}

	//  void test() {struct s206450 x; x./*cursor*/
	public void testNestedAnonymousStructs_206450() throws Exception {
		final String[] expected = { "a1", "a2", "u1", "u2", "a4", "b", "s206450" };
		assertCompletionResults(expected);
	}

	//  void test() {_f204758/*cursor*/
	public void testTypedefToAnonymous_204758() throws Exception {
		final String[] expected = { "_f204758(_e204758 x) : void" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	#define CATCH(X) } catch (X) {
	//	void foo() {
	//		try {
	//			CATCH(float var)
	//			v/*cursor*/
	//		} catch (int var2) {
	//		}
	//	}
	public void testContentAssistWithBraceInMacro_257915() throws Exception {
		final String[] expected = { "var : float" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	struct X {
	//	   typedef int TInt;
	//	};
	//	void main() {
	//		X::T/*cursor*/  // content assist does not propose TInt
	//	}
	public void testNestedTypesInQualifiedNames_255898() throws Exception {
		final String[] expected = { "TInt" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//template <class type>
	//class Queue {
	//	TClass<type>* myQueue;
	//public:
	//	Queue() {
	//		myQueue = new TClass<type>;
	//	}
	//	bool isEmtpy() {
	//		return myQueue->a/*cursor*/
	//	}
	//};
	public void testContentAssistInDeferredClassInstance_194592() throws Exception {
		final String[] expected = { "add(tOther)" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//namespace ns {
	//  template<class T>
	//  class Base {
	//  public:
	//    Base(int par) {}
	//  };
	//}
	//
	//class Helper {
	//public:
	//  Helper() {}
	//};
	//
	//class InitializerListTest : public ::ns::Base<Helper>, Helper {
	//private:
	//  int mOne;
	//public:
	//  InitializerListTest() : /*cursor*/
	//};
	public void testConstructorInitializerList_EmptyInput_266586() throws Exception {
		final String[] expected = { "mOne", "Base", "Base(int)", "Base(const Base<Helper> &)", "Helper", "Helper(void)",
				"Helper(const Helper &)", "_A_331056", "_B_331056", "bug521016",
				// Namespaces must be offered as well. In order for this code
				// to compile with gcc (e.g. 4.1.2), you need to write
				// ::ns::Base<Helper>() instead of just Base<Helper>().
				"ns" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {
	//  template<class T>
	//  class Base {
	//  public:
	//    Base(int par) {}
	//  };
	//}
	//
	//class Helper {
	//public:
	//  Helper() {}
	//};
	//
	//class InitializerListTest : public ::ns::Base<Helper>, Helper {
	//private:
	//  int mOne;
	//public:
	//  InitializerListTest() : ::ns/*cursor*/
	//};
	public void testCunstructorInitializerList_NameContextInput_266586() throws Exception {
		final String[] expected = { "ns" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {
	//  template<class T>
	//  class Base {
	//  public:
	//    Base(int par) {}
	//  };
	//}
	//
	//class Helper {
	//public:
	//  Helper() {}
	//};
	//
	//class InitializerListTest : public ::ns::Base<Helper>, Helper {
	//private:
	//  int mOne;
	//public:
	//  InitializerListTest() : m/*cursor*/
	//};
	public void testCunstructorInitializerList_MemberInput_266586() throws Exception {
		final String[] expected = { "mOne" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {
	//  template<class T>
	//  class Base {
	//  public:
	//    Base(int par) {}
	//  };
	//}
	//
	//class Helper {
	//public:
	//  Helper() {}
	//};
	//
	//class InitializerListTest : public ::ns::Base<Helper>, Helper {
	//private:
	//  int mOne;
	//public:
	//  InitializerListTest() : h/*cursor*/
	//};
	public void testConstructorInitializerList_BaseClassInput_266586() throws Exception {
		final String[] expected = { "Helper", "Helper(void)", "Helper(const Helper &)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct Waldo {
	//		Waldo(int, int);
	//	};
	//
	//	int main() {
	//		Waldo waldo{/*cursor*/}
	//	}
	public void testUniformInitializationInSimpleDeclaration_509185() throws Exception {
		final String[] expected = { "Waldo(const Waldo &)", "Waldo(int, int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
		assertCompletionResults(new String[] { "", "" }); // No replacements, just context info
	}

	//	struct Waldo {
	//		Waldo(int, int);
	//	};
	//
	//	int main() {
	//		auto waldo = Waldo{/*cursor*/}
	//	}
	public void testUniformInitializationInSimpleTypeConstructorExpression_509185() throws Exception {
		final String[] expected = { "Waldo(const Waldo &)", "Waldo(int, int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
		assertCompletionResults(new String[] { "", "" }); // No replacements, just context info
	}

	//	struct Waldo {
	//		Waldo(int, int);
	//	};
	//
	//	struct Finder {
	//		Waldo waldo;
	//		Finder() : waldo{/*cursor*/
	//	};
	public void testUniformInitializationInConstructorChainInitializer_509185() throws Exception {
		final String[] expected = { "Waldo(const Waldo &)", "Waldo(int, int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
		assertCompletionResults(new String[] { "", "" }); // No replacements, just context info
	}

	//	struct Waldo {
	//	    Waldo(int, int);
	//	};
	//
	//	int main() {
	//	    Waldo waldo = Waldo(/*cursor*/)
	//	}
	public void testConstructorCallWithEmptyParentheses_509731() throws Exception {
		final String[] expected = { "Waldo(const Waldo &)", "Waldo(int, int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct Waldo {
	//		~Waldo();
	//	};
	//	Waldo::~/*cursor*/
	public void testDestructorDefinition_456293() throws Exception {
		final String[] expectedDisplay = { "~Waldo(void)" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
		final String[] expectedReplacement = { "Waldo()" };
		assertCompletionResults(fCursorOffset, expectedReplacement, REPLACEMENT);
	}

	//	template <typename T> struct vector {
	//      typedef T value_type;
	//		void push_back(const value_type& value) {}
	//	};
	//	typedef int MyType;
	//	void test() {
	//	    vector<MyType> v;
	//	    v.push_back(/*cursor*/);
	//	}
	public void testTypedefSpecialization_307818() throws Exception {
		final String[] expected = { "push_back(const vector<MyType>::value_type & value) : void" };
		assertParameterHint(expected);
	}

	//	using namespace ::_B_331056;
	//	Ref/*cursor*/
	public void testUsingDeclaration_331056() throws Exception {
		final String[] expected = { "Reference" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template<class T> struct BaseClass {
	//	    void BaseMethod();
	//	};
	//	template<class T> struct DerivedClass : BaseClass<T> {
	//	    void DerivedMethod() {
	//	        this->BaseM/*cursor*/
	//	    }
	//	};
	public void testDeferredBaseClass_330762() throws Exception {
		final String[] expected = { "BaseMethod(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	int par1;
	//	auto x = [](int par2) { return par/*cursor*/
	public void testLambdaParameter_481070() throws Exception {
		final String[] expected = { "par1", "par2" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	#define fooBar
	//  #define foo_bar
	//  fB/*cursor*/
	public void testUserMacroSegmentMatch() throws Exception {
		final String[] expected = { "fooBar", "foo_bar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//  __bVA/*cursor*/
	public void testBuiltinMacroSegmentMatch() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected = { "__builtin_va_arg(ap, type)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	namespace N {
	//	  void foo(int);
	//	}
	//	using N::f/*cursor*/
	public void testUsingDeclaration_379631() throws Exception {
		final String[] expected = { "foo;" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	namespace N {
	//		class waldo {};
	//	}
	//	using N::w/*cursor*/
	public void testUsingDeclaration_509182() throws Exception {
		final String[] expected = { "waldo;" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	class Base {
	//	private:
	//	    void priv();
	//	protected:
	//	    void prot();
	//	public:
	//	    void publ();
	//	};
	//	class Derived : Base {
	//	    using Base::/*cursor*/
	//	};
	public void testUsingDeclarationInClass_511048() throws Exception {
		final String[] expected = { "prot(void)", "publ(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template <typen/*cursor*/
	public void testTemplateDeclaration_397288() throws Exception {
		final String[] expected = { "typename" };
		assertContentAssistResults(fCursorOffset, expected, IS_COMPLETION, REPLACEMENT);
	}

	//	class Base {
	//	  int c;
	//	};
	//
	//	struct Cat {
	//	  void meow();
	//	};
	//
	//	struct Derived : Base {
	//	  void foo() {
	//	    c./*cursor*/
	//	  }
	//
	//	  Cat c;
	//	};
	public void testShadowingBaseClassMember_407807() throws Exception {
		final String[] expected = { "Cat", "meow(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct Cat {
	//	  void meow();
	//	};
	//
	//	struct Waldo {
	//	  void bar() {
	//	    c./*cursor*/
	//	  }
	//
	//	  Cat c;
	//	};
	//
	//	void foo() {
	//	  __LINE__;
	//	}
	public void testPreprocessorProvidedMacro_412463() throws Exception {
		final String[] expected = { "Cat", "meow(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct Cat {
	//	  void meow();
	//	};
	//
	//	struct Waldo {
	//	  void bar() {
	//	    c./*cursor*/
	//	  }
	//
	//	  Cat c;
	//	};
	//
	//	int x = __CDT_PARSER__;
	public void testPredefinedMacro_412463() throws Exception {
		final String[] expected = { "Cat", "meow(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	void foo() { Spec/*cursor*/
	public void testTemplateSpecialization() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected = { "Specialization<typename T1, typename T2>" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	// struct Wrapper {
	// 	template<typename T>
	// 	struct A {
	// 		static void test();
	// 	};
	//
	// 	struct B : A<B> {
	// 		void run(){ te/*cursor*/ }
	// 	};
	// };
	public void testTemplateInstanceMemberAccess_459047() throws Exception {
		final String[] expected = { "test(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// template <int T>
	// struct A {
	// 	template <int TT>
	// 	struct AA {
	// 		template <typename TTT>
	// 		using Type = TTT;
	// 	};
	// };
	//
	// struct B{
	// 	static int i;
	// };
	//
	// A<0>::AA<0>::Type<B>::/*cursor*/
	public void testNestedTemplateSpecialization_460341() throws Exception {
		final String[] expected = { "i" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template <typename T>
	//	struct meta1;
	//
	//	template <typename T>
	//	struct meta2;
	//
	//	template <typename T>
	//	void waldo(T, typename meta1<T>::type, typename meta2<T>::type);
	//
	//	int main() {
	//	    wald/*cursor*/
	//	}
	public void testNestingClassNameInCompletion_395571() throws Exception {
		final String[] expected = { "waldo(T, meta1<T>::type, meta2<T>::type) : void" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	void foo() { Specialization<int, /*cursor*/
	public void testTemplateArgumentList() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected = { "Specialization<typename T1, typename T2>" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	template<typename T,typename U>
	//	struct TestTemplate {
	//		class NestedClass {};
	//	};
	//	template<typename T>
	//	struct TestTemplate<T,int> {
	//		class NestedClass {};
	//	};
	//	template<>
	//	struct TestTemplate<int,int> {
	//		class NestedClass {};
	//	};
	//	template<typename T,typename U>
	//	class TestTemplateSelfReference : TestTemplate<T,U>::/*cursor*/
	public void testTemplateSelfReference_bug456101() throws Exception {
		final String[] expected = { "NestedClass" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	template<typename T>
	//	class TestTemplateSelfReference : TClass<T>::/*cursor*/
	public void testTemplateSelfReferencePDOM_bug456101() throws Exception {
		final String[] expected = { "NestedClass" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	namespace N {
	//	void foo(int);
	//	}
	//	using N::fo/*cursor*/;
	public void testUsingCompletionWithFollowingSemicolon() throws Exception {
		final String[] expected = { "foo" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
		final String[] expectedInformation = { "null" };
		assertCompletionResults(fCursorOffset, expectedInformation, CONTEXT);
	}

	//	namespace N {
	//	template<typename T> struct Tpl {};
	//	}
	//	using N::Tp/*cursor*/;
	public void testUsingCompletionWithoutTemplateArguments() throws Exception {
		final String[] expected = { "Tpl" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	namespace N {
	//	template<typename T> struct Tpl {};
	//	}
	//	using N::Tp/*cursor*/
	public void testUsingCompletionWithoutTemplateArgumentsButSemicolon() throws Exception {
		final String[] expected = { "Tpl;" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	using Alias = C/*cursor*/
	public void testAliasDeclarationCompletion() throws Exception {
		final String[] expectedID = { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expectedID, ID);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testDefaultFunctionArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(true);
		final String[] expectedDisplay = { "default_argument(int i = 23) : void" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
		final String[] expectedReplacement = { "default_argument(i)" };
		assertCompletionResults(fCursorOffset, expectedReplacement, REPLACEMENT);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testNoDefaultFunctionArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument(int i) : void" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testNoDefaultFunctionParameter() throws Exception {
		setDisplayDefaultedParameters(false);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument() : void" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testDefaultTemplateArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(true);
		final String[] expectedDisplay = { "default_argument<typename T = int>" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
		final String[] expectedReplacement = { "default_argument<>" };
		assertCompletionResults(fCursorOffset, expectedReplacement, REPLACEMENT);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testNoDefaultTemplateArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument<typename T>" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testNoDefaultTemplateParameter() throws Exception {
		setDisplayDefaultedParameters(false);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument<>" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
	}

	//	template<typename T>
	//	struct tpl {};
	//	template<typename T1, typename T2 = tpl<T1>>
	//	struct other_tpl {};
	//	other_tpl/*cursor*/
	public void testDefaultTemplateTemplateArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(true);
		final String[] expectedDisplay = { "other_tpl<typename T1, typename T2 = tpl<T1>>" };
		assertCompletionResults(fCursorOffset, expectedDisplay, DISPLAY);
	}

	//	void foo(int x, int y);
	//	void caller() {
	//		foo/*cursor*/
	//	}
	public void testFunctionWithNoParameterGuesses_497190() throws Exception {
		try {
			enableParameterGuessing(false);
			assertCompletionResults(new String[] { "foo()" });
		} finally {
			enableParameterGuessing(true);
		}
	}

	//	void foo(int x, int y);
	//	void caller() {
	//		fo/*cursor*/
	//	}
	public void testReplacementLength_511274() throws Exception {
		Object[] results = invokeContentAssist(fCursorOffset, 0, true, false, true).results;
		assertEquals(1, results.length);
		assertInstance(results[0], CCompletionProposal.class);
		assertEquals(2, ((CCompletionProposal) results[0]).getReplacementLength());
	}

	//	struct A {
	//	    void foo();
	//	};
	//
	//	template <typename>
	//	struct B {
	//	    A val;
	//	};
	//
	//	template <typename T>
	//	void test(B<T> b) {
	//	    b.val./*cursor*/
	//	}
	public void testFieldOfDeferredClassInstance_bug402617() throws Exception {
		final String[] expected = { "A", "foo(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct A {
	//		int foo;
	//	};
	//	typedef A* B;
	//	int main() {
	//		B waldo;
	//		waldo./*cursor*/
	//	}
	public void testDotToArrowConversionForTypedef_bug461527() throws Exception {
		setReplaceDotWithArrow(true);
		final String[] expected = { "A", "foo : int" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
		assertDotReplacedWithArrow();
	}

	//	struct A {
	//	    void foo();
	//	};
	//
	//	template <class T>
	//	class C : public T {};
	//
	//	int main() {
	//	    C<A> c;
	//	    c./*cursor*/
	//	}
	public void testInheritanceFromTemplateParameter_bug466861() throws Exception {
		final String[] expected = { "A", "C", "foo(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	int [[f(./*cursor*/)]] i;
	public void testCompletionInsideAttribute_bug477359() throws Exception {
		final String[] expected = {};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template <typename T>
	//	struct vector {
	//	    T& front();
	//	};
	//	template <class T>
	//	void foo(vector<vector<T>> a) {
	//	    a.front()./*cursor*/
	//	}
	public void testDependentScopes_bug472818a() throws Exception {
		final String[] expected = { "vector<typename T>", "front(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template <typename T>
	//	struct vector {
	//	    T& front();
	//	};
	//	template <class T>
	//	void foo(vector<vector<vector<T>>> a) {
	//	    a.front().front()./*cursor*/
	//	}
	public void testDependentScopes_bug472818b() throws Exception {
		final String[] expected = { "vector<typename T>", "front(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	// This is a simplification of the actual std::vector implementation
	//	// that ships with gcc 5.1's libstdc++.
	//	template <typename T>
	//	struct allocator {
	//	    typedef T value_type;
	//		template <typename U>
	//		struct rebind {
	//			typedef allocator<U> other;
	//		};
	//	};
	//	template <typename Alloc, typename T>
	//	struct alloctr_rebind {
	//		typedef typename Alloc::template rebind<T>::other type;
	//	};
	//	template <typename Alloc>
	//	struct allocator_traits {
	//	    typedef typename Alloc::value_type value_type;
	//		template <typename T>
	//		using rebind_alloc = typename alloctr_rebind<Alloc, T>::type;
	//	};
	//	template <typename Alloc>
	//	struct alloc_traits {
	//	    typedef allocator_traits<Alloc> base_type;
	//	    typedef typename base_type::value_type value_type;
	//	    typedef value_type& reference;
	//		template <typename T>
	//		struct rebind {
	//			typedef typename base_type::template rebind_alloc<T> other;
	//		};
	//	};
	//	template <typename T, typename Alloc>
	//	struct vector_base {
	//	    typedef typename alloc_traits<Alloc>::template rebind<T>::other allocator_type;
	//	};
	//	template <typename T, typename Alloc = allocator<T>>
	//	struct vector {
	//	    typedef vector_base<T, Alloc> base_type;
	//	    typedef typename base_type::allocator_type allocator_type;
	//	    typedef alloc_traits<allocator_type> alloc_traits_type;
	//	    typedef typename alloc_traits_type::reference reference;
	//	    reference front();
	//	};
	//	template <class T>
	//	void foo(vector<vector<vector<T>>> a) {
	//	    a.front().front()./*cursor*/
	//	}
	public void testDependentScopes_bug472818c() throws Exception {
		final String[] expected = { "vector<typename T, typename Alloc = allocator<T>>", "base_type", "allocator_type",
				"alloc_traits_type", "reference", "front(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	template<typename _Tp>
	//	class allocator {
	//	public:
	//		typedef _Tp& reference;
	//		typedef const _Tp& const_reference;
	//
	//		template<typename _Tp1>
	//		struct rebind {
	//			typedef allocator<_Tp1> other;
	//		};
	//	};
	//
	//	template<typename _Alloc>
	//	struct __alloc_traits {
	//		typedef typename _Alloc::reference reference;
	//		typedef typename _Alloc::const_reference const_reference;
	//
	//		template<typename _Tp>
	//		struct rebind {
	//			typedef typename _Alloc::template rebind<_Tp>::other other;
	//		};
	//	};
	//
	//	template<typename _Tp, typename _Alloc>
	//	struct _Vector_base {
	//		typedef typename __alloc_traits<_Alloc>::template
	//		rebind<_Tp>::other _Tp_alloc_type;
	//	};
	//
	//	template<typename _Tp, typename _Alloc = allocator<_Tp> >
	//	class vector {
	//		typedef _Vector_base<_Tp, _Alloc> _Base;
	//		typedef typename _Base::_Tp_alloc_type _Tp_alloc_type;
	//		typedef __alloc_traits <_Tp_alloc_type> _Alloc_traits;
	//		typedef typename _Alloc_traits::reference reference;
	//		typedef typename _Alloc_traits::const_reference const_reference;
	//	public:
	//		reference front();
	//		const_reference front() const;
	//
	//	};
	//
	//	template<typename T>
	//	void foo(vector<vector<T>> v) {
	//		v.front().f/*cursor*/
	//	}
	public void testDependentScopes_472818d() throws Exception {
		assertCompletionResults(new String[] { "front()" });
	}

	//	template <int k>
	//	struct D {
	//		struct C {
	//			C* c;
	//		};
	//		C c;
	//		void f() {
	//			c.c->c->c./*cursor*/
	//		}
	//	};
	public void testDependentMemberChain_bug478121() throws Exception {
		setReplaceDotWithArrow(true);
		final String[] expected = { "c" };
		assertCompletionResults(fCursorOffset, expected, ID);
		assertDotReplacedWithArrow();
	}

	//	struct A {
	//		int waldo;
	//	};
	//	class B : private A {
	//	public:
	//		using A::waldo;  // allows public access despire private inheritance
	//	};
	//	void test() {
	//		B obj;
	//		obj./*cursor*/
	//	}
	public void testMemberExposedViaUsingDecl_bug292236a() throws Exception {
		final String[] expected = { "B", "A", "waldo" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	struct A {
	//		int waldo;
	//	};
	//	class B : private A {
	//	public:
	//		A::waldo;  // allows public access despire private inheritance
	//	};
	//	void test() {
	//		B obj;
	//		obj./*cursor*/
	//	}
	public void testMemberExposedViaUsingDecl_bug292236b() throws Exception {
		final String[] expected = { "B", "A", "waldo" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	void waldo();
	//	int main() {
	//		wal/*cursor*/();
	//	}
	public void testExistingParens_72391() throws Exception {
		assertCompletionResults(new String[] { "waldo" }); // expect no parens in replacement
	}

	//	struct A{
	//	    A(int,int);
	//	};
	//	struct B: A{
	//	    using A::/*cursor*/
	//	};
	public void testInheritingConstructor_511653() throws Exception {
		assertCompletionResults(new String[] { "A;" });
	}

	//	template<int I>
	//	struct A {
	//
	//	  struct Hello{};
	//
	//	};
	//
	//	struct B {
	//
	//	  template<int T>
	//	  using Test = A<T>;
	//
	//	  void main() {
	//	    Te/*cursor*/
	//	  }
	//
	//	};
	public void testAliasTemplateTypeSpecifier_521820() throws Exception {
		assertCompletionResults(new String[] { "Test<>" });
	}

	//	template <int I, int J>
	//	struct A {
	//	    struct Default {
	//	    };
	//	};
	//
	//	template <>
	//	template <int J>
	//	struct A<0, J> {
	//	    struct Partial {
	//	    };
	//	};
	//
	//	template <int I>
	//	struct B {
	//	    A<0, I>::/*cursor*/;
	//	};
	public void testPartialSpecializationWithDeferredClassInstance_456224a() throws Exception {
		assertCompletionResults(new String[] { "Partial" });
	}

	//	template <int I, int J>
	//	struct A {
	//	    struct Default {
	//	    };
	//	};
	//
	//	template <int J>
	//	struct A<0, J> {
	//	    struct Partial {
	//	        struct Result {};
	//	    };
	//	};
	//
	//	template <int I>
	//	struct B {
	//	    A<0, I>::Partial::/*cursor*/
	//	};
	public void testPartialSpecializationWithDeferredClassInstance_456224b() throws Exception {
		assertCompletionResults(new String[] { "Result" });
	}

	// template<int TestParam>
	// struct A {
	//   using type_t = A<Te/*cursor*/>
	// };
	public void testNonTypeTemplateParameterCompletion_522010() throws Exception {
		assertCompletionResults(new String[] { "TestParam" });
	}

	//	void find();
	//	void waldo() {
	//		fin/*cursor*/
	//	}
	public void testCursorPositionForZeroArgFunction_537031() throws Exception {
		// Test that the cursor ends up after the closing parenthesis.
		// The 6 below is 4 for "find" + 2 for opening AND closing parentheses.
		int expectedPos = getDocument().get().lastIndexOf("fin") + 6;
		assertCursorPositionsAfterReplacement(new int[] { expectedPos });
	}

	//	namespace outer { namespace inner {} }
	//	namespace alias = o/*cursor*/
	public void testCompletionInNamespaceAlias_545360() throws Exception {
		assertCompletionResults(new String[] { "outer::" });
	}
}
