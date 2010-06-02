/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests;

import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;


public class XlcExtensionsTest extends XlcTestBase {

	public XlcExtensionsTest() {
	}
	
	public XlcExtensionsTest(String name) {
		super(name);
	}
	
	public void testHexadecimalFloatingPointLiterals() throws Exception {
		String code =
			"int test() {        \n"+
			"	 0x0A2B.0FDp+2f; \n"+
			"	 0X12D.p-44F;    \n"+
			"	 0xBACP+2L;      \n"+
			"}\n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testFuncPredefinedIdentifier() {
		String code =
			"void test() {       \n" +
            "     __func__;      \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testStringConcatenation() {
		String code =
			"void test() {       \n" +
            "     \"hello \" \"there\";   \n" +
            "     \"hello \" L\"there\";  \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testLongLong() {
		String code =
			"void test() {       \n" +
            "     long long x;   \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testComplex() {
		String code =
			"void test() {       \n" +
            "     float _Complex x;   \n" +
            "     double _Complex y;   \n" +
            "     long double _Complex z;   \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testBool() {
		String code =
			"_Bool f(int a, int b) { \n" +
            "    return a==b; \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
	}
	
	public void testTrailingCommaInEnum() {
		String code =
			"void test() {       \n" +
            "     enum grain { oats, wheat, barley, corn, rice, };   \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	

	public void testNonLValueArraySubscript() {
		String code =
			"struct trio{int a[3];};  \n" +
	        "struct trio f();         \n" +
	        "foo (int index)          \n" +
	        "{                        \n" +
	        "   return f().a[index];  \n" +
	        "}  \n";             

		parse(code, getCLanguage(), true);
	}
	
	public void testStaticArrayIndices() {
		String code =
			"void test() {       \n" +
            "     void foo1(int arr [static 10]);       \n" +
            "     int i = 10;                           \n" +
            "     void foo2(int arr [static const i]);  \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
	}
	
	public void testFunctionLikeMacrosVariableArguments() {
		String code =
			"#define debug(...)   fprintf(stderr, __VA_ARGS__) \n" +
			"int test() { \n" +
            "    debug(\"flag\");  \n" +
		    " } \n";

		parse(code, getCLanguage(), false);
	}
	
	public void testFunctionLikeMacrosEmptyArgument() {
		String code =
			"#define SUM(a,b,c) a + b + c \n" +
			"int test() { \n" +
            "   SUM(1,,3);  \n" +
		    " } \n";

		parse(code, getCLanguage(), true);
	}
	
	public void testPredefinedMacroNamesC() {
		String code =
			"void test() {         \n" +
            "    __DATE__;         \n" +
            "    __FILE__;         \n" +
            "    __LINE__;         \n" +
            "    __STDC_HOSTED__;  \n" +
            "    __STDC_VERSION__; \n" +
            "    __TIME__;         \n" +
            "}  \n";

		parse(code, getCLanguage(), true);
	}
	
	public void testPredefinedMacroNamesCpp() {
		String code =
			"void test() {      \n" +
            "    __DATE__;      \n" +
            "    __FILE__;      \n" +
            "    __LINE__;      \n" +
            "    __TIME__;      \n" +
          //  "    __cplusplus;   \n" +
            "}  \n";

		parse(code, getCPPLanguage(), true);
	}
	
	
	public void testCompoundLiterals() {
		String code =
			"void test() {       \n" +
            "     drawline((struct point){6,7});   \n" +
            " }  \n";

		parse(code, getCLanguage(), false);
	}
	
	

	public void testPragma() {
		String code =
			"void test() {       \n" +
            "    _Pragma ( \"pack(full)\" )   \n" +
            " }  \n";

		parse(code, getCLanguage(), true);
	}
	
	
	public void testStandardPragmas() {
		String code =
			"#pragma STDC FP_CONTRACT ON           \n" +
			"#pragma STDC FENV_ACCESS OFF          \n" +
			"#pragma STDC CX_LIMITED_RANGE DEFAULT \n";

		parse(code, getCLanguage(), true);
	}
	
	public void testLineDirective() {
		String code =
			"#define LINE200 200 \n" +
			"#line 100           \n" +
			"#line LINE200       \n";

		parse(code, getCLanguage(), true);
	}
	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=228826
	 * http://publib.boulder.ibm.com/infocenter/comphelp/v101v121/index.jsp?topic=/com.ibm.xlcpp101.aix.doc/language_ref/restrict_type_qualifier.html
	 * 
	 * TODO Need a properties page so that things like this can be configured by the user.
	 */
	public void testRestrictC() {
		String code =
			"void foo(int n, int * restrict  a, int * __restrict b, int * __restrict__  c) {} ";

		parse(code, getCLanguage(), true);
	}
	
	public void testRestrictCPPOn() {
		String code =
			"void foo(int n, int * restrict  a, int * __restrict b, int * __restrict__  c) {} ";

		parse(code, getCPPLanguage(), true);
	}
	public void testRestrictCPPOff() {
		XlcLanguagePreferences.setWorkspacePreference(XlcPref.SUPPORT_RESTRICT_IN_CPP, String.valueOf(false));
		String code =
			"void restrict(); \n " +
			"void foo(int n, int * __restrict b, int * __restrict__  c) {} ";

		parse(code, getCPPLanguage(), true);
	}
	
	
	
	public void testUTFLiterals() {
		String code =
			"void test() { \n " +
			"  u\"ucs characters \\u1234 and \\u8180 \"; \n " +
			"  U\"ucs characters \\u1234 and \\u8180 \"; \n " +
			"  U\"concatenation \\u1234 \"  u\"is allowed \\u8180 \"; \n " +
			"  u'\\u1234'; \n " +
		    "  U'\\u1234'; \n " +
		    "}";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	
	public void testFloatingPointTypes() {
		String code =
			"  _Decimal32 x = 22.2df; \n " +
			"  _Decimal64 y = 33.3dd; \n " +
			"  _Decimal128 z = 33.3dl; \n ";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
	
	public void testVariableLengthArrays() {
		String code =
			"double maximum1(int n, int m, double a[n][m]);\n" +
			"double maximum2(int n, int m, double a[*][*]);\n" +
			"double maximum3(int n, int m, double a[ ][*]);\n" +
			"double maximum4(int n, int m, double a[ ][m]);\n";
		
		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true); // xlc supports this in C++
	}
	
	public void testV11Attributes() {
		String code =
			"#define __inline__ __inline__ __attribute__((gnu_inline)) \n" +
			
			"static int w() __attribute__ ((weakref (\"y\")));\n" + 
			/* is equivalent to... */
			"static int x() __attribute__ ((weak, weakref, alias (\"y\")));\n" +
			/* and to... */
			"static int y() __attribute__ ((weakref));\n" +
			"static int z() __attribute__ ((alias (\"y\"))); \n" +
		
			"int foo() __attribute__((gnu_inline)); \n" +
			"static inline __attribute__((gnu_inline)) int ins (int *a){ \n" +
			"   (*a)++; \n" +
			"} \n" +
			"inline __attribute__((gnu_inline)) int inc (int *a){ \n" +
			"   (*a)++; \n" +
			"} ";

		parse(code, getCLanguage(), true);
		parse(code, getCPPLanguage(), true);
	}
}
