/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.editor;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for org.eclipse.cdt.internal.ui.editor.SourceHeaderPartnerFinder.
 */
public class SourceHeaderPartnerFinderTest extends BaseUITestCase {

	protected static IProgressMonitor NPM = new NullProgressMonitor();

	private ICProject fCProject;

	public SourceHeaderPartnerFinderTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject = CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin",
				IPDOMManager.ID_FAST_INDEXER);
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}

	public void testFilesWithSameNameInSubdirectory_421544() throws Exception {
		IProject project = fCProject.getProject();
		IFile originFile = createFile(project, "code.cc", "");
		IFile expectedTargetFile = createFile(project, "code.h", "");
		createFile(project, "sub/code.cc", "");
		createFile(project, "sub/code.h", "");
		ITranslationUnit originTU = (ITranslationUnit) CoreModel.getDefault().create(originFile);
		ITranslationUnit targetTU = SourceHeaderPartnerFinder.getPartnerTranslationUnit(originTU);
		IFile targetFile = (IFile) targetTU.getResource();
		assertEquals(expectedTargetFile, targetFile);
	}
}
