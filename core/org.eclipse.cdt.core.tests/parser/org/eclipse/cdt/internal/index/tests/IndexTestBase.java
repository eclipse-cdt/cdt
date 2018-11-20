/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class IndexTestBase extends BaseTestCase {
	public IndexTestBase(String name) {
		super(name);
	}

	protected ICProject createProject(final boolean useCpp, final String importSource)
			throws CoreException, InterruptedException {
		// Create the project
		final ICProject[] result = new ICProject[] { null };
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				String name = "IndexTest_" + System.currentTimeMillis();
				if (useCpp) {
					result[0] = CProjectHelper.createCCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				} else {
					result[0] = CProjectHelper.createCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				}
				CProjectHelper.importSourcesFromPlugin(result[0], CTestPlugin.getDefault().getBundle(), importSource);
			}
		}, null);
		CCorePlugin.getIndexManager().setIndexerId(result[0], IPDOMManager.ID_FAST_INDEXER);
		// wait until the indexer is done
		waitForIndexer(result[0]);
		return result[0];
	}

	protected String readTaggedComment(String tag) throws Exception {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
	}

	protected StringBuilder[] getContentsForTest(int blocks) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "parser", getClass(),
				getName(), blocks);
	}
}
