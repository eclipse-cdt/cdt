/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.search;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * Base class for tests that test functionality based on CSearchQuery.
 */
public abstract class SearchTestBase extends BaseUITestCase {
	protected ICProject fCProject;
	protected String fHeaderContents;
	protected IFile fHeaderFile;
	protected String fSourceContents;
	protected IFile fSourceFile;
	protected CharSequence[] fTestData;

	protected ITestStrategy fStrategy;

	public void setStrategy(ITestStrategy strategy) {
		fStrategy = strategy;
	}

	protected interface ITestStrategy {
		void setUp() throws Exception;

		void tearDown() throws Exception;

		// The scope for searches.
		ICElement[] getScope();
	}

	protected class SingleProjectStrategy implements ITestStrategy {
		@Override
		public void setUp() throws Exception {
			fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin",
					IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			fTestData = TestSourceReader.getContentsForTest(b, "ui", SearchTestBase.this.getClass(), getName(), 2);
			assertEquals("Incomplete test data", 2, fTestData.length);

			fHeaderContents = fTestData[0].toString();
			fHeaderFile = TestSourceReader.createFile(fCProject.getProject(), new Path("header.h"), fHeaderContents);
			CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
			waitForIndexer(fCProject);

			fSourceContents = fTestData[1].toString();
			fSourceFile = TestSourceReader.createFile(fCProject.getProject(), new Path("references.cpp"),
					fSourceContents);
			waitForIndexer(fCProject);
		}

		@Override
		public void tearDown() throws Exception {
			if (fCProject != null) {
				fCProject.getProject().delete(true, npm());
			}
		}

		@Override
		public ICElement[] getScope() {
			return new ICElement[] { fCProject };
		}
	}

	protected class ReferencedProjectStrategy implements ITestStrategy {
		private ICProject fReferencedCProject;

		@Override
		public void setUp() throws Exception {
			fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin",
					IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			fTestData = TestSourceReader.getContentsForTest(b, "ui", SearchTestBase.this.getClass(), getName(), 2);
			assertEquals("Incomplete test data", 2, fTestData.length);

			fReferencedCProject = CProjectHelper.createCCProject(
					"ReferencedContent" + getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);

			fHeaderContents = fTestData[0].toString();
			fHeaderFile = TestSourceReader.createFile(fReferencedCProject.getProject(), new Path("header.h"),
					fHeaderContents);

			CCorePlugin.getIndexManager().setIndexerId(fReferencedCProject, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(fReferencedCProject);
			waitForIndexer(fReferencedCProject);

			TestScannerProvider.sIncludes = new String[] {
					fReferencedCProject.getProject().getLocation().toOSString() };

			fSourceContents = fTestData[1].toString();
			fSourceFile = TestSourceReader.createFile(fCProject.getProject(), new Path("refs.cpp"), fSourceContents);

			IProject[] refs = new IProject[] { fReferencedCProject.getProject() };
			IProjectDescription desc = fCProject.getProject().getDescription();
			desc.setReferencedProjects(refs);
			fCProject.getProject().setDescription(desc, new NullProgressMonitor());

			CCorePlugin.getIndexManager().setIndexerId(fCProject, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(fCProject);
			waitForIndexer(fCProject);
		}

		@Override
		public void tearDown() throws Exception {
			if (fCProject != null) {
				fCProject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
						new NullProgressMonitor());
			}
			if (fReferencedCProject != null) {
				fReferencedCProject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
						new NullProgressMonitor());
			}
		}

		@Override
		public ICElement[] getScope() {
			return new ICElement[] { fReferencedCProject, fCProject };
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fStrategy.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		fStrategy.tearDown();
		super.tearDown();
	}

	protected CSearchResult getSearchResult(CSearchQuery query) {
		query.run(npm());
		return (CSearchResult) query.getSearchResult();
	}

	protected void assertOccurrences(CSearchQuery query, int expected) {
		CSearchResult result = getSearchResult(query);
		assertEquals(expected, result.getMatchCount());
	}
}
