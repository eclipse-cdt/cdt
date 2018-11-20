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
import org.eclipse.cdt.testsrunner.internal.ui.view.ResultsView;
import org.eclipse.jface.action.Action;

/**
 * Toggles the orientation of the view.
 */
public class ToggleOrientationAction extends Action {

	private ResultsView resultsView;
	private ResultsView.Orientation orientation;

	public ToggleOrientationAction(ResultsView resultsView, ResultsView.Orientation orientation) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		this.resultsView = resultsView;
		if (orientation == ResultsView.Orientation.Horizontal) {
			setText(ActionsMessages.ToggleOrientationAction_horizontal_text);
			setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_horizontal.gif")); //$NON-NLS-1$
			setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/orientation_horizontal.gif")); //$NON-NLS-1$
			setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_horizontal.gif")); //$NON-NLS-1$
		} else if (orientation == ResultsView.Orientation.Vertical) {
			setText(ActionsMessages.ToggleOrientationAction_vertical_text);
			setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_vertical.gif")); //$NON-NLS-1$
			setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/orientation_vertical.gif")); //$NON-NLS-1$
			setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_vertical.gif")); //$NON-NLS-1$
		} else if (orientation == ResultsView.Orientation.Auto) {
			setText(ActionsMessages.ToggleOrientationAction_automatic_text);
			setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_auto.gif")); //$NON-NLS-1$
			setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/orientation_auto.gif")); //$NON-NLS-1$
			setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/orientation_auto.gif")); //$NON-NLS-1$
		}
		this.orientation = orientation;
	}

	public ResultsView.Orientation getOrientation() {
		return orientation;
	}

	@Override
	public void run() {
		if (isChecked()) {
			resultsView.setOrientation(orientation);
		}
	}

}
