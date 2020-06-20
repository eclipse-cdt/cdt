/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Symbian - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.Test;

/**
 * See bugzilla
 */
public class FilesOnReindexTests extends PDOMTestBase {
	protected ICProject project;
	protected IIndex pdom;

	public static Test suite() {
		return suite(FilesOnReindexTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			project = createProject("filesOnReindex");
			pdom = CCorePlugin.getIndexManager().getIndex(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}
	}

	public void testFilesOnReindex() throws CoreException, InterruptedException {
		IFile file = project.getProject().getFile("simple.cpp");
		performAssertions(file);
		pdom.releaseReadLock();
		CCoreInternals.getPDOMManager().reindex(project);

		// wait until the indexer is done
		waitForIndexer(project);
		pdom.acquireReadLock();
		performAssertions(file);
	}

	void performAssertions(IFile file) throws CoreException {
		IIndex index = CCorePlugin.getIndexManager().getIndex(project);
		assertTrue(index.getFiles(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file)).length != 0);

		IBinding[] bs = index.findBindings(Pattern.compile("C"), true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bs.length);

		IIndexBinding binding = (IIndexBinding) bs[0];
		IIndexFile file2 = index.findDefinitions(binding)[0].getFile();
		assertEquals(file.getLocationURI(), file2.getLocation().getURI());
	}
}
