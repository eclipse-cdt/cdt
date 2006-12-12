/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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
import org.eclipse.swt.widgets.Tree;

import org.eclipse.cdt.core.testplugin.TestScannerProvider;


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
		IFile user= createFile(getProject(), "user.h", "");
		IFile system= createFile(getProject(), "system.h", "");
		IFile source= createFile(getProject(), "source.cpp", contents[0].toString());
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
}
