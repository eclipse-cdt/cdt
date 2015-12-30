/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core.internal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.build.core.CBuildConfiguration;
import org.eclipse.cdt.build.core.IBuildConfigurationManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class CBuildConfigurationManager
		implements IBuildConfigurationManager, IResourceChangeListener, IAdapterFactory {

	Map<IBuildConfiguration, CBuildConfiguration> configMap = new HashMap<>();

	@Override
	public CBuildConfiguration getBuildConfiguration(ILaunchDescriptor descriptor, String mode, ILaunchTarget target) {
		// TODO
		CBuildConfiguration config = null;

		configMap.put(config.getBuildConfiguration(), config);
		return config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IBuildConfiguration) {
			if (CBuildConfiguration.class.isAssignableFrom(adapterType)) {
				return (T) configMap.get(adaptableObject);
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { CBuildConfiguration.class };
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
			if (event.getResource().getType() == IResource.PROJECT) {
				IProject project = event.getResource().getProject();

				// Clean up the configMap
				try {
					for (IBuildConfiguration buildConfig : project.getBuildConfigs()) {
						configMap.remove(buildConfig);
					}
				} catch (CoreException e) {
					Activator.log(e);
				}

				// Clean up the config settings
				Preferences parentNode = InstanceScope.INSTANCE.getNode(Activator.getId()).node("config"); //$NON-NLS-1$
				if (parentNode != null) {
					Preferences projectNode = parentNode.node(project.getName());
					if (projectNode != null) {
						try {
							projectNode.removeNode();
							parentNode.flush();
						} catch (BackingStoreException e) {
							Activator.log(e);
						}
					}
				}

				// Clean up the scanner info data
				IPath stateLoc = Activator.getDefault().getStateLocation();
				IPath scannerInfoPath = stateLoc.append(project.getName());
				Path directory = scannerInfoPath.toFile().toPath();
				try {
					Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		}
	}

}
