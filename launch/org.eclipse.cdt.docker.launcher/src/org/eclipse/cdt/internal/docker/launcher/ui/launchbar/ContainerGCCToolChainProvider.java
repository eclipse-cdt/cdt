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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager2;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IToolChainProvider;
import org.eclipse.cdt.docker.launcher.ContainerTargetTypeProvider;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.docker.launcher.IContainerLaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;

/**
 *
 * @author jjohnstn
 *
 * @since 1.2
 *
 */
public class ContainerGCCToolChainProvider implements IToolChainProvider, IDockerConnectionManagerListener {

	public static final String PROVIDER_ID = "org.eclipse.cdt.docker.launcher.gcc.provider"; //$NON-NLS-1$
	public static final String CONTAINER_LINUX_CONFIG_ID = "linux-container-id"; //$NON-NLS-1$

	private IToolChainManager toolChainManager;

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public synchronized void init(IToolChainManager manager) throws CoreException {
		this.toolChainManager = manager;
		IDockerConnection[] connections = DockerConnectionManager.getInstance().getConnections();
		Map<String, IDockerConnection> connectionMap = new HashMap<>();
		for (IDockerConnection connection : connections) {
			connectionMap.put(connection.getUri(), connection);
			List<IDockerImage> images = connection.getImages();
			for (IDockerImage image : images) {
				if (!image.isDangling() && !image.isIntermediateImage()) {

					Map<String, String> properties = new HashMap<>();

					properties.put(ILaunchTarget.ATTR_OS, ContainerTargetTypeProvider.CONTAINER_LINUX);
					properties.put(ILaunchTarget.ATTR_ARCH, Platform.getOSArch());
					properties.put(IContainerLaunchTarget.ATTR_CONNECTION_URI, connection.getUri());
					properties.put(IContainerLaunchTarget.ATTR_IMAGE_ID, image.repoTags().get(0));
					// following can be used for naming build configurations
					properties.put(CONTAINER_LINUX_CONFIG_ID, image.repoTags().get(0).replace(':', '_'));
					// .replace('/', '_'));

					ContainerGCCToolChain toolChain = new ContainerGCCToolChain(
							"gcc-img-" + image.id().substring(0, 19), //$NON-NLS-1$
							this, properties, null);

					manager.addToolChain(toolChain);
				}
			}
		}

		// add a Docker Connection listener to handle enablement/disablement of
		// Connections
		DockerConnectionManager.getInstance().addConnectionManagerListener(this);

		// re-check configs in case an enabled Connection has made old configs
		// valid again do this in a separate job to prevent a possible
		// deadlock trying to get the lock on the CBuildConfigurationManager
		// "configs" map (Bug 540085)
		Job checkConfigs = new Job("Check configs") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// call the recheckConfigs method in case any disabled targets
				// are now
				// ok
				ICBuildConfigurationManager mgr = CCorePlugin.getService(ICBuildConfigurationManager.class);
				ICBuildConfigurationManager2 cbuildmanager = (ICBuildConfigurationManager2) mgr;
				cbuildmanager.recheckConfigs();
				return Status.OK_STATUS;
			}
		};
		checkConfigs.setUser(true);
		checkConfigs.schedule();
	}

	@Override
	public synchronized void changeEvent(IDockerConnection connection, int type) {

		final ContainerGCCToolChainProvider provider = this;

		Job checkConfigs = new Job("Check configs") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				ICBuildConfigurationManager mgr = CCorePlugin.getService(ICBuildConfigurationManager.class);
				ICBuildConfigurationManager2 manager = (ICBuildConfigurationManager2) mgr;

				if (type == IDockerConnectionManagerListener.ADD_EVENT
						|| type == IDockerConnectionManagerListener.ENABLE_EVENT) {
					List<IDockerImage> images = connection.getImages();
					for (IDockerImage image : images) {
						if (!image.isDangling() && !image.isIntermediateImage()) {

							Map<String, String> properties = new HashMap<>();

							properties.put(ILaunchTarget.ATTR_OS, ContainerTargetTypeProvider.CONTAINER_LINUX);
							properties.put(ILaunchTarget.ATTR_ARCH, Platform.getOSArch());
							properties.put(IContainerLaunchTarget.ATTR_CONNECTION_URI, connection.getUri());
							properties.put(IContainerLaunchTarget.ATTR_IMAGE_ID, image.repoTags().get(0));
							// following can be used for naming build
							// configurations
							properties.put(CONTAINER_LINUX_CONFIG_ID, image.repoTags().get(0).replace(':', '_'));
							// .replace('/', '_'));

							Collection<IToolChain> toolChains;
							try {
								toolChains = toolChainManager.getToolChainsMatching(properties);
								if (toolChains.isEmpty()) {
									ContainerGCCToolChain toolChain = new ContainerGCCToolChain(
											"gcc-img-" + image.id().substring(0, //$NON-NLS-1$
													19),
											provider, properties, null);
									toolChainManager.addToolChain(toolChain);
								}
							} catch (CoreException e) {
								DockerLaunchUIPlugin.log(e);
							}
						}
					}
					// Re-evaluate config list in case a build config was marked
					// invalid and is now enabled
					manager.recheckConfigs();
				} else if (type == IDockerConnectionManagerListener.REMOVE_EVENT
						|| type == IDockerConnectionManagerListener.DISABLE_EVENT) {
					try {
						String connectionURI = connection.getUri();
						Collection<IToolChain> toolChains = toolChainManager.getAllToolChains();
						IToolChain[] toolChainArray = toolChains.toArray(new IToolChain[0]);
						for (int i = 0; i < toolChains.size(); ++i) {
							IToolChain toolChain = toolChainArray[i];
							String uri = toolChain.getProperty(IContainerLaunchTarget.ATTR_CONNECTION_URI);
							if (connectionURI.equals(uri)) {
								toolChainManager.removeToolChain(toolChain);
							}
						}
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						DockerLaunchUIPlugin.log(e1);
					}
				}
				return Status.OK_STATUS;
			}
		};
		checkConfigs.setUser(true);
		checkConfigs.schedule();
	}

}
