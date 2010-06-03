/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
import org.eclipse.core.runtime.NullProgressMonitor;

public class IndexTestBase extends BaseTestCase {
	protected static int INDEXER_WAIT_TIME= 8000;
	
	public IndexTestBase(String name) {
		super(name);
	}

	protected ICProject createProject(final boolean useCpp, final String importSource) throws CoreException {
		// Create the project
		final ICProject[] result= new ICProject[] {null};
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				String name= "IndexTest_" + System.currentTimeMillis();
				if (useCpp) {
					result[0]= CProjectHelper.createCCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				} else {
					result[0]= CProjectHelper.createCProject(name, null, IPDOMManager.ID_NO_INDEXER);
				}					
				CProjectHelper.importSourcesFromPlugin(result[0], CTestPlugin.getDefault().getBundle(), importSource);
			}
		}, null);
		CCorePlugin.getIndexManager().setIndexerId(result[0], IPDOMManager.ID_FAST_INDEXER);		
		// wait until the indexer is done
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(10000, new NullProgressMonitor()));
		return result[0];
	}
	
	protected String readTaggedComment(String tag) throws Exception {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
	}
	
    protected StringBuffer[] getContentsForTest(int blocks) throws IOException {
    	return TestSourceReader.getContentsForTest(
    			CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), blocks);
    }
}
