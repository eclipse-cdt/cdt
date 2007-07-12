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
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.search.PDOMSearchPatternQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchResult;
import org.eclipse.cdt.internal.ui.search.PDOMSearchViewPage;

public class BasicSearchTest extends BaseUITestCase {
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

	// // empty

	// #include "extHead.h"
	// void bar() {
	//   foo();
	// }
	public void testExternalPathRenderedCorrectly_79193() throws Exception {
		// make an external file
		File dir= CProjectHelper.freshDir();
		File externalFile= new File(dir, "extHead.h");
		externalFile.deleteOnExit();
		FileWriter fw= new FileWriter(externalFile);
		fw.write("void foo() {}\n");
		fw.close();
		
		// rebuild the index
		TestScannerProvider.sIncludes= new String[] {dir.getAbsolutePath()};
		CCorePlugin.getIndexManager().reindex(fCProject);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		
		// open a query
		PDOMSearchQuery query= makeProjectQuery("foo");
		PDOMSearchResult result= runQuery(query);
		assertEquals(2, result.getElements().length);
		
		ISearchResultViewPart vp= NewSearchUI.getSearchResultView();
		ISearchResultPage page= vp.getActivePage();
		assertTrue(""+page, page instanceof PDOMSearchViewPage);
		
		PDOMSearchViewPage pdomsvp= (PDOMSearchViewPage) page;
		StructuredViewer viewer= pdomsvp.getViewer();
		ILabelProvider labpv= (ILabelProvider) viewer.getLabelProvider();
		IStructuredContentProvider scp= (IStructuredContentProvider) viewer.getContentProvider();
		
		Object result0= result.getElements()[0];
		Object result1= result.getElements()[1];
		
		// check the results are rendered
		String expected0= fCProject.getProject().getName();
		String expected1= new Path(externalFile.getAbsolutePath()).toString();
		assertEquals(expected0,labpv.getText(scp.getElements(result)[0]));
		assertEquals(expected1,labpv.getText(scp.getElements(result)[1]));
	}
	
	/**
	 * Run the specified query, and return the result. When this method returns the
	 * search page will have been opened.
	 * @param query
	 * @return
	 */
	protected PDOMSearchResult runQuery(PDOMSearchQuery query) {
		final ISearchResult result[]= new ISearchResult[1];
		IQueryListener listener= new IQueryListener() {
			public void queryAdded(ISearchQuery query) {}
			public void queryFinished(ISearchQuery query) {
				result[0]= query.getSearchResult();
			}
			public void queryRemoved(ISearchQuery query) {}
			public void queryStarting(ISearchQuery query) {}
		};
		NewSearchUI.addQueryListener(listener);
		NewSearchUI.runQueryInForeground(new IRunnableContext() {
			public void run(boolean fork, boolean cancelable,
					IRunnableWithProgress runnable)
					throws InvocationTargetException, InterruptedException {
				runnable.run(NPM);
			}
		}, query);
		assertTrue(result[0] instanceof PDOMSearchResult);
		runEventQueue(500);
		return (PDOMSearchResult) result[0];
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
