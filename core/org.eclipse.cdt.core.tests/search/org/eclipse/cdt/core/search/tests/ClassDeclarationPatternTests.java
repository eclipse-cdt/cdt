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
 * Created on Jul 3, 2003
 */
package org.eclipse.cdt.core.search.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.matching.ClassDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
//import org.eclipse.cdt.internal.ui.search.Match;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClassDeclarationPatternTests extends BaseSearchTest implements ICSearchConstants {

	private MatchLocator matchLocator;
	
	private String cppPath;
	
	public ClassDeclarationPatternTests(String name) {
		super(name);
	}
	
	public void testMatchSimpleDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", TYPE, DECLARATIONS, true );
		
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
	}
	
	public void testMatchNamespaceNestedDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "NS::B", TYPE, DECLARATIONS, true );
		
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		ClassDeclarationPattern clsPattern = (ClassDeclarationPattern)pattern;
		
		assertTrue( CharOperation.equals( new char[] { 'B' }, clsPattern.getName() ) );
		assertTrue( clsPattern.getContainingTypes().length == 1 );
		assertTrue( CharOperation.equals( new char[] { 'N', 'S' }, clsPattern.getContainingTypes()[0] ) );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testBug39652() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B", TYPE, DECLARATIONS, true );
		
		search( workspace, pattern, scope, resultCollector );
		List matches = resultCollector.getSearchResults();
		
		/* Test should find 1 match */
		assertTrue( matches != null );
		assertTrue( matches.size() == 1 );
				
		pattern = SearchEngine.createSearchPattern( "NS::NS2::a", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		matches = resultCollector.getSearchResults();
		assertTrue( matches != null );
		
		pattern = SearchEngine.createSearchPattern( "NS::B::A", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		matches = resultCollector.getSearchResults();
		assertTrue( matches != null );
	}
	
	public void testMatchStruct(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", STRUCT, DECLARATIONS, true );
		
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "NS::B::A", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		
		List matches2 = resultCollector.getSearchResults();
		assertTrue( matches2 != null );
		assertEquals( matches2.size(), 1 );
		
		Iterator iter = matches.iterator();
		Iterator iter2 = matches2.iterator();
		
		IMatch match = (IMatch)iter.next();
		IMatch match2 = (IMatch)iter2.next();
		
		//assertTrue( match.path.equals( match2.path ) );
		assertEquals( match.getStartOffset(), match2.getStartOffset() );
		assertEquals( match.getEndOffset(), match2.getEndOffset() );
	}
	
	public void testWildcardQualification() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::*::A", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 0 );
		
		pattern = SearchEngine.createSearchPattern( "NS::*::A", TYPE, DECLARATIONS, false );
		search( workspace, pattern, scope, resultCollector );
		
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
	}
	
	public void testElaboratedType(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "struct A", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "union u", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );

		pattern = SearchEngine.createSearchPattern( "union ::*::u", TYPE, DECLARATIONS, true );
		search( workspace, pattern, scope, resultCollector );

		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testClassIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "struct A::B::*::c", TYPE, DECLARATIONS, true );
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		ClassDeclarationPattern clsPattern = (ClassDeclarationPattern)pattern;
		assertEquals( CharOperation.compareWith( "typeDecl/S/c/".toCharArray(), clsPattern.indexEntryPrefix() ), 0);
		
		clsPattern = (ClassDeclarationPattern) SearchEngine.createSearchPattern( "class ::*::A::B::c", TYPE, DECLARATIONS, true );
		assertEquals( CharOperation.compareWith( "typeDecl/C/c/B/A/".toCharArray(), clsPattern.indexEntryPrefix() ), 0);
				
		clsPattern = (ClassDeclarationPattern) SearchEngine.createSearchPattern( "enum ::RT*::c", TYPE, REFERENCES, true );
		assertEquals( CharOperation.compareWith( "typeRef/E/c/RT".toCharArray(), clsPattern.indexEntryPrefix() ), 0);
				
		clsPattern = (ClassDeclarationPattern) SearchEngine.createSearchPattern( "union A::B::c", TYPE, REFERENCES, false );
		assertEquals( CharOperation.compareWith( "typeRef/U/".toCharArray(), clsPattern.indexEntryPrefix() ), 0);
	}
	
	public void testGloballyQualifiedItem(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::A", TYPE, DECLARATIONS, true );
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );

		pattern = SearchEngine.createSearchPattern( "::u", TYPE, DECLARATIONS, true );
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		search( workspace, pattern, scope, resultCollector );
		
		matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );		
	}
	
	public void testClassReferences(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::A", TYPE, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 3 );
	}
	
	public void testClassReferenceInFieldType(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS::B::A", TYPE, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "NS::B" ) );
	}
	
	public void testTypeReferenceVisibleByUsingDirective(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS::NS2::a", STRUCT, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "NS::B" ) );
	}
	
	public void testEnumerationReferenceVisibleByInheritance(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS::B::e", ENUM, REFERENCES, true );
		
		search( workspace, pattern, scope, resultCollector );
		
		List matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );

		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "NS3::C" ) );
	}
	
}
