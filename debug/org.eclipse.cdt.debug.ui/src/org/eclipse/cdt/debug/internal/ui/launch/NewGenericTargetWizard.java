/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.cdt.debug.core.launch.GenericTargetTypeProvider;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;

public class NewGenericTargetWizard extends LaunchTargetWizard {

	private NewGenericTargetWizardPage page;

	public NewGenericTargetWizard() {
		setWindowTitle(LaunchMessages.getString("NewGenericTargetWizard_0")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		super.addPages();

		page = new NewGenericTargetWizardPage(getLaunchTarget());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = CDebugUIPlugin.getService(ILaunchTargetManager.class);
		String typeId = GenericTargetTypeProvider.TYPE_ID;
		String id = page.getTargetName();

		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			target = manager.addLaunchTarget(typeId, id);
		}

		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(ILaunchTarget.ATTR_OS, page.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, page.getArch());
		wc.save();

		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void performDelete() {
		ILaunchTargetManager manager = CDebugUIPlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null) {
			manager.removeLaunchTarget(target);
		}
	}

}
