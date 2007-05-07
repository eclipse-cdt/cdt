/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;

import org.eclipse.cdt.ui.CUIPlugin;

public class ManageConfigRunner implements IConfigManager {
	private static final String MANAGE_TITLE = UIMessages.getString("ManageConfigDialog.0");  //$NON-NLS-1$

	protected static ManageConfigRunner instance = null;
	
	public static ManageConfigRunner getDefault() {
		if (instance == null)
			instance = new ManageConfigRunner();
		return instance;
	}
	
	public boolean canManage(IProject[] obs) {
		// Only one project can be accepted
		return (obs != null && obs.length == 1);
	}

	public boolean manage(IProject[] obs, boolean doOk) {
		if (!canManage(obs))
			return false;
		
		ManageConfigDialog d = new ManageConfigDialog(CUIPlugin.getActiveWorkbenchShell(),
				obs[0].getName()+ " : " + MANAGE_TITLE, obs[0]); //$NON-NLS-1$
		boolean result = false;
		if (d.open() == Window.OK) {
			if (doOk) {
				CDTPropertyManager.performOk(d.getShell());
			}
			AbstractPage.updateViews(obs[0]);
			result = true;
		}
		return result;
	}
}
