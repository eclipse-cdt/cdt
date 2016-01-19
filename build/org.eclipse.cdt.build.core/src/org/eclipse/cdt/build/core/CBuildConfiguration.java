/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.cdt.build.core.internal.Activator;
import org.eclipse.cdt.build.core.internal.ScannerInfoData;
import org.eclipse.cdt.build.core.internal.ToolChainScannerInfo;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
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
 * @since 5.12
 */
public abstract class CBuildConfiguration extends PlatformObject {

	private static final String TOOLCHAIN_TYPE = "cdt.toolChain.type"; //$NON-NLS-1$
	private static final String TOOLCHAIN_NAME = "cdt.toolChain.name"; //$NON-NLS-1$

	private final IBuildConfiguration config;
	private final IToolChain toolChain;

	private ScannerInfoData scannerInfoCache;

	protected CBuildConfiguration(IBuildConfiguration config) {
		this.config = config;

		// Load toolchain from prefs
		Preferences settings = getSettings();
		String typeId = settings.get(TOOLCHAIN_TYPE, ""); //$NON-NLS-1$
		String id = settings.get(TOOLCHAIN_NAME, ""); //$NON-NLS-1$
		toolChain = !id.isEmpty() ? Activator.getToolChainManager().getToolChain(typeId, id) : null;
	}

	protected CBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		this.config = config;
		this.toolChain = toolChain;
		Preferences settings = getSettings();
		settings.put(TOOLCHAIN_TYPE, toolChain.getType().getId());
		settings.put(TOOLCHAIN_NAME, toolChain.getName());
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	public IBuildConfiguration getBuildConfiguration() {
		return config;
	}

	public String getName() {
		return config.getName();
	}

	public IProject getProject() {
		return config.getProject();
	}

	public IFolder getBuildFolder() {
		String configName = getBuildConfiguration().getName();
		if (configName.isEmpty()) {
			configName = "default"; //$NON-NLS-1$
		}

		try {
			// TODO should really be passing a monitor in here or create this in
			// a better spot. should also throw the core exception
			// TODO make the name of this folder a project property
			IFolder buildRootFolder = getProject().getFolder("build"); //$NON-NLS-1$
			if (!buildRootFolder.exists()) {
				buildRootFolder.create(IResource.FORCE | IResource.DERIVED, true, new NullProgressMonitor());
			}
			IFolder buildFolder = buildRootFolder.getFolder(configName);
			if (!buildFolder.exists()) {
				buildFolder.create(true, true, new NullProgressMonitor());
			}
			return buildFolder;
		} catch (CoreException e) {
			Activator.log(e);
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
		return InstanceScope.INSTANCE.getNode(Activator.getId()).node("config") //$NON-NLS-1$
				.node(getProject().getName()).node(config.getName());
	}

	public IToolChain getToolChain() {
		return toolChain;
	}

	public IScannerInfo getScannerInfo(IResource resource) throws IOException {
		return getCachedScannerInfo(resource);
	}

	protected IScannerInfo getCachedScannerInfo(IResource resource) throws IOException {
		return getScannerInfoCache().getScannerInfo(resource);
	}

	public void putScannerInfo(ILanguage language, IExtendedScannerInfo info) {
		getScannerInfoCache().putScannerInfo(language, info);
	}

	public void putScannerInfo(IResource resource, ToolChainScannerInfo info) {
		getScannerInfoCache().putScannerInfo(resource, info);
	}

	private ScannerInfoData getScannerInfoCache() {
		if (scannerInfoCache == null) {
			scannerInfoCache = ScannerInfoData.load(this);
		}
		return scannerInfoCache;
	}

	public void clearScannerInfoCache() throws CoreException {
		scannerInfoCache = null;
	}

	public Collection<CConsoleParser> getConsoleParsers() throws CoreException {
		IToolChain toolChain = getToolChain();
		return toolChain != null ? toolChain.getConsoleParsers() : null;
	}

}
