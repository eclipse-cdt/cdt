/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
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
			IPathEntry[] entries= new IPathEntry[] {
					CoreModel.newIncludeEntry(fProject.getPath(),
							null, fProject.getResource().getLocation())};
			fProject.setRawPathEntries(entries, NPM);
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
		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "false");
		waitForIndexer();
		checkHeader(false);

		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
		waitForIndexer();
		checkHeader(true);
		
		checkContext();
	}

	private void waitForIndexer() {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, NPM));
	}

	public void testFullIndexer() throws Exception {
		CCoreInternals.getPDOMManager().setIndexerId(fProject, IPDOMManager.ID_FULL_INDEXER);
		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "false");
		waitForIndexer();
		checkHeader(false);

		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
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
		final IFile file= (IFile) fProject.getProject().findMember(new Path("included.h"));
		assertNotNull("Can't find included.h", file);
		waitForIndexer();
		
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				file.setContents(new ByteArrayInputStream( "int included; int CONTEXT;\n".getBytes()), false, false, NPM);
				file.setLocalTimeStamp(timestamp+1000); 
			}
		}, NPM);
		assertTrue("Timestamp was not increased", file.getLocalTimeStamp() >= timestamp);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, 4000);
		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull("Can't find " + file.getLocation(), ifile);
			assertTrue("timestamp not ok", ifile.getTimestamp() >= timestamp);

			IIndexBinding[] result= fIndex.findBindings(Pattern.compile("testInclude_cpp"), true, IndexFilter.ALL, NPM);
			assertEquals(1, result.length);

			result= fIndex.findBindings("testInclude_cpp".toCharArray(), IndexFilter.ALL, NPM);
			assertEquals(1, result.length);
		}
		finally {
			fIndex.releaseReadLock();
		}
	}			

	// {source20061107}
	// #include "user20061107.h"
	// #include <system20061107.h>
	public void testIncludeProperties() throws Exception {
		waitForIndexer();
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		try {
			String content= readTaggedComment("source20061107");
			TestSourceReader.createFile(fProject.getProject(), "user20061107.h", "");
			TestSourceReader.createFile(fProject.getProject(), "system20061107.h", "");
			IFile file= TestSourceReader.createFile(fProject.getProject(), "source20061107.cpp", content);
			TestSourceReader.waitUntilFileIsIndexed(fIndex, file, 4000);

			fIndex.acquireReadLock();
			try {
				IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
				assertNotNull(ifile);
				IIndexInclude[] includes= ifile.getIncludes();
				assertEquals(2, includes.length);
				
				checkInclude(includes[0], content, "user20061107.h", false);
				checkInclude(includes[1], content, "system20061107.h", true);
			}
			finally {
				fIndex.releaseReadLock();
			}
		}
		finally {
			TestScannerProvider.sIncludes= null;
		}
	}
	
	public void testIncludeProperties_2() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		try {
			TestSourceReader.createFile(fProject.getProject(), "header20061107.h", "");
			String content = "// comment \n#include \"header20061107.h\"\n";
			IFile file= TestSourceReader.createFile(fProject.getProject(), "intermed20061107.h", content);
			TestSourceReader.createFile(fProject.getProject(), "source20061107.cpp", "#include \"intermed20061107.h\"\n");
			CCoreInternals.getPDOMManager().reindex(fProject);
			waitForIndexer();
			

			fIndex.acquireReadLock();
			try {
				IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
				assertNotNull(ifile);
				IIndexInclude[] includes= ifile.getIncludes();
				assertEquals(1, includes.length);
				
				checkInclude(includes[0], content, "header20061107.h", false);
			}
			finally {
				fIndex.releaseReadLock();
			}
		}
		finally {
			TestScannerProvider.sIncludes= null;
		}
	}

	public void testInactiveInclude() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		try {
			String content = "#if 0\n#include \"inactive20070213.h\"\n#endif\n";
			IFile file= TestSourceReader.createFile(fProject.getProject(), "source20070213.cpp", content);
			CCoreInternals.getPDOMManager().reindex(fProject);
			waitForIndexer();
			
			fIndex.acquireReadLock();
			try {
				IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
				assertNotNull(ifile);
				IIndexInclude[] includes= ifile.getIncludes();
				assertEquals(1, includes.length);
				
				assertFalse(includes[0].isActive());
				checkInclude(includes[0], content, "inactive20070213.h", false);
			}
			finally {
				fIndex.releaseReadLock();
			}
		}
		finally {
			TestScannerProvider.sIncludes= null;
		}
	}

	public void testUnresolvedInclude() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		try {
			String content = "#include \"unresolved20070213.h\"\n";
			IFile file= TestSourceReader.createFile(fProject.getProject(), "source20070214.cpp", content);
			CCoreInternals.getPDOMManager().reindex(fProject);
			waitForIndexer();
			
			fIndex.acquireReadLock();
			try {
				IIndexFile ifile= fIndex.getFile(IndexLocationFactory.getWorkspaceIFL(file));
				assertNotNull(ifile);
				IIndexInclude[] includes= ifile.getIncludes();
				assertEquals(1, includes.length);
				
				assertTrue(includes[0].isActive());
				assertFalse(includes[0].isResolved());
				checkInclude(includes[0], content, "unresolved20070213.h", false);
			}
			finally {
				fIndex.releaseReadLock();
			}
		}
		finally {
			TestScannerProvider.sIncludes= null;
		}
	}

	private void checkInclude(IIndexInclude include, String content, String includeName, boolean isSystem) throws CoreException {
		int offset= content.indexOf(includeName);
		assertEquals(offset, include.getNameOffset());
		assertEquals(includeName.length(), include.getNameLength());
		assertEquals(isSystem, include.isSystemInclude());
	}	

}
