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
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.CharOperation;
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
		
		MethodDeclarationPattern functionPattern = (MethodDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "functionDecl/c".toCharArray(), functionPattern.indexEntryPrefix() ), 0);
		
		functionPattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "rt*()", FUNCTION, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "functionDecl/rt".toCharArray(), functionPattern.indexEntryPrefix() ), 0);
				
		functionPattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "Ac", FUNCTION, REFERENCES, false );
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
		
		assertEquals( matches.size(), 4 );
	}
	
	public void testMethodDeclarationWithParams() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::f( A )", METHOD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );	
	}
	
	public void testMethodDeclarationParameterMatching(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "f( A & )", METHOD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "f( A * )", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "f( int &, const char  [],  A** )", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testOperators_bug43063_bug42979(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "operator \\*", METHOD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		IMatch match1 = (IMatch) matches.iterator().next();
		
		pattern = SearchEngine.createSearchPattern( "operator \\*", METHOD, DEFINITIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		IMatch match2 = (IMatch) matches.iterator().next();

		assertTrue( match1.getStartOffset() == match2.getStartOffset() );
		
		pattern = SearchEngine.createSearchPattern( "operator \\*=", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		match1 = (IMatch) matches.iterator().next();
		
		pattern = SearchEngine.createSearchPattern( "operator \\*=", METHOD, DEFINITIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		match2 = (IMatch) matches.iterator().next();

		assertTrue( match1.getStartOffset() != match2.getStartOffset() );
		
		pattern = SearchEngine.createSearchPattern( "operator *", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 6 ); //3 in classDecl.cpp
	}
	
	public void testBug43498(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "operator ?elete", METHOD, DECLARATIONS, true );

		assertTrue( pattern instanceof MethodDeclarationPattern );		
		MethodDeclarationPattern methodPattern = (MethodDeclarationPattern) pattern;
		
		char [] string = new char[] {'o','p','e','r','a','t','o','r',' ','?','e','l','e','t','e'};
		assertTrue( CharOperation.equals( string, methodPattern.getSimpleName() ) );
		
		pattern = SearchEngine.createSearchPattern( "operator delete", METHOD, DECLARATIONS, true );
		assertTrue( pattern instanceof MethodDeclarationPattern );		
		methodPattern = (MethodDeclarationPattern) pattern;
		string = new char[] {'o','p','e','r','a','t','o','r',' ','d','e','l','e','t','e'};
		assertTrue( CharOperation.equals( string, methodPattern.getSimpleName() ) );
		
		pattern = SearchEngine.createSearchPattern( "word?word", METHOD, DECLARATIONS, true );
		assertTrue( pattern instanceof MethodDeclarationPattern );		
		methodPattern = (MethodDeclarationPattern) pattern;

		string = new char[] {'w','o','r','d','?','w','o','r','d'};
		assertTrue( CharOperation.equals( string, methodPattern.getSimpleName() ) );
	}
	
	public void testBug43062(){
		MethodDeclarationPattern pattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "operator const short &", METHOD, DECLARATIONS, true );
		char [] string = new char [] { 'o','p','e','r','a','t','o','r',' ','c','o','n','s','t',' ','s','h','o','r','t',' ','&' };
		assertTrue( CharOperation.equals( string, pattern.getSimpleName() ) );
		
		pattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "operator short", METHOD, DECLARATIONS, true );
		string = new char [] { 'o','p','e','r','a','t','o','r',' ','s','h','o','r','t' };
		assertTrue( CharOperation.equals( string, pattern.getSimpleName() ) );
				
		pattern = (MethodDeclarationPattern) SearchEngine.createSearchPattern( "operator short int", METHOD, DECLARATIONS, true );
		string = new char [] { 'o','p','e','r','a','t','o','r',' ','s','h','o','r','t',' ','i','n','t' };
		assertTrue( CharOperation.equals( string, pattern.getSimpleName() ) );
	}
	
	public void testConstructorDestructor(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "~A", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testLookupForDefinition(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "turn", METHOD, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		pattern = SearchEngine.createSearchPattern( "Direction::turn", METHOD, DEFINITIONS, true );
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 0 );
	}
}
