/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.contentassist2;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;

/**
 * Completion tests for plain C.
 * 
 * @since 4.0
 */
public class CompletionTests_PlainC extends AbstractContentAssistTest {

	private static final String HEADER_FILE_NAME = "CompletionTest.h";
	private static final String SOURCE_FILE_NAME = "CompletionTest.c";
	private static final String CURSOR_LOCATION_TAG = "/*cursor*/";
	private static final String INCLUDE_LOCATION_TAG = "/*include*/";
	private static final String DISTURB_FILE_NAME= "DisturbWith.c";
	
	protected int fCursorOffset;
	private IProject fProject;

	//{CompletionTest.h}
	//int gGlobalInt;
	//struct Struct1;
	//struct Struct2;
	//union Union1;
	//union Union2;
	//
	//#define DEBUG 1
	//#define AMacro(x) x+1
	//#define XMacro(x,y) x+y
	//
	//int aVariable;
	//int xVariable;
	//
	//void aFunction();
	//void xFunction();
	//
	//enum anEnumeration {
	//	aFirstEnum,
	//	aSecondEnum, 
	//	aThirdEnum
	//};
	//typedef enum anEnumeration anEnumerationType;
	//
	//enum xEnumeration {
	//	xFirstEnum,
	//	xSecondEnum, 
	//	xThirdEnum
	//};
	//typedef enum xEnumeration xEnumerationType;
	//
	//struct AStruct{
	//	int aStructField;
	//	int xaStructField;
	//};
	//typedef struct AStruct AStructType;
	//
	//struct XStruct{
	//	int xStructField;
	//	int axStructField;
	//};
	//typedef struct XStruct XStructType;
	//
	//void anotherFunction(){
	//   int aLocalDeclaration = 1;
	//}
	//
	//void xOtherFunction(){
	//   int xLocalDeclaration = 1;
	//}
	//#ifdef ANONYMOUS
	//enum {
	//	anonFirstEnum,
	//	anonSecondEnum, 
	//	anonThirdEnum
	//};
	//
	//enum {
	//	xanonFirstEnum,
	//	xanonSecondEnum, 
	//	xanonThirdEnum
	//};
	//
	//int notAnonymous;
	//enum notAnonymousEnum {};
	//struct notAnonymousStruct {};
	//
	//struct {
	//	int anonStructField;
	//};
	//
	//struct {
	//	int xanonStructField;
	//};
	//
	//union {
	//	int anonUnionMember1, anonUnionMember2;
	//};
	//#endif /* ANONYMOUS */
	//
	//#ifdef STRUCT_C1
	//enum E1 {e11, e12};	
	//
	//struct C1_s {
	//  enum E2 {e21, e22};	
	//	
	//  struct C1_s* fMySelf;
	//
	//  int m123;
	//  int m12;
	//  int m13;
	//};
	//typedef struct C1_s C1;
	//extern C1* gfC1();
	//C1* gfC2();
	//C1 gC1, gC2;
	//#endif /* STRUCT_C1 */
	//	struct s206450 {
	//		struct {int a1; int a2;};
	//		union {int u1; char u2;};
	//		struct {int a3;} a4;
	//		int b;
	//	};
	// #ifdef bug204758
	// typedef enum {__nix} _e204758;
	// void _f204758(_e204758 x);
	// #endif
	
	//{DisturbWith.c}
	// int gTemp;
	// void gFunc();
	// typedef struct {
	//    int mem;
	// } gStruct;

	public static Test suite() {
		return BaseTestCase.suite(CompletionTests_PlainC.class, "_");
	}
	
	/**
	 * @param name
	 */
	public CompletionTests_PlainC(String name) {
		super(name, false);
	}

	/*
	 * @see org.eclipse.cdt.ui.tests.text.contentassist2.AbstractContentAssistTest#setUpProjectContent(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected IFile setUpProjectContent(IProject project) throws Exception {
		fProject= project;
		String headerContent= readTaggedComment(HEADER_FILE_NAME);
		StringBuffer sourceContent= getContentsForTest(1)[0];
		int includeOffset= Math.max(0, sourceContent.indexOf(INCLUDE_LOCATION_TAG));
		sourceContent.insert(includeOffset, "#include \""+HEADER_FILE_NAME+"\"\n");
		fCursorOffset= sourceContent.indexOf(CURSOR_LOCATION_TAG);
		assertTrue("No cursor location specified", fCursorOffset >= 0);
		sourceContent.delete(fCursorOffset, fCursorOffset+CURSOR_LOCATION_TAG.length());
		assertNotNull(createFile(project, HEADER_FILE_NAME, headerContent));
		IFile sourceFile= createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
		// re-indexing is necessary to parse the header in context of the source. 
		CCorePlugin.getIndexManager().reindex(fCProject);
		CCorePlugin.getIndexManager().joinIndexer(4000, new NullProgressMonitor());
		return sourceFile;
	}

	protected void assertCompletionResults(String[] expected) throws Exception {
		assertContentAssistResults(fCursorOffset, expected, true, AbstractContentAssistTest.COMPARE_ID_STRINGS);
	}
	
	//void test() {
    //  int myvar;
    //  (my/*cursor*/
	public void testLocalVariableAfterOpeningParen_Bug180885() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

	//void test() {
    //  int myvar;
    //  int x = my/*cursor*/
	public void testLocalVariableInAssignment() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

	//void test() {
    //  int myvar;
    //  my/*cursor*/
	public void testLocalVariableOnLHS() throws Exception {
		final String[] expected= {
				"myvar"
		};
		assertCompletionResults(expected);
	}

	// void test() {
	//    g/*cursor*/
	public void testBindingsWithoutDeclaration() throws Exception {
		// gStruct: fix for 214146, type from a source file is not proposed.
		final String[] expected= {
			"gGlobalInt", "gTemp", "gFunc(void)", 
		};
		final String[] expected2= {
				"gGlobalInt"
			};
		String disturbContent= readTaggedComment(DISTURB_FILE_NAME);
		IFile dfile= createFile(fProject, DISTURB_FILE_NAME, disturbContent);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, npm()));
		assertCompletionResults(expected);
		
		dfile.delete(true, npm());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, npm()));
		assertCompletionResults(expected2);		
	}
	
	//// to_be_replaced_
	//void gfunc(){aNew/*cursor*/
	public void testGlobalVariableBeforeSave_Bug180883() throws Exception {
		String replace=   "// to_be_replaced_";
		String globalVar= "int aNewGlobalVar;";
		IDocument doc= getDocument();
		int idx= doc.get().indexOf(replace);
		doc.replace(idx, replace.length(), globalVar);

		final String[] expected= {
				"aNewGlobalVar"
		};
		assertCompletionResults(expected);
	}

	// static int staticVar197990;
	// void gFunc() {
	//   stat/*cursor*/
	public void testStaticVariables_Bug197990() throws Exception {
		final String[] expected= {
				"staticVar197990"
		};
		assertCompletionResults(expected);
	}
	
	// struct Struct/*cursor*/
	public void testElaboratedTypeSpecifierStruct_bug208710() throws Exception {
		final String[] expected= { "Struct1", "Struct2" };
		assertCompletionResults(expected);
	}
	
	// struct Union/*cursor*/
	public void testElaboratedTypeSpecifierNotStruct_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(expected);
	}
	
	// union Union/*cursor*/
	public void testElaboratedTypeSpecifierUnion_bug208710() throws Exception {
		final String[] expected= { "Union1", "Union2" };
		assertCompletionResults(expected);
	}
	
	// union Struct/*cursor*/
	public void testElaboratedTypeSpecifierNotUnion_bug208710() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(expected);
	}
	
	// void func() {float a; a= 1./*cursor*/}
	public void testCompletionInFloatingPointLiteral_Bug193464() throws Exception {
		final String[] expected= new String[0];
		assertCompletionResults(expected);
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
		StringBuffer[] content= getContentsForTest(3);
		createFile(fProject, "header191315.h", content[0].toString());
		createFile(fProject, "source191315.c", content[1].toString());
		createFile(fProject, "source191315.cpp", content[1].toString());
		IFile dfile= createFile(fProject, "header191315.h", content[0].toString());
		TestSourceReader.waitUntilFileIsIndexed(CCorePlugin.getIndexManager().getIndex(fCProject), dfile, 8000);
		final String[] expected= {
			"c_linkage(void)"
		};
		assertCompletionResults(expected);
	}

	//#define ANONYMOUS
	///*include*/
	///*cursor*/
	public void testAnonymousTypes() throws Exception {
		final String[] expected = { "AStructType", "XStructType", "anEnumerationType", "xEnumerationType" };
		assertCompletionResults(expected);
	}
	
	//void foo ( a/*cursor*/
	public void testArgumentTypes_Prefix() throws Exception {
		final String[] expected = { "AMacro(x)", "AStructType", "anEnumerationType" };
		assertCompletionResults(expected);
	}

	//struct aThirdStruct {
	//   int x;
	//   /*cursor*/
	//};
	public void testFieldType_NoPrefix() throws Exception {
		final String[] expected = { "AStructType", "XStructType", "anEnumerationType", "xEnumerationType" };
		assertCompletionResults(expected);
	}

	//struct aThirdStruct {
	//   int x;
	//   a/*cursor*/
	//};
	public void testFieldType_Prefix() throws Exception {
		final String[] expected = { "AMacro(x)", "AStructType", "anEnumerationType" };
		assertCompletionResults(expected);
	}
	
	//#ifdef /*cursor*/
	public void testMacroRef_NoPrefix() throws Exception {
		final String[] expected = {
				"AMacro(x)",
				"DEBUG",
				"XMacro(x, y)",
				"__CDT_PARSER__",
				"__DATE__",
				"__FILE__",
				"__LINE__",
				"__STDC_HOSTED__",
				"__STDC_VERSION__",
				"__STDC__",
				"__TIME__",
				"__builtin_constant_p(exp)",
				"__builtin_va_arg(ap, type)",
				"__builtin_offsetof(T, m)",
				"__builtin_types_compatible_p(x, y)",
				"__complex__",
				"__extension__",
				"__imag__",
				"__null",
				"__offsetof__(x)",
				"__real__",
				"__stdcall",
				"__thread",
		};
		assertCompletionResults(expected);
	}

	//#ifdef D/*cursor*/
	public void testMacroRef_Prefix() throws Exception {
		final String[] expected = { "DEBUG" };
		assertCompletionResults(expected);
	}
	
	//void fooFunction()
	//{
	//    AStructType* c;
	//    c->/*cursor*/
	//}
	public void testMemberReference_Arrow_NoPrefix() throws Exception {
		final String[] expected = { 
				"aStructField", "xaStructField",
		};
		assertCompletionResults(expected);
	}

	//void main() {
	//    struct AStruct a, *ap;
	//    ap->/*cursor*/
	//}
	public void testMemberReference_Arrow_NoPrefix2() throws Exception {
		final String[] expected = { 
				"aStructField", "xaStructField",
		};
		assertCompletionResults(expected);
	}

	//void fooFunction()
	//{
	//    AStructType* c;
	//    c->a/*cursor*/
	//}
	public void testMemberReference_Arrow_Prefix() throws Exception {
		final String[] expected = { 
				"aStructField",
		};
		assertCompletionResults(expected);
	}

	//AStructType* foo();
	//
	//void fooFunction()
	//{
	//    foo()->a/*cursor*/
	//}
	public void testMemberReference_Arrow_Prefix2() throws Exception {
		final String[] expected = { 
				"aStructField",
		};
		assertCompletionResults(expected);
	}

	//void fooFunction()
	//{
	//    AStructType c;
	//    c./*cursor*/
	//}
	public void testMemberReference_Dot_NoPrefix() throws Exception {
		final String[] expected = { 
				"aStructField", "xaStructField",
		};
		assertCompletionResults(expected);
	}

	//void fooFunction()
	//{
	//    AStructType c;
	//    c.a/*cursor*/
	//}
	public void testMemberReference_Dot_Prefix() throws Exception {
		final String[] expected = { 
				"aStructField",
		};
		assertCompletionResults(expected);
	}

	//typedef int myType;
	//
	//m/*cursor*/
	public void testTypeDef_Prefix() throws Exception {
		final String[] expected = { 
				"myType",
		};
		assertCompletionResults(expected);
	}

	//void fooFunction(int x)
	//{
	//    /*cursor*/
	//}
	public void testSingleName_Function_NoPrefix() throws Exception {
		final String[] expected = { 
				"x",
				"aVariable",
				"xVariable",
				"aFunction(void)",
				"anotherFunction(void)",
				"fooFunction(int)",
				"xFunction(void)",
				"xOtherFunction(void)",
				"anEnumerationType",
				"xEnumerationType",
				"AStructType",
				"XStructType",
				"aFirstEnum",
				"aSecondEnum",
				"aThirdEnum",
				"xFirstEnum",
				"xSecondEnum",
				"xThirdEnum",
				"gGlobalInt"
		};
		assertCompletionResults(expected);
	}

	//void fooFunction(int x)
	//{
	//    a/*cursor*/
	//}
	public void testSingleName_Function_Prefix() throws Exception {
		final String[] expected = { 
				"AMacro(x)",
				"AStructType",
				"aVariable",
				"aFunction(void)",
				"anotherFunction(void)",
				"anEnumerationType",
				"aFirstEnum",
				"aSecondEnum",
				"aThirdEnum",
		};
		assertCompletionResults(expected);
	}
	
	//void fooFunction(int x)
	//{
	//    int y = /*cursor*/
	//}
	public void testSingleName_Assignment_NoPrefix() throws Exception {
		final String[] expected = {
				"x",
				"y",
				"aVariable",
				"xVariable",
				"aFunction(void)",
				"anotherFunction(void)",
				"fooFunction(int)",
				"xFunction(void)",
				"xOtherFunction(void)",
				"anEnumerationType",
				"xEnumerationType",
				"AStructType",
				"XStructType",
				"aFirstEnum",
				"aSecondEnum",
				"aThirdEnum",
				"xFirstEnum",
				"xSecondEnum",
				"xThirdEnum",
				"gGlobalInt"
		};
		assertCompletionResults(expected);
	}
	
	//void foo(int x)
	//{
	//    int y = AM/*cursor*/
	//}
	public void testSingleName_Assignment_Prefix() throws Exception {
		final String[] expected = {
				"AMacro(x)"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void gfunc() {C1 v; v.m/*cursor*/
	public void testLocalVariable() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void gfunc() {C1 v; v.fMySelf->m/*cursor*/
	public void testLocalVariable_MemberVariable() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void gfunc() {gfC1().m/*cursor*/
	public void testGlobalFunction() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {gC/*cursor*/
	public void testGlobalVariables_GlobalScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void foo() {gC/*cursor*/
	public void testGlobalVariables_FunctionScope() throws Exception {
		final String[] expected= {
				"gC1", "gC2"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//typedef struct {
	//  enum E2 {e21, e22};	
	//	
	//  C2* fMySelf;
	//
	//  int m123;
	//  int m12;
	//  int m13;
	//} C2;
	//void f() {C2* cLocal1; while(true) {C1* cLocal2; cL/*cursor*/
	public void testLocalVariables_FunctionScope() throws Exception {
		final String[] expected= {
				"cLocal1", "cLocal2"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* cLocal1; cLocal1->f/*cursor*/
	public void testDataMembers_FunctionScope() throws Exception {
		final String[] expected= {
				"fMySelf"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {gf/*cursor*/
	public void testGlobalFunctions_FunctionScope() throws Exception {
		final String[] expected= {
				"gfC1(void)", "gfC2(void)"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//typedef struct {} C2;
	//typedef union {} C3;
	//void f() {C/*cursor*/
	public void testTypes_FunctionScope() throws Exception {
		final String[] expected= {
				"C1", "C2", "C3"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {e/*cursor*/
	public void testEnums_FunctionScope() throws Exception {
		final String[] expected= {
				"e11", "e12", "e21", "e22"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1 c; (&c)->m/*cursor*/
	public void testAddressOf() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; (*c).m/*cursor*/
	public void testDereferencingOperator1() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1** c; (**c).m/*cursor*/
	public void testDereferencingOperator2() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1** c; (*c)->m/*cursor*/
	public void testDereferencingOperator3() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; c[0].m/*cursor*/
	public void testArrayAccessOperator1() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1** c; c[0][1].m/*cursor*/
	public void testArrayAccessOperator2() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1** c; c[0]->m/*cursor*/
	public void testArrayAccessOperator3() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; (&c[0])->m/*cursor*/
	public void testArrayAccessOperator4() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {void* c; ((C1*)c)->m/*cursor*/
	public void testCasts1() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void g(int a) {}; void f() {void* c; g(((C1*)c)->m/*cursor*/
	public void testCasts2() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; c++->m/*cursor*/
	public void testPointerArithmetic1() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; (*++c).m/*cursor*/
	public void testPointerArithmetic2() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1* c; c--->m/*cursor*/
	public void testPointerArithmetic3() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1 c; (&c+1)->m/*cursor*/
	public void testPointerArithmetic4() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {C1 c; (&c-1)->m/*cursor*/
	public void testPointerArithmetic5() throws Exception {
		final String[] expected= {
				"m123", "m12", "m13"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//void f() {int localVar=0; if (*cond && somefunc(&local/*cursor*/
	public void testNestedCalls() throws Exception {
		final String[] expected= {
				"localVar"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//int a[] = {1,2}; void f(int _0306_b) {_0306_b/*cursor*/
	public void testCuttingInput1() throws Exception {
		final String[] expected= {
				"_0306_b"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//int a[] = {1,2}; void f(int b) {int _0306_b[] = {2,3}; _0306_b/*cursor*/
	public void testCuttingInput2() throws Exception {
		final String[] expected= {
				"_0306_b"
		};
		assertCompletionResults(expected);
	}

	//#define STRUCT_C1
	///*include*/
	//enum EnumType function() {int _031209_v; _031209/*cursor*/
	public void testDisturbingMacros() throws Exception {
		final String[] expected= {
				"_031209_v"
		};
		assertCompletionResults(expected);
	}
	
	// void test() {
	// int local;
	// switch(loc/*cursor*/
	public void testSwitchStatement() throws Exception {
		final String[] expected= {
				"local"
		};
		assertCompletionResults(expected);
	}
	
	// void test() {
	// int local;
	// while(loc/*cursor*/
	public void testWhileStatement() throws Exception {
		final String[] expected= {
				"local"
		};
		assertCompletionResults(expected);
	}

	// void test() {
	// int local;
	// for(loc/*cursor*/
	public void testForStatement1() throws Exception {
		final String[] expected= {
				"local"
		};
		assertCompletionResults(expected);
	}

	// void test() {
	// int local;
	// for(int i=0;i<loc/*cursor*/
	public void testForStatement2() throws Exception {
		final String[] expected= {
				"local"
		};
		assertCompletionResults(expected);
	}

	// void test() {
	// int local;
	// for(int i=0;i<local;loc/*cursor*/
	public void testForStatement3() throws Exception {
		final String[] expected= {
				"local"
		};
		assertCompletionResults(expected);
	}

	//	#define INIT_PTR(PtrName)   (PtrName) = 0;
	//	struct CCApp {
	//	   int pIShell;
	//	};
	//
	//	int main(void) {
	//	   struct CCApp *pThis = 0;
	//	   INIT_PTR(pTh/*cursor*/);
	//	}
	public void testCompletionInMacroArguments1_Bug200208() throws Exception {
		final String[] expected= {"pThis"};
		assertCompletionResults(expected);
	}

	//	#define INIT_PTR(PtrName)   (PtrName) = 0;
	//	#define COPY_PTR(pTarget, pSource)   (pTarget) = (pSource)
	//
	//	struct CCApp {
	//	   int pIShell;
	//	};
	//
	//	int main(void) {
	//	   struct CCApp *pThis = 0;
	//
	//	   INIT_PTR(pThis);
	//     COPY_PTR(pThis->pIShell, pThis->pI/*cursor*/)
	//	}
	public void testCompletionInMacroArguments2_Bug200208() throws Exception {
		final String[] expected= {"pIShell"};
		assertCompletionResults(expected);
	}
	
	//	enum {enum0, enum1, enum2};
	//	typedef struct {
	//	   int byte1;
	//	   int byte2;
	//	} MYSTRUCT_TYPE;
	//	static const MYSTRUCT_TYPE myArrayOfStructs[] = {{enum/*cursor*/
	public void testCompletionInInitializerList_Bug230389() throws Exception {
		final String[] expected= {"enum0", "enum1", "enum2"};
		assertCompletionResults(expected);
	}
	
	//  void test() {struct s206450 x; x./*cursor*/
	public void testNestedAnonymousStructs_Bug206450() throws Exception {
		final String[] expected= {"a1", "a2", "u1", "u2", "a4", "b"};
		assertCompletionResults(expected);
	}

	// #define bug204758
	///*include*/
	//  void test() {_f204758/*cursor*/
	public void testTypedefToAnonymous_Bug204758() throws Exception {
		final String[] expected= {"_f204758(_e204758)"};
		assertCompletionResults(expected);
	}
}
