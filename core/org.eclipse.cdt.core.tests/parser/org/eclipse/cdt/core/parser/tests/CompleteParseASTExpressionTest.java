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
	// IASTExpression.Kind.PRIMARY_INTEGER_LITERAL 
	public void testExpressionResultValueWithSimpleTypes1() throws Exception
	{
		Iterator i = parse ("int f(int, int); \n int f(int); \n int x = f(1, 2+3);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_CHAR_LITERAL 
	public void testExpressionResultValueWithSimpleTypes2() throws Exception
	{
		Iterator i = parse ("int f(char, int); \n int f(char); \n int x = f('c');").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_FLOAT_LITERAL 
	public void testExpressionResultValueWithSimpleTypes3() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(float); \n int x = f(1.13);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_STRING_LITERAL 
	public void testExpressionResultValueWithSimpleTypes4() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(char*); \n int x = f(\"str\");").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL 
	public void testExpressionResultValueWithSimpleTypes5() throws Exception
	{
		Iterator i = parse ("int f(bool); \n int f(float); \n int x = f(true);").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f1 );
		 
	}	
	// IASTExpression.Kind.PRIMARY_EMPTY 
	public void testExpressionResultValueWithSimpleTypes6() throws Exception
	{
		Iterator i = parse ("int f(char); \n int f(void); \n int x = f();").getDeclarations();
		IASTFunction f1 = (IASTFunction) i.next();
		IASTFunction f2 = (IASTFunction) i.next();
		IASTVariable x  = (IASTVariable) i.next();
		Iterator references = callback.getReferences().iterator();
		IASTFunctionReference fr1 = (IASTFunctionReference) references.next();
		assertEquals( fr1.getReferencedElement(), f2 );
		 
	}	
	// IASTExpression.Kind.ID_EXPRESSION
	public void testExpressionResultValueWithReferenceTypes() throws Exception
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
	// IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION	
	public void testExpressionResultValueWithReferenceTypesAndPointers1() throws Exception
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
	// IASTExpression.Kind.ID_EXPRESSION ( refers to a pointer )
	public void testExpressionResultValueWithReferenceTypesAndPointers2() throws Exception
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
	// IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION
	public void testExpressionResultValueWithReferenceTypesAndPointers3() throws Exception
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
	// IASTExpression.Kind.POSTFIX_FUNCTIONCALL
	public void testBug42822() throws Exception
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
	
	// IASTExpression.Kind.POSTFIX_SIMPLETYPE_*
	public void testBug42823() throws Exception
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

	// IASTExpression.Kind.POSTFIX_INCREMENT
	public void testBug42822B() throws Exception
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

}
