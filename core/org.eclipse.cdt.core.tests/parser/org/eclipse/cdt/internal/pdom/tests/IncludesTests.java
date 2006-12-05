/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * @author Doug Schaefer
 *
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
	
	public void testIncludedBy() throws Exception {
		IResource loc = project.getProject().findMember("I2.h");
		IIndexFile file = index.getFile(IndexLocationFactory.getWorkspaceIFL((IFile)loc));
		assertNotNull(file);
		IIndexInclude[] allIncludedBy = index.findIncludedBy(file, -1);
		assertEquals(9, allIncludedBy.length); // i.e. all of them
	}
	
	public void testIncludes() throws Exception {
		IResource loc = project.getProject().findMember("I1.cpp");
		IIndexFile file = index.getFile(IndexLocationFactory.getWorkspaceIFL((IFile)loc));
		assertNotNull(file);
		IIndexInclude[] allIncludesTo= index.findIncludes(file, -1);
		assertEquals(2, allIncludesTo.length); // i.e. I1.h, I2.h
	}
}
