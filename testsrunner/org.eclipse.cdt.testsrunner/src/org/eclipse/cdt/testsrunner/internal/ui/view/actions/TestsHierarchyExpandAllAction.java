/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;

/**
 * Expands the tests hierarchy tree in the viewer.
 */
public class TestsHierarchyExpandAllAction extends Action {

	private AbstractTreeViewer testsHierarchyTreeViewer;


	public TestsHierarchyExpandAllAction(AbstractTreeViewer testsHierarchyTreeViewer) {
		setText(ActionsMessages.TestsHierarchyExpandAllAction_text);
		setToolTipText(ActionsMessages.TestsHierarchyExpandAllAction_tooltip);
		this.testsHierarchyTreeViewer = testsHierarchyTreeViewer;
	}

	@Override
	public void run(){
		testsHierarchyTreeViewer.expandAll();
	}
	
}

