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
 * Created on Jul 4, 2003
 */
package org.eclipse.cdt.core.search.failedTests;

import java.util.Set;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.matching.MatchLocator;
import org.eclipse.cdt.internal.ui.search.CSearchResultCollector;
import org.eclipse.core.runtime.Path;

import junit.framework.TestCase;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PatternsFailedTests extends TestCase implements ICSearchConstants {

	private MatchLocator matchLocator;
	private CSearchResultCollector resultCollector;
	private String cppPath;
	
	public PatternsFailedTests(String name) {
		super(name);
	}
		
	private void initialize( CSearchPattern pattern ){
		cppPath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
		cppPath += "resources/search/classDecl.cpp";
		
		resultCollector = new CSearchResultCollector();
		matchLocator = new MatchLocator( pattern, resultCollector, null, null );
	}
			
	public void testBug39652() {
		CSearchPattern pattern = CSearchPattern.createPattern( "A::B", TYPE, DECLARATIONS, EXACT_MATCH, true );
		
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		Set matches = resultCollector.getMatches();
		/* Test should find 1 match */
		//assertTrue( matches != null );
		//assertTrue( matches.size() == 1 );
		
		/* instead it finds none because qualifications are wrong*/
		assertTrue( matches == null );
		
		pattern = CSearchPattern.createPattern( "NS::NS2::a", TYPE, DECLARATIONS, EXACT_MATCH, true );
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertTrue( matches == null );
		
		pattern = CSearchPattern.createPattern( "NS::B::A", TYPE, DECLARATIONS, EXACT_MATCH, true );
		initialize( pattern );
		matchLocator.locateMatches( new String[] { cppPath }, null, null );
		matches = resultCollector.getMatches();
		assertTrue( matches == null );
	}
	
	
}
