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

import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Launches the new debug session for the currently selected items of test
 * hierarchy.
 */
public class RedebugSelectedAction extends RelaunchSelectedAction {

	public RedebugSelectedAction(ITestingSession testingSession, TreeViewer treeViewer) {
		super(testingSession, treeViewer);
		setText(ActionsMessages.RedebugSelectedAction_text);
		setToolTipText(ActionsMessages.RedebugSelectedAction_tooltip);
	}

	@Override
	protected String getLaunchMode() {
		return ILaunchManager.DEBUG_MODE;
	}

}
