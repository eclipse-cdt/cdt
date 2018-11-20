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

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.ResultsPanel;
import org.eclipse.jface.action.Action;

/**
 * Toggles the filter for the passed test items.
 */
public class ShowFailedOnlyAction extends Action {

	private ResultsPanel resultsPanel;

	public ShowFailedOnlyAction(ResultsPanel resultsPanel) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.resultsPanel = resultsPanel;
		setText(ActionsMessages.ShowFailedOnlyAction_text);
		setToolTipText(ActionsMessages.ShowFailedOnlyAction_tooltip);
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("obj16/show_failed_only.gif")); //$NON-NLS-1$
		setChecked(resultsPanel.getShowFailedOnly());
	}

	@Override
	public void run() {
		resultsPanel.setShowFailedOnly(isChecked());
	}

}
