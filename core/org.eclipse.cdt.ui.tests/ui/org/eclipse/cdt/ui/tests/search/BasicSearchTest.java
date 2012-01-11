/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.search.CSearchPatternQuery;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.CSearchViewPage;

public class BasicSearchTest extends BaseUITestCase {
	ICProject fCProject;
	CharSequence[] testData;

	public static TestSuite suite() {
		return suite(BasicSearchTest.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER); 
		Bundle b = CTestPlugin.getDefault().getBundle();
		testData = TestSourceReader.getContentsForTest(b, "ui", this.getClass(), getName(), 2);
		assertEquals("Incomplete test data", 2, testData.length);

		IFile file = TestSourceReader.createFile(fCProject.getProject(), new Path("header.h"), testData[0].toString());
		CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		IFile cppfile= TestSourceReader.createFile(fCProject.getProject(), new Path("references.cpp"), testData[1].toString());
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			fCProject.getProject().delete(true, npm());
		}
		super.tearDown();
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
		CSearchQuery query= makeProjectQuery("foo");
		CSearchResult result= runQuery(query);
		assertEquals(2, result.getElements().length);
		
		ISearchResultViewPart vp= NewSearchUI.getSearchResultView();
		ISearchResultPage page= vp.getActivePage();
		assertTrue(""+page, page instanceof CSearchViewPage);
		
		CSearchViewPage pdomsvp= (CSearchViewPage) page;
		StructuredViewer viewer= pdomsvp.getViewer();
		ILabelProvider labpv= (ILabelProvider) viewer.getLabelProvider();
		IStructuredContentProvider scp= (IStructuredContentProvider) viewer.getContentProvider();

		// project results are in a project node, containing directories and files
		Object[] resultElements = scp.getElements(result);
		String label0= labpv.getText(resultElements[0]);
		String label1= labpv.getText(resultElements[1]);
		// external results are in a tree, directory containing files
		Object externalResult = resultElements[1];
		String path1= labpv.getText(externalResult);
		String file1= labpv.getText(scp.getElements(externalResult)[0]);

		externalResult = resultElements[0];
		String path2= labpv.getText(externalResult);
		String file2= labpv.getText(scp.getElements(externalResult)[0]);
		
		// check the results are rendered
		String expected0= fCProject.getProject().getName();
		String expected1= new Path(externalFile.getAbsolutePath()).toString();
		assertTrue(expected0.equals(label0) || expected0.equals(label1));
		assertTrue(expected1.equals(new Path(path1).append(file1).toString()) || expected1.equals(new Path(path2).append(file2).toString()));
	}

	// int x, y, xx, yy;

	// // empty
	public void testNoIndexerEnabled_158955() throws Exception {
		// rebuild the index with no indexer
		CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_NO_INDEXER);
		CCorePlugin.getIndexManager().reindex(fCProject);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		
		// open a query
		CSearchQuery query= makeProjectQuery("x");
		CSearchResult result= runQuery(query);
		assertEquals(0, result.getElements().length);
		
		ISearchResultViewPart vp= NewSearchUI.getSearchResultView();
		ISearchResultPage page= vp.getActivePage();
		assertTrue("" + page, page instanceof CSearchViewPage);
		
		CSearchViewPage pdomsvp= (CSearchViewPage) page;
		StructuredViewer viewer= pdomsvp.getViewer();
		ILabelProvider labpv= (ILabelProvider) viewer.getLabelProvider();
		IStructuredContentProvider scp= (IStructuredContentProvider) viewer.getContentProvider();

		// first result is a project node
		Object firstRootNode = scp.getElements(result)[0];
		String label0= labpv.getText(firstRootNode);
		// ... containing a status message
		IStatus firstRootChildNode= (IStatus) scp.getElements(firstRootNode)[0];
		
		assertEquals(IStatus.WARNING, firstRootChildNode.getSeverity());
		// can't really verify text in case message is localized...
	}

	final int INDEXER_IN_PROGRESS_FILE_COUNT = 10;
	final int INDEXER_IN_PROGRESS_STRUCT_COUNT = 100;
	
	// #include "hugeHeader0.h"

	// // empty
	public void testIndexerInProgress() throws Exception {
		// make an external file
		File dir= CProjectHelper.freshDir();
		
		// make other project files so we can get incomplete results during indexing
		for (int i = 1; i < 10; i++) {
			TestSourceReader.createFile(fCProject.getProject(), new Path("references" + i + ".cpp"), 
					"#include \"hugeHeader" + i + ".h\"\n");
		}
		
		for (int f = 0; f < INDEXER_IN_PROGRESS_FILE_COUNT; f++) {
			File externalFile= new File(dir, "hugeHeader" + f + ".h");
			externalFile.deleteOnExit();
			FileWriter fw= new FileWriter(externalFile);
			for (int i = 0; i < INDEXER_IN_PROGRESS_STRUCT_COUNT; i++) {
				fw.write("typedef struct confusingType_" + f + "_" + i + " {\n");
				if (i == 0)
					fw.write("   void *data" + i + ";\n");
				else
					fw.write("   myConfusingType_" + f + "_" + (i-1) +" *data" + i + ";\n");
				fw.write("   int a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z;\n");
				fw.write("} myConfusingType_" + f + "_" + i + ";\n");
			}
			fw.close();
		}
		
		// rebuild the index and DO NOT JOIN
		TestScannerProvider.sIncludes= new String[] {dir.getAbsolutePath()};
		CCorePlugin.getIndexManager().reindex(fCProject);

		// immediate test, likely 0 matches
		coreTestIndexerInProgress(false);
		
		// wait some amount of time to get non-zero and hopefully non-complete results
		Thread.sleep(500);
		if (!CCorePlugin.getIndexManager().isIndexerIdle())
			coreTestIndexerInProgress(false);
		
		// now join and test again to get the full results
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		coreTestIndexerInProgress(true);
	}
	/**
	 * 
	 */
	private void coreTestIndexerInProgress(boolean expectComplete) {
		// open a query
		CSearchQuery query= makeProjectQuery("data*");
		CSearchResult result= runQuery(query);
		
		final int maximumHits = INDEXER_IN_PROGRESS_FILE_COUNT * INDEXER_IN_PROGRESS_STRUCT_COUNT;
		Object[] elements = result.getElements();
		if (expectComplete)
			assertEquals(maximumHits, elements.length);
		else
			assertTrue(maximumHits >= elements.length);	// >= because may still be done

		ISearchResultViewPart vp= NewSearchUI.getSearchResultView();
		ISearchResultPage page= vp.getActivePage();
		assertTrue(""+page, page instanceof CSearchViewPage);
		
		CSearchViewPage pdomsvp= (CSearchViewPage) page;
		StructuredViewer viewer= pdomsvp.getViewer();
		ILabelProvider labpv= (ILabelProvider) viewer.getLabelProvider();
		IStructuredContentProvider scp= (IStructuredContentProvider) viewer.getContentProvider();

		if (!expectComplete) {
			// even if we don't think the indexer is complete, we can't be 100% sure
			// the indexer didn't finish before the query started, so don't fail here
			// if all the hits were found
			if (elements.length < maximumHits) {
				// first result is an IStatus indicating indexer was busy
				Object[] nodeElements = scp.getElements(result);
				Object node = nodeElements[0];
				if (!(node instanceof IStatus))
					node = nodeElements[1];
				if (node instanceof IStatus) {
					IStatus firstRootNode = (IStatus) node;				
					assertEquals(IStatus.WARNING, firstRootNode.getSeverity());
					// can't really verify text in case message is localized...
				} else {
					fail("can't get status");
				}
			}
		} else {
			// must NOT have the IStatus
			Object firstRootNode = scp.getElements(result)[0];
			
			assertFalse(firstRootNode instanceof IStatus);
		}
		
	}

	/**
	 * Run the specified query, and return the result. When tehis method returns the
	 * search page will have been opened.
	 * @param query
	 * @return
	 */
	protected CSearchResult runQuery(CSearchQuery query) {
		final ISearchResult result[]= new ISearchResult[1];
		IQueryListener listener= new IQueryListener() {
			@Override
			public void queryAdded(ISearchQuery query) {}
			@Override
			public void queryFinished(ISearchQuery query) {
				result[0]= query.getSearchResult();
			}
			@Override
			public void queryRemoved(ISearchQuery query) {}
			@Override
			public void queryStarting(ISearchQuery query) {}
		};
		NewSearchUI.addQueryListener(listener);
		NewSearchUI.runQueryInForeground(new IRunnableContext() {
			@Override
			public void run(boolean fork, boolean cancelable,
					IRunnableWithProgress runnable)
					throws InvocationTargetException, InterruptedException {
				runnable.run(npm());
			}
		}, query);
		assertTrue(result[0] instanceof CSearchResult);
		runEventQueue(500);
		return (CSearchResult) result[0];
	}
	
	// void foo() {}

	// void bar() {
	//   foo();
	// }
	public void testNewResultsOnSearchAgainA() throws Exception {
		CSearchQuery query= makeProjectQuery("foo");
		assertOccurrences(query, 2);
		assertOccurrences(query, 2);
		
		String newContent= "void bar() {}";
		IFile file = fCProject.getProject().getFile(new Path("references.cpp"));
		file.setContents(new ByteArrayInputStream(newContent.getBytes()), IResource.FORCE, npm());
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

		assertOccurrences(query, 1);
	}
	
	// void foo() {}

	// void bar() {foo();foo();foo();}
	public void testNewResultsOnSearchAgainB() throws Exception {
		CSearchQuery query= makeProjectQuery("foo");
		assertOccurrences(query, 4);
		assertOccurrences(query, 4);
		
		// whitespace s.t. new match offset is same as older 
		String newContent= "void bar() {      foo();      }";
		IFile file = fCProject.getProject().getFile(new Path("references.cpp"));
		file.setContents(new ByteArrayInputStream(newContent.getBytes()), IResource.FORCE, npm());
		runEventQueue(1000);
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		indexManager.update(new ICElement[] {fCProject}, IIndexManager.UPDATE_ALL);
		assertTrue(indexManager.joinIndexer(360000, new NullProgressMonitor()));

		assertOccurrences(query, 2);
		
		String newContent2= "void bar() {foo(); foo();}";
		file.setContents(new ByteArrayInputStream(newContent2.getBytes()), IResource.FORCE, npm());
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		assertTrue(indexManager.joinIndexer(360000, new NullProgressMonitor()));

		assertOccurrences(query, 3);
	}
	
	private CSearchQuery makeProjectQuery(String pattern) {
		String scope1= "Human Readable Description";
		return new CSearchPatternQuery(new ICElement[] {fCProject}, scope1, pattern, true, CSearchQuery.FIND_ALL_OCCURRENCES | CSearchPatternQuery.FIND_ALL_TYPES);
	}
	
	private void assertOccurrences(CSearchQuery query, int expected) {
		query.run(npm());
		CSearchResult result= (CSearchResult) query.getSearchResult();
		assertEquals(expected, result.getMatchCount());
	}
	
	//	template<typename T> class CT {};
	//	template<typename T> class CT<T*> {};
	//	template<typename T> void f(T) {};
	//	template<typename T> void f(T*) {};

	//	void a() {
	//	  CT<int>* r1;
	//	  CT<char>* r2;
	//	  CT<int*>* r3;
	//
	//	  int a;
	//	  f(a);
	//	  f(&a);
	//	  f<int>(a);
	//	  f<int>(&a);
	//	}
	public void testSearchAndTemplateIDs() throws Exception {
		CSearchQuery query= makeProjectQuery("CT");
		assertOccurrences(query, 5); 
		query= makeProjectQuery("f");
		assertOccurrences(query, 6);
	}

}
