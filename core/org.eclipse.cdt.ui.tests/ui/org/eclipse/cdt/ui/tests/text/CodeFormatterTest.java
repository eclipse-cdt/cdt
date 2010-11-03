/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.internal.formatter.align.Alignment;

/**
 * Tests for the CodeFormatter.
 *
 * @since 4.0
 */
public class CodeFormatterTest extends BaseUITestCase {

	private Map<String, Object> fOptions;
	private Map<String, String> fDefaultOptions;

	public static TestSuite suite() {
		return suite(CodeFormatterTest.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fDefaultOptions= DefaultCodeFormatterOptions.getDefaultSettings().getMap();
		fOptions= new HashMap<String, Object>(fDefaultOptions);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void assertFormatterResult() throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString();
		String expected= contents[1].toString();
		assertFormatterResult(before, expected);
	}

	private void assertFormatterResult(String original, String expected) throws BadLocationException {
		IDocument document= new Document(original);
		TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_TRANSLATION_UNIT, original, 0, TextUtilities.getDefaultLineDelimiter(document), fOptions);
		assertNotNull(edit);
		edit.apply(document);
		assertEquals(expected, document.get());
	}
	
	//void foo(int arg);
	//void foo(int arg){}
	
	//void foo (int arg);
	//void foo (int arg) {
	//}
	public void testInsertSpaceBeforeOpeningParen_Bug190184() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION, CCorePlugin.INSERT);
		assertFormatterResult();
	}

	//void FailSwitchFormatting(void)
	//{
	//        switch (confusefomatter)
	//        {
	//
	//        case START_CONFUSION:
	//                SomeFunctionCallWithTypecast(( castConfusion_t)myvar1,
	//                (castNoAdditionalConfusion_t) myvar2);
	//                break;
	//
	//                case REVEAL_CONFUSION:
	//                if (myBlockIndentIsOk)
	//                {
	//                        myBlockstuff();
	//                }
	//                break;
	//
	//                case CONTINUE_CONFUSION:
	//                {
	//                        //the indentation problem continues...
	//                }
	//                default://....still not right
	//        }
	//}

	//void FailSwitchFormatting(void) {
	//	switch (confusefomatter) {
	//
	//	case START_CONFUSION:
	//		SomeFunctionCallWithTypecast((castConfusion_t) myvar1,
	//				(castNoAdditionalConfusion_t) myvar2);
	//		break;
	//
	//	case REVEAL_CONFUSION:
	//		if (myBlockIndentIsOk) {
	//			myBlockstuff();
	//		}
	//		break;
	//
	//	case CONTINUE_CONFUSION: {
	//		//the indentation problem continues...
	//	}
	//	default://....still not right
	//	}
	//}
	public void testIndentConfusionByCastExpression_Bug191021() throws Exception {
		assertFormatterResult();
	}
	
	//int
	//var;
	//int*
	//pvar;
	
	//int var;
	//int* pvar;
	public void testSpaceBetweenTypeAndIdentifier_Bug194603() throws Exception {
		assertFormatterResult();
	}

	//int a = sizeof(     int)    ;
	
	//int a = sizeof(int);
	public void testSizeofExpression_Bug195246() throws Exception {
		assertFormatterResult();
	}

	//int x;
	//int a = sizeof     x    ;
	
	//int x;
	//int a = sizeof x;
	public void testSizeofExpression_Bug201330() throws Exception {
		assertFormatterResult();
	}

	//void foo(){
	//for(;;){
	//int a=0;
	//switch(a){
	//case 0:
	//++a;
	//break;
	//case 1:
	//--a;
	//break;
	//}
	//}
	//}
	//int main(void){
	//foo();
	//return 1;
	//}

	//void foo() {
	//	for (;;) {
	//		int a = 0;
	//		switch (a) {
	//		case 0:
	//			++a;
	//			break;
	//		case 1:
	//			--a;
	//			break;
	//		}
	//	}
	//}
	//int main(void) {
	//	foo();
	//	return 1;
	//}
	public void testForWithEmptyExpression_Bug195942() throws Exception {
		assertFormatterResult();
	}

	//#define MY private:
	//
	//class ClassA
	//{
	//MY ClassA() {}
	//};

	//#define MY private:
	//
	//class ClassA {
	//MY
	//	ClassA() {
	//	}
	//};
	public void testAccessSpecifierAsMacro_Bug197494() throws Exception {
		assertFormatterResult();
	}

	//int verylooooooooooooooooooooooooooooooooooongname = 0000000000000000000000000000000;
	
	//int verylooooooooooooooooooooooooooooooooooongname =
	//		0000000000000000000000000000000;
	public void testLineWrappingOfInitializerExpression_Bug200961() throws Exception {
		assertFormatterResult();
	}
	
	//void functionWithLooooooooooooooooooooooooooooooooooooooooooooooooongName() throw(float);
	
	//void functionWithLooooooooooooooooooooooooooooooooooooooooooooooooongName()
	//		throw (float);
	public void testLineWrappingOfThrowSpecification_Bug200959() throws Exception {
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation1_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//    A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation2_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.FALSE);
		assertFormatterResult();
	}

	//class A {
	//public:
	//A();
	//};
	
	//class A
	//    {
	//    public:
	//	A();
	//    };
	public void testWhiteSmithsAccessSpecifierIndentation3_Bug204575() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_ACCESS_SPECIFIER_COMPARE_TO_TYPE_HEADER, DefaultCodeFormatterConstants.TRUE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ACCESS_SPECIFIER, DefaultCodeFormatterConstants.TRUE);
		assertFormatterResult();
	}

	//template<typename T> class B {};
	//template<typename T1,typename T2=B<T1> > class A {};

	//template<typename T> class B {
	//};
	//template<typename T1, typename T2 = B<T1> > class A {
	//};
	public void testNestedTemplateParameters_Bug206801() throws Exception {
		assertFormatterResult();
	}

	//int main
	//(
	//    int           argc,
	//    char const int*  argv[]
	//)
	//try
	//{
	//    for ( int i = 1 ; i < argc ; ++i )
	//    {
	//    }
	//    return 0;
	//}
	//catch ( float e )
	//{
	//    return 1;
	//}
	//catch ( ... )
	//{
	//	return 2;
	//}

	//int main(int argc, char const int* argv[])
	//try {
	//	for (int i = 1; i < argc; ++i) {
	//	}
	//	return 0;
	//}
	//catch (float e) {
	//	return 1;
	//}
	//catch (...) {
	//	return 2;
	//}
	public void testFunctionTryCatchBlock() throws Exception {
		assertFormatterResult();
	}

	//int main(int argc, char const int * argv[]) { try { for (int i = 1; i < argc; ++i) { } return 0; } catch (float e) { return 1; } catch (...) { return 2; } }
	
	//int main(int argc, char const int * argv[]) {
	//	try {
	//		for (int i = 1; i < argc; ++i) {
	//		}
	//		return 0;
	//	} catch (float e) {
	//		return 1;
	//	} catch (...) {
	//		return 2;
	//	}
	//}
	public void testTryCatchBlock() throws Exception {
		assertFormatterResult();
	}

	//void f() {
	//#define I 0
	//    int i = I;
	//}

	//void f() {
	//#define I 0
	//	int i = I;
	//}
	public void testMacroAsInitializer_Bug214354() throws Exception {
		assertFormatterResult();
	}
	
	//#define break_start(); { int foo;
	//#define break_end(); foo = 0; }
	//
	//void break_indenter(int a, int b) {
	//    break_start(); // This semicolon moves to its own line.
	//    if(a > b) {
	//        indentation_remains();
	//    }
	//
	//    if(b>a)
	//        indentation_vanishes();
	//
	//    break_end();
	//
	//    if(b == a)
	//      indentation_remains();
	//}
	
	//#define break_start(); { int foo;
	//#define break_end(); foo = 0; }
	//
	//void break_indenter(int a, int b) {
	//	break_start(); // This semicolon moves to its own line.
	//		if (a > b) {
	//			indentation_remains();
	//		}
	//
	//		if (b > a)
	//			indentation_vanishes();
	//
	//		break_end();
	//
	//	if (b == a)
	//		indentation_remains();
	//}
	public void testBracesInMacros_Bug217435() throws Exception {
		assertFormatterResult();
	}
	
	//int a=1+2;
	//int b= - a;
	//int c =b ++/-- b;
	
	//int a = 1 + 2;
	//int b = -a;
	//int c = b++ / --b;
	public void testWhitespaceSurroundingOperators() throws Exception {
		assertFormatterResult();
	}
	
	//void f() {
	//int *px= :: new int(  0 );
	//int* py [] =  new   int [5 ] (0, 1,2,3, 4);
	//int  *pz[ ] =new ( px)int(0);
	//delete  []  py;
	//:: delete px;}

	//void f() {
	//	int *px = ::new int(0);
	//	int* py[] = new int[5](0, 1, 2, 3, 4);
	//	int *pz[] = new (px) int(0);
	//	delete[] py;
	//	::delete px;
	//}
	public void testNewAndDeleteExpressions() throws Exception {
		assertFormatterResult();
	}

	//namespace   X=
	//   Y ::
	// 	    Z ;

	//namespace X = Y::Z;
	public void testNamespaceAlias() throws Exception {
		assertFormatterResult();
	}

	//using
	//   typename:: T
	//;
	//using X::
	// T ;

	//using typename ::T;
	//using X::T;
	public void testUsingDeclaration() throws Exception {
		assertFormatterResult();
	}

	//using
	//  namespace
	//    X ;

	//using namespace X;
	public void testUsingDirective() throws Exception {
		assertFormatterResult();
	}

	//static void *f(){}
	//static void * g();
	//static void* h();

	//static void *f() {
	//}
	//static void * g();
	//static void* h();
	public void testSpaceBetweenDeclSpecAndDeclarator() throws Exception {
		assertFormatterResult();
	}

	//typedef signed int TInt;
	//extern void Bar();  // should not have space between parens
	//
	//void Foo()    // should not have space between parens
	//{
	//  TInt a(3);  // should become TInt a( 3 );
	//  Bar();   // should not have space between parens
	//}

	//typedef signed int TInt;
	//extern void Bar(); // should not have space between parens
	//
	//void Foo() // should not have space between parens
	//	{
	//	TInt a( 3 ); // should become TInt a( 3 );
	//	Bar(); // should not have space between parens
	//	}
	public void testSpaceBetweenParen_Bug217918() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION, DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY, DefaultCodeFormatterConstants.FALSE);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION, CCorePlugin.DO_NOT_INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION, CCorePlugin.DO_NOT_INSERT);
		assertFormatterResult();
	}

	//class Example: public FooClass, public virtual BarClass {};
	
	//class Example:
	//		public FooClass,
	//		public virtual BarClass {
	//};
	public void testAlignmentOfClassDefinitionBaseClause1_Bug192656() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BASE_CLAUSE_IN_TYPE_DECLARATION,
				Integer.toString(Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE));
		assertFormatterResult();
	}

	//class Example: public FooClass, public virtual BarClass {};
	
	//class Example:	public FooClass,
	//				public virtual BarClass {
	//};
	public void testAlignmentOfClassDefinitionBaseClause2_Bug192656() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_BASE_CLAUSE_IN_TYPE_DECLARATION,
				Integer.toString(Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_FORCE | Alignment.M_INDENT_ON_COLUMN));
		assertFormatterResult();
	}

	//class Example { void foo() throw(int); };
	//void Example::foo()throw(int){}

	//class Example {
	//	void foo()
	//		throw (int);
	//};
	//void Example::foo()
	//	throw (int) {
	//}
	public void testAlignmentOfExceptionSpecificationInMethodDeclaration_Bug191980() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_METHOD_DECLARATION,
				Integer.toString(Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE | Alignment.M_INDENT_BY_ONE));
		assertFormatterResult();
	}

	//int foo(){try{}catch(...){}}
	//float* bar();
	//template<typename _CharT, typename _Traits>class basic_ios : public ios_base{public:
	//  // Types:
	//};
	
	//int
	//foo()
	//{
	//  try
	//    {
	//    }
	//  catch (...)
	//    {
	//    }
	//}
	//float*
	//bar();
	//template<typename _CharT, typename _Traits>
	//  class basic_ios : public ios_base
	//  {
	//  public:
	//    // Types:
	//  };
	public void testGNUCodingStyleConformance_Bug192764() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getGNUSettings().getMap());
		assertFormatterResult();
	}

	//NOT_DEFINED void foo(){
	//	}
	//
	//enum T1
	//    {
	//    E1 = 1
	//    };

	//NOT_DEFINED void foo() {
	//}
	//
	//enum T1 {
	//	E1 = 1
	//};
	public void testPreserveWhitespace_Bug225326() throws Exception {
		assertFormatterResult();
	}

	//NOT_DEFINED void foo()
	//	{
	//	}
	//
	//enum T1
	//    {
	//    E1 = 1
	//    };

	//NOT_DEFINED void foo()
	//    {
	//    }
	//
	//enum T1
	//    {
	//    E1 = 1
	//    };
	public void testPreserveWhitespace2_Bug225326() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		assertFormatterResult();
	}
	
	//enum Tthe3rdtestIds
	//{
	//ECommand1 = 0x6001,
	//ECommand2,
	//EHelp,
	//EAbout
	//};
	//
	//CActiveScheduler* scheduler = new (ELeave) CActiveScheduler();

	//enum Tthe3rdtestIds
	//    {
	//    ECommand1 = 0x6001,
	//    ECommand2,
	//    EHelp,
	//    EAbout
	//    };
	//
	//CActiveScheduler* scheduler = new (ELeave) CActiveScheduler();
	public void testFormatterRegressions_Bug225858() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getWhitesmithsSettings().getMap());
		assertFormatterResult();
	}

	//typedef int int_;
	//int_ const f(int_ const i);

	//typedef int int_;
	//int_ const f(int_ const i);
	public void testPreserveWhitespaceInParameterDecl_Bug228997() throws Exception {
		assertFormatterResult();
	}

	//void f() { throw 42; }
	
	//void f() {
	//	throw 42;
	//}
	public void testSpaceAfterThrowKeyword_Bug229774() throws Exception {
		assertFormatterResult();
	}

	//struct { int l; } s;
	//void f() {
	//  int x = (s.l -5);
	//  // Comment
	//  for(;;);
	//}
	
	//struct {
	//	int l;
	//} s;
	//void f() {
	//	int x = (s.l - 5);
	//	// Comment
	//	for (;;)
	//		;
	//}
	public void testIndentAfterDotL_Bug232739() throws Exception {
		assertFormatterResult();
	}

	//struct { int e; } s;
	//void f() {
	//  int x = (s.e -5);
	//  // Comment
	//  for(;;);
	//}
	
	//struct {
	//	int e;
	//} s;
	//void f() {
	//	int x = (s.e - 5);
	//	// Comment
	//	for (;;)
	//		;
	//}
	public void testIndentAfterDotE_Bug232739() throws Exception {
		assertFormatterResult();
	}

	//struct { int f; } s;
	//void f() {
	//  int x = (s.f -5);
	//  // Comment
	//  for(;;);
	//}
	
	//struct {
	//	int f;
	//} s;
	//void f() {
	//	int x = (s.f - 5);
	//	// Comment
	//	for (;;)
	//		;
	//}
	public void testIndentAfterDotF_Bug232739() throws Exception {
		assertFormatterResult();
	}

	//int a = 0, b = 1, c = 2, d = 3;
	
	//int a = 0,b = 1,c = 2,d = 3;
	public void testSpaceAfterCommaInDeclaratorList_Bug234915() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_DECLARATOR_LIST, CCorePlugin.DO_NOT_INSERT);
		assertFormatterResult();
	}

	//int a = 0,b = 1,c = 2,d = 3;
	
	//int a = 0, b = 1, c = 2, d = 3;
	public void testSpaceAfterCommaInDeclaratorList2_Bug234915() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_DECLARATOR_LIST, CCorePlugin.INSERT);
		assertFormatterResult();
	}

	//void f() {
	// 	class Object;
	//	int aVeryLongParameterThatShouldBeInOneLine1;
	//	int aVeryLongParameterThatShouldBeInOneLine2;
	//
	//	myNewFunctionCall1(Object(aVeryLongParameterThatShouldBeInOneLine1, aVeryLongParameterThatShouldBeInOneLine2));
	//
	//	myNewFunctionCall2(new Object(aVeryLongParameterThatShouldBeInOneLine1, aVeryLongParameterThatShouldBeInOneLine2));
	//}

	//void f() {
	//	class Object;
	//	int aVeryLongParameterThatShouldBeInOneLine1;
	//	int aVeryLongParameterThatShouldBeInOneLine2;
	//
	//	myNewFunctionCall1(
	//			Object(aVeryLongParameterThatShouldBeInOneLine1,
	//					aVeryLongParameterThatShouldBeInOneLine2));
	//
	//	myNewFunctionCall2(
	//			new Object(aVeryLongParameterThatShouldBeInOneLine1,
	//					aVeryLongParameterThatShouldBeInOneLine2));
	//}
	public void testLineWrappingOfConstructorCall_Bug237097() throws Exception {
		assertFormatterResult();
	}

	//bool test(bool x)
	//{
	//   return x or x and not (not x);
	//}

	//bool test(bool x) {
	//	return x or x and not (not x);
	//}
	public void testSpaceBeforeAndAfterAlternativeLogicalOperator_Bug239461() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR, CCorePlugin.DO_NOT_INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR, CCorePlugin.DO_NOT_INSERT);
		assertFormatterResult();
	}

	//void A::a(C e) { if (D::iterator it = m.find (e)) m.erase(it);}
	//T* A::b(T* t) { S::iterator it = m.find(t); if (!it) return NULL; else return *it; }
	//M* A::c(M* tm) { N::iterator it = myN.find(tm); if (!it) return NULL; else return *it; }

	//void A::a(C e) {
	//	if (D::iterator it = m.find (e))
	//		m.erase(it);
	//}
	//T* A::b(T* t) {
	//	S::iterator it = m.find(t);
	//	if (!it)
	//		return NULL;
	//	else
	//		return *it;
	//}
	//M* A::c(M* tm) {
	//	N::iterator it = myN.find(tm);
	//	if (!it)
	//		return NULL;
	//	else
	//		return *it;
	//}
	public void testHandleParsingProblemsInIfCondition_Bug240564() throws Exception {
		assertFormatterResult();
	}

	//TestType1<TESTNS::TestType2<3> > test_variable;

	//TestType1<TESTNS::TestType2<3> > test_variable;
	public void testNestedTemplatedArgument_Bug241058() throws Exception {
		assertFormatterResult();
	}
	
	//#define TP_SMALLINT int32_t
	//void foo(const TP_SMALLINT &intVal) { }
	//void bar(const TP_SMALLINT intVal) { }

	//#define TP_SMALLINT int32_t
	//void foo(const TP_SMALLINT &intVal) {
	//}
	//void bar(const TP_SMALLINT intVal) {
	//}
	public void testPreserveSpaceInParameterDecl_Bug241967() throws Exception {
		assertFormatterResult();
	}
	
	//#define MY_MACRO int a; \
	//    int b; \
	//    int c();
	//
	//class asdf {
	//		MY_MACRO
	//
	//public:
	//		asdf();
	//~asdf();
	//};
	
	//#define MY_MACRO int a; \
	//    int b; \
	//    int c();
	//
	//class asdf {
	//	MY_MACRO
	//
	//public:
	//	asdf();
	//	~asdf();
	//};
	public void testMacroWithMultipleDeclarations_Bug242053() throws Exception {
		assertFormatterResult();
	}

	//void foo() {
	//for(int i=0;i<50;++i){}
	//}
	
	//void foo() {
	//	for (int i = 0 ; i < 50 ; ++i) {
	//	}
	//}
	public void testSpaceBeforeSemicolonInFor_Bug242232() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR, CCorePlugin.INSERT);
		assertFormatterResult();
	}

	//char *b, * const a;
	
	//char *b, * const a;
	public void testPreserveSpaceBetweenPointerModifierAndIdentifier_Bug243056() throws Exception {
		assertFormatterResult();
	}

	//#define FUNCTION_NAME myFunc
	//#define VARIABLE_NAME myVar
	//
	//void FUNCTION_NAME( void );
	//void FUNCTION_NAME( void )
	//{
	//int VARIABLE_NAME;
	//}

	//#define FUNCTION_NAME myFunc
	//#define VARIABLE_NAME myVar
	//
	//void FUNCTION_NAME(void);
	//void FUNCTION_NAME(void) {
	//	int VARIABLE_NAME;
	//}
	public void testPreserveNecessarySpace_Bug250969() throws Exception {
		assertFormatterResult();
	}

	//#define FOREVER1 for(;;)
	//#define FOREVER2 while(1)
	//
	//int main(int argc, char **argv) {
	//	FOREVER1 {
	//		doSomething();
	//	}
	//	FOREVER2 {
	//		doSomething();
	//	}
	//}

	//#define FOREVER1 for(;;)
	//#define FOREVER2 while(1)
	//
	//int main(int argc, char **argv) {
	//	FOREVER1 {
	//		doSomething();
	//	}
	//	FOREVER2 {
	//		doSomething();
	//	}
	//}
	public void testFormatterProblemsWithForeverMacro() throws Exception {
		assertFormatterResult();
	}

	//#define BLOCK { }
	//#define DOIT1() { }
	//#define DOIT2() do { } while(false)
	//#define ALWAYS if(true)
	//#define NEVER if(false)
	//#define FOREVER for(;;)
	//
	//void foo() {
	//	int i=0;
	//  if (true) DOIT1();
	//  if (true) DOIT2();
	//	for (;;) BLOCK
	//	ALWAYS BLOCK
	//	NEVER FOREVER BLOCK
	//	switch(i) {
	//	case 0: BLOCK
	//	}
	//}

	//#define BLOCK { }
	//#define DOIT1() { }
	//#define DOIT2() do { } while(false)
	//#define ALWAYS if(true)
	//#define NEVER if(false)
	//#define FOREVER for(;;)
	//
	//void foo() {
	//	int i = 0;
	//	if (true)
	//		DOIT1();
	//	if (true)
	//		DOIT2();
	//	for (;;)
	//		BLOCK
	//	ALWAYS
	//		BLOCK
	//	NEVER
	//		FOREVER
	//			BLOCK
	//	switch (i) {
	//	case 0:
	//		BLOCK
	//	}
	//}
	public void testCompoundStatementAsMacro_Bug244928() throws Exception {
		assertFormatterResult();
	}

	//#define BLOCK { }
	//#define DOIT1() { }
	//#define DOIT2() do { } while(false)
	//#define ALWAYS if(true)
	//#define NEVER if(false)
	//#define FOREVER for(;;)
	//
	//void foo() {
	//	int i=0;
	//  if (true) DOIT1();
	//  if (true) DOIT2();
	//	for (;;) BLOCK
	//	ALWAYS BLOCK
	//	NEVER FOREVER BLOCK
	//	switch(i) {
	//	case 0: BLOCK
	//	}
	//}

	//#define BLOCK { }
	//#define DOIT1() { }
	//#define DOIT2() do { } while(false)
	//#define ALWAYS if(true)
	//#define NEVER if(false)
	//#define FOREVER for(;;)
	//
	//void
	//foo()
	//{
	//  int i = 0;
	//  if (true)
	//    DOIT1();
	//  if (true)
	//    DOIT2();
	//  for (;;)
	//    BLOCK
	//  ALWAYS
	//    BLOCK
	//  NEVER
	//    FOREVER
	//      BLOCK
	//  switch (i)
	//    {
	//  case 0:
	//    BLOCK
	//    }
	//}
	public void testCompoundStatementAsMacroGNU_Bug244928() throws Exception {
		fOptions.putAll(DefaultCodeFormatterOptions.getGNUSettings().getMap());
		assertFormatterResult();
	}

	//#define A (0)
	//#define B (1)
	//#define ARGS (A, B)
	//#define CALL foo ARGS
	//void zoo(void) {
	//        foo(A,B);
	//foo ARGS;
	//CALL;
	//#if X
	//                        if (1)
	//                        {
	//                                t = 1;
	//                        }
	//#endif
	//                }

	//#define A (0)
	//#define B (1)
	//#define ARGS (A, B)
	//#define CALL foo ARGS
	//void zoo(void) {
	//	foo(A, B);
	//	foo ARGS;
	//	CALL;
	//#if X
	//	if (1)
	//	{
	//		t = 1;
	//	}
	//#endif
	//}

	public void testMacroAsFunctionArguments_Bug253039() throws Exception {
		assertFormatterResult();
	}

	//#define assign1(uc1, uc2, uc3, uc4, val) \
	//      uc1##uc2##uc3##uc4 = val;
	//
	//#define assign2(ucn, val) ucn = val;
	//
	//void foo1(void)
	//{
	//int \U00010401\U00010401\U00010401\U00010402;
	//assign1(\U00010401, \U00010401, \U00010401, \U00010402, 4);
	//}
	//
	//void foo2(void)
	//{
	//int \U00010401\U00010401\U00010401\U00010402;
	//assign2(\U00010401\U00010401\U00010401\U00010402, 4);
	//}

	//#define assign1(uc1, uc2, uc3, uc4, val) \
	//      uc1##uc2##uc3##uc4 = val;
	//
	//#define assign2(ucn, val) ucn = val;
	//
	//void foo1(void) {
	//	int \U00010401\U00010401\U00010401\U00010402;
	//	assign1(\U00010401, \U00010401, \U00010401, \U00010402, 4);
	//}
	//
	//void foo2(void) {
	//	int \U00010401\U00010401\U00010401\U00010402;
	//	assign2(\U00010401\U00010401\U00010401\U00010402, 4);
	//}
	public void testUniversalCharacters_Bug255949() throws Exception {
		assertFormatterResult();
	}

	//void foo() throw(E1,E2);

	//void foo() throw ( E1, E2 );
	public void testWhitespaceOptionsForExceptionSpecification_Bug243567() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION, CCorePlugin.INSERT);
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_EXCEPTION_SPECIFICATION, CCorePlugin.INSERT);
		assertFormatterResult();
	}

	//void Foo::bar() {
	//*this.*FncPointer () ;  this->*FncPointer( ); }

	//void Foo::bar() {
	//	*this.*FncPointer();
	//	this->*FncPointer();
	//}
	public void testDotStarAndArrowStarOperators_Bug257700() throws Exception {
		assertFormatterResult();
	}
	
	//void zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz1(unsigned char __attribute__((unused)) x, unsigned char __attribute__((unused)) y){;}

	//void zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz1(
	//		unsigned char __attribute__((unused)) x,
	//		unsigned char __attribute__((unused)) y) {
	//	;
	//}
	public void test__attribute__InParameterDecl_Bug206271() throws Exception {
		assertFormatterResult();
	}
	
	//#define assert(e) if(!(e)) printf("Failed assertion")
	//void test(){assert(1 > 0);}

	//#define assert(e) if(!(e)) printf("Failed assertion")
	//void test() {
	//	assert(1 > 0);
	//}

	public void testMacroFormatting1_Bug241819() throws Exception {
		assertFormatterResult();
	}

	//#define PRINT if(printswitch) printf
	//void test(){int i=0;PRINT("Watch the format");if(i>0){i=1;}}

	//#define PRINT if(printswitch) printf
	//void test() {
	//	int i = 0;
	//	PRINT("Watch the format");
	//	if (i > 0) {
	//		i = 1;
	//	}
	//}
	public void testMacroFormatting2_Bug241819() throws Exception {
		assertFormatterResult();
	}

	//bool member __attribute__ ((__unused__)) = false;

	//bool member __attribute__ ((__unused__)) = false;
	public void testPreserveSpaceBetweenNameAnd__attribute__Bug261967() throws Exception {
		assertFormatterResult();
	}
	
	//extern "C" void f(int i, char c, float x);

	//extern "C" void f(int i, char c, float x);
	public void testPreserveSpaceInExternCDeclaration() throws Exception {
		assertFormatterResult();
	}

	//#define X
	//
	//typedef X struct {
	//};

	//#define X
	//
	//typedef X struct {
	//};
	public void testPreserveNecessarySpace_Bug268962() throws Exception {
		assertFormatterResult();
	}

	//inline   typename A foo();
	//void   bar(const typename A x)  ;
	//static   typename A x  ;

	//inline typename A foo();
	//void bar(const typename A x);
	//static typename A x;
	public void testFormatterProblemsWithTypename_Bug269590() throws Exception {
		assertFormatterResult();
	}
	
	//void
	//foo();
	//int*
	//bar();
	
	//void
	//foo();
	//int*
	//bar();
	public void testPreserveNewlineBetweenTypeAndFunctionDeclarator() throws Exception {
		assertFormatterResult();
	}

	public void testFormatGeneratedClass_Bug272006() throws Exception {
		String original = 
			"class \u5927\u5927\u5927\u5927\n" + 
			"{\n" + 
			"public:\n" + 
			"	\u5927\u5927\u5927\u5927();\n" + 
			"	virtual ~\u5927\u5927\u5927\u5927();\n" + 
			"};\n";
		String expected = 
			"class \u5927\u5927\u5927\u5927 {\n" + 
			"public:\n" + 
			"	\u5927\u5927\u5927\u5927();\n" + 
			"	virtual ~\u5927\u5927\u5927\u5927();\n" + 
			"};\n";
		assertFormatterResult(original, expected);
	}

	//void f() {
	//  Canvas1->MoveTo((50 + (24* 20 ) +xoff) *Scale,(200+yoff)*ScaleY);
	//  Canvas1->LineTo((67+(24*20) +xoff)*Scale,(200+yoff)*ScaleY);
	//  Canvas1->MoveTo((50+(24*20) +xoff)*Scale,((200+yoff)*ScaleY)-1);
	//  Canvas1->LineTo((67+(24*20) +xoff)*Scale,((200+yoff)*ScaleY)-1);
	//  Canvas1->MoveTo((50+(24*20) +xoff)*Scale,((200+yoff)*ScaleY)+1);
	//  Canvas1->LineTo((67+(24*20) +xoff)*Scale,((200+yoff)*ScaleY)+1);
	//}

	//void f() {
	//	Canvas1->MoveTo((50 + (24 * 20) + xoff) * Scale, (200 + yoff) * ScaleY);
	//	Canvas1->LineTo((67 + (24 * 20) + xoff) * Scale, (200 + yoff) * ScaleY);
	//	Canvas1->MoveTo((50 + (24 * 20) + xoff) * Scale,
	//			((200 + yoff) * ScaleY) - 1);
	//	Canvas1->LineTo((67 + (24 * 20) + xoff) * Scale,
	//			((200 + yoff) * ScaleY) - 1);
	//	Canvas1->MoveTo((50 + (24 * 20) + xoff) * Scale,
	//			((200 + yoff) * ScaleY) + 1);
	//	Canvas1->LineTo((67 + (24 * 20) + xoff) * Scale,
	//			((200 + yoff) * ScaleY) + 1);
	//}
	public void testScannerErrorWithIntegerFollowedByStar_Bug278118() throws Exception {
		assertFormatterResult();
	}

	//#define If  if (1 == 1){
	//#define Else } else {
	//#define EndElse }
	//
	//#define Try  try{
	//#define Catch } catch(...) {
	//#define EndCatch }
	//
	//int main() {
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//
	//	If
	//		cout << "OK" << endl;
	//	Else
	//		cout << "Strange" << endl;
	//	EndElse
	//
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//
	//	return 0;
	//}

	//#define If  if (1 == 1){
	//#define Else } else {
	//#define EndElse }
	//
	//#define Try  try{
	//#define Catch } catch(...) {
	//#define EndCatch }
	//
	//int main() {
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//
	//	If
	//		cout << "OK" << endl;
	//	Else
	//		cout << "Strange" << endl;
	//	EndElse
	//
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//	Try
	//		cout << "OK2" << endl;
	//	Catch
	//		cout << "Exception" << endl;
	//	EndCatch
	//
	//	return 0;
	//}
	public void testControlStatementsAsMacro_Bug290630() throws Exception {
		assertFormatterResult();
	}

	//#define new new(__FILE__, __LINE__)
	//void func() {char* a = new    char[10];}
	
	//#define new new(__FILE__, __LINE__)
	//void func() {
	//	char* a = new char[10];
	//}
	public void testPlacementNewAsMacro_Bug298593() throws Exception {
		assertFormatterResult();
	}

	//#define MACRO(a) class b : public a
	//MACRO(aClass){ int a;};
	
	//#define MACRO(a) class b : public a
	//MACRO(aClass) {
	//	int a;
	//};
	public void testCompositeTypeSpecAsMacro_Bug298592() throws Exception {
		assertFormatterResult();
	}
	
	//void f() {
	//w_char* p   =  L"wide string literal";
	//int x = 0;
	//if (x == 0) x = 5;}
	
	//void f() {
	//	w_char* p = L"wide string literal";
	//	int x = 0;
	//	if (x == 0)
	//		x = 5;
	//}
	public void testWideStringLiteral_Bug292626() throws Exception {
		assertFormatterResult();
	}

	//#define INT (int)
	//int i = INT 1;
	
	//#define INT (int)
	//int i = INT 1;
	public void testCastAsMacro_Bug285901() throws Exception {
		assertFormatterResult();
	}
	
	//PARENT_T sample={.a=1,.b={a[2]=1,.b.c=2}};

	//PARENT_T sample = { .a = 1, .b = { a[2] = 1, .b.c = 2 } };
	public void testDesignatedInitializer_Bug314958() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_LANGUAGE, GCCLanguage.getDefault());
		assertFormatterResult();
	}

	//void extend_terminal_bond_to_label(vector<atom_t> &atom, const vector<letters_t> &letters, int n_letters, const vector<bond_t> &bond, int n_bond, const vector<label_t> &label, int n_label, double avg, double maxh, double max_dist_double_bond);
	
	//void extend_terminal_bond_to_label(vector<atom_t> &atom, const vector<letters_t> &letters, int n_letters,
	//                                   const vector<bond_t> &bond, int n_bond, const vector<label_t> &label, int n_label,
	//                                   double avg, double maxh, double max_dist_double_bond);
	public void testWrappingOfTemplateIdAsParameterType_Bug325783() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION,
				Integer.toString(Alignment.M_COMPACT_SPLIT | Alignment.M_INDENT_ON_COLUMN));
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "120");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		assertFormatterResult();
	}

	//void extend_terminal_bond_to_label(vector<atom_t> &atom, const vector<letters_t> &letters, int n_letters, const vector<bond_t> &bond, int n_bond, const vector<label_t> &label, int n_label, double avg, double maxh, double max_dist_double_bond);
	
	//void extend_terminal_bond_to_label(vector<atom_t> &atom,
	//                                   const vector<letters_t> &letters,
	//                                   int n_letters, const vector<bond_t> &bond,
	//                                   int n_bond, const vector<label_t> &label,
	//                                   int n_label, double avg, double maxh,
	//                                   double max_dist_double_bond);
	public void testWrappingOfTemplateIdAsParameterType_Bug325783_2() throws Exception {
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION,
				Integer.toString(Alignment.M_COMPACT_SPLIT | Alignment.M_INDENT_ON_COLUMN));
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "80");
		fOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		assertFormatterResult();
	}

	////#define throws /* */
	//struct Foo {
	//    void foo() const throws {
	//    }
	//    void bar() const throws {
	//    }
	//};

	////#define throws /* */
	//struct Foo {
	//	void foo() const throws {
	//	}
	//	void bar() const throws {
	//	}
	//};
	public void testCodeCorruptionWithIllegalKeyword_Bug329165() throws Exception {
		assertFormatterResult();
	}
}
