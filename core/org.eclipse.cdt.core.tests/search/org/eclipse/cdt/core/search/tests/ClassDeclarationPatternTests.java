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
import java.util.Set;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.CharOperation;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.matching.ClassDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.cdt.internal.ui.search.Match;
import org.eclipse.core.runtime.Path;

import junit.framework.TestCase;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClassDeclarationPatternTests extends TestCase implements ICSearchConstants {

	private MatchLocator matchLocator;
	private CSearchResultCollector resultCollector;
	private String cppPath;
	
	public ClassDeclarationPatternTests(String name) {
		super(name);
	}
	
	private void initialize( ICSearchPattern pattern ){
		cppPath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
		cppPath += "resources/search/classDecl.cpp";
		
		resultCollector = new CSearchResultCollector();
		matchLocator = new MatchLocator( pattern, resultCollector, null, null );
	}
	
	public void testMatchSimpleDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", TYPE, DECLARATIONS, true );

		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		initialize( pattern );
		
		matchLocator.locateMatches( new String [] { cppPath }, null, null );		
		
		Set matches = resultCollector.getMatches();
		assertEquals( matches.size(), 2 );
	}
	
	public void testMatchNamespaceNestedDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "NS::B", TYPE, DECLARATIONS, true );
		
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		ClassDeclarationPattern clsPattern = (ClassDeclarationPattern)pattern;
		
		assertTrue( CharOperation.equals( new char[] { 'B' }, clsPattern.getName() ) );
		assertTrue( clsPattern.getContainingTypes().length == 1 );
		assertTrue( CharOperation.equals( new char[] { 'N', 'S' }, clsPattern.getContainingTypes()[0] ) );
		
		initialize( pattern );
		
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		
		Set matches = resultCollector.getMatches();
		assertEquals( matches.size(), 1 );
	}
	
	public void testBug39652() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B", TYPE, DECLARATIONS, true );
		
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		Set matches = resultCollector.getMatches();
		/* Test should find 1 match */
		assertTrue( matches != null );
		assertTrue( matches.size() == 1 );
				
		pattern = SearchEngine.createSearchPattern( "NS::NS2::a", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertTrue( matches != null );
		
		pattern = SearchEngine.createSearchPattern( "NS::B::A", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertTrue( matches != null );
	}
	
	public void testMatchStruct(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", STRUCT, DECLARATIONS, true );
		
		assertTrue( pattern instanceof ClassDeclarationPattern );
		
		ClassDeclarationPattern clsPattern = (ClassDeclarationPattern) pattern;
		
		initialize( pattern );
		
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		
		Set matches = resultCollector.getMatches();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "NS::B::A", TYPE, DECLARATIONS, true );
		
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		
		Set matches2 = resultCollector.getMatches();
		assertTrue( matches2 != null );
		assertEquals( matches2.size(), 1 );
		
		Iterator iter = matches.iterator();
		Iterator iter2 = matches2.iterator();
		
		Match match = (Match)iter.next();
		Match match2 = (Match)iter2.next();
		
		//assertTrue( match.path.equals( match2.path ) );
		assertEquals( match.start, match2.start );
		assertEquals( match.end, match2.end );
	}
	
	public void testWildcardQualification() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::*::A", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		
		Set matches = resultCollector.getMatches();
		assertEquals( matches, null );
		
		pattern = SearchEngine.createSearchPattern( "NS::*::A", TYPE, DECLARATIONS, false );
		initialize( pattern );
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		
		matches = resultCollector.getMatches();
		assertEquals( matches.size(), 2 );
	}
	
	public void testElaboratedType(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "struct A", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		Set matches = resultCollector.getMatches();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "union u", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertEquals( matches.size(), 2 );

		pattern = SearchEngine.createSearchPattern( "union ::*::u", TYPE, DECLARATIONS, true );
		initialize( pattern );
		matchLocator.locateMatches( new String [] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertEquals( matches.size(), 1 );
	}
	
}
