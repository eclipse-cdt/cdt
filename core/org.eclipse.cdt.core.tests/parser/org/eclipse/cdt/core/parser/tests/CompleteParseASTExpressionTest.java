/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CompleteParseASTExpressionTest extends CompleteParseBaseTest{
	
	public CompleteParseASTExpressionTest(String a)
	{
		super(a);
	}
	// Kind PRIMARY_EMPTY : void
	public void testPrimaryEmpty() throws Exception
	{
		Iterator i = parse ("int f(char); \r\n int f(void); \r\n int x = f();").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences(1, createTaskList( new Task( f2 ) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f()" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_INTEGER_LITERAL : int 
	public void testPrimaryIntegerLiteral() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, 2+3);").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f1 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(1, 2 + 3)" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_CHAR_LITERAL : char
	public void testPrimaryCharLiteral() throws Exception
	{
		Iterator i = parse ("int f(char, int); \n int f(char); \n int x = f('c');").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f('c')" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_FLOAT_LITERAL : float
	public void testPrimaryFloatLiteral() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(float); \n int x = f(1.13);").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(1.13)" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_STRING_LITERAL : char*
	public void testPrimaryStringLiteral() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(char*); \n int x = f(\"str\");").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(\"str\")" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_BOOLEAN_LITERAL : bool
	public void testPrimaryBooleanLiteral() throws Exception
	{
		Iterator i = parse ("int f(bool); \n int f(float); \n int x = f(true);").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f1 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(true)" ); //$NON-NLS-1$
	}
	// Kind PRIMARY_THIS : type of inner most enclosing structure scope
	public void testPrimaryThis() throws Exception
	{
		Iterator i = parse ("class A{ int m(); }; A a;  \n int f(void); \n int f(A * a); \n int A::m(){ int x = f(this); }").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		Iterator members = getDeclarations(cl);
		IASTMethod method = (IASTMethod)members.next();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTMethod   m  = (IASTMethod) i.next();
		Iterator r = callback.getReferences().iterator();
		assertAllReferences( 4, createTaskList( new Task( cl, 3 ), new Task( f2 )));
		
		Iterator body = getDeclarations( m );
		IASTVariable x = (IASTVariable) body.next();
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(this)" ); //$NON-NLS-1$
	}	
	// Kind PRIMARY_BRACKETED_EXPRESSION : LHS
	public void testPrimaryBracketedExpression() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, (2+3));").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f1 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(1, (2 + 3))" ); //$NON-NLS-1$
	}
	// Kind ID_EXPRESSION : type of the ID
	public void testIdExpression() throws Exception
	{
		Iterator i = parse ("class A{}a;  \n int f(A a); \n int f(void); \n int x = f(a);").getDeclarations(); //$NON-NLS-1$
		
		IASTVariable a  = (IASTVariable) i.next();
		IASTClassSpecifier cl = (IASTClassSpecifier)a.getAbstractDeclaration().getTypeSpecifier();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		
		assertAllReferences( 3, createTaskList( new Task( cl ), new Task( f1 ),new Task( a ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(a)" ); //$NON-NLS-1$
	}	
	// Kind ID_EXPRESSION ( refers to a pointer ) : pointer to type of ID
	public void testIdExpressionToPointer() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A *ia){} \n int f(void); \n int x = f(pa);").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task( cl, 2 ), new Task( f1 ), new Task( a ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(pa)" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_SUBSCRIPT	
	public void testPostfixSubscript() throws Exception
	{
		Iterator i = parse ("int pa[10]; \n int f(int ia){} \n int f(void); \n int x = f(pa[1]);").getDeclarations(); //$NON-NLS-1$
		IASTVariable pa  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();		
		assertAllReferences( 2, createTaskList( new Task( f1 ), new Task( pa ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(pa[1])" ); //$NON-NLS-1$
	}
		
 	public void testPostfixSubscriptA() throws Exception
	{
		Iterator i = parse ("int pa[10][5] ; \n int f(int ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations(); //$NON-NLS-1$
		IASTVariable pa  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 2, createTaskList( new Task( f1 ), new Task( pa ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(pa[1][2])" ); //$NON-NLS-1$
	}	 
  
 	public void testPostfixSubscriptB() throws Exception
	{
		Iterator i = parse ("int* pa[10][5] ; \n int f(int* ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations(); //$NON-NLS-1$
		IASTVariable pa  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 2, createTaskList( new Task( f1 ), new Task( pa ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(pa[1][2])" ); //$NON-NLS-1$
	}	
	
	public void testPostfixSubscriptWithReferences() throws Exception
	{
		Iterator i = parse ("class A{}; \n A *pa[10][5] ; \n int f(A* ia){} \n int f(void); \n int x = f(pa[1][2]);").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable pa  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task( cl, 2 ), new Task( pa ), new Task( f1 )));
	}
	
	// Kind POSTFIX_FUNCTIONCALL : return type of called function
	public void testPostfixFunctioncallBug42822() throws Exception
	{
		Iterator i = parse( "int foo( float b );  int bar( int a, int b ); int test( void ) { int x = bar( foo( 3.0 ), foo( 5.0 ) ) ; }").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next(); 
		IASTFunction bar = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( bar ), new Task( foo, 2 )));
		
		i = getDeclarations( test );
		IASTVariable x = (IASTVariable) i.next();
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "bar(foo(3.0), foo(5.0))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_SIMPLETYPE_* : simple type
	public void testPostfixSimpletypesBug42823() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "void foo( int anInt, short aShort, double aDouble, float aFloat, char aChar, wchar_t aWchar, signed aSigned, unsigned anUnsigned, bool aBool, long aLong );"); //$NON-NLS-1$
		buffer.append( "void test( void ) { int someInt = foo( int(3), short(4), double(3.0), float(4.0), char( 'a'), wchar_t( 'a' ), signed( 2 ), unsigned( 3 ), bool( false ), long( 3L ) ); }"); //$NON-NLS-1$
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction test = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 1, createTaskList( new Task( foo )));
		
		i = getDeclarations( test );
		IASTVariable someInt = (IASTVariable) i.next();
		IASTExpression exp = someInt.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(int(3), short(4), double(3.0), float(4.0), char('a'), wchar_t('a'), signed(2), unsigned(3), bool(false), long(3))" ); //$NON-NLS-1$
	}
	
	// Kind POSTFIX_TYPENAME_IDENTIFIER
	public void testPostfixTypenameIdentifier() throws Exception{
		Iterator i = parse( "class A {}; \n int foo(); int foo( A a ); \n int x = foo( typename A() );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 3, createTaskList( new Task( cl, 2 ), new Task( f2)  ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typename A())" ); //$NON-NLS-1$
	}
	
	// Kind POSTFIX_TYPENAME_TEMPLATEID
	public void testPostfixTypeNameTemplateId() throws Exception{
		Iterator i = parse( " template<class T> class A {}; int foo( A<int> a ); \n int x = foo( typename template A< int >() );").getDeclarations(); //$NON-NLS-1$
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();
		IASTFunction f = (IASTFunction) i.next();
		IASTVariable x = (IASTVariable) i.next();
		assertAllReferences( 3, createTaskList( new Task( A, 2 ), new Task( f ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typename template A<int>())" ); //$NON-NLS-1$
	}
	
	public void testPostfixTypeNameTemplateId_2() throws Exception{
		Iterator i = parse( "namespace NS{ template<class T> class A {}; } int foo( NS::A<int> a ); \n int x = foo( typename NS::template A< int >() );").getDeclarations(); //$NON-NLS-1$
		IASTNamespaceDefinition NS = (IASTNamespaceDefinition) i.next();
		IASTFunction f = (IASTFunction) i.next();
		IASTVariable x = (IASTVariable) i.next();

		i = getDeclarations( NS );
		IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier A = (IASTClassSpecifier) template.getOwnedDeclaration();

		assertAllReferences( 5, createTaskList( new Task( NS, 2 ), new Task( A, 2 ), new Task( f ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typename NS::template A<int>())" ); //$NON-NLS-1$
	}
	
	// Kind POSTFIX_DOT_IDEXPRESSION : type of member in the scope of the container
	public void testPostfixDotExpression() throws Exception{
		Iterator i = parse( "class A {int m;}; \n A  a; \n int foo(char); int foo( int ); \n int x = foo( a.m );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator members = getDeclarations(cl);
		IASTField m = (IASTField)members.next();
		assertAllReferences( 4, createTaskList( new Task(cl), new Task(a), new Task(m), new Task(f2) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a.m)" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_ARROW_IDEXPRESSION : type of member in the scope of the container
	public void testPostfixArrowExpression() throws Exception{
		Iterator i = parse( "class A {int m;}; \n A * a; \n int foo(char); int foo( int ); \n int x = foo( a->m );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator members = getDeclarations(cl);
		IASTField m = (IASTField)members.next();
		assertAllReferences( 4, createTaskList( new Task(cl), new Task(a), new Task(m), new Task(f2) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a->m)" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_DOT_TEMPL_IDEXPRESS 
	// Kind POSTFIX_ARROW_TEMPL_IDEXP
	
	// Kind POSTFIX_DOT_DESTRUCTOR
	// Kind POSTFIX_ARROW_DESTRUCTOR
	
	// Kind POSTFIX_INCREMENT : LHS 
	public void testPostfixIncrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( x++ ); } ").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( x ), new Task( foo2)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(x++)" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_DECREMENT : LHS
	public void testPostfixDecrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( x-- ); } ").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( x ), new Task( foo2)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(x--)" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_DYNAMIC_CAST 
	public void testPostfixDynamicCast() throws Exception{
		Iterator i = parse( "class A {}; class B : public A{}; \n A *a; \n int foo(); int foo( B* ); \n int x = foo( dynamic_cast<B*>(a) );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 6, createTaskList( new Task( cla, 2 ), new Task( clb, 2), new Task(a), new Task(f2)));		
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(dynamic_cast<B*>(a))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_REINTERPRET_CAST
	public void testPostfixReinterpretCast() throws Exception{
		Iterator i = parse( "int *a; \n int foo(); int foo( double* ); \n int x = foo( reinterpret_cast<double*>(a) );").getDeclarations(); //$NON-NLS-1$
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(a), new Task(f2)));	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(reinterpret_cast<double*>(a))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_STATIC_CAST
	public void testPostfixStaticCast() throws Exception{
		Iterator i = parse( "int a; \n int foo(); int foo( char ); \n int x = foo( static_cast<char>(a) );").getDeclarations(); //$NON-NLS-1$
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(a), new Task(f2)));		
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(static_cast<char>(a))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_CONST_CAST
	public void testPostfixConstCast() throws Exception{
		Iterator i = parse( "const int a; \n int foo(); int foo( int * ); \n int x = foo( const_cast<int *>(&a) );").getDeclarations(); //$NON-NLS-1$
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(a), new Task(f2)));		
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(const_cast<int*>(&a))" ); //$NON-NLS-1$
	}	
	// Kind POSTFIX_TYPEID_EXPRESSION : LHS
	public void testPostfixTypeIdExpression() throws Exception{
		Iterator i = parse( "int foo(char); int foo( int ); \n int x = foo( typeid(5) );").getDeclarations(); //$NON-NLS-1$
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 1, createTaskList( new Task( f2 ))); 	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typeid(5))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_TYPEID_EXPRESSION : type of the ID
	public void testPostfixTypeIdExpression2() throws Exception{
		Iterator i = parse( "class A {}; \n A  a; \n int foo(A); int foo( int ); \n int x = foo( typeid(a) );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task(cl, 2),new Task(a),new Task(f1)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typeid(a))" ); //$NON-NLS-1$
	}
	// Kind POSTFIX_TYPEID_TYPEID : type of the ID
	public void testPostfixTypeIdTypeId() throws Exception{
		Iterator i = parse( "class A {}; \n A  a; \n int foo(A); int foo( int ); \n int x = foo( typeid(A) );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task(cl, 3), new Task(f1)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typeid(A))" ); //$NON-NLS-1$
	}	
	// Kind POSTFIX_TYPEID_TYPEID : type of the ID
	public void testPostfixTypeIdTypeId2() throws Exception{
		Iterator i = parse( "class A {}; \n A  a; \n int foo(A); int foo( int ); \n int x = foo( typeid(const A) );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task(cl, 3), new Task(f1)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(typeid(const A))" ); //$NON-NLS-1$
	}	
	// Kind UNARY_INCREMENT : LHS             
	public void testUnaryIncrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( ++x ); } ").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(foo2), new Task(x) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(++x)" ); //$NON-NLS-1$
	}	
	// Kind UNARY_DECREMENT : LHS             
	public void testUnaryDecrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( --x ); } ").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(foo2), new Task(x) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(--x)" ); //$NON-NLS-1$	
	}
	// Kind UNARY_STAR_CASTEXPRESSION : LHS + t_pointer	
	public void testUnaryStarCastExpression() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ia){} \n int f(void); \n int x = f(*pa);").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task(cl, 2 ), new Task( a ), new Task(f1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(*pa)" ); //$NON-NLS-1$
	}
	// Kind UNARY_AMPSND_CASTEXPRESSION : LHS + t_reference
	public void testUnaryAmpersandCastExpression() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ** ia){} \n int f(void); \n int x = f(&pa);").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertAllReferences( 4, createTaskList( new Task(cl, 2 ), new Task( a ), new Task(f1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "f(&pa)" ); //$NON-NLS-1$
	}
	// Kind UNARY_PLUS_CASTEXPRESSION  : LHS
	public void testUnaryPlusCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = foo( +5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 1, createTaskList( new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(+5)" ); //$NON-NLS-1$
	}
	// Kind UNARY_MINUS_CASTEXPRESSION : LHS
	public void testUnaryMinusCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = foo( -5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 1, createTaskList( new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(-5)" ); //$NON-NLS-1$
	}
	// Kind UNARY_NOT_CASTEXPRESSION : LHS   
	public void testUnaryNotCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); bool b=true; int x = foo( !b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( b ), new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(!b)" ); //$NON-NLS-1$
	}
	// Kind UNARY_TILDE_CASTEXPRESSION : LHS   
	public void testTildeNotCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( ~x );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( x ), new Task( foo2 )));

		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(~x)" ); //$NON-NLS-1$
	}
	// Kind UNARY_SIZEOF_UNARYEXPRESSION : unsigned int 
	public void testUnarySizeofUnaryExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( sizeof(5) );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 1, createTaskList( new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(sizeof (5))" ); //$NON-NLS-1$
	}
	// Kind UNARY_SIZEOF_TYPEID : unsigned int          
	public void testUnarySizeofTypeId() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( sizeof(x) );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task( x ), new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "5" ); //$NON-NLS-1$
		exp = y.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(sizeof (x))" ); //$NON-NLS-1$
	}
	// Kind NEW_NEWTYPEID                
	// Kind NEW_TYPEID                   
	public void testNewTypeId() throws Exception { 
		Iterator i = parse( "class A{}; void foo(); int foo( A * a ); int x = foo( new A() );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( cl, 2), new Task( foo2 )));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(new A())" ); //$NON-NLS-1$
	}

	// Kind DELETE_CASTEXPRESSION        
	// Kind DELETE_VECTORCASTEXPRESSION
	  
	// Kind CASTEXPRESSION      
	public void testCastExpression() throws Exception{
		Iterator i = parse( "class A {}; class B : public A{}; \n B *b; \n int foo(); int foo( A* ); \n int x = foo( (A*)b );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable b  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 6, createTaskList( new Task( cla, 3 ), new Task( clb, 1), new Task(b), new Task(f2)));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo((A*)b)" ); //$NON-NLS-1$
	}
	         
	// Kind PM_DOTSTAR                   
	// failed
	
	// Kind PM_ARROWSTAR
	// failed 
	          
	// Kind MULTIPLICATIVE_MULTIPLY : usual arithmetic conversions
	public void testMultiplicativeMultiply() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( a * b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		assertAllReferences( 3, createTaskList( new Task(a), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a * b)" ); //$NON-NLS-1$
	}
	// Kind MULTIPLICATIVE_DIVIDE : usual arithmetic conversions        
	public void testMultiplicativeDivide() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b / a );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task(a), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b / a)" ); //$NON-NLS-1$
	}	
	// Kind MULTIPLICATIVE_MODULUS : usual arithmetic conversions      
	public void testMultiplicativeModulus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b % a );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task(a), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b % a)" ); //$NON-NLS-1$
	}	
	// Kind ADDITIVE_PLUS : usual arithmetic conversions              
	public void testAdditivePlus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b + a );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task(a), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b + a)" ); //$NON-NLS-1$
	}	
	// Kind ADDITIVE_MINUS : usual arithmetic conversions           
	public void testAdditiveMinus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b - a );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task(a), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b - a)" ); //$NON-NLS-1$
	}	
	// Kind SHIFT_LEFT : LHS
	public void testShiftLeft() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a << 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(a), new Task( foo1 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a << 5)" ); //$NON-NLS-1$
	}              
	// Kind SHIFT_RIGHT : LHS                  
	public void testShiftRight() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a >> 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(a), new Task( foo1 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a >> 5)" ); //$NON-NLS-1$
	}
	// Kind RELATIONAL_LESSTHAN : bool          
	public void testRelationalLessThan() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b < 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b < 3)" ); //$NON-NLS-1$
	}
	// Kind RELATIONAL_GREATERTHAN : bool      
	public void testRelationalGreaterThan() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b > 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b > 3)" ); //$NON-NLS-1$
	}
	// Kind RELATIONAL_LESSTHANEQUALTO : bool  
	public void testRelationalLessThanOrEqual() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b <= 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b <= 3)" ); //$NON-NLS-1$
	}
	// Kind RELATIONAL_GREATERTHANEQUALTO : bool
	public void testRelationalGreaterThanOrEqual() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b >= 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b >= 3)" ); //$NON-NLS-1$
	}
	// Kind EQUALITY_EQUALS : bool         
	public void testEqualityEquals() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b == 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b == 3)" ); //$NON-NLS-1$
	}
	// Kind EQUALITY_NOTEQUALS : bool      
	public void testEqualityNotEquals() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b != 3 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 2, createTaskList( new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(b != 3)" ); //$NON-NLS-1$
	}
	// Kind ANDEXPRESSION  : usual arithmetic conversions          
	public void testAndExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a & b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( a ), new Task(b), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a & b)" ); //$NON-NLS-1$
	}
	// Kind EXCLUSIVEOREXPRESSION : usual arithmetic conversions      
	public void testExclusiveOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a ^ b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( a ), new Task(b), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a ^ b)" ); //$NON-NLS-1$
	}
	// Kind INCLUSIVEOREXPRESSION : : usual arithmetic conversions     
	public void testInclusiveOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a | b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( a ), new Task(b), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a | b)" ); //$NON-NLS-1$
	}
	// Kind LOGICALANDEXPRESSION : bool      
	public void testLogicalAndExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( bool ); bool a = true; bool b= false; int x = foo( a && b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( a ), new Task(b), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a && b)" ); //$NON-NLS-1$
	}
	// Kind LOGICALOREXPRESSION  : bool      
	public void testLogicalOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( bool ); bool a = true; bool b= false; int x = foo( a || b );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 3, createTaskList( new Task( a ), new Task(b), new Task( foo2 ) ) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a || b)" ); //$NON-NLS-1$
	}
	// Kind CONDITIONALEXPRESSION : conditional Expression Conversions     
	public void testConditionalExpression() throws Exception { 
		Iterator i = parse( "int foo(bool); int foo(int); int a = 10, b = 4, c = 2; int x = foo( a > 5 ? b : c );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable c = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 4, createTaskList( new Task( a ), new Task(b), new Task( c ), new Task( foo2 ) ) );	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a > 5 ? b : c)" ); //$NON-NLS-1$
	}
	// Kind CONDITIONALEXPRESSION with references : conditional Expression Conversions      
	public void testConditionalExpressionWithReferencesA() throws Exception { 
		Iterator i = parse( "class A{}; class B : public A{}; int foo(); int foo(A*); A *a ; B *b; int c = 0; int x = foo( c > 5 ? b : a );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable c = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(8, createTaskList( new Task( cla, 3 ), new Task( clb ), new Task( c ), new Task( b ), new Task( a ), new Task( foo2 )) );
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(c > 5 ? b : a)" ); //$NON-NLS-1$
	}
	public void testConditionalExpressionWithReferencesB_Bug43106() throws Exception { 
		Iterator i = parse( "class A{}; class B : public A{}; int foo(); int foo(A&); A a ; B b; int c = 0; int x = foo( c > 5 ? b : a );").getDeclarations(); //$NON-NLS-1$
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable c = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences( 8, 
			createTaskList( new Task( cla, 3 ), new Task( clb ), new Task( c), new Task( b ), new Task( a ), new Task( foo2) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(c > 5 ? b : a)" ); //$NON-NLS-1$
	}	
	// Kind THROWEXPRESSION
	            
	// Kind ASSIGNMENTEXPRESSION_NORMAL : LHS
	public void testAssignmentExpressionNormal() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a = 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a = 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_PLUS : LHS  
	public void testAssignmentExpressionPlus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a += 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a += 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_MINUS : LHS 
	public void testAssignmentExpressionMinus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a -= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a -= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_MULT : LHS  
	public void testAssignmentExpressionMulti() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a *= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a *= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_DIV : LHS   
	public void testAssignmentExpressionDiv() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a /= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a /= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_MOD : LHS   
	public void testAssignmentExpressionMod() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a %= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a %= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_LSHIFT : LHS
	public void testAssignmentExpressionLShift() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a >>= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a >>= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_RSHIFT : LHS
	public void testAssignmentExpressionRShift() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a <<= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a <<= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_AND : LHS
	public void testAssignmentExpressionAnd() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a &= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a &= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_OR : LHS 
	public void testAssignmentExpressionOr() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a |= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a |= 5)" ); //$NON-NLS-1$
	}
	// Kind ASSIGNMENTEXPRESSION_XOR : LHS
	public void testAssignmentExpressionXOr() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a ^= 5 );").getDeclarations(); //$NON-NLS-1$
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertAllReferences(2, createTaskList( new Task(a), new Task(foo1) ));	
		
		IASTExpression exp = x.getInitializerClause().getAssigmentExpression(); 
		assertEquals( exp.toString(), "foo(a ^= 5)" ); //$NON-NLS-1$
	}
	// Kind EXPRESSIONLIST : list of LHS, RHS
	// Already tested with each test trying to find a reference to function.



}
