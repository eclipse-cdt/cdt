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

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

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
		
		checkContext();
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
		
		checkContext();
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
	
	private void checkContext() throws Exception {
		final long timestamp= System.currentTimeMillis();
		final IResource file= fProject.getProject().findMember(new Path("included.h"));
		assertNotNull(file);
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				((IFile) file).setContents(new ByteArrayInputStream( "int included; int CONTEXT;\n".getBytes()), false, false, NPM);
			}
		}, NPM);
		waitForIndexer();
		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(file.getLocation());
			assertNotNull(ifile);
			assertTrue(ifile.getTimestamp() >= timestamp);

			IIndexBinding[] result= fIndex.findBindings(Pattern.compile("testInclude_cpp"), true, IndexFilter.ALL, NPM);
			assertEquals(1, result.length);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}			

}
