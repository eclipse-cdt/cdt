/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseBaseTest;

/**
 * @author jcamelon
 *
 */
public class FailedCompleteParseASTExpressionTest extends CompleteParseBaseTest
{
    /**
     * 
     */
    public FailedCompleteParseASTExpressionTest()
    {
        super();
    }
    /**
     * @param name
     */
    public FailedCompleteParseASTExpressionTest(String name)
    {
        super(name);
    }
    
	// IASTExpression.Kind.POSTFIX_FUNCTIONCALL
	public void testBug42822() throws Exception
	{
		Iterator i = parse( "int foo( float b );  int bar( int a, int b ); int test( void ) { int x = bar( foo( 3.0 ), foo( 5.0 ) ) ; }").getDeclarations();
		IASTFunction foo = (IASTFunction)i.next(); 
		IASTFunction bar = (IASTFunction)i.next(); 
		IASTFunction test = (IASTFunction)i.next();
		assertFalse( i.hasNext() ); 
		assertEquals( callback.getReferences().size(), 6 ); // THIS IS WRONG, THIS SHOULD BE 3, 2 references of foo(), one reference of bar()
	}
    
	// IASTExpression.Kind.POSTFIX_SIMPLETYPE_*
	public void testBug42823() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "void foo( int anInt, short aShort, double aDouble, float aFloat, char aChar, wchar_t aWchar, signed aSigned, unsigned anUnsigned, bool aBool, long aLong );");
		buffer.append( "void test( void ) { int someInt = f( int(3), short(4), double(3.0), float(4.0), char( 'a'), wchar_t( 'a' ), signed( 2 ), unsigned( 3 ), bool( false ), long( 3L ) ); }");
		Iterator i = parse( buffer.toString() ).getDeclarations();
		IASTFunction foo = (IASTFunction)i.next();
		IASTFunction test = (IASTFunction)i.next();
		assertFalse( i.hasNext() );
		assertEquals( callback.getReferences().size(), 0 ); // should be 1
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
		assertEquals( ((IASTReference)references.next()).getReferencedElement(), foo ); // should be foo2 
		assertFalse( references.hasNext() );
	}
}
