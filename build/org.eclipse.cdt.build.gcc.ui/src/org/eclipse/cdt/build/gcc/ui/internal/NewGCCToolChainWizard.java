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
package org.eclipse.cdt.build.gcc.ui.internal;

import java.nio.file.Path;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.build.gcc.core.GCCUserToolChainProvider;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IUserToolChainProvider;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.ui.build.ToolChainWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class NewGCCToolChainWizard extends ToolChainWizard {

	private GCCToolChainSettingsPage settingsPage;
	private ToolChainEnvironmentPage envPage;

	@Override
	public boolean performFinish() {
		Path path = settingsPage.getPath();
		String os = settingsPage.getOS().trim();
		String arch = settingsPage.getArch().trim();
		IEnvironmentVariable[] envvars = envPage.getEnvVars();

		new Job(Messages.NewGCCToolChainWizard_Add) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					IToolChainManager manager = Activator.getService(IToolChainManager.class);
					IUserToolChainProvider provider = (IUserToolChainProvider) manager
							.getProvider(GCCUserToolChainProvider.PROVIDER_ID);

					if (toolChain != null) {
						provider.removeToolChain(toolChain);
					}

					GCCToolChain gcc = new GCCToolChain(provider, path, arch, envvars);
					if (!os.isEmpty()) {
						gcc.setProperty(IToolChain.ATTR_OS, os);
					}
					provider.addToolChain(gcc);
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		}.schedule();
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();

		settingsPage = new GCCToolChainSettingsPage((GCCToolChain) toolChain, false);
		addPage(settingsPage);

		envPage = new ToolChainEnvironmentPage(toolChain);
		addPage(envPage);

		setWindowTitle(Messages.NewGCCToolChainWizard_New);
	}

}
