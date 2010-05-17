/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
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
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class IndexIncludeTest extends IndexTestBase {

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

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (fProject == null) {
			fProject= createProject(true, "resources/indexTests/includes");
			IPathEntry[] entries= new IPathEntry[] {
					CoreModel.newIncludeEntry(fProject.getPath(),
							null, fProject.getResource().getLocation())};
			fProject.setRawPathEntries(entries, npm());
		}
		fIndex= CCorePlugin.getIndexManager().getIndex(fProject);
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
		
	public void deleteProject() {
		if (fProject != null) {
			CProjectHelper.delete(fProject);
		}
	}
	
	public void testFastIndexer() throws Exception {
		CCorePlugin.getIndexManager().setIndexerId(fProject, IPDOMManager.ID_FAST_INDEXER);
		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "false");
		waitForIndexer();
		checkHeader(false);

		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "true");
		waitForIndexer();
		checkHeader(true);
		
		checkContext();
	}

	private void waitForIndexer() {
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, npm()));
	}

	private void checkHeader(boolean all) throws Exception {
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] result= fIndex.findBindings(Pattern.compile(".*included"), true, IndexFilter.ALL, npm());
			assertEquals(all ? 2 : 1, result.length);
		} finally {
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
				file.setContents(new ByteArrayInputStream( "int included; int CONTEXT;\n".getBytes()), false, false, npm());
				file.setLocalTimeStamp(timestamp+1000); 
			}
		}, npm());
		assertTrue("Timestamp was not increased", file.getLocalTimeStamp() >= timestamp);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, 4000);
		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull("Can't find " + file.getLocation(), ifile);
			assertTrue("timestamp not ok", ifile.getTimestamp() >= timestamp);

			IIndexBinding[] result= fIndex.findBindings(Pattern.compile("testInclude_cpp"), true, IndexFilter.ALL, npm());
			assertEquals(1, result.length);

			result= fIndex.findBindings("testInclude_cpp".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, result.length);
		} finally {
			fIndex.releaseReadLock();
		}
	}			

	// {source20061107}
	// #include "user20061107.h"
	// #include <system20061107.h>
	public void testIncludeProperties() throws Exception {
		waitForIndexer();
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		String content= readTaggedComment("source20061107");
		TestSourceReader.createFile(fProject.getProject(), "user20061107.h", "");
		TestSourceReader.createFile(fProject.getProject(), "system20061107.h", "");
		IFile file= TestSourceReader.createFile(fProject.getProject(), "source20061107.cpp", content);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, file, 4000);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(2, includes.length);

			checkInclude(includes[0], content, "user20061107.h", false);
			checkInclude(includes[1], content, "system20061107.h", true);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	public void testIncludeProperties_2() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		TestSourceReader.createFile(fProject.getProject(), "header20061107.h", "");
		String content = "// comment \n#include \"header20061107.h\"\n";
		IFile file= TestSourceReader.createFile(fProject.getProject(), "intermed20061107.h", content);
		TestSourceReader.createFile(fProject.getProject(), "source20061107.cpp", "#include \"intermed20061107.h\"\n");
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();


		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);

			checkInclude(includes[0], content, "header20061107.h", false);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public void testInactiveInclude() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		String content = "#if 0\n#include \"inactive20070213.h\"\n#endif\n";
		IFile file= TestSourceReader.createFile(fProject.getProject(), "source20070213.cpp", content);
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);

			assertFalse(includes[0].isActive());
			checkInclude(includes[0], content, "inactive20070213.h", false);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	public void testUnresolvedInclude() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		String content = "#include \"unresolved20070213.h\"\n";
		IFile file= TestSourceReader.createFile(fProject.getProject(), "source20070214.cpp", content);
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
			assertNotNull(ifile);
			IIndexInclude[] includes= ifile.getIncludes();
			assertEquals(1, includes.length);

			assertTrue(includes[0].isActive());
			assertFalse(includes[0].isResolved());
			checkInclude(includes[0], content, "unresolved20070213.h", false);
		} finally {
			fIndex.releaseReadLock();
		}
	}

	private void checkInclude(IIndexInclude include, String content, String includeName, boolean isSystem) throws CoreException {
		int offset= content.indexOf(includeName);
		assertEquals(offset, include.getNameOffset());
		assertEquals(includeName.length(), include.getNameLength());
		assertEquals(isSystem, include.isSystemInclude());
	}	

	public void testUpdateOfIncluded() throws Exception {
		String content1 = "int CONTEXT_20070404(x);\n";
		String content2 = "int CONTEXT_20070404(y);\n";
		String content3 = "#define CONTEXT_20070404(x) ctx_20070404##x\n #include \"included_20070404.h\"\n int source_20070404;\n";
		TestSourceReader.createFile(fProject.getProject(), "included_20070404.h", content1);
		TestSourceReader.createFile(fProject.getProject(), "notIncluded_20070404.h", "int notIncluded_20070404\n;");
		TestSourceReader.createFile(fProject.getProject(), "includer_20070404.cpp", content3);
		IndexerPreferences.set(fProject.getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "false");
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			assertEquals(0, fIndex.findBindings("notIncluded_20070404".toCharArray(), IndexFilter.ALL, npm()).length);
			assertEquals(1, fIndex.findBindings("source_20070404".toCharArray(), IndexFilter.ALL, npm()).length);
			IBinding[] bindings= fIndex.findBindings("ctx_20070404x".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IVariable);
		} finally {
			fIndex.releaseReadLock();
		}
		
		Thread.sleep(1000);
		// now change the header and see whether it gets parsed
		TestSourceReader.createFile(fProject.getProject(), "included_20070404.h", content2);
		TestSourceReader.createFile(fProject.getProject(), "notIncluded_20070404.h", "int notIncluded_20070404\n;");
		Thread.sleep(1000);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			assertEquals(0, fIndex.findBindings("notIncluded_20070404".toCharArray(), IndexFilter.ALL, npm()).length);
			IBinding[] bindings= fIndex.findBindings("ctx_20070404y".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bindings.length);
			assertTrue(bindings[0] instanceof IVariable);
		} finally {
			fIndex.releaseReadLock();
		}
	}
	
	// #define SOME_MACRO1 ok_1_220358
	// #define SOME_MACRO2 ok_2_220358
	
	// int SOME_MACRO1;
	
	// int SOME_MACRO2;

	// #include "header1.h"
	// #include "header2.h"
	public void testParsingInContext_bug220358() throws Exception {
		StringBuffer[] sources= getContentsForTest(4);
		IFile h1= TestSourceReader.createFile(fProject.getProject(), "header1.h", sources[0].toString());
		IFile h2= TestSourceReader.createFile(fProject.getProject(), "header2.h", sources[1].toString());
		IFile s1= TestSourceReader.createFile(fProject.getProject(), "s1.cpp", sources[3].toString());
		// make sure it is parsed in context
		waitForIndexer();
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();

		fIndex.acquireReadLock();
		try {
			IIndexBinding[] binding= fIndex.findBindings("ok_1_220358".toCharArray(), IndexFilter.ALL_DECLARED, npm());
			assertEquals(1, binding.length);
			assertTrue(binding[0] instanceof IVariable);
		} finally {
			fIndex.releaseReadLock();
		}
		
		// change header2:
		h2= TestSourceReader.createFile(fProject.getProject(), "header2.h", sources[2].toString());
		TestSourceReader.waitUntilFileIsIndexed(fIndex, h2, INDEXER_WAIT_TIME);
		fIndex.acquireReadLock();
		try {
			IIndexBinding[] binding= fIndex.findBindings("ok_2_220358".toCharArray(), IndexFilter.ALL_DECLARED, npm());
			assertEquals(1, binding.length);
			assertTrue(binding[0] instanceof IVariable);
		} finally {
			fIndex.releaseReadLock();
		}		
	}
	
	// #include "resolved20070426.h"
	public void testFixedContext() throws Exception {
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		String source= getContentsForTest(1)[0].toString();
		IFile header= TestSourceReader.createFile(fProject.getProject(), "resolved20070426.h", "");
		IFile s1= TestSourceReader.createFile(fProject.getProject(), "s1.cpp", source);
		// make sure it is parsed in context
		waitForIndexer();
		CCorePlugin.getIndexManager().reindex(fProject);
		waitForIndexer();

		IFile s2= TestSourceReader.createFile(fProject.getProject(), "s2.cpp", source);
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s2, INDEXER_WAIT_TIME);

		fIndex.acquireReadLock();
		try {
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(2, includes.length);
			
			IIndexInclude context= ifile.getParsedInContext();
			assertNotNull(context);
			assertEquals(s1.getFullPath().toString(), context.getIncludedByLocation().getFullPath());

			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(s2.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
		} finally {
			fIndex.releaseReadLock();
		}
		
		s1= TestSourceReader.createFile(fProject.getProject(), "s1.cpp", source + "\nint a20070426;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings("a20070426".toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(2, includes.length);
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(s2.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			IIndexInclude context= ifile.getParsedInContext();
			assertNotNull(context);
			assertEquals(s1.getFullPath().toString(), context.getIncludedByLocation().getFullPath());
		} finally {
			fIndex.releaseReadLock();
		}

		s2= TestSourceReader.createFile(fProject.getProject(), "s2.cpp", source + "\nint b20070426;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings("b20070426".toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);
			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(2, includes.length);
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(s2.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			IIndexInclude context= ifile.getParsedInContext();
			assertNotNull(context);
			assertEquals(s1.getFullPath().toString(), context.getIncludedByLocation().getFullPath());
		} finally {
			fIndex.releaseReadLock();
		}
	}

	// #include "resolved20070427.h"
	// #include "unesolved20070427.h"
	// #if 0
	// #include "inactive20070427.h"
	// #endif

	// #include <unesolved20070427.h>
	// #if 0
	// #include <inactive20070427.h>
	// #endif

	// #include <resolved20070427.h>
	// #if 0
	// #include <inactive20070427.h>
	// #endif

	// #include "resolved20070427.h"
	// #include "unesolved20070427.h"
	public void testUpdateIncludes() throws Exception {
		waitForIndexer();
		TestScannerProvider.sIncludes= new String[]{fProject.getProject().getLocation().toOSString()};
		StringBuffer[] source= getContentsForTest(4);
		IFile header= TestSourceReader.createFile(fProject.getProject(), "resolved20070427.h", "");
		IFile s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[0].toString() + "\nint a20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		standardCheckUpdateIncludes(header, s1, "a20070427");
		
		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[0].toString() + "\nint b20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		standardCheckUpdateIncludes(header, s1, "b20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[1].toString() + "\nint c20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		checkUpdateIncludes1(header, s1, "c20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[0].toString() + "\nint d20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		standardCheckUpdateIncludes(header, s1, "d20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[2].toString() + "\nint e20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		checkUpdateIncludes2(header, s1, "e20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[0].toString() + "\nint f20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		standardCheckUpdateIncludes(header, s1, "f20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[3].toString() + "\nint g20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		checkUpdateIncludes3(header, s1, "g20070427");

		s1= TestSourceReader.createFile(fProject.getProject(), "s20070427.cpp", 
				source[0].toString() + "\nint h20070427;");
		TestSourceReader.waitUntilFileIsIndexed(fIndex, s1, INDEXER_WAIT_TIME);
		standardCheckUpdateIncludes(header, s1, "h20070427");

	}

	private void standardCheckUpdateIncludes(IFile header, IFile s1, String tag) throws Exception {
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings(tag.toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);

			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			IIndexFile sfile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(s1));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(1, includes.length);
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[0].isSystemInclude());

			assertNotNull(sfile);
			includes= fIndex.findIncludes(sfile);
			assertEquals(3, includes.length);
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[0].isSystemInclude());

			assertNull(includes[1].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			assertTrue(includes[1].isActive());
			assertFalse(includes[1].isResolved());
			assertFalse(includes[1].isSystemInclude());

			assertNull(includes[2].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[2].getIncludedByLocation().getFullPath());
			assertFalse(includes[2].isActive());
			assertFalse(includes[2].isResolved());
			assertFalse(includes[2].isSystemInclude());
		} finally {
			fIndex.releaseReadLock();
		}
	}	

	private void checkUpdateIncludes1(IFile header, IFile s1, String tag) throws Exception {
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings(tag.toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);

			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			IIndexFile sfile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(s1));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(0, includes.length);

			assertNotNull(sfile);
			includes= fIndex.findIncludes(sfile);
			assertEquals(2, includes.length);

			assertNull(includes[0].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertFalse(includes[0].isResolved());
			assertTrue(includes[0].isSystemInclude());

			assertNull(includes[1].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			assertFalse(includes[1].isActive());
			assertFalse(includes[1].isResolved());
			assertTrue(includes[1].isSystemInclude());
		} finally {
			fIndex.releaseReadLock();
		}
	}	

	private void checkUpdateIncludes2(IFile header, IFile s1, String tag) throws Exception {
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings(tag.toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);

			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			IIndexFile sfile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(s1));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(1, includes.length);
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertTrue(includes[0].isSystemInclude());

			assertNotNull(sfile);
			includes= fIndex.findIncludes(sfile);
			assertEquals(2, includes.length);
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertTrue(includes[0].isSystemInclude());

			assertNull(includes[1].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			assertFalse(includes[1].isActive());
			assertFalse(includes[1].isResolved());
			assertTrue(includes[1].isSystemInclude());
		} finally {
			fIndex.releaseReadLock();
		}
	}	

	private void checkUpdateIncludes3(IFile header, IFile s1, String tag) throws Exception {
		fIndex.acquireReadLock();
		try {
			assertEquals(1, fIndex.findBindings(tag.toCharArray(), IndexFilter.ALL_DECLARED, npm()).length);

			IIndexFile ifile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(header));
			IIndexFile sfile= fIndex.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(s1));
			assertNotNull(ifile);
			IIndexInclude[] includes= fIndex.findIncludedBy(ifile);
			assertEquals(1, includes.length);
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[0].isSystemInclude());

			assertNotNull(sfile);
			includes= fIndex.findIncludes(sfile);
			assertEquals(2, includes.length);
			assertEquals(header.getFullPath().toString(), includes[0].getIncludesLocation().getFullPath());
			assertEquals(s1.getFullPath().toString(), includes[0].getIncludedByLocation().getFullPath());
			assertTrue(includes[0].isActive());
			assertTrue(includes[0].isResolved());
			assertFalse(includes[0].isSystemInclude());

			assertNull(includes[1].getIncludesLocation());
			assertEquals(s1.getFullPath().toString(), includes[1].getIncludedByLocation().getFullPath());
			assertTrue(includes[1].isActive());
			assertFalse(includes[1].isResolved());
			assertFalse(includes[1].isSystemInclude());
		} finally {
			fIndex.releaseReadLock();
		}
	}	
}
