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

package org.eclipse.cdt.ui.tests.includebrowser;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;

import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;


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
	
	public void testSimpleInclusion() throws Exception {
		TestScannerProvider.sIncludes= new String[]{getProject().getProject().getLocation().toOSString()};

		StringBuffer[] contents= getContentsForTest(1);
		IProject project= getProject().getProject();
		IFile user= createFile(project, "user.h", "");
		IFile system= createFile(project, "system.h", "");
		IFile source= createFile(project, "source.cpp", contents[0].toString());
		waitForIndexer(fIndex, source, INDEXER_WAIT_TIME);

		openIncludeBrowser(source);
		Tree tree = getIBTree();
		checkTreeNode(tree, 0, "source.cpp");
		checkTreeNode(tree, 0, 0, "user.h");
		checkTreeNode(tree, 0, 1, "system.h");
		
		// the tree has to be reversed
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
	
	public void _testInclusionAccrossProjects() throws Exception {
		ICProject op= CProjectHelper.createCCProject("__ibTest_other__", "bin", IPDOMManager.ID_FAST_INDEXER);
		try {
			IndexerPreferences.set(op.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
			CCoreInternals.getPDOMManager().reindex(op);
			fIndex= CCorePlugin.getIndexManager().getIndex(new ICProject[] {getProject(), op});
			
			TestScannerProvider.sIncludes= new String[]{op.getProject().getLocation().toOSString()};

			StringBuffer[] contents= getContentsForTest(1);
			IFile user= createFile(op.getProject(), "user.h", "");
			IFile system= createFile(op.getProject(), "system.h", "");
			IFile source= createFile(getProject().getProject(), "source.cpp", contents[0].toString());
			waitForIndexer(fIndex, source, INDEXER_WAIT_TIME);

			openIncludeBrowser(source);
			Tree tree = getIBTree();
			checkTreeNode(tree, 0, "source.cpp");
			checkTreeNode(tree, 0, 0, "user.h");
			checkTreeNode(tree, 0, 1, "system.h");
			
			// the tree has to be reversed
			openIncludeBrowser(user, true);
			checkTreeNode(tree, 0, "user.h");
			checkTreeNode(tree, 0, 0, "source.cpp");

			openIncludeBrowser(system);
			checkTreeNode(tree, 0, "system.h");
			checkTreeNode(tree, 0, 0, "source.cpp");

		}
		finally {
			CProjectHelper.delete(op);
		}
	}
	
}
