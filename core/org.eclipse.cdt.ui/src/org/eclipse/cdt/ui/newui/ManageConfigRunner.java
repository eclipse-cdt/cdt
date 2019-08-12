/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ManageConfigRunner implements IConfigManager {
	private static final String MANAGE_TITLE = Messages.ManageConfigDialog_0;

	protected static ManageConfigRunner instance = null;

	private ICProjectDescription des = null;
	private IProject prj = null;

	public static ManageConfigRunner getDefault() {
		if (instance == null)
			instance = new ManageConfigRunner();
		return instance;
	}

	@Override
	public boolean canManage(IProject[] obs) {
		// Only one project can be accepted
		return (obs != null && obs.length == 1);
	}

	@Override
	public boolean manage(IProject[] obs, boolean doOk) {
		if (!canManage(obs))
			return false;

		ManageConfigDialog d = new ManageConfigDialog(Display.getDefault().getActiveShell(),
				obs[0].getName() + ": " + MANAGE_TITLE, obs[0]); //$NON-NLS-1$
		boolean result = false;
		if (d.open() == Window.OK) {
			if (doOk) {
				des = d.getProjectDescription();
				prj = obs[0];
				if (des != null)
					try {
						PlatformUI.getWorkbench().getProgressService().run(false, false, getRunnable());
					} catch (InvocationTargetException e) {
					} catch (InterruptedException e) {
					}
			}
			AbstractPage.updateViews(obs[0]);
			result = true;
		} else if (doOk) {
			CDTPropertyManager.performCancel(d.getShell());
		}
		return result;
	}

	public IRunnableWithProgress getRunnable() {
		return new WorkspaceModifyDelegatingOperation(
				imonitor -> CUIPlugin.getDefault().getShell().getDisplay().syncExec(() -> {
					try {
						CoreModel.getDefault().setProjectDescription(prj, des);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}));
	}
}
