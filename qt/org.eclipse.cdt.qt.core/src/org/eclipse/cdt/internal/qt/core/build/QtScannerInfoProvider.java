/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.build;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class QtScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			IProject project = resource.getProject();
			IBuildConfiguration config = project.getActiveBuildConfig();
			QtBuildConfiguration qtConfig = config.getAdapter(QtBuildConfiguration.class);
			if (qtConfig != null) {
				IQtInstall qtInstall = qtConfig.getQtInstall();
				
				String cxx = qtConfig.getProperty("QMAKE_CXX"); //$NON-NLS-1$
				if (cxx == null) {
					Activator.log("No QMAKE_CXX for " + qtInstall.getSpec()); //$NON-NLS-1$
					return null;
				}
				String[] cxxSplit = cxx.split(" "); //$NON-NLS-1$
				String command = cxxSplit[0];

				List<String> args = new ArrayList<>();
				for (int i = 1; i < cxxSplit.length; ++i) {
					args.add(cxxSplit[i]);
				}
				args.addAll(Arrays.asList(qtConfig.getProperty("QMAKE_CXXFLAGS").split(" "))); //$NON-NLS-1$ //$NON-NLS-2$
				args.add("-o"); //$NON-NLS-1$
				args.add("-"); //$NON-NLS-1$

				String srcFile;
				if (resource instanceof IFile) {
					srcFile = resource.getLocation().toOSString();
					// Only add file if it's an IFile
					args.add(srcFile);
				} else {
					// Doesn't matter, the toolchain will create a tmp file for this
					srcFile = "scannerInfo.cpp"; //$NON-NLS-1$
				}

				String[] includePaths = qtConfig.getProperty("INCLUDEPATH").split(" "); //$NON-NLS-1$ //$NON-NLS-2$
				for (int i = 0; i < includePaths.length; ++i) {
					Path path = Paths.get(includePaths[i]);
					if (!path.isAbsolute()) {
						includePaths[i] = qtConfig.getBuildDirectory().resolve(path).toString();
					}
				}

				Path dir = Paths.get(project.getLocationURI());
				IExtendedScannerInfo extendedInfo = qtConfig.getToolChain().getScannerInfo(command, args,
						Arrays.asList(includePaths), resource, dir);
				return extendedInfo;
			}
		} catch (CoreException | IOException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

}
