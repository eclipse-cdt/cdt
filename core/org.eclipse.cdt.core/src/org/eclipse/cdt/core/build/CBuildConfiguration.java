/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.nio.file.Path;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.build.ToolChainManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Root class for CDT build configurations. Provides access to the build
 * settings for subclasses.
 * 
 * @since 6.0
 */
public abstract class CBuildConfiguration extends PlatformObject {

	private static final String TOOLCHAIN_TYPE = "cdt.toolChain.type"; //$NON-NLS-1$
	private static final String TOOLCHAIN_NAME = "cdt.toolChain.name"; //$NON-NLS-1$

	private final String name;
	private final IBuildConfiguration config;
	private final IToolChain toolChain;

	protected CBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
		String[] split = config.getName().split("/");
		if (split.length == 2) {
			name = split[1];
		} else {
			name = config.getName();
		}

		// Load toolchain from prefs
		Preferences settings = getSettings();
		String typeId = settings.get(TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
		String id = settings.get(TOOLCHAIN_NAME, ""); //$NON-NLS-1$
		IToolChainManager toolChainManager = CCorePlugin.getService(IToolChainManager.class);
		toolChain = !id.isEmpty() ? toolChainManager.getToolChain(typeId, id) : null;
	}

	protected CBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		this.config = config;
		String[] split = config.getName().split("/");
		if (split.length == 2) {
			name = split[1];
		} else {
			name = config.getName();
		}

		this.toolChain = toolChain;
		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_TYPE, toolChain.getType().getId());
		settings.put(TOOLCHAIN_NAME, toolChain.getName());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			CCorePlugin.log(e);
		}
	}

	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return name;
	}

	public IProject getProject() {
		return config.getProject();
	}

	public IFolder getBuildFolder() {
		try {
			// TODO should really be passing a monitor in here or create this in
			// a better spot. should also throw the core exception
			// TODO make the name of this folder a project property
			IFolder buildRootFolder = getProject().getFolder("build"); //$NON-NLS-1$
			if (!buildRootFolder.exists()) {
				buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, new NullProgressMonitor());
			}
			IFolder buildFolder = buildRootFolder.getFolder(name);
			if (!buildFolder.exists()) {
				buildFolder.create(true, true, new NullProgressMonitor());
			}
			return buildFolder;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public Path getBuildDirectory() {
		return getBuildFolder().getLocation().toFile().toPath();
	}

	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already set
			return;
		}

		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setActiveBuildConfig(config.getName());
		project.setDescription(projectDesc, monitor);
	}

	protected Preferences getSettings() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node("config") //$NON-NLS-1$
				.node(getProject().getName()).node(config.getName());
	}

	public IToolChain getToolChain() {
		return toolChain;
	}

}
