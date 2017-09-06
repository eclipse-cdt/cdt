/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.cdt.debug.core.launch.GenericTargetTypeProvider;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewGenericTargetWizard extends Wizard implements INewWizard {

	private NewGenericTargetWizardPage page;

	public NewGenericTargetWizard() {
		setWindowTitle("New Generic Target");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing
	}

	@Override
	public void addPages() {
		super.addPages();

		page = new NewGenericTargetWizardPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = CDebugUIPlugin.getService(ILaunchTargetManager.class);
		String typeId = GenericTargetTypeProvider.TYPE_ID;
		String id = page.getTargetName();

		ILaunchTarget target = manager.addLaunchTarget(typeId, id);
		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setAttribute(ILaunchTarget.ATTR_OS, page.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, page.getArch());
		wc.save();

		return true;
	}

}
