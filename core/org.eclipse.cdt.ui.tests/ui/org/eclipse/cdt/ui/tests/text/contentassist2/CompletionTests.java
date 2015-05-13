/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;

/**
 * A collection of code completion tests.
 *
 * @since 4.0
 */
public class CompletionTests extends AbstractContentAssistTest {
	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.cpp";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";
	private static final String DISTURB_FILE_NAME= "DisturbWith.cpp";

	protected int fCursorOffset;
	private boolean fCheckExtraResults= true;
	private IProject fProject;

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

	//	{CompletionTest.h}
	//	class C1;
	//	class C2;
	//	class C3;
	//
	//	extern C1* gC1;
	//	C2* gC2 = 0;
	//
	//	extern C1* gfC1();
	//	C2* gfC2();
	//
	//	enum E1 {e11, e12};
	//
	//	class C1 {
	//		public:
	//			enum E2 {e21, e22};
	//
	//			C1* fMySelf;
	//			void iam1();
	//
	//			C1* m123();
	//			C1* m12();
	//			C1* m13();
	//
	//			protected:
	//				void m1protected();
	//			private:
	//				void m1private();
	//	};
	//	typedef C1 T1;
	//	using A1 = C1;
	//
	//	class C2 : public T1 {
	//		public:
	//			C2* fMySelf;
	//	void iam2();
	//
	//	C2* m123();
	//	C2* m12();
	//	C2* m23();
	//	C1* operator()(int x);
	//
	//	protected:
	//		void m2protected();
	//	private:
	//		void m2private();
	//	friend void _friend_function(C3* x);
	//	friend class _friend_class;
	//	};
	//	typedef C2 T2;
	//
	//	class C3 : public C2 {
	//		public:
	//			C3* fMySelf;
	//	void iam3();
	//
	//	C3* m123();
	//	C3* m13();
	//
	//	template<typename T> T tConvert();
	//	protected:
	//		void m3protected();
	//	private:
	//		void m3private();
	//	};
	//	typedef C3 T3;
	//
	//	namespace ns {
	//		const int NSCONST= 1;
	//		class CNS {
	//			void mcns();
	//		};
	//	};
	//	template <class T> class TClass {
	//		T fTField;
	//		public:
	//			TClass(T tArg) : fTField(tArg) {
	//			}
	//			T add(T tOther) {
	//				return fTField + tOther;
	//			}
	//		class NestedClass{};
	//	};
	//	// bug 109480
	//	class Printer
	//	{
	//		public:
	//			static void InitPrinter(unsigned char port);
	//	private:
	//		//Storage for port printer is on
	//		static unsigned char port;
	//	protected:
	//	};
	//	struct Struct1;
	//	struct Struct2;
	//	union Union1;
	//	union Union2;
	//	struct s206450 {
	//		struct {int a1; int a2;};
	//		union {int u1; char u2;};
	//		struct {int a3;} a4;
	//		int b;
	//	};
	//	typedef enum {__nix} _e204758;
	//	void _f204758(_e204758 x);
	//
	// // Bug 331056
	//	namespace _A_331056 {
	//		class Reference {};
	//	}
	//	namespace _B_331056 {
	//		using ::_A_331056::Reference;
	//	}
	//
	//	template<typename T1, typename T2>
	//	struct Specialization {
	//	};
	//	template<typename T2>
	//	struct Specialization<int, T2> {
	//	};
	//	template<>
	//	struct Specialization<int, int> {
	//	};
	//
	//	template<typename T1, typename T2>
	//	using AliasForSpecialization = Specialization<T1, T2>;
	//
	//	template<typename T1, typename T2>
	//	using AliasForTemplateAlias = AliasForSpecialization<T1, T2>;

	public CompletionTests(String name) {
		super(name, true);
	}

	public static Test suite() {
		return BaseTestCase.suite(CompletionTests.class, "_");
	}

	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractCompletionTest#setUpProjectContent(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		fProject= project;
		String headerContent= readTaggedComment(HEADER_FILE_NAME);
		StringBuilder sourceContent= getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \"" + HEADER_FILE_NAME + "\"\n");
		fCursorOffset= sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset + CURSOR_LOCATION_TAG.length());
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest#doCheckExtraResults()
	 */
	@Override
	protected boolean doCheckExtraResults() {
		return fCheckExtraResults;
	}

	private void setCheckExtraResults(boolean check) {
		fCheckExtraResults= check;
	}

	private void assertMinimumCompletionResults(int offset, String[] expected, CompareType compareType) throws Exception {
		setCheckExtraResults(false);
		try {
			assertCompletionResults(offset, expected, compareType);
		} finally {
			setCheckExtraResults(true);
		}
	}

	protected void assertCompletionResults(int offset, String[] expected, CompareType compareType) throws Exception {
		assertContentAssistResults(offset, expected, true, compareType);
	}

	protected void assertCompletionResults(String[] expected) throws Exception {
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	protected void assertParameterHint(String[] expected) throws Exception {
		assertContentAssistResults(fCursorOffset, expected, false, CONTEXT);
	}
	
	protected void assertDotReplacedWithArrow() throws Exception {
		assertEquals("->", getDocument().get(fCursorOffset - 1, 2));
	}

	private static void setDisplayDefaultArguments(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS, value);
	}

	private void setReplaceDotWithArrow(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW, value);
		fProcessorNeedsConfiguring = true;  // to pick up the modified auto-activation preference
	}
	
	private static void setDisplayDefaultedParameters(boolean value) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT, value);
	}

	//void gfunc() {C1 v; v.m/*cursor*/
	public void testLocalVariable() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {C1 v; v.fMySelf->m/*cursor*/
	public void testLocalVariable_MemberVariable() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {C1 v; v.m12()->m/*cursor*/
	public void testLocalVariable_MemberFunction() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {gfC1()->m/*cursor*/
	public void testGlobalFunction() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::self() {m/*cursor*/
	public void testOwnMember() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m1private(void)", "m1protected(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::self() {this->m/*cursor*/
	public void testOwnMemberViaThis() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m1private(void)", "m1protected(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {try{int bla;}catch(C1 v) {v.fMySelf->m/*cursor*/
	public void testCatchBlock1() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc() {try{int bla;}catch(C2 c){} catch(C1 v) {v.fMySelf->m/*cursor*/
	public void testCatchBlock2() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {gC/*cursor*/
	public void testGlobalVariables_GlobalScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2", "gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C1::f() {gC/*cursor*/
	public void testGlobalVariables_MethodScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2", "gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_GlobalScope() throws Exception {
		final String[] expected= {
				"cLocal1", "cLocal2"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_MethodScope() throws Exception {
		final String[] expected= {
				"cLocal1", "cLocal2"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* cLocal1; cLocal1->f/*cursor*/
	public void testDataMembers_GlobalScope() throws Exception {
		final String[] expected= { "fMySelf" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {while(true) {f/*cursor*/
	public void testDataMembers_MethodScope() throws Exception {
		final String[] expected= { "fMySelf" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {gf/*cursor*/
	public void testGlobalFunctions_GlobalScope() throws Exception {
		final String[] expected= {
				"gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {gf/*cursor*/
	public void testGlobalFunctions_MethodScope() throws Exception {
		final String[] expected= {
				"gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* l1; l1->m/*cursor*/
	public void testMethods_GlobalScope() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {m/*cursor*/
	public void testMethods_MethodScope() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m3private(void)", "m3protected(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C/*cursor*/
	public void testTypes_GlobalScope() throws Exception {
		final String[] expected= { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//class _friend_class { C3* x; void m() {x->m/*cursor*/
	public void testTypes_FriendClass() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m2private(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns { class _friend_class { C3* x; void m() {x->m/*cursor*/  // Not a friend due to namespace
	public void testTypes_FakeFriendClass() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m23(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void _friend_function(C3* x) { x->m/*cursor*/
	public void testTypes_FriendFunction() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m23(void)", "m1protected(void)",
				"m2protected(void)", "m2private(void)"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void _friend_function(C2* x) { x->m/*cursor*/  // Not a friend due to parameter type mismatch
	public void testTypes_FakeFriendFunction() throws Exception {
		final String[] expected= { "m123(void)", "m12(void)", "m13(void)", "m23(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C2::f() {T/*cursor*/
	public void testTypes_MethodScope() throws Exception {
		final String[] expected= { "T1", "T2", "T3", "TClass<typename T>" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void nsfunc(){C/*cursor*/
	public void testTypes_NamespaceScope() throws Exception {
		final String[] expected= { "C1", "C2", "C3", "CNS" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void gfunc(){::C/*cursor*/
	public void testTypes_GlobalQualification() throws Exception {
		final String[] expected= { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {e/*cursor*/
	public void testEnums_GlobalScope() throws Exception {
		final String[] expected= { "e11", "e12", "E1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void C3::f() {e/*cursor*/
	public void testEnums_MethodScope() throws Exception {
		final String[] expected= { "e11", "e12", "e21", "e22", "E1", "E2" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C/*cursor*/
	public void testQualificationForAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "C3", "C2", "C1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C2* l1; l1->C/*cursor*/
	public void testQualificationForAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "C2", "C1" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C3::fMySelf->iam/*cursor*/
	public void testQualifiedAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "iam3(void)", "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C2::fMySelf->iam/*cursor*/
	public void testQualifiedAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->C1::fMySelf->iam/*cursor*/
	public void testQualifiedAccess3() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T3::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "iam3(void)", "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T2::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= { "iam2(void)", "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C3* l1; l1->T1::fMySelf->iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier3() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1().iam/*cursor*/
	public void testTemporaryObject() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c)->iam/*cursor*/
	public void testAddressOf() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (*c).iam/*cursor*/
	public void testDereferencingOperator1() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; (**c).iam/*cursor*/
	public void testDereferencingOperator2() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; (*c)->iam/*cursor*/
	public void testDereferencingOperator3() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c[0].iam/*cursor*/
	public void testArrayAccessOperator1() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; c[0][1].iam/*cursor*/
	public void testArrayAccessOperator2() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1** c; c[0]->iam/*cursor*/
	public void testArrayAccessOperator3() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (&c[0])->iam/*cursor*/
	public void testArrayAccessOperator4() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {void* c; ((C1*)c)->iam/*cursor*/
	public void testCasts1() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}
	//void g(int a) {}; void f() {void* c; g(((C1*)c)->iam/*cursor*/
	public void testCasts2() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c++->iam/*cursor*/
	public void testPointerArithmetic1() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; (*++c).iam/*cursor*/
	public void testPointerArithmetic2() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1* c; c--->iam/*cursor*/
	public void testPointerArithmetic3() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c+1)->iam/*cursor*/
	public void testPointerArithmetic4() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {C1 c; (&c-1)->iam/*cursor*/
	public void testPointerArithmetic5() throws Exception {
		final String[] expected= { "iam1(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void f() {int localVar=0; if (*cond && somefunc(&local/*cursor*/
	public void testNestedCalls() throws Exception {
		final String[] expected= { "localVar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//int a[] = {1,2}; void f(int _0306_b) {_0306_b/*cursor*/
	public void testCuttingInput1() throws Exception {
		final String[] expected= { "_0306_b" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//int a[] = {1,2}; void f(int b) {int _0306_b[] = {2,3}; _0306_b/*cursor*/
	public void testCuttingInput2() throws Exception {
		final String[] expected= { "_0306_b" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//enum EnumType function() {int _031209_v; _031209/*cursor*/
	public void testDisturbingMacros() throws Exception {
		final String[] expected= { "_031209_v" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//namespace ns {void x() {NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember1() throws Exception {
		final String[] expected= { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void ns::CNS::mcns(){NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember2() throws Exception {
		final String[] expected= { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//#i/*cursor*/
	public void testCompletePreprocessorDirective() throws Exception {
		final String[] expected= {
				"#if", "#ifdef", "#ifndef", "#include"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//#  d/*cursor*/
	public void testCompletePreprocessorDirective2() throws Exception {
		final String[] expected= { "define " };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//#  if d/*cursor*/
	public void testCompletePreprocessorDirective3() throws Exception {
		final String[] expected= { "defined" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc(){TClass<int> t(0); t.a/*cursor*/
	public void testTemplateClassMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected= { "add(int)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc(){C3 c3; c3.t/*cursor*/
	public void testTemplateMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected= { "tConvert(void)" };
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
		// TODO Bug 455797, proposals are currently not presented as templates.
		final String[] expected = { "AliasForSpecialization",
				"AliasForTemplateAlias" };
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
		final String[] expected = { "D", "E" };
		assertContentAssistResults(fCursorOffset, expected, true, ID);
	}

	//using namespace ns;void gfunc(){NSC/*cursor*/
	public void testUsingDirective() throws Exception {
		final String[] expected= { "NSCONST" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void gfunc(){n/*cursor*/
	public void testAutoColons() throws Exception {
		final String[] expected= { "ns::" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//using namespace n/*cursor*/
	public void testAutoColons2() throws Exception {
		final String[] expected= { "ns" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//// to_be_replaced_
	//void gfunc(){aNew/*cursor*/
	public void testGlobalVariableBeforeSave_180883() throws Exception {
		String replace=   "// to_be_replaced_";
		String globalVar= "int aNewGlobalVar;";
		IDocument doc= getDocument();
		int idx= doc.get().indexOf(replace);
		doc.replace(idx, replace.length(), globalVar);

		// succeeds when buffer is saved
//		fEditor.doSave(new NullProgressMonitor());
//		EditorTestHelper.joinBackgroundActivities((AbstractTextEditor)fEditor);

		final String[] expected= { "aNewGlobalVar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//void Printer::InitPrinter(unsigned char port) {
	//	Printer::/*cursor*/
	public void testPrivateStaticMember_109480() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=109480
		final String[] expected= { "InitPrinter()", "port" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class vector3 {
	// public:
	//   void blah(const vector3& v) { x += v./*cursor*/; }
	//   float x;
	// };
	public void testForwardMembersInInlineMethods_103857a() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=103857
		final String[] expected= { "x" };
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
		final String[] expected= { "mem" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void Pri/*cursor*/
	public void testMethodDefinitionClassName_190296() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=190296
		final String[] expected= { "Printer::" };
		assertMinimumCompletionResults(fCursorOffset, expected, REPLACEMENT);
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
		final String[] expected= { "func(my_struct s) : void" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	// namespace gns {
	//   void test() {
	//      g/*cursor*/
	public void testBindingsWithoutDeclaration() throws Exception {
		// gC1all, gStruct, gnsClass, gnsStruct: fix for 214146, type from a source file is not proposed.
		final String[] expected= {
			"gC1", "gC2", "gfC1()", "gfC2()",
			"gns::", "gnsFunc()", "gnsTemp",
			"gFunc()", "gTemp"
		};
		final String[] expected2= {
			"gC1", "gC2", "gfC1()", "gfC2()", "gns::"
		};
		String disturbContent= readTaggedComment(DISTURB_FILE_NAME);
		IFile dfile= createFile(fProject, DISTURB_FILE_NAME, disturbContent);
		waitForIndexer(fCProject);
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

		dfile.delete(true, npm());
		waitForIndexer(fCProject);
		assertCompletionResults(fCursorOffset, expected2, REPLACEMENT);
	}

	// struct Struct/*cursor*/
	public void testElaboratedTypeSpecifierStruct_bug208710() throws Exception {
		final String[] expected= { "Struct1", "Struct2" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// struct Union/*cursor*/
	public void testElaboratedTypeSpecifierNotStruct_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// struct C/*cursor*/
	public void testElaboratedTypeSpecifierNotStruct2_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union Union/*cursor*/
	public void testElaboratedTypeSpecifierUnion_bug208710() throws Exception {
		final String[] expected= { "Union1", "Union2" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union Struct/*cursor*/
	public void testElaboratedTypeSpecifierNotUnion_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// union C/*cursor*/
	public void testElaboratedTypeSpecifierNotUnion2_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class C/*cursor*/
	public void testElaboratedTypeSpecifierClass_bug208710() throws Exception {
		final String[] expected= { "C1", "C2", "C3" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// class Struct/*cursor*/
	public void testElaboratedTypeSpecifierNotClass_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

    // void test() {
    //    C1::/*cursor*/
	public void testEnumInClass_bug199598() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=199598
		final String[] expected= {
				"E2", "e21", "e22"
		};
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	// class Union/*cursor*/
	public void testElaboratedTypeSpecifierNotClass2_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void func() {float a; a= 1./*cursor*/}
	public void testCompletionInFloatingPointLiteral_193464() throws Exception {
		final String[] expected= new String[0];
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
		CharSequence[] content= getContentsForTest(3);
		createFile(fProject, "header191315.h", content[0].toString());
		createFile(fProject, "source191315.c", content[1].toString());
		createFile(fProject, "source191315.cpp", content[1].toString());
		waitForIndexer(fCProject);
		final String[] expected= {
			"c_linkage()"
		};
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//#include "/*cursor*/
	public void testInclusionProposals_bug113568() throws Exception {
		File tempRoot= new File(System.getProperty("java.io.tmpdir"));
		File tempDir= new File(tempRoot, "cdttest_113568");
		tempDir.mkdir();
		try {
			createIncludeFiles(tempDir, new String[] {
				"h1/inc1.h",
				"h1/sub1/inc11.h",
				"h2/inc2.h"
			});
			String[] expected= {
				"\"inc1.h\"",
				"\"sub1/\"",
				"\"inc2.h\""
			};
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset++, 0, "i");
			expected= new String[] {
				"\"inc1.h\"",
				"\"inc2.h\""
			};
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset, 0, "\"");
			expected= new String[] {
				"\"inc1.h",
				"\"inc2.h"
			};
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			createFile(fProject, "inc113568.h", "");
			expected= new String[] {
				"\"inc1.h",
				"\"inc113568.h",
				"\"inc2.h"
			};
			assertCompletionResults(fCursorOffset, expected, REPLACEMENT);

			getDocument().replace(fCursorOffset - 1, 1, "sub1/");
			expected= new String[] {
				"\"sub1/inc11.h"
			};
			assertCompletionResults(fCursorOffset += 4, expected, REPLACEMENT);

			// bug 278967
			getDocument().replace(fCursorOffset - 5, 5, "../");
			expected= new String[] {
				"\"../h1/",
				"\"../h2/",
			};
			assertCompletionResults(fCursorOffset -= 2, expected, REPLACEMENT);
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
		Set<String> includeDirs= new HashSet<String>();
		for (String file2 : files) {
			File file = new File(dir, file2);
			final File parentFile= file.getParentFile();
			if (parentFile.getName().startsWith("sub")) {
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
			} else if (includeDirs.add(parentFile.getAbsolutePath())) {
				parentFile.mkdirs();
			}
			file.createNewFile();
		}
		TestScannerProvider.sIncludes= includeDirs.toArray(new String[includeDirs.size()]);
	}

	// void test() {
	// int local;
	// switch(loc/*cursor*/
	public void testSwitchStatement() throws Exception {
		final String[] expected= { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// while(loc/*cursor*/
	public void testWhileStatement() throws Exception {
		final String[] expected= { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(loc/*cursor*/
	public void testForStatement1() throws Exception {
		final String[] expected= { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(int i=0;i<loc/*cursor*/
	public void testForStatement2() throws Exception {
		final String[] expected= { "local" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	// void test() {
	// int local;
	// for(int i=0;i<local;loc/*cursor*/
	public void testForStatement3() throws Exception {
		final String[] expected= { "local" };
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
		final String[] expected= { "pThis" };
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
		final String[] expected= { "pIShell" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	void test() {
	//		int alocal, blocal;
	//		if (alocal < b/*cursor*/
	public void testCompletionAfterLessThan_229062() throws Exception {
		final String[] expected= { "blocal" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	enum {enum0, enum1, enum2};
	//	typedef struct {
	//	   int byte1;
	//	   int byte2;
	//	} MYSTRUCT_TYPE;
	//	static const MYSTRUCT_TYPE myArrayOfStructs[] = {{enum/*cursor*/
	public void testCompletionInInitializerList_230389() throws Exception {
		final String[] expected= { "enum0", "enum1", "enum2" };
		assertCompletionResults(expected);
	}

	// void test() {
	//    C2 c2;
	//    c2(1)->iam/*cursor*/
	public void testUserdefinedCallOperator_231277() throws Exception {
		final String[] expected= { "iam1()" };
		assertCompletionResults(expected);
	}

	//  void test() {struct s206450 x; x./*cursor*/
	public void testNestedAnonymousStructs_206450() throws Exception {
		final String[] expected= { "a1", "a2", "u1", "u2", "a4", "b", "s206450" };
		assertCompletionResults(expected);
	}

	//  void test() {_f204758/*cursor*/
	public void testTypedefToAnonymous_204758() throws Exception {
		final String[] expected= { "_f204758(_e204758 x) : void" };
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
		final String[] expected= { "var : float" };
		assertCompletionResults(fCursorOffset, expected, DISPLAY);
	}

	//	struct X {
	//	   typedef int TInt;
	//	};
	//	void main() {
	//		X::T/*cursor*/  // content assist does not propose TInt
	//	}
	public void testNestedTypesInQualifiedNames_255898() throws Exception {
		final String[] expected= { "TInt" };
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
		final String[] expected= { "add()" };
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
		final String[] expected= { "mOne", "Base",
				"Base(int)", "Base(const Base<Helper> &)", "Helper",
				"Helper(void)", "Helper(const Helper &)", "_A_331056", "_B_331056",
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
		final String[] expected= { "ns" };
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
		final String[] expected= { "mOne" };
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
		final String[] expected= { "Helper", "Helper(void)", "Helper(const Helper &)" };
		assertCompletionResults(fCursorOffset, expected, ID);
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
		final String[] expected= { "push_back(const vector<MyType>::value_type & value) : void" };
		assertParameterHint(expected);
	}

	//	using namespace ::_B_331056;
	//	Ref/*cursor*/
	public void testUsingDeclaration_331056() throws Exception {
		final String[] expected= { "Reference" };
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
		final String[] expected= { "BaseMethod(void)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	#define fooBar
	//  #define foo_bar
	//  fB/*cursor*/
	public void testUserMacroSegmentMatch() throws Exception {
		final String[] expected= { "fooBar", "foo_bar" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//  __bVA/*cursor*/
	public void testBuiltinMacroSegmentMatch() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected= { "__builtin_va_arg(ap, type)" };
		assertCompletionResults(fCursorOffset, expected, ID);
	}

	//	namespace N {
	//	  void foo(int);
	//	}
	//	using N::f/*cursor*/
	public void testUsingDeclaration_379631() throws Exception {
		final String[] expected= { "foo;" };
		assertCompletionResults(fCursorOffset, expected, REPLACEMENT);
	}

	//	template <typen/*cursor*/
	public void testTemplateDeclaration_397288() throws Exception {
		final String[] expected= { "typename" };
		assertContentAssistResults(fCursorOffset, 0, expected, true, false, false, REPLACEMENT);
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
		assertContentAssistResults(fCursorOffset, expected, true, ID);
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
		assertContentAssistResults(fCursorOffset, expected, true, ID);
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
		assertContentAssistResults(fCursorOffset, expected, true, ID);
	}

	//	void foo() { Spec/*cursor*/
	public void testTemplateSpecialization() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected = { "Specialization<typename T1, typename T2>" };
		assertContentAssistResults(fCursorOffset, expected, true, DISPLAY);
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
		assertContentAssistResults(fCursorOffset, expected, true, ID);
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
		assertContentAssistResults(fCursorOffset, expected, true, ID);
	}

	//	void foo() { Specialization<int, /*cursor*/
	public void testTemplateArgumentList() throws Exception {
		setCommaAfterFunctionParameter(CCorePlugin.INSERT);
		final String[] expected = { "Specialization<typename T1, typename T2>" };
		assertContentAssistResults(fCursorOffset, expected, true, DISPLAY);
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
		assertContentAssistResults(fCursorOffset, expected, true, DISPLAY);
	}

	//	template<typename T>
	//	class TestTemplateSelfReference : TClass<T>::/*cursor*/
	public void testTemplateSelfReferencePDOM_bug456101() throws Exception {
		final String[] expected = { "NestedClass" };
		assertContentAssistResults(fCursorOffset, expected, true, DISPLAY);
	}

	//	namespace N {
	//	void foo(int);
	//	}
	//	using N::fo/*cursor*/;
	public void testUsingCompletionWithFollowingSemicolon() throws Exception {
		final String[] expected = { "foo" };
		assertContentAssistResults(fCursorOffset, expected, true, REPLACEMENT);
		final String[] expectedInformation = { "null" };
		assertContentAssistResults(fCursorOffset, expectedInformation, true, CONTEXT);
	}

	//	namespace N {
	//	template<typename T> struct Tpl {};
	//	}
	//	using N::Tp/*cursor*/;
	public void testUsingCompletionWithoutTemplateArguments() throws Exception {
		final String[] expected = { "Tpl" };
		assertContentAssistResults(fCursorOffset, expected, true, REPLACEMENT);
	}

	//	namespace N {
	//	template<typename T> struct Tpl {};
	//	}
	//	using N::Tp/*cursor*/
	public void testUsingCompletionWithoutTemplateArgumentsButSemicolon() throws Exception {
		final String[] expected = { "Tpl;" };
		assertContentAssistResults(fCursorOffset, expected, true, REPLACEMENT);
	}

	//	using Alias = C/*cursor*/
	public void testAliasDeclarationCompletion() throws Exception {
		final String[] expectedID = { "C1", "C2", "C3" };
		assertContentAssistResults(fCursorOffset, expectedID, true, ID);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testDefaultFunctionArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(true);
		final String[] expectedDisplay = { "default_argument(int i = 23) : void" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
		final String[] expectedReplacement = { "default_argument()" };
		assertContentAssistResults(fCursorOffset, expectedReplacement, true, REPLACEMENT);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testNoDefaultFunctionArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument(int i) : void" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
	}

	//	void default_argument(int i = 23) {
	//		default_arg/*cursor*/
	//	}
	public void testNoDefaultFunctionParameter() throws Exception {
		setDisplayDefaultedParameters(false);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument() : void" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testDefaultTemplateArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(true);
		final String[] expectedDisplay = { "default_argument<typename T = int>" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
		final String[] expectedReplacement = { "default_argument<>" };
		assertContentAssistResults(fCursorOffset, expectedReplacement, true, REPLACEMENT);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testNoDefaultTemplateArgument() throws Exception {
		setDisplayDefaultedParameters(true);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument<typename T>" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
	}

	//	template<typename T = int>
	//	struct default_argument {};
	//	default_arg/*cursor*/
	public void testNoDefaultTemplateParameter() throws Exception {
		setDisplayDefaultedParameters(false);
		setDisplayDefaultArguments(false);
		final String[] expectedDisplay = { "default_argument<>" };
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
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
		assertContentAssistResults(fCursorOffset, expectedDisplay, true, DISPLAY);
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
}
