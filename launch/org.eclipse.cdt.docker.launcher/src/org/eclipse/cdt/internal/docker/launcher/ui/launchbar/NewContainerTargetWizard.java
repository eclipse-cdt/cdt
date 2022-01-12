/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;

@SuppressWarnings("restriction")
/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public class NewContainerTargetWizard extends LaunchTargetWizard {

	private NewContainerTargetWizardPage page;
	protected IToolChainManager toolChainManager = CDebugCorePlugin.getService(IToolChainManager.class);

	public NewContainerTargetWizard() {
		if (getLaunchTarget() == null) {
			setWindowTitle(Messages.NewContainerTargetWizard_title);
		} else {
			setWindowTitle(Messages.EditContainerTargetWizard_title);
		}
	}

	@Override
	public void addPages() {
		super.addPages();

		page = new NewContainerTargetWizardPage(getLaunchTarget());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = CDebugUIPlugin.getService(ILaunchTargetManager.class);
		String typeId = ContainerTargetTypeProvider.TYPE_ID;
		String id = page.getTargetName();

		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			target = manager.addLaunchTarget(typeId, id);
		}

		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(ILaunchTarget.ATTR_OS, Platform.getOS());
		wc.setAttribute(ILaunchTarget.ATTR_ARCH, ContainerTargetTypeProvider.CONTAINER_LINUX);
		wc.setAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI, page.getConnectionURI());
		wc.setAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID, page.getImageId());
		wc.save();

		// Pick the first one that matches
		Map<String, String> properties = new HashMap<>();
		properties.putAll(wc.getAttributes());
		Collection<IToolChain> toolChains = Collections.emptyList();
		try {
			toolChains = toolChainManager.getToolChainsMatching(properties);
		} catch (CoreException e) {
			// do nothing
		}

		// if (toolChains.size() == 0) {
		// // add new Container toolchain with attributes above
		// ContainerToolChain toolChain = new ContainerToolChain();
		// toolChain.add(properties);
		// }

		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void performDelete() {
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null) {
			manager.removeLaunchTarget(getLaunchTarget());
		}
	}

}
