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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.includebrowser.IBViewPart;

public class IncludeBrowserBaseTest extends BaseUITestCase {
	protected static final int INDEXER_WAIT_TIME = 8000;
	protected static IProgressMonitor NPM= new NullProgressMonitor();

	private ICProject fCProject;
	protected IIndex fIndex;


	public IncludeBrowserBaseTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCCProject("__ibTest__", "bin", IPDOMManager.ID_FAST_INDEXER);
		
		// clear the index
		CCorePlugin.getIndexManager().reindex(fCProject);
		fIndex= CCorePlugin.getIndexManager().getIndex(fCProject);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null) {
			CProjectHelper.delete(fCProject);
		}
		super.tearDown();
	}
	
	protected ICProject getProject() {
		return fCProject;
	}
	
	protected IBViewPart openIncludeBrowser(IFile file) throws PartInitException {
		IBViewPart result = doOpenIncludeBrowser(file);
		runEventQueue(200);
		return result;
	}

	protected IBViewPart openIncludeBrowser(IFile file, boolean includedBy) throws PartInitException {
		IBViewPart result = doOpenIncludeBrowser(file);
		result.onSetDirection(includedBy);
		runEventQueue(200);
		return result;
	}

	private IBViewPart doOpenIncludeBrowser(IFile file) throws PartInitException {
		ITranslationUnit tu= CoreModelUtil.findTranslationUnit(file);
		if (tu == null) {
			fail(file.getFullPath().toString() + " is no translation unit!");
		}
        IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IBViewPart result= (IBViewPart)page.showView(CUIPlugin.ID_INCLUDE_BROWSER);
        result.setInput(tu);
		return result;
	}

	protected Tree getIBTree() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		runEventQueue(0);
		IBViewPart ib= (IBViewPart)page.findView(CUIPlugin.ID_INCLUDE_BROWSER);
		assertNotNull(ib);
		Tree tree= ib.getTreeViewer().getTree();
		return tree;
	}
}
