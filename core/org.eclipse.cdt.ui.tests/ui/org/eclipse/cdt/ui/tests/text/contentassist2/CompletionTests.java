/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * A collection of code completion tests.
 *
 * @since 4.0
 */
public class CompletionTests extends AbstractContentAssistTest {

	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.cpp";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";
	
	protected int fCursorOffset;

//{CompletionTest.h}
//class C1;
//class C2;
//class C3;
//
//extern C1* gC1;
//C2* gC2 = 0;
//
//extern C1* gfC1();
//C2* gfC2();
//	
//enum E1 {e11, e12};	
//
//class C1 {
//public:
//		enum E2 {e21, e22};	
//	
//		C1* fMySelf;
//		void iam1();
//
//		C1* m123();
//		C1* m12();
//		C1* m13();
//
//private:
//		void m1private();
//};
//typedef C1 T1;
//
//
//class C2 : public T1 {
//public:
//		C2* fMySelf;
//		void iam2();
//
//		C2* m123();
//		C2* m12();
//		C2* m23();
//
//private:
//		void m2private();
//};
//typedef C2 T2;
//
//
//class C3 : public C2 {
//public:
//		C3* fMySelf;
//		void iam3();
//
//		C3* m123();
//		C3* m13();
//	
//  	template<typename T> T tConvert();
//private:
//		void m3private();
//};
//typedef C3 T3;
//
//namespace ns {
//   const int NSCONST= 1;
//   class CNS {
//	      void mcns();
//   };
//};
//template <class T> class TClass {
//	T fTField;
//public:
//	TClass(T tArg) : fTField(tArg) {
//	}
//	T add(T tOther) {
//		return fTField + tOther;
//	}
//};
//// bug 109480
//class Printer
//{
//public:
//	static void InitPrinter(unsigned char port);
//private:
//	//Storage for port printer is on
//	static unsigned char port;
//protected:
//};

	public CompletionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return BaseTestCase.suite(CompletionTests.class, "_");
	}
	
	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractCompletionTest#setUpProjectContent(org.eclipse.core.resources.IProject)
	 */
	protected IFile setUpProjectContent(IProject project) throws Exception {
		String headerContent= readTaggedComment(HEADER_FILE_NAME);
		StringBuffer sourceContent= getContentsForTest(1)[0];
		sourceContent.insert(0, "#include \""+HEADER_FILE_NAME+"\"\n");
		fCursorOffset= sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset+CURSOR_LOCATION_TAG.length());
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
	}

	protected void assertCompletionResults(int offset, String[] expected, int compareType) throws Exception {
		assertContentAssistResults(offset, expected, true, compareType);
	}
	
	//void gfunc() {C1 v; v.m/*cursor*/
	public void _testLocalVariable() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void gfunc() {C1 v; v.fMySelf.m/*cursor*/
	public void _testLocalVariable_MemberVariable() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void gfunc() {C1 v; v.m12().m/*cursor*/
	public void _testLocalVariable_MemberFunction() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void gfunc() {gfC1().m/*cursor*/
	public void _testGlobalFunction() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C1::self() {m/*cursor*/
	public void testOwnMember() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m1private(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C1::self() {this->m/*cursor*/
	public void testOwnMemberViaThis() throws Exception {
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)", "m1private(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void gfunc() {try{int bla;}catch(C1 v) {v.fMySelf.m/*cursor*/
	public void _testCatchBlock1() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void gfunc() {try{int bla;}catch(C2 c){} catch(C1 v) {v.fMySelf.m/*cursor*/
	public void _testCatchBlock2() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {gC/*cursor*/
	public void testGlobalVariables_GlobalScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C1::f() {gC/*cursor*/
	public void testGlobalVariables_MethodScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_GlobalScope() throws Exception {
		final String[] expected= {
				"cLocal1", "cLocal2"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C2::f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_MethodScope() throws Exception {
		final String[] expected= {
				"cLocal1", "cLocal2"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C2* cLocal1; cLocal1.f/*cursor*/
	public void testDataMembers_GlobalScope() throws Exception {
		final String[] expected= {
				"fMySelf"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C2::f() {while(true) {f/*cursor*/
	public void testDataMembers_MethodScope() throws Exception {
		final String[] expected= {
				"fMySelf"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {gf/*cursor*/
	public void testGlobalFunctions_GlobalScope() throws Exception {
		final String[] expected= {
				"gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C3::f() {gf/*cursor*/
	public void testGlobalFunctions_MethodScope() throws Exception {
		final String[] expected= {
				"gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1* l1; l1.m/*cursor*/
	public void _testMethods_GlobalScope() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C3::f() {m/*cursor*/
	public void _testMethods_MethodScope() throws Exception {
		// fails because of additional m1private(void)
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172305
		final String[] expected= {
				"m123(void)", "m12(void)", "m13(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C/*cursor*/
	public void testTypes_GlobalScope() throws Exception {
		final String[] expected= {
				"C1", "C2", "C3"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C2::f() {T/*cursor*/
	public void testTypes_MethodScope() throws Exception {
		final String[] expected= {
				"T1", "T2", "T3", "TClass"
		};
		assertCompletionResults(fCursorOffset, expected, 
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//namespace ns {void nsfunc(){C/*cursor*/
	public void testTypes_NamespaceScope() throws Exception {
		final String[] expected= {
				"C1", "C2", "C3", "CNS"	
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//namespace ns {void gfunc(){::C/*cursor*/
	public void testTypes_GlobalQualification() throws Exception {
		final String[] expected= {
				"C1", "C2", "C3"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void f() {e/*cursor*/
	public void testEnums_GlobalScope() throws Exception {
		final String[] expected= {
				"e11", "e12", "E1"
		};
		assertCompletionResults(fCursorOffset, expected, 
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void C3::f() {e/*cursor*/
	public void testEnums_MethodScope() throws Exception {
		final String[] expected= {
				"e11", "e12", "e21", "e22", "E1", "E2"
		};
		assertCompletionResults(fCursorOffset, expected, 
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.C/*cursor*/
	public void testQualificationForAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"C3", "C2", "C1"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C2* l1; l1.C/*cursor*/
	public void testQualificationForAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"C2", "C1"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.C3::fMySelf.iam/*cursor*/
	public void testQualifiedAccess1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"iam3(void)", "iam2(void)", "iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.C2::fMySelf.iam/*cursor*/
	public void testQualifiedAccess2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"iam2(void)", "iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.C1::fMySelf.iam/*cursor*/
	public void testQualifiedAccess3() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.T3::fMySelf.iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier1() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"iam3(void)", "iam2(void)", "iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.T2::fMySelf.iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier2() throws Exception {
		// TLETODO ordering is significant here (currently ignored)
		final String[] expected= {
				"iam2(void)", "iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C3* l1; l1.T1::fMySelf.iam/*cursor*/
	public void testQualifiedAccess_TypedefAsQualifier3() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1().iam/*cursor*/
	public void testTemporaryObject() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1 c; (&c)->iam/*cursor*/
	public void testAddressOf() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1* c; (*c).iam/*cursor*/
	public void testDereferencingOperator1() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1** c; (**c).iam/*cursor*/
	public void testDereferencingOperator2() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1** c; (*c)->iam/*cursor*/
	public void testDereferencingOperator3() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1* c; c[0].iam/*cursor*/
	public void testArrayAccessOperator1() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1** c; c[0][1].iam/*cursor*/
	public void testArrayAccessOperator2() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1** c; c[0]->iam/*cursor*/
	public void testArrayAccessOperator3() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1* c; (&c[0])->iam/*cursor*/
	public void testArrayAccessOperator4() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {void* c; ((C1*)c)->iam/*cursor*/
	public void testCasts1() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void g(int a) {}; void f() {void* c; g(((C1*)c)->iam/*cursor*/
	public void testCasts2() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {C1* c; c++->iam/*cursor*/
	public void testPointerArithmetic1() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1* c; (*++c).iam/*cursor*/
	public void testPointerArithmetic2() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1* c; c--->iam/*cursor*/
	public void testPointerArithmetic3() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1 c; (&c+1)->iam/*cursor*/
	public void testPointerArithmetic4() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void f() {C1 c; (&c-1)->iam/*cursor*/
	public void testPointerArithmetic5() throws Exception {
		final String[] expected= {
				"iam1(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//void f() {int localVar=0; if (*cond && somefunc(&local/*cursor*/
	public void testNestedCalls() throws Exception {
		final String[] expected= {
				"localVar"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//int a[] = {1,2}; void f(int _0306_b) {_0306_b/*cursor*/
	public void testCuttingInput1() throws Exception {
		final String[] expected= {
				"_0306_b"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//int a[] = {1,2}; void f(int b) {int _0306_b[] = {2,3}; _0306_b/*cursor*/
	public void testCuttingInput2() throws Exception {
		final String[] expected= {
				"_0306_b"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//enum EnumType function() {int _031209_v; _031209/*cursor*/
	public void testDisturbingMacros() throws Exception {
		final String[] expected= {
				"_031209_v"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}

	//namespace ns {void x() {NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember1() throws Exception {
		final String[] expected= {
				"NSCONST"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	//void ns::CNS::mcns(){NSCO/*cursor*/
	public void testAccessToNamespaceFromClassMember2() throws Exception {
		final String[] expected= {
				"NSCONST"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//#i/*cursor*/
	public void testCompletePreprocessorDirective() throws Exception {
		final String[] expected= {
				"#if", "#ifdef", "#ifndef", "#include"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void gfunc(){TClass<int> t(0); t.a/*cursor*/
	public void testTemplateClassMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected= {
				"add(int)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void gfunc(){C3 c3; c3.t/*cursor*/
	public void testTemplateMethod() throws Exception {
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=172436
		final String[] expected= {
				"tConvert(void)"
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//using namespace ns;void gfunc(){NSC/*cursor*/
	public void testUsingDirective() throws Exception {
		final String[] expected= {
				"NSCONST"	
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void gfunc(){n/*cursor*/
	public void testAutoColons() throws Exception {
		final String[] expected= {
				"ns::"	
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_REP_STRINGS);
	}
	
	//using namespace /*cursor*/
	public void testAutoColons2() throws Exception {
		final String[] expected= {
				"ns"	
		};
		assertCompletionResults(fCursorOffset, expected,
				AbstractContentAssistTest.COMPARE_REP_STRINGS);
	}

	//// to_be_replaced_
	//void gfunc(){aNew/*cursor*/
	public void testGlobalVariableBeforeSave_Bug180883() throws Exception {
		String replace=   "// to_be_replaced_";
		String globalVar= "int aNewGlobalVar;";
		IDocument doc= getDocument();
		int idx= doc.get().indexOf(replace);
		doc.replace(idx, replace.length(), globalVar);

		// succeeds when buffer is saved
//		fEditor.doSave(new NullProgressMonitor());
//		EditorTestHelper.joinBackgroundActivities((AbstractTextEditor)fEditor);

		final String[] expected= {
				"aNewGlobalVar"	
		};
		assertCompletionResults(fCursorOffset, expected, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void Printer::InitPrinter(unsigned char port) {
	//	Printer::/*cursor*/
	public void testPrivateStaticMember_Bug109480() throws Exception {
		final String[] expected= {
				"InitPrinter()", "port"	
		};
		assertCompletionResults(fCursorOffset, expected, AbstractContentAssistTest.COMPARE_REP_STRINGS);
	}
}
