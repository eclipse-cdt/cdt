/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.action.Action;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Retard console document to the previous error reported by compiler
 */
public class PreviousErrorAction extends Action {

	private BuildConsolePage fConsolePage;

	public PreviousErrorAction(BuildConsolePage page) {
		super(ConsoleMessages.PreviousErrorAction_Tooltip); 
		fConsolePage = page;
		setEnabled(true);
		setToolTipText(ConsoleMessages.PreviousErrorAction_Tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_PREV);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		fConsolePage.moveToError(BuildConsolePage.POSITION_PREV);
	}

}
