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
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFieldReference;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTVariableReference;

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
		Iterator i = parse ("int f(char); \n int f(void); \n int x = f();").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// Kind PRIMARY_INTEGER_LITERAL : int 
	public void testPrimaryIntegerLiteral() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, 2+3);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}	
	// Kind PRIMARY_CHAR_LITERAL : char
	public void testPrimaryCharLiteral() throws Exception
	{
		Iterator i = parse ("int f(char, int); \n int f(char); \n int x = f('c');").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// Kind PRIMARY_FLOAT_LITERAL : float
	public void testPrimaryFloatLiteral() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(float); \n int x = f(1.13);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// Kind PRIMARY_STRING_LITERAL : char*
	public void testPrimaryStringLiteral() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(char*); \n int x = f(\"str\");").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// Kind PRIMARY_BOOLEAN_LITERAL : bool
	public void testPrimaryBooleanLiteral() throws Exception
	{
		Iterator i = parse ("int f(bool); \n int f(float); \n int x = f(true);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}
	// Kind PRIMARY_THIS
	
	// Kind PRIMARY_BRACKETED_EXPRESSION : LHS
	public void testPrimaryBracketedExpression() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, (2+3));").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
	}	 
	// Kind ID_EXPRESSION : type of the ID
	public void testIdExpression() throws Exception
	{
		Iterator i = parse ("class A{}a;  \n int f(A a); \n int f(void); \n int x = f(a);").getDeclarations();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}	
	// Kind ID_EXPRESSION ( refers to a pointer ) : pointer to type of ID
	public void testIdExpressionToPointer() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A *ia){} \n int f(void); \n int x = f(pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}
	// Kind POSTFIX_SUBSCRIPT
	 
	// Kind POSTFIX_FUNCTIONCALL : return type of called function
	public void testPostfixFunctioncallBug42822() throws Exception
	{
		Iterator i = parse( "int foo( float b );  int bar( int a, int b ); int test( void ) { int x = bar( foo( 3.0 ), foo( 5.0 ) ) ; }").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next(); 
		IASTFunction bar = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		Iterator references =  callback.getReferences().iterator();
		//THIS SHOULD BE 3, 2 references of foo(), one reference of bar()
		assertEquals( callback.getReferences().size(), 3 ); 
	}
	// Kind POSTFIX_SIMPLETYPE_* : simple type
	public void testPostfixSimpletypesBug42823() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "void foo( int anInt, short aShort, double aDouble, float aFloat, char aChar, wchar_t aWchar, signed aSigned, unsigned anUnsigned, bool aBool, long aLong );");
		buffer.append( "void test( void ) { int someInt = foo( int(3), short(4), double(3.0), float(4.0), char( 'a'), wchar_t( 'a' ), signed( 2 ), unsigned( 3 ), bool( false ), long( 3L ) ); }");
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction test = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		Iterator references = callback.getReferences().iterator();
		//should be 1		
		assertEquals( callback.getReferences().size(), 1 ); 
	}
	// Kind POSTFIX_TYPENAME_IDENTIFIER
	
	// Kind POSTFIX_TYPENAME_TEMPLATEID
	
	// Kind POSTFIX_DOT_IDEXPRESSION : type of member in the scope of the container
	public void testPostfixDotExpression() throws Exception{
		Iterator i = parse( "class A {int m;}; \n A  a; \n int foo(char); int foo( int ); \n int x = foo( a.m );").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator members = getDeclarations(cl);
		IASTField m = (IASTField)members.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr= (IASTClassReference)references.next();
		assertEquals(clr.getReferencedElement(), cl);
		IASTVariableReference ar = (IASTVariableReference)references.next();
		assertEquals(ar.getReferencedElement(), a);
		IASTFieldReference mr = (IASTFieldReference) references.next();
		assertEquals(mr.getReferencedElement(), m);
		IASTFunctionReference fr = (IASTFunctionReference) references.next();
		assertEquals(fr.getReferencedElement(), f2);   		
	}
	// Kind POSTFIX_ARROW_IDEXPRESSION : type of member in the scope of the container
	public void testPostfixArrowExpression() throws Exception{
		Iterator i = parse( "class A {int m;}; \n A * a; \n int foo(char); int foo( int ); \n int x = foo( a->m );").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator members = getDeclarations(cl);
		IASTField m = (IASTField)members.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr= (IASTClassReference)references.next();
		assertEquals(clr.getReferencedElement(), cl);
		IASTVariableReference ar = (IASTVariableReference)references.next();
		assertEquals(ar.getReferencedElement(), a);
		IASTFieldReference mr = (IASTFieldReference) references.next();
		assertEquals(mr.getReferencedElement(), m);
		IASTFunctionReference fr = (IASTFunctionReference) references.next();
		assertEquals(fr.getReferencedElement(), f2);   		
	}
	// Kind POSTFIX_DOT_TEMPL_IDEXPRESS 
	// Kind POSTFIX_ARROW_TEMPL_IDEXP
	// Kind POSTFIX_DOT_DESTRUCTOR
	// Kind POSTFIX_ARROW_DESTRUCTOR
	
	// Kind POSTFIX_INCREMENT : LHS 
	public void testPostfixIncrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( x++ ); } ").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); // should be foo2 
		assertFalse( references.hasNext() );
	}
	// Kind POSTFIX_DECREMENT : LHS
	public void testPostfixDecrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( x-- ); } ").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); // should be foo2 
		assertFalse( references.hasNext() );
	}
	
	// Kind POSTFIX_DYNAMIC_CAST 
/*	public void testPostfixDynamicCast() throws Exception{
		Iterator i = parse( "class A {}; class B : public A{}; \n B * b; \n int foo(); int foo( A* ); \n int x = foo( dynamic_cast<A*>(b) );").getDeclarations();
		IASTClassSpecifier cla = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTClassSpecifier clb = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable b  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), cla);
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), clb);
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), cla);
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), b);
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), cla);
		assertEquals(((IASTClassReference)references.next()).getReferencedElement(), f2);
	}
*/	
	// Kind POSTFIX_REINTERPRET_CAST
	// Kind POSTFIX_STATIC_CAST
	// Kind POSTFIX_CONST_CAST
	
	// Kind POSTFIX_TYPEID_EXPRESSION : LHS
	public void testPostfixTypeIdExpression() throws Exception{
		Iterator i = parse( "int foo(char); int foo( int ); \n int x = foo( typeid(5) );").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr = (IASTFunctionReference) references.next();
		assertEquals(fr.getReferencedElement(), f2);   		
		
	}
	// Kind POSTFIX_TYPEID_TYPEID : type of the ID
	public void testPostfixTypeIdTypeId() throws Exception{
		Iterator i = parse( "class A {}; \n A  a; \n int foo(A); int foo( int ); \n int x = foo( typeid(a) );").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr= (IASTClassReference)references.next();
		assertEquals(clr.getReferencedElement(), cl);
		IASTClassReference clr2= (IASTClassReference)references.next();
		assertEquals(clr2.getReferencedElement(), cl);
		IASTVariableReference ar = (IASTVariableReference)references.next();
		assertEquals(ar.getReferencedElement(), a);
		IASTFunctionReference fr = (IASTFunctionReference) references.next();
		assertEquals(fr.getReferencedElement(), f1);   		
		
	}
	// Kind UNARY_INCREMENT : LHS             
	public void testUnaryIncrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( ++x ); } ").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); // should be foo2 
		assertFalse( references.hasNext() );
	}	
	// Kind UNARY_DECREMENT : LHS             
	public void testUnaryDecrement() throws Exception
	{
		Iterator i = parse( "void foo(); int foo( int ); void test( void ) { int x = 5; int y = foo( --x ); } ").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		Iterator subDecls = getDeclarations( test );
		IASTVariable x = (IASTVariable)subDecls.next();
		IASTVariable y = (IASTVariable)subDecls.next();
		assertFalse( subDecls.hasNext() ); 
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); // should be foo2 
		assertFalse( references.hasNext() );
	}
	// Kind UNARY_STAR_CASTEXPRESSION : LHS + t_pointer	
	public void testUnaryStarCastExpression() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ia){} \n int f(void); \n int x = f(*pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
		
	}
	// Kind UNARY_AMPSND_CASTEXPRESSION : LHS + t_reference
	public void testUnaryAmpersandCastExpression() throws Exception
	{
		Iterator i = parse ("class A {}; \n A * pa; \n int f(A ** ia){} \n int f(void); \n int x = f(&pa);").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
		IASTVariable a  = (IASTVariable) i.next();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		IASTVariableReference ar1 = (IASTVariableReference) references.next();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( clr1.getReferencedElement(), cl );
		assertEquals( ar1.getReferencedElement(), a );
		assertEquals( fr1.getReferencedElement(), f1 );
	}
	// Kind UNARY_PLUS_CASTEXPRESSION  : LHS
	public void testUnaryPlusCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = foo( +5 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind UNARY_MINUS_CASTEXPRESSION : LHS
	public void testUnaryMinusCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = foo( -5 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind UNARY_NOT_CASTEXPRESSION : LHS   
	public void testUnaryNotCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); bool b=true; int x = foo( !b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind UNARY_TILDE_CASTEXPRESSION : LHS   
	public void testTildeNotCastExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( ~x );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );
	}
	// Kind UNARY_SIZEOF_UNARYEXPRESSION : unsigned int 
	public void testUnarySizeofUnaryExpression() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( sizeof(5) );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 1 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );
	}
	// Kind UNARY_SIZEOF_TYPEID : unsigned int          
	public void testUnarySizeofTypeId() throws Exception { 
		Iterator i = parse( "void foo(); int foo( int ); int x = 5; int y = foo( sizeof(x) );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		IASTVariable y = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), x );
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );
	}

	// Kind NEW_NEWTYPEID                
	// Kind NEW_TYPEID                   
	// There are so many ways to call new, only this case is handeled.
/*	public void testNewTypeId() throws Exception { 
		Iterator i = parse( "class A{}; void foo(); int foo( A a ); int x = foo( new A() );").getDeclarations();
		IASTClassSpecifier cl = (IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();		
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		//assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		IASTClassReference clr1 = (IASTClassReference) references.next();
		IASTClassReference clr2 = (IASTClassReference) references.next();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );
	}
*/
	// Kind DELETE_CASTEXPRESSION        
	// Kind DELETE_VECTORCASTEXPRESSION  
	// Kind CASTEXPRESSION               
	// Kind PM_DOTSTAR                   
	// Kind PM_ARROWSTAR          
	       
	// Kind MULTIPLICATIVE_MULTIPLY : usual arithmetic conversions
	public void testMultiplicativeMultiply() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( a * b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind MULTIPLICATIVE_DIVIDE : usual arithmetic conversions        
	public void testMultiplicativeDivide() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b / a );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}	
	// Kind MULTIPLICATIVE_MODULUS : usual arithmetic conversions      
	public void testMultiplicativeModulus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b % a );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}	
	// Kind ADDITIVE_PLUS : usual arithmetic conversions              
	public void testAdditivePlus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b + a );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}	
	// Kind ADDITIVE_MINUS : usual arithmetic conversions           
	public void testAdditiveMinus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( float ); int a = 3; float b=5.1 ; int x = foo( b - a );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}	
	// Kind SHIFT_LEFT : LHS
	public void testShiftLeft() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a << 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	                   
	// Kind SHIFT_RIGHT : LHS                  
	public void testShiftRight() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a >> 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	
	// Kind RELATIONAL_LESSTHAN : bool          
	public void testRelationalLessThan() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b < 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind RELATIONAL_GREATERTHAN : bool      
	public void testRelationalGreaterThan() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b > 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind RELATIONAL_LESSTHANEQUALTO : bool  
	public void testRelationalLessThanOrEqual() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b <= 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind RELATIONAL_GREATERTHANEQUALTO : bool
	public void testRelationalGreaterThanOrEqual() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b >= 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind EQUALITY_EQUALS : bool         
	public void testEqualityEquals() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b == 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind EQUALITY_NOTEQUALS : bool      
	public void testEqualityNotEquals() throws Exception { 
		Iterator i = parse( "void foo(); int foo( bool ); int b=5; int x = foo( b != 3 );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ANDEXPRESSION  : usual arithmetic conversions          
	public void testAndExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a & b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind EXCLUSIVEOREXPRESSION : usual arithmetic conversions      
	public void testExclusiveOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a ^ b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind INCLUSIVEOREXPRESSION : : usual arithmetic conversions     
	public void testInclusiveOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( int ); int a = 3; int b= 5; int x = foo( a | b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind LOGICALANDEXPRESSION : bool      
	public void testLogicalAndExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( bool ); bool a = true; bool b= false; int x = foo( a && b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind LOGICALOREXPRESSION  : bool      
	public void testLogicalOrExpression() throws Exception { 
		Iterator i = parse( "int foo(); int foo( bool ); bool a = true; bool b= false; int x = foo( a || b );").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable b = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 3 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), b ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo2 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind CONDITIONALEXPRESSION      
	
	// Kind THROWEXPRESSION
	            
	// Kind ASSIGNMENTEXPRESSION_NORMAL : LHS
	public void testAssignmentExpressionNormal() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a = 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	
	// Kind ASSIGNMENTEXPRESSION_PLUS : LHS  
	public void testAssignmentExpressionPlus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a += 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_MINUS : LHS 
	public void testAssignmentExpressionMinus() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a -= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_MULT : LHS  
	public void testAssignmentExpressionMulti() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a *= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_DIV : LHS   
	public void testAssignmentExpressionDiv() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a /= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_MOD : LHS   
	public void testAssignmentExpressionMod() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a %= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_LSHIFT : LHS
	public void testAssignmentExpressionLShift() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a >>= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_RSHIFT : LHS
	public void testAssignmentExpressionRShift() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a <<= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_AND : LHS
	public void testAssignmentExpressionAnd() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a &= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_OR : LHS 
	public void testAssignmentExpressionOr() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a |= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind ASSIGNMENTEXPRESSION_XOR : LHS
	public void testAssignmentExpressionXOr() throws Exception { 
		Iterator i = parse( "int foo(int); int foo( bool ); int a = 10; int x = foo( a ^= 5 );").getDeclarations();
		IASTFunction foo1 = (IASTFunction)i.next();
		IASTFunction foo2 = (IASTFunction)i.next(); 
		IASTVariable a = (IASTVariable)i.next();
		IASTVariable x = (IASTVariable)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 2 );
		Iterator references =callback.getReferences().iterator();
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), a ); 
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo1 ); 
		assertFalse( references.hasNext() );	
	}
	// Kind EXPRESSIONLIST : list of LHS, RHS
	// Already tested with each test trying to find a reference to function.
}
