/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserFactoryError;
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


}
