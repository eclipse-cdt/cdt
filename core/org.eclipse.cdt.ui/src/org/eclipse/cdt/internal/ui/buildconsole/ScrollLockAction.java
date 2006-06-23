/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		super(ConsoleMessages.getString("ScrollLockAction.Scroll_Lock_1")); //$NON-NLS-1$
		fConsoleViewer = viewer;
		setToolTipText(ConsoleMessages.getString("ScrollLockAction.Scroll_Lock_1")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SCROLL_LOCK);
		setChecked(false);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fConsoleViewer.setAutoScroll(!isChecked());
	}

}
