/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 *
 */
public class CompleteParseProblemTest extends CompleteParseBaseTest {

	/**
	 * 
	 */
	public CompleteParseProblemTest() {
		super();
	}

	/**
	 * @param name
	 */
	public CompleteParseProblemTest(String name) {
		super(name);
	}
	
	public void testBadClassName() throws Exception
	{
		validateInvalidClassName("12345");	 //$NON-NLS-1$
		validateInvalidClassName("*"); //$NON-NLS-1$
	}

	/**
	 * @throws ParserException
	 * @throws ParserFactoryError
	 */
	protected void validateInvalidClassName( String name ) throws ParserException, ParserFactoryError {
		StringBuffer buffer = new StringBuffer( "class "); //$NON-NLS-1$
		
		buffer.append( name );
		buffer.append( " { };"); //$NON-NLS-1$
		String code = buffer.toString();
		parse( code, false ); 
		assertFalse( callback.problems.isEmpty() );
		assertEquals( callback.problems.size(), 1 );
		IProblem p = (IProblem) callback.problems.get( 0 );
		assertTrue( p.checkCategory( IProblem.SYNTAX_RELATED ));
		assertEquals( p.getID(), IProblem.SYNTAX_ERROR );
		assertEquals( p.getSourceStart(), code.indexOf( name )); //$NON-NLS-1$
		assertEquals( p.getSourceEnd(), code.indexOf( name ) + name.length() ); //$NON-NLS-1$
	}

	public void testBug68306() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "class Foo { int bar( int ); };\n" ); //$NON-NLS-1$
		buffer.append( "int Foo::bar( int ){}\n" ); //$NON-NLS-1$
		buffer.append( "int Foo::bar( int ){}  //error\n" ); //$NON-NLS-1$
		String code = buffer.toString();
		parse( code, false );
		assertFalse( callback.problems.isEmpty() );
		assertEquals( callback.problems.size(), 1 );
		IProblem p = (IProblem) callback.problems.get( 0 );
		assertTrue( p.checkCategory( IProblem.SEMANTICS_RELATED ));

	}

	public void testBug68931() throws Exception
	{
	    String code = "void foo(){ SomeUnknownType t; } "; //$NON-NLS-1$
	    parse( code, false );
	    
	    int start = code.indexOf( "SomeUnknownType" ); //$NON-NLS-1$
	    int end = start + 15;
	    
	    assertFalse( callback.problems.isEmpty() );
	    assertEquals( callback.problems.size(), 1 );
		IProblem p = (IProblem) callback.problems.get( 0 );
		assertTrue( p.checkCategory( IProblem.SEMANTICS_RELATED ));
		assertEquals( p.getSourceStart(), start );
		assertEquals( p.getSourceEnd(), end );
		assertEquals( p.getID(), IProblem.SEMANTIC_NAME_NOT_FOUND );   
	}
	
	public void testBug69744() throws Exception
	{
	    String code = "int f() {  try { } catch( foo bar ) {} catch ( ... ) {} }  int i;"; //$NON-NLS-1$
	    
	    Iterator i = parse( code, false ).getDeclarations();
	    
	    int start = code.indexOf( "foo" ); //$NON-NLS-1$
	    int end = start + 3;
	    
	    assertEquals( callback.problems.size(), 1 );
	    IProblem p = (IProblem) callback.problems.get( 0 );
	    
	    assertEquals( p.getSourceStart(), start );
	    assertEquals( p.getSourceEnd(), end );
	    assertEquals( p.getID(), IProblem.SEMANTIC_NAME_NOT_FOUND );
	    
	    IASTFunction f = (IASTFunction) i.next();
	    IASTVariable varI = (IASTVariable) i.next();
	}
	
	public void testBug69745() throws Exception
	{
	    StringBuffer buffer = new StringBuffer();
	    buffer.append( "namespace NS{ template < class T > int foo(){};  }   \n" ); //$NON-NLS-1$
	    buffer.append( "void f() { using NS::foo;  using NS::foo<int>;   }   \n" ); //$NON-NLS-1$
	    
	    String code = buffer.toString();
	    
	    parse( code, false );
	    
	    int start = code.indexOf( "using NS::foo<int>;" ); //$NON-NLS-1$
	    int end = start + "using NS::foo<int>;".length(); //$NON-NLS-1$
	    
	    assertEquals( callback.problems.size(), 1 );
	    IProblem p = (IProblem) callback.problems.get( 0 );
	    
	    assertEquals( p.getSourceStart(), start );
	    assertEquals( p.getSourceEnd(), end );
	    assertEquals( p.getID(), IProblem.SEMANTIC_INVALID_USING );
	}
}
