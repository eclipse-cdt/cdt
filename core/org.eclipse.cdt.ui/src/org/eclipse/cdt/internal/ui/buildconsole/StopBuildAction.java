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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Action to stop previously build previously run from Build Console 
 */
public class StopBuildAction extends Action {
	private static final String ID = "org.eclipse.cdt.ui.buildconsole.stopbuild"; //$NON-NLS-1$
	
	private BuildConsolePage fConsolePage;

	public StopBuildAction(BuildConsolePage page) {
		super(ConsoleMessages.RunBuildAction_Tooltip); 
		fConsolePage = page;
		setEnabled(false);
		setToolTipText(ConsoleMessages.StopBuildAction_Tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_LCL_CANCEL);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		this.setEnabled(false);
		fConsolePage.fRunBuildAction.cancelBuildJob();
	}
	
	@Override
	public String getId() {
		return ID;
	}

}
