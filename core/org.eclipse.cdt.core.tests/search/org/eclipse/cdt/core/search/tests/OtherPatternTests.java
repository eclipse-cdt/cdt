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

import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.matching.FieldDeclarationPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.cdt.internal.core.search.matching.NamespaceDeclarationPattern;
import org.eclipse.core.runtime.Path;

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
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::c", NAMESPACE, DEFINITIONS, true ); //$NON-NLS-1$
		assertTrue( pattern instanceof NamespaceDeclarationPattern );
		
		NamespaceDeclarationPattern nsPattern = (NamespaceDeclarationPattern)pattern;
		assertEquals( getSearchPattern(IIndex.NAMESPACE, IIndex.ANY, IIndex.DEFINITION, "c/B/A"), nsPattern.indexEntryPrefix() ); //$NON-NLS-1$
		
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "::*::A::B::c", NAMESPACE, DEFINITIONS, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.NAMESPACE, IIndex.ANY, IIndex.DEFINITION, "c/B/A/"), nsPattern.indexEntryPrefix() ); //$NON-NLS-1$
				
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "::RT*::c", NAMESPACE, REFERENCES, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.NAMESPACE, IIndex.ANY, IIndex.REFERENCE, "c/RT"), nsPattern.indexEntryPrefix() ); //$NON-NLS-1$
				
		nsPattern = (NamespaceDeclarationPattern) SearchEngine.createSearchPattern( "A::B::c", NAMESPACE, REFERENCES, false ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.NAMESPACE, IIndex.ANY, IIndex.REFERENCE, ""), nsPattern.indexEntryPrefix() ); //$NON-NLS-1$
	}
	
	public void testVariableIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "c", VAR, DECLARATIONS, true ); //$NON-NLS-1$
		assertTrue( pattern instanceof FieldDeclarationPattern );
		
		FieldDeclarationPattern variablePattern = (FieldDeclarationPattern)pattern;
		assertEquals(  getSearchPattern(IIndex.TYPE, IIndex.TYPE_VAR, IIndex.DECLARATION, "c"), variablePattern.indexEntryPrefix() ); //$NON-NLS-1$
		
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "rt*", VAR, DECLARATIONS, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.TYPE, IIndex.TYPE_VAR, IIndex.DECLARATION, "rt"), variablePattern.indexEntryPrefix() ); //$NON-NLS-1$
				
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "Ac", VAR, REFERENCES, false ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.TYPE, IIndex.TYPE_VAR, IIndex.REFERENCE, ""), variablePattern.indexEntryPrefix() ); //$NON-NLS-1$
		
		variablePattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "A?c", VAR, REFERENCES, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.TYPE, IIndex.TYPE_VAR, IIndex.REFERENCE, "A"), variablePattern.indexEntryPrefix() ); //$NON-NLS-1$
	}
	
	public void testFieldIndexPrefix(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A::B::c", FIELD, DECLARATIONS, true ); //$NON-NLS-1$
		assertTrue( pattern instanceof FieldDeclarationPattern );
		
		FieldDeclarationPattern fieldPattern = (FieldDeclarationPattern)pattern;
		assertEquals( getSearchPattern(IIndex.FIELD, IIndex.ANY, IIndex.DECLARATION, "c/B/A"), fieldPattern.indexEntryPrefix() ); //$NON-NLS-1$
		
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "::*::A::B::c", FIELD, DECLARATIONS, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.FIELD, IIndex.ANY, IIndex.DECLARATION, "c/B/A/"), fieldPattern.indexEntryPrefix() ); //$NON-NLS-1$
				
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "::RT*::c", FIELD, REFERENCES, true ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.FIELD, IIndex.ANY, IIndex.REFERENCE, "c/RT"), fieldPattern.indexEntryPrefix() ); //$NON-NLS-1$
				
		fieldPattern = (FieldDeclarationPattern) SearchEngine.createSearchPattern( "A::B::c", FIELD, REFERENCES, false ); //$NON-NLS-1$
		assertEquals( getSearchPattern(IIndex.FIELD, IIndex.ANY, IIndex.REFERENCE, ""), fieldPattern.indexEntryPrefix() ); //$NON-NLS-1$
	}
	
	public void testNamespaceDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "NS*", NAMESPACE, DEFINITIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 3 );
	}
	
	public void testNamespaceReferenceInUsingDirective() {
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS::NS2", NAMESPACE, REFERENCES, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 1 );
	}
	
	public void testNamespaceReferenceInClassBaseClause(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "::NS", NAMESPACE, REFERENCES, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
	}
	
	public void testFieldDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "a*Struct", FIELD, DEFINITIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testVariableDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "b?", VAR, DEFINITIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getParentName().equals( "" ) );		 //$NON-NLS-1$
	}
	
	/*public void testParameterDeclaration(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "index", VAR, DECLARATIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( 5, matches.size());
	}
	
	public void testOrPattern(){
		OrPattern orPattern = new OrPattern();
		orPattern.addPattern( SearchEngine.createSearchPattern( "::NS::B::e", ENUM, REFERENCES, true ) ); //$NON-NLS-1$
		orPattern.addPattern( SearchEngine.createSearchPattern( "Hea*", CLASS, DECLARATIONS, true ) ); //$NON-NLS-1$
		
		search( workspace, orPattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		
		assertEquals( matches.size(), 3 );
		
		orPattern = new OrPattern();
		orPattern.addPattern( SearchEngine.createSearchPattern( "b?", VAR, DECLARATIONS, true ) ); //$NON-NLS-1$
		orPattern.addPattern( SearchEngine.createSearchPattern( "a*Struct", FIELD, DECLARATIONS, true ) ); //$NON-NLS-1$
		orPattern.addPattern( SearchEngine.createSearchPattern( "::NS::NS2", NAMESPACE, REFERENCES, true ) ); //$NON-NLS-1$
		orPattern.addPattern( SearchEngine.createSearchPattern( "A::B::f( A )", METHOD, DECLARATIONS, true ) ); //$NON-NLS-1$
		
		search( workspace, orPattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals(  5, matches.size() );
	}*/

	public void testMacroPattern(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "FOO", MACRO, DECLARATIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getName().equals( "FOO" ) ); //$NON-NLS-1$
		assertTrue( match.getParentName().equals( "" )); //$NON-NLS-1$
		
		pattern = SearchEngine.createSearchPattern( "FOO", MACRO, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
	
	public void testDerived(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", DERIVED, DECLARATIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getName().equals( "A" ) ); //$NON-NLS-1$
	}
	
	public void testEnumerators(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "One", ENUMTOR, DEFINITIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getName().equals( "One" ) ); //$NON-NLS-1$
		
		pattern = SearchEngine.createSearchPattern( "NS::B::Two", ENUMTOR, DEFINITIONS, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		match = (IMatch) matches.iterator().next();
		assertTrue( match.getName().equals( "Two" ) ); //$NON-NLS-1$
	}
	
	public void testEnumeratorReferences(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "One", ENUMTOR, REFERENCES, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		IMatch match = (IMatch) matches.iterator().next();
		assertTrue( match.getName().equals( "One" ) ); //$NON-NLS-1$
	}
	
	/*public void testParameterReferences(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "index", VAR, REFERENCES, true ); //$NON-NLS-1$
		
		search( workspace, pattern, scope, resultCollector );
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 3 );
	}*/
	

	/*public void testBug43129(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "externalInt", VAR, DECLARATIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "externalIntWithInitializer", VAR, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "externCInt", VAR, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );

		pattern = SearchEngine.createSearchPattern( "externCIntWithInitializer", VAR, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "forwardFunction", FUNCTION, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		pattern = SearchEngine.createSearchPattern( "normalFunction", FUNCTION, DECLARATIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "normalFunction", FUNCTION, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "forwardMethod", METHOD, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
		
		pattern = SearchEngine.createSearchPattern( "staticField", FIELD, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 2 );
	}*/
	
	public void testNoResourceSearching() throws Exception {
		String path = CTestPlugin.getDefault().getFileInPlugin(new Path("resources/search/include.h")).getAbsolutePath(); //$NON-NLS-1$
		
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "Head", CLASS, REFERENCES, true ); //$NON-NLS-1$
		
		resultCollector.aboutToStart();

		MatchLocator matchLocator = new MatchLocator( pattern, resultCollector, scope);
		matchLocator.setProgressMonitor(monitor);
		
		try {
			matchLocator.locateMatches( new String [] { path }, workspace, null );
		} catch (InterruptedException e1) { //boo
		}
		
		resultCollector.done();
		
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 4 );
	}
	
	public void testBug42911_43988(){
		BasicSearchMatch match1 = new BasicSearchMatch();
		BasicSearchMatch match2 = new BasicSearchMatch();
		
		assertTrue( match1.equals( match2 ) );
		assertTrue( match2.equals( match1 ) );
		
		match1.setName( "IWasSaying" ); //$NON-NLS-1$
		match1.setParentName( "boo" ); //$NON-NLS-1$
		match1.setReturnType( "urns" ); //$NON-NLS-1$
		
		assertFalse( match1.equals( match2 ) );
		assertFalse( match2.equals( match1 ) );
		
		match2.setName( "IWasSaying" ); //$NON-NLS-1$
		match2.setParentName( "boo" ); //$NON-NLS-1$
		match2.setReturnType( "urns" ); //$NON-NLS-1$
		
		assertTrue( match1.equals( match2 ) );
	}
	
	public void testBug68235(){
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "bug68235::xTag", STRUCT, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		Set matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );		
		
		pattern = SearchEngine.createSearchPattern( "bug68235::yTag", STRUCT, DEFINITIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "bug68235::xType", TYPEDEF, DECLARATIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "bug68235::yType", TYPEDEF, DECLARATIONS, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "bug68235::xType", TYPEDEF, REFERENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
		
		pattern = SearchEngine.createSearchPattern( "bug68235::yType", TYPEDEF, REFERENCES, true ); //$NON-NLS-1$
		search( workspace, pattern, scope, resultCollector );
		matches = resultCollector.getSearchResults();
		assertEquals( matches.size(), 1 );
	}
}
