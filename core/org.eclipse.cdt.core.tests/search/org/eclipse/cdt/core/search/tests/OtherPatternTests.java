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
import org.eclipse.cdt.internal.core.search.matching.FieldDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.NamespaceDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.OrPattern;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OtherPatternTests extends BaseSearchTest {

	/**
	 * @param name
	 */
	public OtherPatternTests(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
		
	public void testNamespaceIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::c", NAMESPACE, DECLARATIONS, true );
		assertTrue( pattern instanceof NamespaceDeclarationPattern );
		
		NamespaceDeclarationPattern nsPattern = (NamespaceDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "namespaceDecl/c/B/A".toCharArray(), nsPattern.indexEntryPrefix() ), 0);
		
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "::*::A::B::c", NAMESPACE, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "namespaceDecl/c/B/A/".toCharArray(), nsPattern.indexEntryPrefix() ), 0);
				
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "::RT*::c", NAMESPACE, REFERENCES, true );
		assertEquals( CharOperation.compareWith( "namespaceRef/c/RT".toCharArray(), nsPattern.indexEntryPrefix() ), 0);
				
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "A::B::c", NAMESPACE, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "namespaceRef/".toCharArray(), nsPattern.indexEntryPrefix() ), 0);
	}
	
	public void testVariableIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "c", VAR, DECLARATIONS, true );
		assertTrue( pattern instanceof FieldDeclarationPattern );
		
		FieldDeclarationPattern variablePattern = (FieldDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "typeDecl/V/c".toCharArray(), variablePattern.indexEntryPrefix() ), 0);
		
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "rt*", VAR, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "typeDecl/V/rt".toCharArray(), variablePattern.indexEntryPrefix() ), 0);
				
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "Ac", VAR, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "typeRef/V/".toCharArray(), variablePattern.indexEntryPrefix() ), 0);
		
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "A?c", VAR, REFERENCES, true );
		assertEquals( CharOperation.compareWith( "typeRef/V/A".toCharArray(), variablePattern.indexEntryPrefix() ), 0);
	}
	
	public void testFieldIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::c", FIELD, DECLARATIONS, true );
		assertTrue( pattern instanceof FieldDeclarationPattern );
		
		FieldDeclarationPattern fieldPattern = (FieldDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "fieldDecl/c/B/A".toCharArray(), fieldPattern.indexEntryPrefix() ), 0);
		
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "::*::A::B::c", FIELD, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "fieldDecl/c/B/A/".toCharArray(), fieldPattern.indexEntryPrefix() ), 0);
				
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "::RT*::c", FIELD, REFERENCES, true );
		assertEquals( CharOperation.compareWith( "fieldRef/c/RT".toCharArray(), fieldPattern.indexEntryPrefix() ), 0);
				
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "A::B::c", FIELD, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "fieldRef/".toCharArray(), fieldPattern.indexEntryPrefix() ), 0);
	}
	
	public void testNamespaceDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "NS*", NAMESPACE, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 3 );
	}
	
	public void testNamespaceReferenceInUsingDirective() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS::NS2", NAMESPACE, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "NS::B" ) );
	}
	
	public void testNamespaceReferenceInClassBaseClause(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS", NAMESPACE, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
	}
	
	public void testFieldDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "a*Struct", FIELD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "NS::B" ) );
	}
	
	public void testVariableDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "b?", VAR, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "" ) );		
	}
	
	public void testOrPattern(){
		OrPattern orPattern = new OrPattern();
		orPattern.addPattern( SearchEngine.createSearchPattern( "::NS::B::e", ENUM, REFERENCES, true ) );
		orPattern.addPattern( SearchEngine.createSearchPattern( "Hea*", CLASS, DECLARATIONS, true ) );
		
		search( workspace, orPattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 3 );
		
		orPattern = new OrPattern();
		orPattern.addPattern( SearchEngine.createSearchPattern( "b?", VAR, DECLARATIONS, true ) );
		orPattern.addPattern( SearchEngine.createSearchPattern( "a*Struct", FIELD, DECLARATIONS, true ) );
		orPattern.addPattern( SearchEngine.createSearchPattern( "::NS::NS2", NAMESPACE, REFERENCES, true ) );
		orPattern.addPattern( SearchEngine.createSearchPattern( "A::B::f( A )", METHOD, DECLARATIONS, true ) );
		
		search( workspace, orPattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 6 );
	}

	public void testMacroPattern(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "FOO", MACRO, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testEnumerators(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "One", FIELD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "NS::B::Two", FIELD, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
}
