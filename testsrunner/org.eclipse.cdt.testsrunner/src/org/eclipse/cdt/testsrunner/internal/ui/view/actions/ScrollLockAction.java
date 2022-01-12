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
import org.eclipse.cdt.testsrunner.internal.ui.view.UIUpdater;
import org.eclipse.jface.action.Action;

/**
 * Toggles the auto-scroll for tests hierarchy tree.
 */
public class ScrollLockAction extends Action {

	private UIUpdater modelSyncronizer;

	public ScrollLockAction(UIUpdater modelSyncronizer) {
		super(ActionsMessages.ScrollLockAction_name);
		this.modelSyncronizer = modelSyncronizer;
		setToolTipText(ActionsMessages.ScrollLockAction_tooltip);
		setDisabledImageDescriptor(TestsRunnerPlugin.getImageDescriptor("dlcl16/scroll_lock.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
		setImageDescriptor(TestsRunnerPlugin.getImageDescriptor("elcl16/scroll_lock.gif")); //$NON-NLS-1$
		setChecked(!this.modelSyncronizer.getAutoScroll());
	}

	@Override
	public void run() {
		modelSyncronizer.setAutoScroll(!isChecked());
	}
}
