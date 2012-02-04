/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.LineRange;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.ui.tests.BaseUITestCase;


import org.eclipse.cdt.internal.ui.editor.CDocumentSetupParticipant;
import org.eclipse.cdt.internal.ui.editor.IndentUtil;

/**
 * Tests for the CIndenter.
 *
 * @since 4.0
 */
public class CIndenterTest extends BaseUITestCase {
	private HashMap<String, String> fOptions;
	private Map<String, String> fDefaultOptions;

	public static TestSuite suite() {
		return suite(CIndenterTest.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fDefaultOptions= DefaultCodeFormatterOptions.getDefaultSettings().getMap();
		fOptions= new HashMap<String, String>();
	}

	@Override
	protected void tearDown() throws Exception {
		CCorePlugin.setOptions(new HashMap<String, String>(fDefaultOptions));
		super.tearDown();
	}

	protected void assertIndenterResult() throws Exception {
		CCorePlugin.setOptions(fOptions);
		StringBuilder[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		IDocument document= new Document(before);
		String expected= contents[1].toString();
		new CDocumentSetupParticipant().setup(document);
		int numLines = document.getNumberOfLines();
		if (document.getLineLength(numLines - 1) == 0) {
			numLines--;  // Exclude an empty line at the end.
		}
		IndentUtil.indentLines(document, new LineRange(0, numLines), null, null);
		assertEquals(expected, document.get());
	}

	//int array[] =
	//{
	//	  sizeof(x)
	//	  , 1
	//};

	//int array[] =
	//{
	//		sizeof(x)
	//		, 1
	//};
	public void testArrayInitializer() throws Exception {
		assertIndenterResult();
	}

	//foo(arg,
	//"string");

	//foo(arg,
	//		"string");
	public void testStringLiteralAsLastArgument_1_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//a::foo(arg,
	//"string");

	//a::foo(arg,
	//		"string");
	public void testStringLiteralAsLastArgument_2_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//a::foo(arg,
	//		"string");

	//a::foo(arg,
	//		"string");
	public void testStringLiteralAsLastArgument_3_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo->bar();
	//dontIndent();
	
	//if (1)
	//	foo->bar();
	//dontIndent();
	public void testIndentationAfterArrowOperator_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo>>bar;
	//  dontIndent();
	
	//if (1)
	//	foo>>bar;
	//dontIndent();
	public void testIndentationAfterShiftRight_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//if (1)
	//foo >= bar();
	//  dontIndent();
	
	//if (1)
	//	foo >= bar();
	//dontIndent();
	public void testIndentationAfterGreaterOrEquals_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//std::ostream& operator<<(std::ostream& stream,
	//const BinFileParser::Exception& exp)
	//{
	//}
	
	//std::ostream& operator<<(std::ostream& stream,
	//		const BinFileParser::Exception& exp)
	//{
	//}
	public void testOperatorMethodBody_1_Bug192412() throws Exception {
		assertIndenterResult();
	}

	//std::ostream& operator<<(std::ostream& stream,
	//const BinFileParser::Exception& exp)
	//{
	//}
	
	//std::ostream& operator<<(std::ostream& stream,
	//                         const BinFileParser::Exception& exp)
	//{
	//}
	public void testOperatorMethodBody_2_Bug192412() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		assertIndenterResult();
	}

	//void func(std::vector<int>* v,
	//const std::string& s)
	//{
	//}
	
	//void func(std::vector<int>* v,
	//    const std::string& s)
	//{
	//}
	public void testFunctionParameters_1() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
						DefaultCodeFormatterConstants.INDENT_DEFAULT));
		assertIndenterResult();
	}

	//void func(std::vector<int>* v,
	//const std::string& s)
	//{
	//}
	
	//void func(std::vector<int>* v,
	//          const std::string& s)
	//{
	//}
	public void testFunctionParameters_2() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		assertIndenterResult();
	}

	//void func(
	//std::vector<int>* v,
	//const std::string& s)
	//{
	//}

	//void func(
	//    std::vector<int>* v,
	//    const std::string& s)
	//{
	//}
	public void testFunctionParameters_3() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		assertIndenterResult();
	}

	//void func(
	//std::vector<int>* v,
	//const std::string& s)
	//{
	//}

	//void func(
	//          std::vector<int>* v,
	//          const std::string& s)
	//{
	//}
	public void testFunctionParameters_4() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		assertIndenterResult();
	}

	//new pair<int, int>(a,
	//b);

	//new pair<int, int>(a,
	//                   b);
	public void testCallOfTemplateFunction() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION, 
				DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_COMPACT,
						DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
		assertIndenterResult();
	}

	//struct x {
	// int f1 : 1;
	// int f2 : 1;
	// int f3 : 1;
	//}
	
	//struct x {
	//	int f1 : 1;
	//	int f2 : 1;
	//	int f3 : 1;
	//}
	public void testBitFields_Bug193298() throws Exception {
		assertIndenterResult();
	}

	//class A {
	//A(int a,
	//int b)
	//{
	//}
	//};

	//class A {
	//	A(int a,
	//			int b)
	//	{
	//	}
	//};
	public void testConstructorBody_Bug194586() throws Exception {
		assertIndenterResult();
	}

	//class A {
	//A(int a,
	//int b)
	//throw()
	//{
	//}
	//};
	
	//class A {
	//	A(int a,
	//			int b)
	//	throw()
	//	{
	//	}
	//};
	public void testConstructorBodyWithThrow_Bug194586() throws Exception {
		assertIndenterResult();
	}

	//class A {
	//A(int a,
	//int b)
	//: f(0),
	//g(0) {
	//}
	//};
	
	//class A {
	//	A(int a,
	//			int b)
	//	: f(0),
	//	  g(0) {
	//	}
	//};
	public void testConstructorBodyWithInitializer_1_Bug194586() throws Exception {
		assertIndenterResult();
	}

	//class A {
	//public:
	//A(int a, int b) :
	//f(0),
	//g(0) {
	//}
	//};
	
	//class A {
	//public:
	//	A(int a, int b) :
	//		f(0),
	//		g(0) {
	//	}
	//};
	public void testConstructorBodyWithInitializer_2() throws Exception {
		assertIndenterResult();
	}

	//void f() {
	//switch(c) {
	//case 'a':
	//{
	//}
	//case 1:
	//{
	//}
	//}
	//}

	//void f() {
	//	switch(c) {
	//	case 'a':
	//	{
	//	}
	//	case 1:
	//	{
	//	}
	//	}
	//}
	public void testCaseBlockAfterCharLiteral_Bug194710() throws Exception {
		assertIndenterResult();
	}

	//int a[]=
	//{
	//1,
	//2
	//};

	//int a[]=
	//{
	//		1,
	//		2
	//};
	public void testInitializerLists_Bug194585() throws Exception {
		assertIndenterResult();
	}

	//struct_t a[]=
	//{
	//{
	//1,
	//2,
	//{ 1,2,3 }
	//},
	//{
	//1,
	//2,
	//{ 1,2,3 }
	//}
	//};
	
	//struct_t a[]=
	//{
	//		{
	//				1,
	//				2,
	//				{ 1,2,3 }
	//		},
	//		{
	//				1,
	//				2,
	//				{ 1,2,3 }
	//		}
	//};
	public void testNestedInitializerLists_Bug194585() throws Exception {
		assertIndenterResult();
	}

	//class MyClass {
	//typedef int MyType;
	//public:
	//int getA() {
	//return a;
	//}
	//MyClass();
	//protected:
	//private:
	//int a;
	//};

	//class MyClass {
	//		typedef int MyType;
	//	public:
	//		int getA() {
	//			return a;
	//		}
	//		MyClass();
	//	protected:
	//	private:
	//		int a;
	//};
	public void testClassDeclaration_Bug278713() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, 
				DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, 
				DefaultCodeFormatterConstants.TRUE);
		assertIndenterResult();
	}

	//namespace ns {
	//class A;
	//}

	//namespace ns {
	//	class A;
	//}
	public void testNamespace_1() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER, 
				DefaultCodeFormatterConstants.TRUE);
		assertIndenterResult();
	}

	//namespace ns {
	//class A;
	//}

	//namespace ns {
	//class A;
	//}
	public void testNamespace_2() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_NAMESPACE_HEADER, 
				DefaultCodeFormatterConstants.FALSE);
		assertIndenterResult();
	}

	//// a comment
	//class MyClass
	//{
	//};
	//  union DisUnion 
	//		{ 
	//};
	
	//// a comment
	//class MyClass
	//	{
	//	};
	//union DisUnion 
	//	{ 
	//	};
	public void testIndentedClass_1_Bug210417() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION, 
				DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		assertIndenterResult();
	}

	//// a comment
	//class MyClass : public Base
	//{
	//};
	
	//// a comment
	//class MyClass : public Base
	//	{
	//	};
	public void testIndentedClass_2_Bug210417() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION, 
				DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		assertIndenterResult();
	}	
	
	//// a comment
	//class MyClass : public Base, public OtherBase
	//{
	//};
	
	//// a comment
	//class MyClass : public Base, public OtherBase
	//	{
	//	};
	public void testIndentedClass_3_Bug210417() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION, 
				DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		assertIndenterResult();
	}	
	
	//// a comment
	//class MyClass : public Base, public OtherBase
	//{
	//};
	
	//// a comment
	//class MyClass : public Base, public OtherBase
	//	{
	//	};
	public void testIndentedClass_4_Bug210417() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION, 
				DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		assertIndenterResult();
	}

	//x =
	//0;
	
	//x =
	//		0;
	public void testWrappedAssignment_1_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//{
	//a = 0;
	//x = 2 +
	//2 +
	//2;

	//{
	//	a = 0;
	//	x = 2 +
	//			2 +
	//			2;
	public void testWrappedAssignment_2_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//if (1 > 0) {
	//double d = a * b /
	//c;
	
	//if (1 > 0) {
	//	double d = a * b /
	//			c;
	public void testWrappedAssignment_3_Bug277624() throws Exception {
		assertIndenterResult();
	}

	//int x = 1 < 2 ?
	//f(0) :
	//1;
	//g();

	//int x = 1 < 2 ?
	//		f(0) :
	//		1;
	//g();
	public void testConditionalExpression_Bug283970() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0;
	//i < 2; i++)
	
	//for (int i = 0;
	//		i < 2; i++)
	public void testWrappedFor_1_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0; i < 2;
	//i++)
	
	//for (int i = 0; i < 2;
	//		i++)
	public void testWrappedFor_2_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//for (int i = 0;
	//i < 2;
	//i++)
	//{
	
	//for (int i = 0;
	//		i < 2;
	//		i++)
	//{
	public void testWrappedFor_3_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//;
	//for (hash_map<Node*, double>::const_iterator it = container_.begin();
	//it != container_.end(); ++it) {

	//;
	//for (hash_map<Node*, double>::const_iterator it = container_.begin();
	//		it != container_.end(); ++it) {
	public void testWrappedFor_4_Bug277625() throws Exception {
		assertIndenterResult();
	}

	//cout << "long text"
	//<< " more text";

	//cout << "long text"
	//		<< " more text";
	public void testWrappedOutputStream() throws Exception {
		assertIndenterResult();
	}

	///* comment */
	//#define MACRO(a, b) \
	//value

	///* comment */
	//#define MACRO(a, b) \
	//		value
	public void testWrappedDefine() throws Exception {
		assertIndenterResult();
	}

	//std::string
	//	func();

	//std::string
	//func();
	public void testFunctionDeclaration_1() throws Exception {
		assertIndenterResult();
	}

	//;
	//std::string
	//	func();

	//;
	//std::string
	//func();
	public void testFunctionDeclaration_2() throws Exception {
		assertIndenterResult();
	}

	//map<int, char*>::iterator
	//	func();

	//map<int, char*>::iterator
	//func();
	public void testFunctionDeclaration_3() throws Exception {
		assertIndenterResult();
	}

	//template <class T, class U = A<T> >
	//	class B {

	//template <class T, class U = A<T> >
	//class B {
	public void testTemplateClass() throws Exception {
		assertIndenterResult();
	}

	//class A
	//{
	//public:
	//A();
	//};
	
	//class A
	//    {
	//public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifier_1_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertIndenterResult();
	}

	//class A
	//{
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifier_2_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.FALSE);
		assertIndenterResult();
	}

	//class A
	//{
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//	A();
	//    };
	public void testWhiteSmithsAccessSpecifier_3_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertIndenterResult();
	}

	//void f()
	//{
	//switch(x)
	//{
	//case 1:
	//doOne();
	//default:
	//doOther();
	//}
	//}
	
	//void f()
	//    {
	//    switch(x)
	//	{
	//    case 1:
	//	doOne();
	//    default:
	//	doOther();
	//	}
	//    }
	public void testWhiteSmithsSwitch_1() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
		assertIndenterResult();
	}

	//void f()
	//{
	//switch(x)
	//{
	//case 1:
	//doOne();
	//default:
	//doOther();
	//}
	//}
	
	//void f()
	//	{
	//	switch(x)
	//		{
	//		case 1:
	//		doOne();
	//		default:
	//		doOther();
	//		}
	//	}
	public void testWhiteSmithsSwitch_2() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.TAB);
		assertIndenterResult();
	}

	//extern "C" {
	//	int a;
	//}

	//extern "C" {
	//int a;
	//}
	public void testIndentationInsideLinkageSpec_Bug299482() throws Exception {
		assertIndenterResult();
	}
	
	//void t() const
	//{
	//}

	//void t() const
	//	{
	//	}
	public void testIndentationOfConstMethodBody_Bug298282() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION, 
				DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		assertIndenterResult();
	}
	
	//class A {
	//int f,g;
	//A():f(0)
	//{
	//}
	//A():f(0),g(0)
	//{
	//}
	//A():f(0),
	//g(0)
	//{
	//}
	//};
	
	//class A {
	//	int f,g;
	//	A():f(0)
	//	{
	//	}
	//	A():f(0),g(0)
	//	{
	//	}
	//	A():f(0),
	//			g(0)
	//	{
	//	}
	//};
	public void testIndentationOfConstructorBodyWithFieldInitializer_Bug298282() throws Exception {
		assertIndenterResult();
	}

	//class A {
	//	enum E1 {
	//			a=1,
	//	b=2,
	//	c=3
	//	};
	//	enum E2 {
	//	x=1,
	//			y=2,
	//	z=3
	//	};
	//};
	
	//class A {
	//	enum E1 {
	//		a=1,
	//		b=2,
	//		c=3
	//	};
	//	enum E2 {
	//		x=1,
	//		y=2,
	//		z=3
	//	};
	//};
	public void testIndentationOfEnumeratorDeclWithInitializer_Bug303175() throws Exception {
		assertIndenterResult();
	}

	//void f() {
	//switch(i) {
	//case -2:
	//f();
	//break;
	//case -1:
	//f();
	//break;
	//case 0:
	//f();
	//break;
	//case +1:
	//f();
	//break;
	//}
	//}

	//void f() {
	//	switch(i) {
	//	case -2:
	//		f();
	//		break;
	//	case -1:
	//		f();
	//		break;
	//	case 0:
	//		f();
	//		break;
	//	case +1:
	//		f();
	//		break;
	//	}
	//}
	public void testIndentationOfCaseWithSignedConstant_Bug304150() throws Exception {
		assertIndenterResult();
	}

	//typedef struct
	//{
	//int i;
	//};
	//typedef enum
	//{
	//e;
	//};

	//typedef struct
	//{
	//	int i;
	//};
	//typedef enum
	//{
	//	e;
	//};
	public void testIndentationOfTypedefedCompositeType_Bug324031() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getAllmanSettings().getMap());
		assertIndenterResult();
	}

	//enum {
	//a=1,
	//b
	//}

	//enum {
	//	a=1,
	//	b
	//}
	public void testIndentationAfterEnumValueAssignment_Bug324031() throws Exception {
		assertIndenterResult();
	}

	//int * f()
	//{
	//}

	//int * f()
	//{
	//}
	public void testIndentationAfterFunctionHeaderWithPointerReturnType_Bug334805() throws Exception {
		assertIndenterResult();
	}
}
