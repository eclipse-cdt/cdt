/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;

/**
 * Base class for tests that test functionality based on CSearchQuery.
 */
public abstract class SearchTestBase extends BaseUITestCase {
	protected ICProject fCProject;
	protected String fHeaderContents;
	protected IFile fHeaderFile;
	protected String fSourceContents;
	protected IFile fSourceFile;
	protected CharSequence[] testData;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER); 
		Bundle b = CTestPlugin.getDefault().getBundle();
		testData = TestSourceReader.getContentsForTest(b, "ui", this.getClass(), getName(), 2);
		assertEquals("Incomplete test data", 2, testData.length);

		fHeaderContents = testData[0].toString();
		fHeaderFile = TestSourceReader.createFile(fCProject.getProject(), new Path("header.h"), fHeaderContents);
		CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(fCProject);

		fSourceContents = testData[1].toString();
		fSourceFile = TestSourceReader.createFile(fCProject.getProject(), new Path("references.cpp"), fSourceContents);
		waitForIndexer(fCProject);
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			fCProject.getProject().delete(true, npm());
		}
		super.tearDown();
	}

	protected CSearchResult getSearchResult(CSearchQuery query) {
		query.run(npm());
		return (CSearchResult) query.getSearchResult();
	}
	
	protected void assertOccurrences(CSearchQuery query, int expected) {
		CSearchResult result= getSearchResult(query);
		assertEquals(expected, result.getMatchCount());
	}
}
