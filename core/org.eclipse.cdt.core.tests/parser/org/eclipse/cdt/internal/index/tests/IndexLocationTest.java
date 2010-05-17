/*******************************************************************************
 * Copyright (c) 2006, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.index.URIRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class IndexLocationTest extends BaseTestCase {
	private static final boolean isWin= Platform.getOS().equals(Platform.OS_WIN32);
	protected List projects= new ArrayList();
	protected ICProject cproject;
	
	public static Test suite() {
		return suite(IndexLocationTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cproject= CProjectHelper.createCProject("LocationTests"+System.currentTimeMillis(), "bin", IPDOMManager.ID_FAST_INDEXER);
		deleteOnTearDown(cproject);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Iterator i= projects.iterator(); i.hasNext(); ) {
			ICProject ptd= (ICProject) i.next();
			if (ptd != null) {
				ptd.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}			
		}
		super.tearDown();
	}

	// //header
	// class foo {};

	// // external.h
	// class bar {};

	// //source
	// #include "header.h"
	// #include "ABS_EXTERNAL"
	// class baz {};
	public void testBasicLocations() throws Exception {
		File externalHeader = new File(CProjectHelper.freshDir(),"external.h");

		try {
			Bundle b = CTestPlugin.getDefault().getBundle();
			StringBuffer[] testData = TestSourceReader.getContentsForTest(b, "parser", getClass(), getName(), 3);

			IFile file1 = TestSourceReader.createFile(cproject.getProject(), "header.h", testData[0].toString());		
			createExternalFile(externalHeader, testData[1].toString());
			String content = testData[2].toString().replaceAll("ABS_EXTERNAL", externalHeader.getAbsolutePath().replaceAll("\\\\","\\\\\\\\"));
			IFile file3 = TestSourceReader.createFile(cproject.getProject(), "source.cpp", content);

			CCorePlugin.getIndexManager().reindex(cproject);		
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor()));

			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			index.acquireReadLock();
			try {
				IBinding[] bs1 = index.findBindings(Pattern.compile("foo"), true, IndexFilter.ALL, new NullProgressMonitor());
				IBinding[] bs2 = index.findBindings(Pattern.compile("bar"), true, IndexFilter.ALL, new NullProgressMonitor());
				IBinding[] bs3 = index.findBindings(Pattern.compile("baz"), true, IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bs1.length);
				assertEquals(1, bs2.length);
				assertEquals(1, bs3.length);
				bs1 = index.findBindings("foo".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
				bs2 = index.findBindings("bar".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
				bs3 = index.findBindings("baz".toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
				assertEquals(1, bs1.length);
				assertEquals(1, bs2.length);
				assertEquals(1, bs3.length);
				IIndexName[] nms1 = index.findNames(bs1[0], IIndex.FIND_ALL_OCCURRENCES);
				IIndexName[] nms2 = index.findNames(bs2[0], IIndex.FIND_ALL_OCCURRENCES);
				IIndexName[] nms3 = index.findNames(bs3[0], IIndex.FIND_ALL_OCCURRENCES);
				assertEquals(1, nms1.length);
				assertEquals(1, nms2.length);
				assertEquals(1, nms3.length);
				URI workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
				assertEquals(
						ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(cproject.getProject().getName()+"/header.h")).getLocationURI(),
						nms1[0].getFile().getLocation().getURI()
				);		
				assertEquals(
						externalHeader.toURI(),
						nms2[0].getFile().getLocation().getURI()
				);
				assertEquals(
						ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(cproject.getProject().getName()+"/source.cpp")).getLocationURI(),
						nms3[0].getFile().getLocation().getURI()
				);

				assertEquals(
						ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(cproject.getProject().getName()+"/header.h")).getFullPath(),
						new Path(nms1[0].getFile().getLocation().getFullPath())
				);
				assertEquals(
						null,
						nms2[0].getFile().getLocation().getFullPath()
				);
				assertEquals(
						ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(cproject.getProject().getName()+"/source.cpp")).getFullPath(),
						new Path(nms3[0].getFile().getLocation().getFullPath())
				);
			} finally {
				index.releaseReadLock();
			}
		} finally {
			externalHeader.delete();
			externalHeader.getParentFile().delete();
		}
	}
	
	public void testLinkedFilesIndexedAsWithinProject() throws Exception {
		File location = new File(CProjectHelper.freshDir(), "external2.h"); 
		createExternalFile(location, "struct External {};\n");
		IFolder content= cproject.getProject().getFolder("content");
		content.createLink(new Path(location.getParentFile().getAbsolutePath()), IResource.NONE, npm());
		
		CCorePlugin.getIndexManager().reindex(cproject);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor()));
		
		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("External".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length);
			IIndexName[] nms= index.findNames(bs[0], IIndex.FIND_ALL_OCCURRENCES);
			assertEquals(1, nms.length);
			IIndexFileLocation ilocation= nms[0].getFile().getLocation();
			assertEquals("/"+cproject.getProject().getName()+"/content/external2.h", ilocation.getFullPath());
		} finally {
			index.releaseReadLock();
		}
	}
	
	public void testSameFileLinkedToOnceInTwoProjects_186214() throws Exception {
		File location = new File(CProjectHelper.freshDir(),"external2.h"); 
		createExternalFile(location, "struct External {};\n");
		assertTrue(location.isFile());
		
		IFolder content= cproject.getProject().getFolder("content");
		content.createLink(new Path(location.getParentFile().getAbsolutePath()), IResource.NONE, null);
		final IFile file = content.getFile("external2.h");
		assertTrue(file.exists());
		
		ICProject cproject2= CProjectHelper.createCProject("LocationTests2"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
		deleteOnTearDown(cproject2);
		
		IFolder content2= cproject2.getProject().getFolder("content");
		content2.createLink(new Path(location.getParentFile().getAbsolutePath()), IResource.NONE, null);
		assertTrue(content2.getFile("external2.h").exists());

		IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
		TestSourceReader.waitUntilFileIsIndexed(index, file, 10000);
		CCorePlugin.getIndexManager().reindex(cproject);
		waitForIndexer(cproject);
		index.acquireReadLock();
		try {
			IBinding[] bs= index.findBindings("External".toCharArray(), IndexFilter.ALL, npm());
			assertEquals(1, bs.length);
			IIndexName[] nms= index.findNames(bs[0], IIndex.FIND_ALL_OCCURRENCES);
			assertEquals(1, nms.length);
			IIndexFileLocation ilocation= nms[0].getFile().getLocation();
			assertEquals("/"+cproject.getProject().getName()+"/content/external2.h", ilocation.getFullPath());
		} finally {
			index.releaseReadLock();
		}
	}
	
	public void testResourceContainerRelativeLocationConverter() throws Exception {
		ICProject emptyCProject= CProjectHelper.createCProject("Empty", "bin", IPDOMManager.ID_NO_INDEXER);
		deleteOnTearDown(emptyCProject);
		
		String[] paths = new String[] {"this.cpp", "inc/header.h", "a b c/d/e f/g.h", "a \\b /c.d"};
		for (String path : paths) {
			IFile file= cproject.getProject().getFile(path);
			IIndexFileLocation ifl1= IndexLocationFactory.getWorkspaceIFL(file);
			ResourceContainerRelativeLocationConverter prlc1= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			String r1= prlc1.toInternalFormat(ifl1);
			assertNotNull(r1);
			ResourceContainerRelativeLocationConverter prlc2= new ResourceContainerRelativeLocationConverter(emptyCProject.getProject());
			IIndexFileLocation ifl2= prlc2.fromInternalFormat(r1);
			assertNotNull(ifl2);
			assertEquals(
					new Path(ifl1.getFullPath()).removeFirstSegments(1),
					new Path(ifl2.getFullPath()).removeFirstSegments(1)
			);
		}
	}
	
	public void testURLC_RCRLC_Interaction1() throws Exception {
		String[] winPaths = new String[] {
				"c:/foo/bar/baz.cpp",
				"c:\\foo\\bar\\a b c\\baz.cpp",
				"c:/foo/bar/a b/baz.cpp",
				"c:\\foo\\bar\\a b c\\a b/baz.cpp"
			};
		String[] unxPaths = new String[] {
				"/home/cdt/foo/bar/baz.cpp",
				"/home/cdt/foo/bar/a b c/baz.cpp",
				"/home/cdt/foo/bar/a b/baz.cpp",
				"/home/cdt/foo/bar/a b c/a b/baz.cpp"
			};
		URI base = URIUtil.toURI(isWin ? "c:/foo/bar/" : "/home/cdt/foo/bar/");
		String[] paths= isWin ? winPaths : unxPaths;
		String[] expectedFullPaths = new String[] {
				"/"+cproject.getProject().getName()+"/baz.cpp",
				"/"+cproject.getProject().getName()+"/a b c/baz.cpp",
				"/"+cproject.getProject().getName()+"/a b/baz.cpp",
				"/"+cproject.getProject().getName()+"/a b c/a b/baz.cpp"
		};
		IContainer root= ResourcesPlugin.getWorkspace().getRoot();
		// loc -uri-> raw -project-> loc
		for (int i= 0; i < paths.length; i++) {
			IIndexFileLocation ifl1 = IndexLocationFactory.getExternalIFL(paths[i]);
			URIRelativeLocationConverter urlc = new URIRelativeLocationConverter(base);
			String r1 = urlc.toInternalFormat(ifl1);
			assertNotNull(r1);
			ResourceContainerRelativeLocationConverter prlc= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			IIndexFileLocation ifl2= prlc.fromInternalFormat(r1);
			String r2= prlc.toInternalFormat(ifl2);
			assertNotNull(r2);
			assertNull(ifl1.getFullPath());
			assertEquals(expectedFullPaths[i], ifl2.getFullPath());
			assertEquals(URIUtil.toURI(paths[i]).normalize(), ifl1.getURI());
			assertEquals(root.getFile(new Path(expectedFullPaths[i])).getLocationURI(), ifl2.getURI());
		}
	}
	
	public void testURLC_RCRLC_Interaction2() throws Exception {
		String[] winPaths = new String[] {
				"a b c/d/e f/g.h",
				"a \\b /c.d",
				"/a b c/d-e/f.g"
			};
		String[] unxPaths = new String[] {
				"a b c/d/e f/g.h",
				"a /b /c.d",
				"/a b c/d-e/f.g"
			};
		String[] paths= isWin ? winPaths : unxPaths;
		String basePath = isWin ? "c:/foo/bar/" : "/home/cdt/foo/bar/";
		String[] expectedFullPaths = new String[] {
				"/"+cproject.getProject().getName()+"/a b c/d/e f/g.h",
				"/"+cproject.getProject().getName()+"/a /b /c.d",
				"/"+cproject.getProject().getName()+"/a b c/d-e/f.g"
		};
		URI base = makeDirectoryURI(basePath);
		URIRelativeLocationConverter c1 = new URIRelativeLocationConverter(base);
		// loc -project-> raw -uri-> loc
		for (int i= 0; i < paths.length; i++) {
			IFile file= cproject.getProject().getFile(paths[i]);
			IIndexFileLocation ifl1= IndexLocationFactory.getWorkspaceIFL(file);
			ResourceContainerRelativeLocationConverter prlc= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			String r1= prlc.toInternalFormat(ifl1);
			assertNotNull(r1);
			IIndexFileLocation ifl2= c1.fromInternalFormat(r1);
			assertNotNull(ifl2);
			assertEquals(expectedFullPaths[i], ifl1.getFullPath());
			assertNull(ifl2.getFullPath());
			assertEquals(cproject.getProject().getFile(paths[i]).getLocationURI(), ifl1.getURI());
			assertEquals(URIUtil.toURI(basePath+paths[i]).normalize(), ifl2.getURI());
		}
	}
	
	public void testURLC_RCRLC_Interaction3() throws Exception {
		IFolder linkedFolder= cproject.getProject().getFolder("linkedFolder");
		String[] winPaths = new String[] {
				"a b c/d/e f/g.h",
				"a \\b /c.d",
				"/a b c/d-e/f.g"
			};
		String[] unxPaths = new String[] {
				"a b c/d/e f/g.h",
				"a /b /c.d",
				"/a b c/d-e/f.g"
			};
		String[] paths= isWin ? winPaths : unxPaths;
		String basePath = isWin ? "c:/foo/bar/" : "/home/cdt/foo/bar/";
		String[] expectedFullPaths = new String[] {
				linkedFolder.getFullPath()+"/a b c/d/e f/g.h",
				linkedFolder.getFullPath()+"/a /b /c.d",
				linkedFolder.getFullPath()+"/a b c/d-e/f.g"
		};
		// loc -project-> raw -uri-> loc
		URI base = makeDirectoryURI(basePath);
		URIRelativeLocationConverter c1 = new URIRelativeLocationConverter(base);
		for (int i= 0; i < paths.length; i++) {
			IFile file= linkedFolder.getFile(paths[i]);
			IIndexFileLocation ifl1= IndexLocationFactory.getWorkspaceIFL(file);
			ResourceContainerRelativeLocationConverter prlc= new ResourceContainerRelativeLocationConverter(linkedFolder);
			String r1= prlc.toInternalFormat(ifl1);
			assertNotNull(r1);
			IIndexFileLocation ifl2= c1.fromInternalFormat(r1);
			assertNotNull(ifl2);
			assertEquals(expectedFullPaths[i], ifl1.getFullPath());
			assertNull(ifl2.getFullPath());
			assertEquals(linkedFolder.getFile(paths[i]).getLocationURI(), ifl1.getURI());
			assertEquals(URIUtil.toURI(basePath+paths[i]).normalize(), ifl2.getURI());
		}
	}
	
	private void deleteOnTearDown(ICProject cproject) {
		if (cproject != null) {
			projects.add(cproject);
		}
	}
	
	private void createExternalFile(File dest, String content) throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		fos.write(content.getBytes());
		fos.close();
	}

	private URI makeDirectoryURI(String dir) throws URISyntaxException {
		URI uri = new File(dir).toURI();
		return new URI(uri.toString() + "/");
	}
}