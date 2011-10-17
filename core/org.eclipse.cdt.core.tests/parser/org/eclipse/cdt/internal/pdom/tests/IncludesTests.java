/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 */
public class IncludesTests extends PDOMTestBase {
	protected ICProject project;
	protected IIndex index;

	public static Test suite() {
		return suite(IncludesTests.class);
	}

	protected void setUp() throws Exception {
		if (index == null) {
			project = createProject("includesTests");
			index = CCorePlugin.getIndexManager().getIndex(project);
		}
		index.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		index.releaseReadLock();
	}
	
	private IIndexFile getIndexFile(IFile file) throws CoreException {
		IIndexFile[] files = index.getFiles(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
		assertTrue("Can't find " + file.getLocation(), files.length > 0);
		assertEquals("Found " + files.length + " files for " + file.getLocation() + " instead of one", 1, files.length);
		return files[0];
	}			
	
	public void testIncludedBy() throws Exception {
		IResource loc = project.getProject().findMember("I2.h");
		IIndexFile file = getIndexFile((IFile) loc);
		IIndexInclude[] allIncludedBy = index.findIncludedBy(file, -1);
		assertEquals(9, allIncludedBy.length); // i.e. all of them
	}

	public void testIncludes() throws Exception {
		IResource loc = project.getProject().findMember("I1.cpp");
		IIndexFile file = getIndexFile((IFile) loc);
		IIndexInclude[] allIncludesTo= index.findIncludes(file, -1);
		assertEquals(2, allIncludesTo.length); // i.e. I1.h, I2.h
	}
	
	public void testIncludeName() throws Exception {
		IResource loc = project.getProject().findMember("a/b/I6.h");
		IIndexFile file = getIndexFile((IFile) loc);
		IIndexInclude[] allIncludedBy = index.findIncludedBy(file, -1);
		assertEquals(2, allIncludedBy.length);
		for (IIndexInclude include : allIncludedBy) {
			assertTrue(include.isResolved());
			assertFalse(include.isSystemInclude());
			IIndexFile includer = include.getIncludedBy();
			String includerName = new Path(includer.getLocation().getFullPath()).lastSegment();
			if ("I6.cpp".equals(includerName)) {
				assertEquals("I6.h", include.getName());
				assertEquals("a/b/I6.h", include.getFullName());
			} else {
				assertEquals("I7.cpp", includerName);
				assertEquals("I6.h", include.getName());
				assertEquals("b/I6.h", include.getFullName());
				IIndexInclude[] includes = includer.getIncludes();
				for (IIndexInclude include2 : includes) {
					if ("I7.h".equals(include2.getName())) {
						assertFalse(include2.isResolved());
						assertFalse(include2.isSystemInclude());
						assertEquals("b/I7.h", include2.getFullName());
					} else {
						assertEquals("I6.h", include2.getName());
					}
				}
			}
		}
	}
}
