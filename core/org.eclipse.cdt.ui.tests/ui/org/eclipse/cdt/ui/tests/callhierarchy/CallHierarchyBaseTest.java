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

package org.eclipse.cdt.ui.tests.callhierarchy;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.BaseTestCase;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;
import org.eclipse.cdt.internal.ui.editor.CEditor;

public class CallHierarchyBaseTest extends BaseTestCase {
	
	private ICProject fCProject;
	private PDOM fPdom;

	public CallHierarchyBaseTest(String name) {
		super(name);
	}

	protected void setUp() throws CoreException {
		fCProject= CProjectHelper.createCProject("__chTest__", "bin");
		CCorePlugin.getPDOMManager().setIndexerId(fCProject, "org.eclipse.cdt.core.fastIndexer");
		fPdom= (PDOM) CCorePlugin.getPDOMManager().getPDOM(fCProject);
		fPdom.clear();
	}
	
	protected void tearDown() throws CoreException {
		if (fCProject != null) {
			fCProject.getProject().delete(IProject.FORCE | IProject.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}
	
	protected IProject getProject() {
		return fCProject.getProject();
	}
	
	protected void waitForIndexer(IFile file, int maxmillis) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		do {
			fPdom.acquireReadLock();
			try {
				PDOMFile pfile= fPdom.getFile(file.getLocation());
				// mstodo check timestamp
				if (pfile != null) {
					return;
				}
			}
			finally {
				fPdom.releaseReadLock();
			}
			
			Thread.sleep(50);
		} while (System.currentTimeMillis() < endTime);
		throw new Exception("Indexer did not complete in time!");
	}

	protected void openCallHierarchy(CEditor editor) {
		CallHierarchyUI.setIsJUnitTest(true);
		CallHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
	}

	protected Tree getCHTree(IWorkbenchPage page) {
		CHViewPart ch= (CHViewPart)page.findView(CUIPlugin.ID_CALL_HIERARCHY);
		assertNotNull(ch);
		Tree tree= ch.getTreeViewer().getTree();
		return tree;
	}

	protected void runEventQueue(int time) {
		long endTime= System.currentTimeMillis()+time;
		do {
			while (Display.getCurrent().readAndDispatch());
		}
		while(System.currentTimeMillis() < endTime);
	}

	protected void checkTreeNode(Tree tree, int i0, String label) {
		TreeItem root= null;
		try {
			root= tree.getItem(i0);
		}
		catch (IllegalArgumentException e) {
			assertTrue("Tree node " + label + "{" + i0 + "} does not exist!", false);
		}
		assertEquals(label, root.getText());
	}

	protected void checkTreeNode(Tree tree, int i0, int i1, String label) {
		TreeItem item= null;
		try {
			TreeItem root= tree.getItem(i0);
			item= root.getItem(i1);
		}
		catch (IllegalArgumentException e) {
			assertTrue("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!", false);
		}
		assertEquals(label, item.getText());
	}
}
