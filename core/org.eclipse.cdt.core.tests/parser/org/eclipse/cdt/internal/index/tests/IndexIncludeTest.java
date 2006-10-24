/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.index.tests;

import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class IndexIncludeTest extends IndexTestBase {
	private static final IProgressMonitor NPM= new NullProgressMonitor();

	public static TestSuite suite() {
		TestSuite suite= suite(IndexIncludeTest.class, "_");
		suite.addTest(new IndexIncludeTest("deleteProject"));
		return suite;
	}

	private ICProject fProject= null;
	private IIndex fIndex= null;
	
	public IndexIncludeTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		if (fProject == null) {
			fProject= createProject(true, "resources/indexTests/includes");
		}
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
	}
		
	public void deleteProject() {
		if (fProject != null) {
			CProjectHelper.delete(fProject);
		}
	}
	
	public void testFastIndexer() throws Exception {
		CCoreInternals.getPDOMManager().setIndexerId(fProject, IPDOMManager.ID_FAST_INDEXER);
		CCoreInternals.getPDOMManager().setIndexAllHeaders(fProject, false);
		waitForIndexer();
		checkHeader(false);

		CCoreInternals.getPDOMManager().setIndexAllHeaders(fProject, true);
		waitForIndexer();
		checkHeader(true);
	}

	private void waitForIndexer() {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, NPM));
	}

	public void testFullIndexer() throws Exception {
		CCoreInternals.getPDOMManager().setIndexerId(fProject, IPDOMManager.ID_FULL_INDEXER);
		CCoreInternals.getPDOMManager().setIndexAllHeaders(fProject, false);
		waitForIndexer();
		checkHeader(false);

		CCoreInternals.getPDOMManager().setIndexAllHeaders(fProject, true);
		waitForIndexer();
		checkHeader(true);
	}

	private void checkHeader(boolean all) throws Exception {
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] result= fIndex.findBindings(Pattern.compile(".*included"), true, IndexFilter.ALL, NPM);
			assertEquals(all ? 2 : 1, result.length);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}			
}
