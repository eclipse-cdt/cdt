/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.jface.action.Action;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Advance console document to the next error reported by compiler
 */
public class NextErrorAction extends Action {

	private BuildConsolePage fConsolePage;

	public NextErrorAction(BuildConsolePage page) {
		super(ConsoleMessages.NextErrorAction_Tooltip); 
		fConsolePage = page;
		setEnabled(true);
		setToolTipText(ConsoleMessages.NextErrorAction_Tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_SHOW_NEXT);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		fConsolePage.moveToError(BuildConsolePage.POSITION_NEXT);
	}

}
