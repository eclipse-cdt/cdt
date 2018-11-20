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

import org.eclipse.cdt.testsrunner.internal.ui.view.TestsHierarchyViewer;
import org.eclipse.jface.action.Action;

/**
 * Toggles the test execution time showing in tests hierarchy viewer.
 */
public class ShowTimeAction extends Action {

	private TestsHierarchyViewer testsHierarchyViewer;

	public ShowTimeAction(TestsHierarchyViewer testsHierarchyViewer) {
		super(ActionsMessages.ShowTimeAction_text, AS_CHECK_BOX);
		this.testsHierarchyViewer = testsHierarchyViewer;
		setToolTipText(ActionsMessages.ShowTimeAction_tooltip);
		setChecked(testsHierarchyViewer.showTime());
	}

	@Override
	public void run() {
		testsHierarchyViewer.setShowTime(isChecked());
	}

}
