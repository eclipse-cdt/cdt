/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class QuickParser2Tests extends TestCase {

    private static final NullLogService NULL_LOG = new NullLogService();

    /**
     *  
     */
    public QuickParser2Tests() {
        super();
    }

    /**
     * @param name
     */
    public QuickParser2Tests(String name) {
        super(name);
    }

    
    @Override
	protected void setUp() throws Exception {
    	CPPASTNameBase.sAllowRecursionBindings= false;
    	CPPASTNameBase.sAllowNameComputation= false;
	}

	/**
     * Test code: int x = 5; Purpose: to test the simple decaration in it's
     * simplest form.
     */
    public void testIntGlobal() throws Exception {
        // Parse and get the translation Unit
        parse("int x = 5;"); 
    }

    /**
     * Test code: class A { } a; Purpose: tests the use of a classSpecifier in
     */
    public void testEmptyClass() throws Exception {
        // Parse and get the translation unit
        Writer code = new StringWriter();
        code.write("class A { } a;"); 
        parse(code.toString());
    }

    /**
     * Test code: class A { public: int x; }; Purpose: tests a declaration in a
     * class scope.
     */
    public void testSimpleClassMember() throws Exception {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("class A { public: int x; };"); 
        parse(code.toString());
    }

    protected void parse(String code, boolean expectedToPass,
            ParserLanguage lang) throws Exception {
        parse(code, expectedToPass, lang, false);
    }

    protected void parse(String code, boolean expectedToPass) throws Exception {
        parse(code, expectedToPass, ParserLanguage.CPP);
    }

    /**
     * @param code
     */
    protected void parse(String code) throws Exception {
        parse(code, true, ParserLanguage.CPP);
    }

    public void testNamespaceDefinition() throws Exception {
        for (int i = 0; i < 2; ++i) {
            if (i == 0)
                parse("namespace KingJohn { int x; }"); 
            else
                parse("namespace { int x; }"); 
        }
    }

    public void testLinkageSpecification() throws Exception {
        for (int i = 0; i < 2; ++i) {
            if (i == 0)
                parse("extern \"C\" { int x(void); }"); 
            else
                parse("extern \"ADA\" int x(void);"); 
        }
    }

    public void testEnumSpecifier() throws Exception {
        Writer code = new StringWriter();
        code.write("enum { yo, go = 3, away };\n"); 
        code.write("enum hasAString { last = 666 };"); 
        parse(code.toString());
    }

    public void testTypedef() throws Exception {
        parse("typedef const struct A * const cpStructA;"); 
    }

    public void testUsingClauses() throws Exception {
        Writer code = new StringWriter();
        code.write("using namespace A::B::C;\n"); 
        code.write("using namespace C;\n"); 
        code.write("using B::f;\n"); 
        code.write("using ::f;\n"); 
        code.write("using typename crap::de::crap;"); 
        parse(code.toString());
    }

    /**
     * Test code: class A : public B, private C, virtual protected D { public:
     * int x, y; float a,b,c; } Purpose: tests a declaration in a class scope.
     */
    public void testSimpleClassMembers() throws Exception {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code
                .write("class A : public B, private C, virtual protected D { public: int x, y; float a,b,c; };"); 
        parse(code.toString());
    }

    /**
     * Test code: int myFunction( void );
     */
    public void testSimpleFunctionDeclaration() throws Exception {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("void myFunction( void );"); 
        parse(code.toString());
    }

    /**
     * Test code: bool myFunction( int parm1 = 3 * 4, double parm2 );
     * 
     * @throws Exception
     */
    public void testFunctionDeclarationWithParameters() throws Exception {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("bool myFunction( int parm1 = 3 * 4, double parm2 );"); 
        parse(code.toString());
    }

    public void testAssignmentExpressions() throws Exception {
        parse("int x = y = z = 5;"); 
    }

    public void testBug39348() throws Exception {
        parse("unsigned char a[sizeof (struct sss)];"); 
    }

    public void testBug39501() throws Exception {
        parse("struct A { A() throw (int); };"); 
    }

    public void testBug39349() throws Exception {
        parse("enum foo {  foo1   = 0,  foo2   = 0xffffffffffffffffULL,  foo3   = 0xf0fffffffffffffeLLU };"); 
    }

    public void testBug39544() throws Exception {
        parse("wchar_t wc = L'X';"); 
    }

    public void testBug36290() throws Exception {
        parse("typedef void ( A:: * pMethod ) ( void ); "); 
        parse("typedef void (boo) ( void ); "); 
        parse("typedef void boo (void); "); 
    }

    public void testBug36769B() throws Exception {
        parse("class X { operator int(); }; \n"); 
        parse("class X { operator int*(); }; \n"); 
        parse("class X { operator int&(); }; \n"); 
        parse("class X { operator A(); }; \n"); 
        parse("class X { operator A*(); }; \n"); 
        parse("class X { operator A&(); }; \n"); 

        parse("X::operator int() { } \n"); 
        parse("X::operator int*() { } \n"); 
        parse("X::operator int&() { } \n"); 
        parse("X::operator A() { } \n"); 
        parse("X::operator A*() { } \n"); 
        parse("X::operator A&() { } \n"); 

        parse("template <class A,B> class X<A,C> { operator int(); }; \n"); 
        parse("template <class A,B> class X<A,C> { operator int*(); }; \n"); 
        parse("template <class A,B> class X<A,C> { operator int&(); }; \n"); 
        parse("template <class A,B> class X<A,C> { operator A(); }; \n"); 
        parse("template <class A,B> class X<A,C> { operator A*(); }; \n"); 
        parse("template <class A,B> class X<A,C> { operator A&(); }; \n"); 

        parse("template <class A,B> X<A,C>::operator int() { }; \n"); 
        parse("template <class A,B> X<A,C>::operator int*() { }; \n"); 
        parse("template <class A,B> X<A,C>::operator int&() { }; \n"); 
        parse("template <class A,B> X<A,C>::operator A() { }; \n"); 
        parse("template <class A,B> X<A,C>::operator A*() { }; \n"); 
        parse("template <class A,B> X<A,C>::operator A&() { }; \n"); 
    }

	// failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236856
    public void _testBug36932C() throws Exception {
        parse("X::X( ) : var( new int ) {}"); 
        parse("X::X( ) : var( new int(5) ) {}"); 
        parse("X::X( ) : var( new int(B) ) {}"); 
        parse("X::X( ) : var( new int(B,C) ) {}"); 
        parse("X::X( ) : var( new int[5] ) {}"); 
        parse("X::X( ) : var( new int[5][10] ) {}"); 
        parse("X::X( ) : var( new int[B] ) {}"); 
        parse("X::X( ) : var( new int[B][C][D] ) {}"); 

        parse("X::X( ) : var( new A ) {}"); 
        parse("X::X( ) : var( new A(5) ) {}"); 
        parse("X::X( ) : var( new A(B) ) {}"); 
        parse("X::X( ) : var( new A(B,C) ) {}"); 
        parse("X::X( ) : var( new A[5] ) {}"); 
        parse("X::X( ) : var( new A[5][10] ) {}"); 
        parse("X::X( ) : var( new A[B] ) {}"); 
        parse("X::X( ) : var( new A[B][C][D] ) {}"); 

        parse("X::X( ) : var( new (int) ) {}"); 
        parse("X::X( ) : var( new (int)(5) ) {}"); 
        parse("X::X( ) : var( new (int)(B) ) {}"); 
        parse("X::X( ) : var( new (int)(B,C) ) {}"); 
        parse("X::X( ) : var( new (int[5]) ) {}"); 
        parse("X::X( ) : var( new (int[5][10]) ) {}"); 
        parse("X::X( ) : var( new (int[B]) ) {}"); 
        parse("X::X( ) : var( new (int[B][C][D]) ) {}"); 

        parse("X::X( ) : var( new (A) ) {}"); 
        parse("X::X( ) : var( new (A)(5) ) {}"); 
        parse("X::X( ) : var( new (A)(B) ) {}"); 
        parse("X::X( ) : var( new (A)(B,C) ) {}"); 
        parse("X::X( ) : var( new (A[5]) ) {}"); 
        parse("X::X( ) : var( new (A[5][10]) ) {}"); 
        parse("X::X( ) : var( new (A[B]) ) {}"); 
        parse("X::X( ) : var( new (A[B][C][D]) ) {}"); 

        parse("X::X( ) : var( new (0) int ) {}"); 
        parse("X::X( ) : var( new (0) int(5) ) {}"); 
        parse("X::X( ) : var( new (0) int(B) ) {}"); 
        parse("X::X( ) : var( new (0) int(B,C) ) {}"); 
        parse("X::X( ) : var( new (0) int[5] ) {}"); 
        parse("X::X( ) : var( new (0) int[5][10] ) {}"); 
        parse("X::X( ) : var( new (0) int[B] ) {}"); 
        parse("X::X( ) : var( new (0) int[B][C][D] ) {}"); 

        parse("X::X( ) : var( new (0) A ) {}"); 
        parse("X::X( ) : var( new (0) A(5) ) {}"); 
        parse("X::X( ) : var( new (0) A(B) ) {}"); 
        parse("X::X( ) : var( new (0) A(B,C) ) {}"); 
        parse("X::X( ) : var( new (0) A[5] ) {}"); 
        parse("X::X( ) : var( new (0) A[5][10] ) {}"); 
        parse("X::X( ) : var( new (0) A[B] ) {}"); 
        parse("X::X( ) : var( new (0) A[B][C][D] ) {}"); 

        parse("X::X( ) : var( new (0) (int) ) {}"); 
        parse("X::X( ) : var( new (0) (int)(5) ) {}"); 
        parse("X::X( ) : var( new (0) (int)(B) ) {}"); 
        parse("X::X( ) : var( new (0) (int)(B,C) ) {}"); 
        parse("X::X( ) : var( new (0) (int[5]) ) {}"); 
        parse("X::X( ) : var( new (0) (int[5][10]) ) {}"); 
        parse("X::X( ) : var( new (0) (int[B]) ) {}"); 
        parse("X::X( ) : var( new (0) (int[B][C][D]) ) {}"); 

        parse("X::X( ) : var( new (0) (A) ) {}"); 
        parse("X::X( ) : var( new (0) (A)(5) ) {}"); 
        parse("X::X( ) : var( new (0) (A)(B) ) {}"); 
        parse("X::X( ) : var( new (0) (A)(B,C) ) {}"); 
        parse("X::X( ) : var( new (0) (A[5]) ) {}"); 
        parse("X::X( ) : var( new (0) (A[5][10]) ) {}"); 
        parse("X::X( ) : var( new (0) (A[B]) ) {}"); 
        parse("X::X( ) : var( new (0) (A[B][C][D]) ) {}"); 

        parse("X::X( ) : var( new (P) int ) {}"); 
        parse("X::X( ) : var( new (P) int(5) ) {}"); 
        parse("X::X( ) : var( new (P) int(B) ) {}"); 
        parse("X::X( ) : var( new (P) int(B,C) ) {}"); 
        parse("X::X( ) : var( new (P) int[5] ) {}"); 
        parse("X::X( ) : var( new (P) int[5][10] ) {}"); 
        parse("X::X( ) : var( new (P) int[B] ) {}"); 
        parse("X::X( ) : var( new (P) int[B][C][D] ) {}"); 

        parse("X::X( ) : var( new (P) A ) {}"); 
        parse("X::X( ) : var( new (P) A(5) ) {}"); 
        parse("X::X( ) : var( new (P) A(B) ) {}"); 
        parse("X::X( ) : var( new (P) A(B,C) ) {}"); 
        parse("X::X( ) : var( new (P) A[5] ) {}"); 
        parse("X::X( ) : var( new (P) A[5][10] ) {}"); 
        parse("X::X( ) : var( new (P) A[B] ) {}"); 
        parse("X::X( ) : var( new (P) A[B][C][D] ) {}"); 

        parse("X::X( ) : var( new (P) (int) ) {}"); 
        parse("X::X( ) : var( new (P) (int)(5) ) {}"); 
        parse("X::X( ) : var( new (P) (int)(B) ) {}"); 
        parse("X::X( ) : var( new (P) (int)(B,C) ) {}"); 
        parse("X::X( ) : var( new (P) (int[5]) ) {}"); 
        parse("X::X( ) : var( new (P) (int[5][10]) ) {}"); 
        parse("X::X( ) : var( new (P) (int[B]) ) {}"); 
        parse("X::X( ) : var( new (P) (int[B][C][D]) ) {}"); 

        parse("X::X( ) : var( new (P) (A) ) {}"); 
        parse("X::X( ) : var( new (P) (A)(5) ) {}"); 
        parse("X::X( ) : var( new (P) (A)(B) ) {}"); 
        parse("X::X( ) : var( new (P) (A)(B,C) ) {}"); 
        parse("X::X( ) : var( new (P) (A[5]) ) {}"); 
        parse("X::X( ) : var( new (P) (A[5][10]) ) {}"); 
        parse("X::X( ) : var( new (P) (A[B]) ) {}"); 
        parse("X::X( ) : var( new (P) (A[B][C][D]) ) {}"); 
    }

    public void testBugSingleton192() throws Exception {
        parse("int Test::* pMember_;"); 
    }

    public void testBug36931() throws Exception {
        parse("A::nested::nested(){}; "); 
        parse("int A::nested::foo() {} "); 
        parse("int A::nested::operator+() {} "); 
        parse("A::nested::operator int() {} "); 
        parse("static const int A::nested::i = 1; "); 

        parse("template <class B,C> A<B>::nested::nested(){}; "); 
        parse("template <class B,C> int A::nested<B,D>::foo() {} "); 
        parse("template <class B,C> int A<B,C>::nested<C,B>::operator+() {} "); 
        parse("template <class B,C> A::nested::operator int() {} "); 
    }

    public void testBug37019() throws Exception {
        parse("static const A a( 1, 0 );"); 
    }

    public void testBug36766and36769A() throws Exception {
        Writer code = new StringWriter();
        code.write("template <class _CharT, class _Alloc>\n"); 
        code.write("rope<_CharT, _Alloc>::rope(size_t __n, _CharT __c,\n"); 
        code.write("const allocator_type& __a): _Base(__a)\n"); 
        code.write("{}\n"); 
        parse(code.toString());
    }

    public void testBug36766and36769B() throws Exception {
        Writer code = new StringWriter();
        code.write("template<class _CharT>\n"); 
        code.write("bool _Rope_insert_char_consumer<_CharT>::operator()\n"); 
        code.write("(const _CharT* __leaf, size_t __n)\n"); 
        code.write("{}\n"); 
        parse(code.toString());
    }

    public void testBug36766and36769C() throws Exception {
        //TODO - requires CPPVisitor
        Writer code = new StringWriter();
        code.write("template <class _CharT, class _Alloc>\n"); 
        code.write("_Rope_char_ref_proxy<_CharT, _Alloc>&\n"); 
        code
                .write("_Rope_char_ref_proxy<_CharT, _Alloc>::operator= (_CharT __c)\n"); 
        code.write("{}\n"); 
        parse(code.toString());
    }

    public void testBug36766and36769D() throws Exception {
        //TODO - requires CPPVisitor
        Writer code = new StringWriter();
        code.write("template <class _CharT, class _Alloc>\n"); 
        code.write("rope<_CharT, _Alloc>::~rope()\n"); 
        code.write("{}\n"); 
        parse(code.toString());
    }

    public void testBug36932A() throws Exception {
        parse("A::A( ) : var( new char [ (unsigned)bufSize ] ) {}"); 
    }

	// failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236856
    public void _testBug36932B() throws Exception {
        parse(" p = new int; "); 
        parse(" p = new int(5); "); 
        parse(" p = new int(B); "); 
        parse(" p = new int(B,C); "); 
        parse(" p = new int[5]; "); 
        parse(" p = new int[5][10]; "); 
        parse(" p = new int[B]; "); 
        parse(" p = new int[B][C][D]; "); 

        parse(" p = new A; "); 
        parse(" p = new A(5); "); 
        parse(" p = new A(B); "); 
        parse(" p = new A(B,C); "); 
        parse(" p = new A[5]; "); 
        parse(" p = new A[5][10]; "); 
        parse(" p = new A[B]; "); 
        parse(" p = new A[B][C][D]; "); 

        parse(" p = new (int); "); 
        parse(" p = new (int)(5); "); 
        parse(" p = new (int)(B); "); 
        parse(" p = new (int)(B,C); "); 
        parse(" p = new (int[5]); "); 
        parse(" p = new (int[5][10]); "); 
        parse(" p = new (int[B]); "); 
        parse(" p = new (int[B][C][D]); "); 

        parse(" p = new (A); "); 
        parse(" p = new (A)(5); "); 
        parse(" p = new (A)(B); "); 
        parse(" p = new (A)(B,C); "); 
        parse(" p = new (A[5]); "); 
        parse(" p = new (A[5][10]); "); 
        parse(" p = new (A[B]); "); 
        parse(" p = new (A[B][C][D]); "); 

        parse(" p = new (0) int; "); 
        parse(" p = new (0) int(5); "); 
        parse(" p = new (0) int(B); "); 
        parse(" p = new (0) int(B,C); "); 
        parse(" p = new (0) int[5]; "); 
        parse(" p = new (0) int[5][10]; "); 
        parse(" p = new (0) int[B]; "); 
        parse(" p = new (0) int[B][C][D]; "); 

        parse(" p = new (0) A; "); 
        parse(" p = new (0) A(5); "); 
        parse(" p = new (0) A(B); "); 
        parse(" p = new (0) A(B,C); "); 
        parse(" p = new (0) A[5]; "); 
        parse(" p = new (0) A[5][10]; "); 
        parse(" p = new (0) A[B]; "); 
        parse(" p = new (0) A[B][C][D]; "); 

        parse(" p = new (0) (int); "); 
        parse(" p = new (0) (int)(5); "); 
        parse(" p = new (0) (int)(B); "); 
        parse(" p = new (0) (int)(B,C); "); 
        parse(" p = new (0) (int[5]); "); 
        parse(" p = new (0) (int[5][10]); "); 
        parse(" p = new (0) (int[B]); "); 
        parse(" p = new (0) (int[B][C][D]); "); 

        parse(" p = new (0) (A); "); 
        parse(" p = new (0) (A)(5); "); 
        parse(" p = new (0) (A)(B); "); 
        parse(" p = new (0) (A)(B,C); "); 
        parse(" p = new (0) (A[5]); "); 
        parse(" p = new (0) (A[5][10]); "); 
        parse(" p = new (0) (A[B]); "); 
        parse(" p = new (0) (A[B][C][D]); "); 

        parse(" p = new (P) int; "); 
        parse(" p = new (P) int(5); "); 
        parse(" p = new (P) int(B); "); 
        parse(" p = new (P) int(B,C); "); 
        parse(" p = new (P) int[5]; "); 
        parse(" p = new (P) int[5][10]; "); 
        parse(" p = new (P) int[B]; "); 
        parse(" p = new (P) int[B][C][D]; "); 

        parse(" p = new (P) A; "); 
        parse(" p = new (P) A(5); "); 
        parse(" p = new (P) A(B); "); 
        parse(" p = new (P) A(B,C); "); 
        parse(" p = new (P) A[5]; "); 
        parse(" p = new (P) A[5][10]; "); 
        parse(" p = new (P) A[B]; "); 
        parse(" p = new (P) A[B][C][D]; "); 

        parse(" p = new (P) (int); "); 
        parse(" p = new (P) (int)(5); "); 
        parse(" p = new (P) (int)(B); "); 
        parse(" p = new (P) (int)(B,C); "); 
        parse(" p = new (P) (int[5]); "); 
        parse(" p = new (P) (int[5][10]); "); 
        parse(" p = new (P) (int[B]); "); 
        parse(" p = new (P) (int[B][C][D]); "); 

        parse(" p = new (P) (A); "); 
        parse(" p = new (P) (A)(5); "); 
        parse(" p = new (P) (A)(B); "); 
        parse(" p = new (P) (A)(B,C); "); 
        parse(" p = new (P) (A[5]); "); 
        parse(" p = new (P) (A[5][10]); "); 
        parse(" p = new (P) (A[B]); "); 
        parse(" p = new (P) (A[B][C][D]); "); 
    }

    public void testBug36769A() throws Exception {

        parse("template <class A, B> cls<A, C>::operator otherType() const {}\n"); 
        parse("template <class A, B> cls<A, C>::cls() {}\n"); 
        parse("template <class A, B> cls<A, C>::~cls() {}\n"); 
    }

    public void testBug36714() throws Exception {
        Writer code = new StringWriter();
        code.write("unsigned long a = 0UL;\n"); 
        code.write("unsigned long a2 = 0L; \n"); 

        parse(code.toString());
    }

    public void testBugFunctor758() throws Exception {
        parse(
        	"class Functor {"+
        	"template <typename Fun> Functor(Fun fun) : spImpl_(new FunctorHandler<Functor, Fun>(fun)){}" +
        	"};"
        ); 
    }

    public void testBug36932() throws Exception {
        parse("A::A(): b( new int( 5 ) ), b( new B ), c( new int ) {}"); 
    }

    public void testBug36704() throws Exception {
        Writer code = new StringWriter();
        code.write("template<typename T, typename U> class Typelist;");
        code.write("template<typename T> struct Length {};");
        code.write("template <class T, class U>\n"); 
        code.write("struct Length< Typelist<T, U> >\n"); 
        code.write("{\n"); 
        code.write("enum { value = 1 + Length<U>::value };\n"); 
        code.write("};\n"); 
        parse(code.toString());
    }

    public void testBug36699() throws Exception {
        Writer code = new StringWriter();
        code.write("template <	template <class> class ThreadingModel = DEFAULT_THREADING,\n"); 
        code.write("std::size_t chunkSize = DEFAULT_CHUNK_SIZE,\n"); 
        code.write("std::size_t maxSmallObjectSize = MAX_SMALL_OBJECT_SIZE	>\n"); 
        code.write("class SmallObject : public ThreadingModel<\n"); 
        code.write("SmallObject<ThreadingModel, chunkSize, maxSmallObjectSize> >\n"); 
        code.write("{};\n"); 
        parse(code.toString());
    }

    public void testBug36691() throws Exception {
        Writer code = new StringWriter();
        code.write("template <class T, class H>\n"); 
        code.write("typename H::template Rebind<T>::Result& Field(H& obj)\n"); 
        code.write("{	return obj;	}\n"); 
        parse(code.toString());
    }

    public void testBug36702() throws Exception {
        Writer code = new StringWriter();
        code.write("void mad_decoder_init(struct mad_decoder *, void *,\n"); 
        code.write("			  enum mad_flow (*)(void *, struct mad_stream *),\n"); 
        code
                .write("			  enum mad_flow (*)(void *, struct mad_header const *),\n"); 
        code.write("			  enum mad_flow (*)(void *,\n"); 
        code.write("					struct mad_stream const *,\n"); 
        code.write("					struct mad_frame *),\n"); 
        code.write("			  enum mad_flow (*)(void *,\n"); 
        code.write("					struct mad_header const *,\n"); 
        code.write("					struct mad_pcm *),\n"); 
        code.write("			  enum mad_flow (*)(void *,\n"); 
        code.write("					struct mad_stream *,\n"); 
        code.write("					struct mad_frame *),\n"); 
        code.write("			  enum mad_flow (*)(void *, void *, unsigned int *)\n"); 
        code.write(");\n"); 

        parse(code.toString());

    }

    public void testBug36852() throws Exception {
        Writer code = new StringWriter();
        code
                .write("int CBT::senseToAllRect( double id_standardQuot = DOSE, double id_minToleranz =15.0,\n"); 
        code
                .write("double id_maxToleranz = 15.0, unsigned int iui_minY = 0, \n"); 
        code.write("unsigned int iui_maxY = HEIGHT );\n"); 
        parse(code.toString());
    }

    public void testBug36689() throws Exception {
        Writer code = new StringWriter();
        code.write("template\n"); 
        code.write("<\n"); 
        code.write("class AbstractFact,\n"); 
        code
                .write("template <class, class> class Creator = OpNewFactoryUnit,\n"); 
        code.write("class TList = typename AbstractFact::ProductList\n"); 
        code.write(">\n"); 
        code.write("class ConcreteFactory\n"); 
        code.write(": public GenLinearHierarchy<\n"); 
        code
                .write("typename TL::Reverse<TList>::Result, Creator, AbstractFact>\n"); 
        code.write("{\n"); 
        code.write("public:\n"); 
        code.write("typedef typename AbstractFact::ProductList ProductList;\n"); 
        code.write("typedef TList ConcreteProductList;\n"); 
        code.write("};\n"); 
        parse(code.toString());
    }

    public void testBug36707() throws Exception {
        parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };"); 
    }

    public void testBug36717() throws Exception {
        parse("enum { eA = A::b };"); 
    }

    public void testBug36693() throws Exception {
        parse("FixedAllocator::Chunk* FixedAllocator::VicinityFind(void* p){}"); 
    }

    public void testWeirdExpression() throws Exception {
        parse("int x = rhs.spImpl_.get();"); 
    }

    public void testBug36696() throws Exception {
        Writer code = new StringWriter();
        code.write("template<typename T> class RefCounted {");
        code.write("template <typename P1> RefCounted(const RefCounted<P1>& rhs)\n"); 
        code.write(": pCount_(reinterpret_cast<const RefCounted&>(rhs).pCount_) {}\n"); 
        code.write("};"); 
        parse(code.toString());
    }

    public void testArrayOfPointerToFunctions() throws Exception {
        parse("unsigned char (*main_data)[MAD_BUFFER_MDLEN];"); 
    }

    public void testBug36073() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("class A{\n"); 
        writer.write("int x;\n"); 
        writer.write("public:\n"); 
        writer.write("A(const A&);\n"); 
        writer.write("};\n"); 
        writer.write("A::A(const A&v) : x(v.x) { }\n"); 
        parse(writer.toString());
    }

    public void testTemplateSpecialization() throws Exception {
        parse("template<> class stream<char> { /* ... */ };"); 
    }

    public void testTemplateInstantiation() throws Exception {
        parse("template class Array<char>;"); 
    }

    /**
     * Test code: "class A { int floor( double input ), someInt; };"
     */
    public void testMultipleDeclarators() throws Exception {
        // Parse and get the translaton unit
        parse("class A { int floor( double input ), someInt; };"); 
    }

    public void testFunctionModifiers() throws Exception {
        parse("class A {virtual void foo( void ) const throw ( yay, nay, we::dont::care ) = 0;};"); 
    }

    public void testArrays() throws Exception {
        parse("int x [5][];"); 
    }

    public void testElaboratedParms() throws Exception {
        parse("int x( struct A myA ) { /* junk */ }"); 
    }

    public void testMemberDeclarations() throws Exception {
        Writer code = new StringWriter();
        code.write("class A {\n"); 
        code.write("public:\n"); 
        code.write(" int is0;\n"); 
        code.write("private:\n"); 
        code.write(" int is1;\n"); 
        code.write("protected:\n"); 
        code.write(" int is2;\n"); 
        code.write("};"); 
        parse(code.toString());
    }

    public void testPointerOperators() throws Exception {
        parse("int * x = 0, & y, * const * volatile * z;"); 
    }

    public void testBug26467() throws Exception {
        StringWriter code = new StringWriter();
        code.write("struct foo { int fooInt; char fooChar;	};\n"); 
        code.write("typedef struct foo fooStruct;\n"); 
        code.write("typedef struct { int anonInt; char anonChar; } anonStruct;\n"); 
        parse(code.toString());
    }

    public void testASMDefinition() throws Exception {
        parse("asm( \"mov ep1 ds2\");"); 
    }

    public void testConstructorChain() throws Exception {
        //TODO - requires CPPVisitor in order to reduce ambiguities
        parse("TrafficLight_Actor::TrafficLight_Actor( RTController * rtg_rts, RTActorRef * rtg_ref )	: RTActor( rtg_rts, rtg_ref ), myId( 0 ) {}"); 
    }

    public void testBug36237() throws Exception {
        parse("A::A():B( (char *)0 ){}"); 
    }

    public void testBug36532() throws Exception {
        try {
            parse("template<int f() {\n"); 
            fail("We should not make it this far"); 
        } catch (ParserException pe) {
        } catch (Exception e) {
            fail("We should have gotten a ParserException rather than" + e); 
        }
    }

    public void testPreprocessor() throws Exception {
        parse("#include <stdio.h>\n#define DEF VALUE\n");
    }

    public void testTemplateDeclarationOfFunction() throws Exception {
        parse("template<class A, typename B=C> A aTemplatedFunction( B bInstance );"); 
    }

    public void testTemplateDeclarationOfClass() throws Exception {
        parse("template<class T, typename Tibor = junk, class, typename, int x, float y,template <class Y> class, template<class A> class AClass> class myarray { /* ... */ };"); 
    }

    public void testBug35906() throws Exception {
        StringWriter code = new StringWriter();
        code.write("void TTest::MTest() {}\n"); 
        code.write("struct TTest::STest *TTest::FTest (int i) {}\n"); 
        parse(code.toString());
    }

    public void testBug36288() throws Exception {
        parse("int foo() {}\nlong foo2(){}"); 
    }

    public void testBug36250() throws Exception {
        parse("int f( int = 0 );"); 
    }

    public void testBug36240() throws Exception {
        parse("A & A::operator=( A ){}"); 
    }

    public void testBug36254() throws Exception {
        parse("unsigned i;\nvoid f( unsigned p1 = 0 );"); 
    }

    public void testBug36432() throws Exception {
        Writer code = new StringWriter();
        code.write("#define CMD_GET		\"g\"\n"); 
        code.write("#define CMD_ACTION   	\"a\"\n"); 
        code.write("#define CMD_QUIT		\"q\"\n"); 
        code
                .write("static const memevent_cmd_func memevent_cmd_funcs[sizeof memevent_cmds - 1] = {\n"); 
        code.write("memevent_get,\n"); 
        code.write("memevent_action,\n"); 
        code.write("memevent_quit,\n"); 
        code.write("};\n"); 
        parse(code.toString());
    }

    public void testBug36594() throws Exception {
        parse("const int n = sizeof(A) / sizeof(B);"); 
    }

    public void testBug36794() throws Exception {
        parse("template<> class allocator<void> {};"); 
    }

    public void testBug36799() throws Exception {
        parse("static const int __WORD_BIT = int(CHAR_BIT*sizeof(unsigned int));"); 
    }

    public void testBug36764() throws Exception {
        parse("struct{ int x : 4; int y : 8; };"); 
    }

    public void testOrder() throws Exception {
        //TODO - requires CPPVisitor
        Writer code = new StringWriter();
        code.write("#define __SGI_STL_INTERNAL_ALGOBASE_H\n"); 
        code.write("#include <string.h>\n"); 
        code.write("template <class _Tp>\n"); 
        code.write("inline void swap(_Tp& __a, _Tp& __b) {\n"); 
        code.write("__STL_REQUIRES(_Tp, _Assignable);\n"); 
        code.write("_Tp __tmp = __a;\n"); 
        code.write("__a = __b;\n"); 
        code.write("__b = __tmp;\n"); 
        code.write("}\n"); 
        parse(code.toString());
    }

    public void testBug36771() throws Exception {
        Writer code = new StringWriter();
        code.write("#include /**/ \"foo.h\"\n"); 

        parse(code.toString());
    }

    public void testBug36811() throws Exception {
        Writer code = new StringWriter();
        code.write("using namespace std;\n"); 
        code.write("class Test {};"); 
        parse(code.toString());
    }

    public void testBug36708() throws Exception {
        parse("enum { isPointer = PointerTraits<T>::result };"); 
    }

    public void testBug36690() throws Exception {
        parse(
        	"class Functor {" +
        	"Functor(const Functor& rhs) : spImpl_(Impl::Clone(rhs.spImpl_.get())){}" +
        	"};"
        ); 
    }

    public void testBug36703() throws Exception {
        parse("const std::type_info& Get() const;"); 
    }

    public void testBug36692() throws Exception {
        Writer code = new StringWriter();
        code.write("template <typename T, typename Destroyer>\n"); 
        code
                .write("void SetLongevity(T* pDynObject, unsigned int longevity,\n"); 
        code.write("Destroyer d = Private::Deleter<T>::Delete){}\n"); 
        parse(code.toString());
    }

    public void testBug36551() throws Exception {
        Writer code = new StringWriter();
        code.write("class TextFrame {\n"); 
        code.write("BAD_MACRO()\n"); 
        code.write("};"); 
        parse(code.toString(), false);
    }

//    public void testBug36247() throws Exception {
//        Writer code = new StringWriter();
//        code.write("class A {\n"); 
//        code.write("INLINE_DEF int f ();\n"); 
//        code.write("INLINE_DEF A   g ();"); 
//        code.write("INLINE_DEF A * h ();"); 
//        code.write("INLINE_DEF A & unlock( void );"); 
//        code.write("};"); 
//        parse(code.toString());
//    }

    public void testStruct() throws Exception {
        StringWriter writer = new StringWriter();
        writer.write("struct mad_bitptr { unsigned char const *byte;\n"); 
        writer.write("unsigned short cache;\n unsigned short left;};"); 
        parse(writer.toString());
    }

    public void testBug36559() throws Exception {
        Writer code = new StringWriter();
        code.write("namespace myNameSpace {\n"); 
        code.write("template<typename T=short> class B {};\n"); 
        code.write("template<> class B<int> {};\n"); 
        code.write("}\n"); 
        parse(code.toString());
    }

    public void testPointersToFunctions() throws Exception {
        Writer code = new StringWriter();
        code.write("void (*name)( void );\n"); 
        code
                .write("static void * (* const orig_malloc_hook)(const char *file, int line, size_t size);\n"); 

        parse(code.toString());
    }

    public void testBug36600() throws Exception {
        parse("enum mad_flow (*input_func)(void *, struct mad_stream *);"); 
    }

    public void testBug36713() throws Exception {
        Writer code = new StringWriter();
        code.write("A ( * const fPtr) (void *); \n"); 
        code.write("A (* const fPtr2) ( A * ); \n"); 
        parse(code.toString());
    }

    // K&R Test hasn't been ported from DOMTests
    // still need to figure out how to represent these in the AST
    //	public void testOldKRFunctionDeclarations() throws Exception
    //	{
    //		// Parse and get the translaton unit
    //		Writer code = new StringWriter();
    //		code.write("bool myFunction( parm1, parm2, parm3 )\n");
    //		code.write("const char* parm1;\n");
    //		code.write("int (*parm2)(float);\n");
    //		code.write("{}");
    //		TranslationUnit translationUnit = parse(code.toString());
    //
    //		// Get the declaration
    //		List declarations = translationUnit.getDeclarations();
    //		assertEquals(1, declarations.size());
    //		SimpleDeclaration simpleDeclaration =
    // (SimpleDeclaration)declarations.get(0);
    //		assertEquals( simpleDeclaration.getDeclSpecifier().getType(),
    // DeclSpecifier.t_bool );
    //		List declarators = simpleDeclaration.getDeclarators();
    //		assertEquals( 1, declarators.size() );
    //		Declarator functionDeclarator = (Declarator)declarators.get( 0 );
    //		assertEquals( functionDeclarator.getName().toString(), "myFunction" );
    //        
    //		ParameterDeclarationClause pdc = functionDeclarator.getParms();
    //		assertNotNull( pdc );
    //		List parameterDecls = pdc.getDeclarations();
    //		assertEquals( 3, parameterDecls.size() );
    //		ParameterDeclaration parm1 = (ParameterDeclaration)parameterDecls.get( 0
    // );
    //		assertNotNull( parm1.getDeclSpecifier().getName() );
    //		assertEquals( "parm1", parm1.getDeclSpecifier().getName().toString() );
    //		List parm1Decls = parm1.getDeclarators();
    //		assertEquals( 1, parm1Decls.size() );
    //
    //		ParameterDeclaration parm2 = (ParameterDeclaration)parameterDecls.get( 1
    // );
    //		assertNotNull( parm2.getDeclSpecifier().getName() );
    //		assertEquals( "parm2", parm2.getDeclSpecifier().getName().toString() );
    //		List parm2Decls = parm2.getDeclarators();
    //		assertEquals( 1, parm2Decls.size() );
    //        
    //		ParameterDeclaration parm3 = (ParameterDeclaration)parameterDecls.get( 2
    // );
    //		assertNotNull( parm3.getDeclSpecifier().getName() );
    //		assertEquals( "parm3", parm3.getDeclSpecifier().getName().toString() );
    //		List parm3Decls = parm3.getDeclarators();
    //		assertEquals( 1, parm3Decls.size() );
    //        
    //		OldKRParameterDeclarationClause clause = pdc.getOldKRParms();
    //		assertNotNull( clause );
    //		assertEquals( clause.getDeclarations().size(), 2 );
    //		SimpleDeclaration decl1 =
    // (SimpleDeclaration)clause.getDeclarations().get(0);
    //		assertEquals( decl1.getDeclarators().size(), 1 );
    //		assertTrue(decl1.getDeclSpecifier().isConst());
    //		assertFalse(decl1.getDeclSpecifier().isVolatile());
    //		assertEquals( decl1.getDeclSpecifier().getType(), DeclSpecifier.t_char);
    //		Declarator declarator1 = (Declarator)decl1.getDeclarators().get( 0 );
    //		assertEquals( declarator1.getName().toString(), "parm1" );
    //		List ptrOps1 = declarator1.getPointerOperators();
    //		assertNotNull( ptrOps1 );
    //		assertEquals( 1, ptrOps1.size() );
    //		PointerOperator po1 = (PointerOperator)ptrOps1.get(0);
    //		assertNotNull( po1 );
    //		assertFalse( po1.isConst() );
    //		assertFalse( po1.isVolatile() );
    //		assertEquals( po1.getType(), PointerOperator.t_pointer );
    //        
    //		SimpleDeclaration declaration =
    // (SimpleDeclaration)clause.getDeclarations().get(1);
    //		assertEquals( declaration.getDeclSpecifier().getType(),
    // DeclSpecifier.t_int );
    //		assertEquals( declaration.getDeclarators().size(), 1);
    //		assertNull( ((Declarator)declaration.getDeclarators().get(0)).getName()
    // );
    //		assertNotNull(
    // ((Declarator)declaration.getDeclarators().get(0)).getDeclarator() );
    //		assertEquals(
    // ((Declarator)declaration.getDeclarators().get(0)).getDeclarator().getName().toString(),
    // "parm2" );
    //		ParameterDeclarationClause clause2 =
    // ((Declarator)declaration.getDeclarators().get(0)).getParms();
    //		assertEquals( clause2.getDeclarations().size(), 1 );
    //		assertEquals(
    // ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().size(),
    // 1 );
    //		assertNull(
    // ((Declarator)((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclarators().get(0)).getName()
    // );
    //		assertEquals(
    // ((ParameterDeclaration)clause2.getDeclarations().get(0)).getDeclSpecifier().getType(),
    // DeclSpecifier.t_float );
    //	}

    public void testPointersToMemberFunctions() throws Exception {
        parse("void (A::*name)(void);"); 
    }

    public void testBug39550() throws Exception {
        parse("double x = 0x1.fp1;"); 
    }

    // digraphs/trigraphs have been temporarily remove
    public void testBug39552A(int x) throws Exception {
        Writer code = new StringWriter();
        code.write("%:define glue(x, y) x %:%: y	/* #define glue(x, y) x ## y. */\n"); 
        code.write("#ifndef glue\n"); 
        code.write("#error glue not defined!\n"); 
        code.write("#endif\n"); 

        code.write("%:define str(x) %:x		/* #define str(x) #x */\n"); 

        code.write("int main (int argc, char *argv<::>) /* argv[] */\n"); 
        code.write("glue (<, %) /* { */\n"); 
        code.write("			 /* di_str[] = */\n"); 
        code.write("  const char di_str glue(<, :)glue(:, >) = str(%:%:<::><%%>%:);\n"); 
        code.write("  /* Check the glue macro actually pastes, and that the spelling of\n"); 
        code.write("	 all digraphs is preserved.  */\n"); 
        code.write("  if (glue(strc, mp) (di_str, \"%:%:<::><%%>%:\"))\n"); 
        code.write("	err (\"Digraph spelling not preserved!\");\n"); 
        code.write("  return 0;\n"); 
        code.write("glue (%, >) /* } */\n"); 

        parse(code.toString());
    }

    // digraphs/trigraphs have been temporarily remove
    public void testBug39552B(int x) throws Exception {
        Writer code = new StringWriter();

        code.write("??=include <stdio.h>\n"); 
        code.write("??=define TWELVE 1??/\n"); 
        code.write("2\n"); 

        code.write("static const char str??(??) = \"0123456789??/n\";\n"); 

        code.write("int\n"); 
        code.write("main(void)\n"); 
        code.write("??<\n"); 
        code.write("  unsigned char x = 5;\n"); 
        code.write("  if (sizeof str != TWELVE)\n"); 
        code.write("	abort ();\n"); 
        code
                .write("  /* Test ^=, the only multi-character token to come from trigraphs.  */\n"); 
        code.write("  x ??'= 3;\n"); 
        code.write("  if (x != 6)\n"); 
        code.write("	abort ();\n"); 
        code.write("  if ((5 ??! 3) != 7)\n"); 
        code.write("	abort ();\n"); 
        code.write("  return 0;\n"); 
        code.write("??>\n"); 

        parse(code.toString());
    }

    public void testBug39553() throws Exception {
        parse("#define COMP_INC \"foobar.h\"  \n" + "#include COMP_INC\n");  //$NON-NLS-2$
    }

    public void testBug39537() throws Exception {
        parse("typedef foo<(U::id > 0)> foobar;"); 
    }

    public void testBug39546() throws Exception {
        parse("signed char c = (signed char) 0xffffffff;"); 
    }

    public void testIndirectDeclarators() throws Exception {
        parse("void (*x)( int );"); 
    }

    public void testBug39532() throws Exception {
        parse("class N1::N2::B : public A {};"); 
    }

    public void testBug39540() throws Exception {
        parse("class {} const null;"); 
    }

    public void testBug39530() throws Exception {
        parse("X sPassed(-1);"); 
    }

    public void testBug39526() throws Exception {
        parse("UnitList unit_list (String(\"keV\"));"); 
    }

    public void testBug39535() throws Exception {
        parse("namespace bar = foo;"); 
    }

    public void testBug39504B() throws Exception {
        parse("int y = sizeof (int*);"); 
    }

    public void testBug39505A() throws Exception {
        parse("int AD::* gp_down = static_cast<int AD::*>(gp_stat);"); 
    }

    public void testBug39505B() throws Exception {
        parse("int* gp_down = static_cast<int*>(gp_stat);"); 
    }

    public void testBug42985() throws Exception {
        parse("const int x = 4; int y = ::x;"); 
    }

    public void testBug40419() throws Exception {
        Writer code = new StringWriter();
        try {
            code.write("template <class T, class U>	struct SuperSubclass {\n"); 
            code.write("enum { value = (::Loki::Conversion<const volatile U*, const volatile T*>::exists && \n"); 
            code.write("!::Loki::Conversion<const volatile T*, const volatile void*>::sameType) };	};"); 
        } catch (IOException ioe) {
        }
        parse(code.toString());
    }

    public void testBug39556() throws Exception {
        parse("int *restrict ip_fn (void);", true, ParserLanguage.C); 
    }

    /**
     * Test code: struct Example { Example(); Example(int); ~Example();};
     * Purpose: tests a declaration in a class scope.
     */
    public void testBug43371() throws Exception {
        // Parse and get the translaton unit
        Writer code = new StringWriter();
        code.write("struct Example { Example(); Example(int); ~Example();};"); 
        parse(code.toString());
    }

    public void testBug43644() throws Exception {
        parse("void foo();{ int x; }", false); 
    }

    public void testBug43062() throws Exception {
        parse("class X { operator short  (); 	operator int unsigned(); operator int signed(); };"); 
    }

    public void testBug39531() throws Exception {
        parse("class AString { operator char const *() const; };"); 
    }

    public void testBug40007() throws Exception {
        parse("int y = #;", false); 
    }

    public void testBug40759() throws Exception {
        parse("#define X SomeName \n class X {};"); 
    }

    public void testBug44633() throws Exception {
        Writer writer = new StringWriter();
        writer.write("template <typename T> class A {};\n"); 
        writer.write("class B {  template <typename T> friend class A;\n"); 
        writer.write("void method();\n"); 
        writer.write("};\n"); 
        parse(writer.toString());
    }

    public void testBug39525() throws Exception {
        parse("C &(C::*DD)(const C &x) = &C::operator=;"); 
    }

    public void testBug41935() throws Exception {
        parse("namespace A	{  int x; } namespace B = A;"); 
    }

    public void testBug39528() throws Exception {
        Writer code = new StringWriter();
        try {
            code.write("struct B: public A {\n"); 
            code.write("  A a;\n"); 
            code.write("  B() try : A(1), a(2)\n"); 
            code.write("	{ throw 1; }\n"); 
            code.write("  catch (...)\n"); 
            code.write("	{ if (c != 3) r |= 1; }\n"); 
            code.write("};\n"); 
        } catch (IOException ioe) {
        }
        parse(code.toString());
    }

    public void testBug39538() throws Exception {
        parse("template C::operator int<float> ();"); 
    }

    public void testBug39536() throws Exception {
        Writer writer = new StringWriter();
        writer.write("template<class E>\n"); 
        writer.write("class X {\n"); 
        writer.write("X<E>();  // This fails \n"); 
        writer.write("inline X<E>(int); // This also fails \n"); 
        writer.write("inline ~X<E>(); // This works fine \n"); 
        writer.write("};\n"); 
        parse(writer.toString());
    }

    public void testBug39536A() throws Exception {
        parse("template<class E> class X { X<E>(); };"); 
    }

    public void testBug39536B() throws Exception {
        parse("template<class E> class X { inline X<E>(int); };"); 
    }

    public void testBug39542() throws Exception {
        parse("void f(int a, struct {int b[a];} c) {}"); 
    }

    //Here starts C99-specific section
    public void testBug39549() throws Exception {
        parse("struct X x = { .b = 40, .z = { sizeof(X), 42 }, .t[3] = 2, .t.f[3].x = A * B };", true, ParserLanguage.C); 
        // with trailing commas
        parse("struct X x = { .b = 40, .z = { sizeof(X), 42,}, .t[3] = 2, .t.f[3].x = A * B  ,};", true, ParserLanguage.C); 
    }

    public void testBug39551A() throws Exception {
        parse("extern float _Complex conjf (float _Complex);", true, ParserLanguage.C); 
    }

    public void testBug39551B() throws Exception {
        parse("_Imaginary double id = 99.99 * __I__;", true, ParserLanguage.C); 
    }

    public void testCBool() throws Exception {
        parse("_Bool x;", true, ParserLanguage.C); 
    }

    public void testBug39678() throws Exception {
        parse("char *s = L\"a\" \"b\";"); 
    }

    public void testBug43110() throws Exception {
        parse("void x( int y, ... );"); 
    }

    //	public void testBug44370() throws Exception
    //	{
    //		parse( "#define SWAP(x,y) {x|=y;y|=x;x|=y;}\n"); 
    //		Iterator macros = quickParseCallback.getMacros();
    //		assertNotNull(macros);
    //		assertTrue( macros.hasNext());
    //		IASTMacro swap = (IASTMacro) macros.next();
    //		assertFalse( macros.hasNext() );
    //		assertEquals( swap.getName(), "SWAP"); 
    //		assertEquals( swap.getMacroType(),
    // IMacroDescriptor.MacroType.FUNCTION_LIKE );
    //		String [] params = swap.getParameters();
    //		assertEquals( params.length, 2 );
    //		assertEquals( params[0], "x"); 
    //		assertEquals( params[1], "y"); 
    //		String completeSignature = swap.getCompleteSignature().trim();
    //		assertEquals( completeSignature, "#define SWAP(x,y) {x|=y;y|=x;x|=y;}");
    // 
    //		assertEquals( swap.getExpansionSignature().trim(),"{x|=y;y|=x;x|=y;}");
    // 
    //		IToken [] tokens = swap.getTokenizedExpansion();
    //		validateToken( tokens[0], IToken.tLBRACE);
    //		validateIdentifier( tokens[1], "x"); 
    //		validateToken( tokens[2], IToken.tBITORASSIGN );
    //		validateIdentifier( tokens[3], "y"); 
    //		validateToken( tokens[4], IToken.tSEMI );
    //		validateIdentifier( tokens[5], "y"); 
    //		validateToken( tokens[6], IToken.tBITORASSIGN );
    //		validateIdentifier( tokens[7], "x"); 
    //		validateToken( tokens[8], IToken.tSEMI );
    //		validateIdentifier( tokens[9], "x"); 
    //		validateToken( tokens[10], IToken.tBITORASSIGN );
    //		validateIdentifier( tokens[11], "y"); 
    //		validateToken( tokens[12], IToken.tSEMI );
    //		validateToken( tokens[13], IToken.tRBRACE );
    //	}
    //	/**
    //	 * @param token
    //	 * @param string
    //	 */
    //	private void validateIdentifier(IToken token, String identifierName ) {
    //		validateToken( token, IToken.tIDENTIFIER);
    //		assertEquals( token.getImage(), identifierName );
    //	}
    //	/**
    //	 * @param token
    //	 * @param i
    //	 */
    //	private void validateToken(IToken token, int signal) {
    //		assertEquals( token.getType(), signal );
    //	}

    public void testBug47752() throws Exception {
        //TODO requires CPPVisitor
        parse("void func( cFoo bar ) try {	} catch ( const char * error ){	}"); 
    }

    public void testBug47628() throws Exception {
        Writer writer = new StringWriter();
        writer.write("void h(char) { }\n"); 
        writer.write("void h(unsigned char) { }\n"); 
        writer.write("void h(signed char) { }  // not shown in outline, parsed as char\n"); 
        parse(writer.toString());
    }

    public void testBug44336() throws Exception {
        parse("class A {};  typedef typename A foo;"); 
    }

    public void testBug39705() throws Exception {
        parse("#ident \"@(#)filename.c   1.3 90/02/12\""); 
    }

    public void testBug45235() throws Exception {
        parse("class A { friend class B; friend void f(); }; "); 
    }

    public void testBug59179() throws Exception {
        parse("class __decl  main{  int main; };", false); 
    }

    public void testBug57652() throws Exception {
        parse("struct file_operations driver_fops = {  open: device_open, release: device_release	};", true, ParserLanguage.C, true); 
    }

    /**
     * @param string
     * @param b
     * @param c
     * @param d
     */
    protected void parse(String code, boolean expectedToPass,
            ParserLanguage lang, boolean gcc) throws Exception {

		FileContent codeReader = FileContent.create("<test-code>", code.toCharArray());
        IScannerInfo scannerInfo = new ScannerInfo();
        IScanner scanner= AST2BaseTest.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);
        ISourceCodeParser parser2 = null;
        if (lang == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = null;
            if (gcc)
                config = new GPPParserExtensionConfiguration();
            else
                config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.QUICK_PARSE,
                    NULL_LOG, config);
        } else {
            ICParserExtensionConfiguration config = null;
            if (gcc)
                config = new GCCParserExtensionConfiguration();
            else
                config = new ANSICParserExtensionConfiguration();

            parser2 = new GNUCSourceParser(scanner, ParserMode.QUICK_PARSE,
                    NULL_LOG, config);
        }
        IASTTranslationUnit tu = parser2.parse();
        if (parser2.encounteredError() && expectedToPass)
            throw new ParserException("FAILURE"); 
        if (expectedToPass)
        {
            if( lang == ParserLanguage.C )
            {
            	IASTProblem [] problems = CVisitor.getProblems(tu);
            	assertEquals( problems.length, 0 );
            }
            else if ( lang == ParserLanguage.CPP )
            {
            	IASTProblem [] problems = CPPVisitor.getProblems(tu);
            	assertEquals( problems.length, 0 );
            }
        }
    }

    public void testBug60142() throws Exception {
        parse("unsigned long var;"); 
    }

    public void testBug61431() throws Exception {
        for (int i = 0; i < 2; ++i) {
            ParserLanguage language = (i == 0) ? ParserLanguage.C
                    : ParserLanguage.CPP;
            parse("int k[][] = { {0, {1}, {2,3}};", false, language); 
        }
    }

    public void testBadIdentifier() throws Exception {
        parse("class 0302 { private: int stinks; };", false); 	
    }

    public void testBug67622() throws Exception {
        parse("const char * x = __FILE__;"); 
    }

    public void testBug68116() throws Exception {
        StringBuffer buffer = new StringBuffer("char dummy[] = \"0123456789"); 
        for (int i = 0; i < 5000; ++i)
            buffer.append("0123456789"); 
        buffer.append("\";"); 
        parse(buffer.toString());
    }

    public void testBug69161() throws Exception {
        Writer writer = new StringWriter();
        writer.write("#define MACRO(s) s\n "); 
        writer.write("char *testQueries[] =\n"); 
        writer.write("{\n"); 
        writer.write("MACRO(\",\"),\n"); 
        writer.write("MACRO(\"(\"),\n"); 
        writer.write("MACRO(\")\")\n"); 
        writer.write("};\n"); 
        parse(writer.toString());
    }

    public void testBug73524() throws Exception {
        Writer writer = new StringWriter();
        writer.write("static char fmt_1002[] = \"(/,\\002At iterate\\002,i5,4x,\\002f= \\002,1p,d12\\\r\n"); 
        writer.write(".5,4x,\\002|proj g|= \\002,1p,d12.5)\";"); 
        parse(writer.toString(), true, ParserLanguage.C);
    }

    public void testBug39694() throws Exception {
        parse("int ab$cd = 1;"); 
    }

    public void testBug39704A() throws Exception {
    	StringWriter writer = new StringWriter();
    	writer.write("#define __declspec(x) __attribute__((x))"); 
    	writer.write("__declspec (dllimport) int foo;"); 
        parse( writer.toString() );
    }

    public void testBug39704D() throws Exception {
    	StringWriter writer = new StringWriter();
    	writer.write("#define __declspec(x) __attribute__((x))"); 
    	writer.write("__declspec(dllexport) int func1 (int a) {}"); 
        parse( writer.toString() );    	
    }

    public void testBug39695() throws Exception {
        parse("int a = __alignof__ (int);", true, ParserLanguage.CPP, true); 
    }

    public void testBug39684() throws Exception {
        parse("typeof(foo(1)) bar () { return foo(1); }", true, ParserLanguage.CPP, true); 
    }

    public void testBug39703() throws Exception {
        Writer code = new StringWriter();
        code.write("/* __extension__ enables GNU C mode for the duration of the declaration.  */\n"); 
        code.write("__extension__ struct G {\n"); 
        code.write("  struct { char z; };\n"); 
        code.write("  char g;\n"); 
        code.write("};\n"); 
        parse(code.toString(), true, ParserLanguage.CPP, true);
    }

    public void testBug39698A() throws Exception {
        parse("int c = a <? b;", true, ParserLanguage.CPP, true); 
    }

    public void testBug39698B() throws Exception {
        parse("int c = a >? b;", true, ParserLanguage.CPP, true); 
    }

    public void testBug39554() throws Exception {
        parse("_Pragma(\"foobar\")", true, ParserLanguage.C); 
    }

    public void testBug39704B() throws Exception {
        parse("extern int (* import) (void) __attribute__((dllimport));", true, ParserLanguage.CPP, true); 
    }

    public void testBug39704C() throws Exception {
        parse("int func2 (void) __attribute__((dllexport));", true, ParserLanguage.CPP, true); 
    }

    public void testBug39686() throws Exception {
        Writer code = new StringWriter();
        code.write("__complex__ double x; // complex double\n"); 
        code.write("__complex__ short int a; // complex short int\n"); 
        code
                .write("__complex__ float y = 2.5fi; // 2.5 imaginary float literal\n"); 
        code.write("__complex__ int a = 3i; // imaginary intege r literal\n"); 
        code.write("double v = __real__ x; // real part of expression\n"); 
        code.write("double w = __imag__ x; // imaginary part of expression\n"); 
        parse(code.toString(), true, ParserLanguage.C, true);
    }

    public void testBug39681() throws Exception {
        Writer code = new StringWriter();
        code.write("double\n"); 
        code.write("foo (double a, double b)\n"); 
        code.write("{\n"); 
        code.write("  double square (double z) { return z * z; }\n"); 
        code.write("  return square (a) + square (b);\n"); 
        code.write("}\n"); 
        parse(code.toString());
    }

    public void testBug39677() throws Exception {
        parse("B::B() : a(({ 1; })) {}", true, ParserLanguage.CPP, true); 
        Writer writer = new StringWriter();
        writer.write("B::B() : a(( { int y = foo (); int z;\n"); 
        writer.write("if (y > 0) z = y;\n"); 
        writer.write("else z = - y;\n");
        writer.write("z; })) {}\n");
        parse(writer.toString(), true, ParserLanguage.CPP, true);
        writer = new StringWriter();
        writer.write("int x = ({ int y = foo (); int z;\n"); 
        writer.write("if (y > 0) z = y;\n"); 
        writer.write("else z = - y;\n");
        writer.write("z; });\n");
        parse(writer.toString(), true, ParserLanguage.CPP, true);
        writer = new StringWriter();
        writer.write("typeof({ int y = foo (); int z;\n"); 
        writer.write("if (y > 0) z = y;\n"); 
        writer.write("else z = - y;\n");
        writer.write("z; }) zoot;\n");
        parse(writer.toString(), true, ParserLanguage.CPP, true);
    }

    public void testBug39701A() throws Exception {
        parse("extern template int max (int, int);", true, ParserLanguage.CPP, true); 
    }

    public void testBug39701B() throws Exception {
        parse("inline template class Foo<int>;", true, ParserLanguage.CPP, true); 
    }

    public void testBug39701C() throws Exception {
        parse("static template class Foo<int>;", true, ParserLanguage.CPP, true); 
    }

}
