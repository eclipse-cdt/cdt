/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.cdt.build.core.CBuildConfiguration;
import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public CMakeBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		super(config, toolChain);
	}

	private IFolder getBuildFolder() {
		String configName = getBuildConfiguration().getName();
		if (configName.isEmpty()) {
			configName = "default"; //$NON-NLS-1$
		}

		try {
			// TODO should really be passing a monitor in here or create this in
			// a better spot. should also throw the core exception
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

	@Override
	public IScannerInfo getScannerInfo(IResource resource) throws IOException {
		IScannerInfo info = super.getScannerInfo(resource);
		if (info == null) {
			ILanguage language = LanguageManager.getInstance()
					.getLanguage(CCorePlugin.getContentType(getProject(), resource.getName()), getProject()); // $NON-NLS-1$
			Path dir = Paths.get(getProject().getLocationURI());

			// TODO this is where we need to pass the compile options for this
			// file.

			IExtendedScannerInfo extendedInfo = getToolChain().getScannerInfo(getToolChain().getCommand(),
					Arrays.asList("-c", new File(resource.getLocationURI()).getAbsolutePath()), //$NON-NLS-1$
					Collections.emptyList(), resource, dir);
			putScannerInfo(language, extendedInfo);
			info = extendedInfo;
		}
		return info;
	}

}
