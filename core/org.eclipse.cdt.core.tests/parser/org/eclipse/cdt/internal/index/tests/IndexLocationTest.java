/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Systems and others.
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
import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class IndexLocationTest extends BaseTestCase {
	ICProject cproject;
	File movedLocation;
	File externalHeader;
	
	public static Test suite() {
		return suite(IndexLocationTest.class);
	}

	protected void setUp() throws Exception {
		cproject= CProjectHelper.createCProject("LocationTests", "bin", IPDOMManager.ID_NO_INDEXER);

		Bundle b = CTestPlugin.getDefault().getBundle();
		StringBuffer[] testData = TestSourceReader.getContentsForTest(b, "parser", getClass(), getName(), 3);

		movedLocation = CProjectHelper.freshDir();
		externalHeader = new File(CProjectHelper.freshDir(),"external.h");

		IFile file1 = TestSourceReader.createFile(cproject.getProject(), "header.h", testData[0].toString());		
		createExternalFile(externalHeader, testData[1].toString());
		String content = testData[2].toString().replaceAll("ABS_EXTERNAL", externalHeader.getAbsolutePath().replaceAll("\\\\","\\\\\\\\"));
		IFile file3 = TestSourceReader.createFile(cproject.getProject(), "source.cpp", content);

		CCorePlugin.getPDOMManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);		
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor()));

		super.setUp();
	}

	private void createExternalFile(File dest, String content) throws IOException {
		FileOutputStream fos = new FileOutputStream(dest);
		fos.write(content.getBytes());
		fos.close();
	}

	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
		movedLocation.delete();
		externalHeader.delete();
		externalHeader.getParentFile().delete();
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
	public void testBasicLocations() throws CoreException, ExecutionException, InterruptedException {
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
			IIndexName[] nms1 = index.findNames(bs1[0], IIndex.FIND_ALL_OCCURENCES);
			IIndexName[] nms2 = index.findNames(bs2[0], IIndex.FIND_ALL_OCCURENCES);
			IIndexName[] nms3 = index.findNames(bs3[0], IIndex.FIND_ALL_OCCURENCES);
			assertEquals(1, nms1.length);
			assertEquals(1, nms2.length);
			assertEquals(1, nms3.length);
			URI workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
			assertEquals(
					ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("LocationTests/header.h")).getLocationURI(),
					nms1[0].getFile().getLocation().getURI()
			);		
			assertEquals(
					externalHeader.toURI(),
					nms2[0].getFile().getLocation().getURI()
			);
			assertEquals(
					ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("LocationTests/source.cpp")).getLocationURI(),
					nms3[0].getFile().getLocation().getURI()
			);

			assertEquals(
					ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("LocationTests/header.h")).getFullPath(),
					new Path(nms1[0].getFile().getLocation().getFullPath())
			);
			assertEquals(
					null,
					nms2[0].getFile().getLocation().getFullPath()
			);
			assertEquals(
					ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("LocationTests/source.cpp")).getFullPath(),
					new Path(nms3[0].getFile().getLocation().getFullPath())
			);
		}
		finally {
			index.releaseReadLock();
		}
	}
}