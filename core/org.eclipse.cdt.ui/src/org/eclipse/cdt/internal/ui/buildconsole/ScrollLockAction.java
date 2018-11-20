/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.action.Action;

/**
 * Toggles console auto-scroll
 */

public class ScrollLockAction extends Action {

	private BuildConsoleViewer fConsoleViewer;

	public ScrollLockAction(BuildConsoleViewer viewer) {
		super(ConsoleMessages.ScrollLockAction_Scroll_Lock_1);
		fConsoleViewer = viewer;
		setToolTipText(ConsoleMessages.ScrollLockAction_Scroll_Lock_1);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SCROLL_LOCK);
		setChecked(false);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		fConsoleViewer.setAutoScroll(!isChecked());
	}

}
