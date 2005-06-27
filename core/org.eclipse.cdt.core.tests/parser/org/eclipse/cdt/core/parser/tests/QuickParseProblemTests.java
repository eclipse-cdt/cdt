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

import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author jcamelon
 *
 */
public class QuickParseProblemTests extends BaseASTTest {

	/**
	 * @param a
	 */
	public QuickParseProblemTests(String a) {
		super(a);
	}
	
	public void testBadClassName() throws Exception
	{
		String code = "class 12345 { };";//$NON-NLS-1$
		parse( code, true, false ); 
		assertFalse( quickParseCallback.problems.isEmpty() );
		assertEquals( quickParseCallback.problems.size(), 1 );
		IProblem p = (IProblem) quickParseCallback.problems.get( 0 );
		assertTrue( p.checkCategory( IProblem.SYNTAX_RELATED ));
		assertEquals( p.getID(), IProblem.SYNTAX_ERROR );
		assertEquals( p.getSourceStart(), code.indexOf( "12345")); //$NON-NLS-1$
		assertEquals( p.getSourceEnd(), code.indexOf( "12345") + 5 ); //$NON-NLS-1$
	}
	

}
