/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 23, 2003
 */
package org.eclipse.cdt.core.search.tests;

import java.util.Set;

import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.matching.FunctionDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.MethodDeclarationPattern;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FunctionMethodPatternTests extends BaseSearchTest {

	/**
	 * @param name
	 */
	public FunctionMethodPatternTests(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public void testFunctionIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "c()", FUNCTION, DECLARATIONS, true );
		assertTrue( pattern instanceof FunctionDeclarationPattern );
		
		FunctionDeclarationPattern functionPattern = (FunctionDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "functionDecl/c".toCharArray(), functionPattern.indexEntryPrefix() ), 0);
		
		functionPattern = (FunctionDeclarationPattern) SearchEngine.createSearchPattern( "rt*()", FUNCTION, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "functionDecl/rt".toCharArray(), functionPattern.indexEntryPrefix() ), 0);
				
		functionPattern = (FunctionDeclarationPattern) SearchEngine.createSearchPattern( "Ac", FUNCTION, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "functionRef/".toCharArray(), functionPattern.indexEntryPrefix() ), 0);
	}
	
	public void testMethodIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::c", METHOD, DECLARATIONS, true );
		assertTrue( pattern instanceof MethodDeclarationPattern );
		
		MethodDeclarationPattern methodPattern = (MethodDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "methodDecl/c/B/A".toCharArray(), methodPattern.indexEntryPrefix() ), 0);
		
		methodPattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "::*::A::B::c", METHOD, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "methodDecl/c/B/A/".toCharArray(), methodPattern.indexEntryPrefix() ), 0);
				
		methodPattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "::RT*::c", METHOD, REFERENCES, true );
		assertEquals( CharOperation.compareWith( "methodRef/c/RT".toCharArray(), methodPattern.indexEntryPrefix() ), 0);
				
		methodPattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "A::B::c", METHOD, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "methodRef/".toCharArray(), methodPattern.indexEntryPrefix() ), 0);
	}
	
	public void testMethodDeclaration() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::f", METHOD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );
	}
	
	public void testMethodDeclarationWithParams() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::f( A )", METHOD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );	}
}
