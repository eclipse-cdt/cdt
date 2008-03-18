/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
		return createFile(project, SOURCE_FILE_NAME, sourceContent.toString());
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
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, NPM));
		assertCompletionResults(expected);
		
		dfile.delete(true, NPM);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(8000, NPM));
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
		createFile(fProject, "source191315.c", content[0].toString());
		createFile(fProject, "source191315.cpp", content[0].toString());
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
		final String[] expected = { "AStructType", "XStructType", "anEnumerationType", "xEnumerationType"};
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
				"_Pragma(arg)",
				"__CDT_PARSER__",
				"__DATE__",
				"__FILE__",
				"__LINE__",
				"__STDC_HOSTED_",
				"__STDC_VERSION_",
				"__STDC__",
				"__TIME__",
				"__asm__",
				"__builtin_constant_p(exp)",
				"__builtin_va_arg(ap, type)",
				"__complex__",
				"__const",
				"__const__",
				"__extension__",
				"__imag__",
				"__inline__",
				"__null",
				"__real__",
				"__restrict",
				"__restrict__",
				"__signed__",
				"__stdcall",
				"__volatile__",
				"__typeof__"
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

}
