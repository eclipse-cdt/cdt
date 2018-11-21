/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
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

package org.eclipse.cdt.ui.tests.includebrowser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import junit.framework.Test;

public class BasicIncludeBrowserTest extends IncludeBrowserBaseTest {

	public BasicIncludeBrowserTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(BasicIncludeBrowserTest.class);
	}

	// // source
	// #include "user.h"
	// #include <system.h>
	// #include "user.h"

	public void testSimpleInclusion() throws Exception {
		TestScannerProvider.sIncludes = new String[] { getProject().getProject().getLocation().toOSString() };

		StringBuilder[] contents = getContentsForTest(1);
		IProject project = getProject().getProject();
		IFile user = createFile(project, "user.h", "");
		IFile system = createFile(project, "system.h", "");
		IFile source = createFile(project, "source.cpp", contents[0].toString());
		waitUntilFileIsIndexed(fIndex, source);

		openIncludeBrowser(source);
		Tree tree = getIBTree();
		TreeItem node = checkTreeNode(tree, 0, "source.cpp");
		checkTreeNode(tree, 0, 0, "user.h");
		checkTreeNode(tree, 0, 1, "system.h");
		assertEquals(2, node.getItemCount());

		// The tree has to be reversed
		openIncludeBrowser(user, true);
		checkTreeNode(tree, 0, "user.h");
		checkTreeNode(tree, 0, 0, "source.cpp");

		openIncludeBrowser(system);
		checkTreeNode(tree, 0, "system.h");
		checkTreeNode(tree, 0, 0, "source.cpp");
	}

	// // source
	// #include "user.h"
	// #include <system.h>

	public void testInclusionAccrossProjects() throws Exception {
		ICProject op = CProjectHelper.createCCProject("__ibTest_other__", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			fIndex = CCorePlugin.getIndexManager().getIndex(new ICProject[] { getProject(), op });

			TestScannerProvider.sIncludes = new String[] { op.getProject().getLocation().toOSString() };

			StringBuilder[] contents = getContentsForTest(1);
			IFile user = createFile(op.getProject(), "user.h", "");
			IFile system = createFile(op.getProject(), "system.h", "");
			IFile source = createFile(getProject().getProject(), "source.cpp", contents[0].toString());
			CCorePlugin.getIndexManager().reindex(op);
			waitForIndexer(op);

			openIncludeBrowser(source);
			Tree tree = getIBTree();
			checkTreeNode(tree, 0, "source.cpp");
			checkTreeNode(tree, 0, 0, "user.h");
			checkTreeNode(tree, 0, 1, "system.h");

			// The tree has to be reversed
			openIncludeBrowser(user, true);
			checkTreeNode(tree, 0, "user.h");
			checkTreeNode(tree, 0, 0, "source.cpp");

			openIncludeBrowser(system);
			checkTreeNode(tree, 0, "system.h");
			checkTreeNode(tree, 0, 0, "source.cpp");
		} finally {
			CProjectHelper.delete(op);
		}
	}
}
