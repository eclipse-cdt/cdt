/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.launchbar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 * 
 * @author jjohnstn
 *
 * @since 1.2
 * 
 */
public class ContainerGCCToolChainProvider implements IToolChainProvider {

	public static final String PROVIDER_ID = "org.eclipse.cdt.docker.launcher.gcc.provider"; //$NON-NLS-1$
	public static final String CONTAINER_LINUX_CONFIG_ID = "linux-container-id"; //$NON-NLS-1$

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public void init(IToolChainManager manager) throws CoreException {
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		for (IDockerConnection connection : connections) {
			List<IDockerImage> images = connection.getImages();
			for (IDockerImage image : images) {
				if (!image.isDangling() && !image.isIntermediateImage()) {

					Map<String, String> properties = new HashMap<>();

					properties.put(ILaunchTarget.ATTR_OS,
							ContainerTargetTypeProvider.CONTAINER_LINUX);
					properties.put(ILaunchTarget.ATTR_ARCH,
							Platform.getOSArch());
					properties.put(IContainerLaunchTarget.ATTR_CONNECTION_URI,
							connection.getUri());
					properties.put(IContainerLaunchTarget.ATTR_IMAGE_ID,
							image.repoTags().get(0));
					// following can be used for naming build configurations
					properties.put(CONTAINER_LINUX_CONFIG_ID,
							image.repoTags().get(0).replace(':', '_'));

					ContainerGCCToolChain toolChain = new ContainerGCCToolChain(
							"gcc-img-" + image.id().substring(0, 19), //$NON-NLS-1$
							this, properties, null);

					manager.addToolChain(toolChain);
				}
			}
		}
	}

}
