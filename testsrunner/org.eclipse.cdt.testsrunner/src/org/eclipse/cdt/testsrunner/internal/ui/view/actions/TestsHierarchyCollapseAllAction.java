/*******************************************************************************
 * Copyright (c) 2011, 2012 Anton Gorenkov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;

/**
 * Collapses the tests hierarchy tree in the viewer.
 */
public class TestsHierarchyCollapseAllAction extends Action {

	private AbstractTreeViewer testsHierarchyTreeViewer;

	public TestsHierarchyCollapseAllAction(AbstractTreeViewer testsHierarchyTreeViewer) {
		setText(ActionsMessages.TestsHierarchyCollapseAllAction_text);
		setToolTipText(ActionsMessages.TestsHierarchyCollapseAllAction_tooltip);
		this.testsHierarchyTreeViewer = testsHierarchyTreeViewer;
	}

	@Override
	public void run() {
		testsHierarchyTreeViewer.collapseAll();
	}

}
