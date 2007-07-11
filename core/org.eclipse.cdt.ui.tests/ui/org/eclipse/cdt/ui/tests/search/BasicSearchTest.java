/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import java.io.ByteArrayInputStream;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.search.PDOMSearchPatternQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchResult;

public class BasicSearchTest extends BaseTestCase {
	ICProject fCProject;
	StringBuffer[] testData;

	public static TestSuite suite() {
		return suite(BasicSearchTest.class);
	}
	
	protected void setUp() throws Exception {
		fCProject = CProjectHelper.createCCProject(getName()+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER); 
		Bundle b = CTestPlugin.getDefault().getBundle();
		testData = TestSourceReader.getContentsForTest(b, "ui", this.getClass(), getName(), 2);

		IFile file = TestSourceReader.createFile(fCProject.getProject(), new Path("header.h"), testData[0].toString());
		CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		IFile cppfile= TestSourceReader.createFile(fCProject.getProject(), new Path("references.cpp"), testData[1].toString());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
	}

	protected void tearDown() throws Exception {
		if(fCProject != null) {
			fCProject.getProject().delete(true, NPM);
		}
	}

	// void foo() {}

	// void bar() {
	//   foo();
	// }
	public void testNewResultsOnSearchAgainA() throws Exception {
		PDOMSearchQuery query= makeProjectQuery("foo");
		assertOccurences(query, 2);
		assertOccurences(query, 2);
		
		String newContent= "void bar() {}";
		IFile file = fCProject.getProject().getFile(new Path("references.cpp"));
		file.setContents(new ByteArrayInputStream(newContent.getBytes()), IResource.FORCE, NPM);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		assertOccurences(query, 1);
	}
	
	// void foo() {}

	// void bar() {foo();foo();foo();}
	public void testNewResultsOnSearchAgainB() throws Exception {
		PDOMSearchQuery query= makeProjectQuery("foo");
		assertOccurences(query, 4);
		assertOccurences(query, 4);
		
		// whitespace s.t. new match offset is same as older 
		String newContent= "void bar() {      foo();      }";
		IFile file = fCProject.getProject().getFile(new Path("references.cpp"));
		file.setContents(new ByteArrayInputStream(newContent.getBytes()), IResource.FORCE, NPM);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		assertOccurences(query, 2);
		
		String newContent2= "void bar() {foo(); foo();}";
		file.setContents(new ByteArrayInputStream(newContent2.getBytes()), IResource.FORCE, NPM);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		assertOccurences(query, 3);
	}
	
	protected PDOMSearchQuery makeProjectQuery(String pattern) {
		String scope1= "Human Readable Description";
		return new PDOMSearchPatternQuery(new ICElement[] {fCProject}, scope1, pattern, true, PDOMSearchQuery.FIND_ALL_OCCURANCES | PDOMSearchPatternQuery.FIND_ALL_TYPES);
	}
	
	protected void assertOccurences(PDOMSearchQuery query, int expected) {
		query.run(NPM);
		PDOMSearchResult result= (PDOMSearchResult) query.getSearchResult();
		assertEquals(expected, result.getMatchCount());
	}
}
