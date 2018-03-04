/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - modified for use with Docker Container launching
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * @since 1.2
 * @author jjohnstn
 *
 */
public class ContainerTargetTypeProvider implements ILaunchTargetProvider {

	public static final String TYPE_ID = "org.eclipse.cdt.docker.launcher.launchTargetType.container"; //$NON-NLS-1$
	public static final String CONTAINER_LINUX = "linux-container"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {
		ILaunchBarManager launchbarManager = CDebugCorePlugin
				.getService(ILaunchBarManager.class);
		ILaunchTarget defaultTarget = null;
		try {
			defaultTarget = launchbarManager.getActiveLaunchTarget();
		} catch (CoreException e) {
			// ignore
		}
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		Set<String> imageNames = new HashSet<>();
		for (IDockerConnection connection : connections) {
			List<IDockerImage> images = connection.getImages();
			for (IDockerImage image : images) {
				if (!image.isDangling() && !image.isIntermediateImage()) {
					String imageName = "[" //$NON-NLS-1$
							+ image.repoTags().get(0).replace(':', '_') + "]"; //$NON-NLS-1$
					if (imageNames.contains(imageName)) {
						imageName += "[" + connection.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					imageNames.add(imageName);
					ILaunchTarget target = targetManager
							.getLaunchTarget(TYPE_ID, imageName);
					if (target == null) {
						target = targetManager.addLaunchTarget(TYPE_ID,
								imageName);
					}
					ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
					wc.setAttribute(ILaunchTarget.ATTR_OS, CONTAINER_LINUX);
					wc.setAttribute(ILaunchTarget.ATTR_ARCH,
							Platform.getOSArch());
					wc.setAttribute(IContainerLaunchTarget.ATTR_CONNECTION_URI,
							connection.getUri());
					wc.setAttribute(IContainerLaunchTarget.ATTR_IMAGE_ID,
							image.repoTags().get(0));

					wc.save();
				}
			}
		}
		try {
			launchbarManager.setActiveLaunchTarget(defaultTarget);
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// Always OK
		return TargetStatus.OK_STATUS;
	}

}
