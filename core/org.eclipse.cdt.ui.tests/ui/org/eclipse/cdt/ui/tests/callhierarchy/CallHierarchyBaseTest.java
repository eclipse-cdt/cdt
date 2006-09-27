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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.tests.BaseTestCase;

import org.eclipse.cdt.internal.core.pdom.PDOM;

import org.eclipse.cdt.internal.ui.callhierarchy.CHViewPart;
import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;
import org.eclipse.cdt.internal.ui.editor.CEditor;

public class CallHierarchyBaseTest extends BaseTestCase {
	
	private ICProject fCProject;
	public PDOM fPdom;

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
			CProjectHelper.delete(fCProject);
		}
	}
	
	protected IProject getProject() {
		return fCProject.getProject();
	}
	
	protected void openCallHierarchy(CEditor editor) {
		CallHierarchyUI.setIsJUnitTest(true);
		CallHierarchyUI.open(editor, (ITextSelection) editor.getSelectionProvider().getSelection());
		runEventQueue(0);
	}

	protected Tree getCHTree(IWorkbenchPage page) {
		runEventQueue(0);
		CHViewPart ch= (CHViewPart)page.findView(CUIPlugin.ID_CALL_HIERARCHY);
		assertNotNull(ch);
		Tree tree= ch.getTreeViewer().getTree();
		return tree;
	}

	protected void checkTreeNode(Tree tree, int i0, String label) {
		TreeItem root= null;
		try {
			for (int i=0; i<20; i++) {
				root= tree.getItem(i0);
				if (!"...".equals(root.getText())) {
					break;
				}
				runEventQueue(50);
			}
		}
		catch (IllegalArgumentException e) {
			assertTrue("Tree node " + label + "{" + i0 + "} does not exist!", false);
		}
		assertEquals(label, root.getText());
	}

	protected void checkTreeNode(Tree tree, int i0, int i1, String label) {
		try {
			TreeItem root= tree.getItem(i0);
			TreeItem item= root.getItem(i1);
			for (int i=0; i<20; i++) {
				if (!"...".equals(item.getText())) {
					break;
				}
				runEventQueue(50);
			}
			assertEquals(label, item.getText());
		}
		catch (IllegalArgumentException e) {
			assertTrue("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!", false);
		}
	}
}
