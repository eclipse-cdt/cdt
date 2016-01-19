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
import org.eclipse.core.resources.IResource;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public CMakeBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		super(config, toolChain);
	}

	@Override
	public IScannerInfo getScannerInfo(IResource resource) throws IOException {
		IScannerInfo info = getCachedScannerInfo(resource);
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
