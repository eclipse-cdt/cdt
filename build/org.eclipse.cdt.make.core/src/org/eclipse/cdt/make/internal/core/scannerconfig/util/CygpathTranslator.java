/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Hans-Erik Floryd (hef-cdt@rt-labs.com)  - http://bugs.eclipse.org/245692
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.ICygwinToolsFactroy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Use binary parser's 'cygpath' command to translate cygpaths to absolute paths.
 * Note that this class does not support build configurations.
 * 
 * @author vhirsl
 */
public class CygpathTranslator {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private CygPath cygPath = null;

	public CygpathTranslator(IProject project) {
		try {
			ICConfigExtensionReference[] parserRef = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(project);
			for (int i = 0; i < parserRef.length; i++) {
				try {
					IBinaryParser parser = CoreModelUtil.getBinaryParser(parserRef[i]);
					ICygwinToolsFactroy cygwinToolFactory = parser.getAdapter(ICygwinToolsFactroy.class);
					if (cygwinToolFactory != null) {
						cygPath = cygwinToolFactory.getCygPath();
					}
				} catch (ClassCastException e) {
				}
			}
		}
		catch (CoreException e) {
		}
	}

	public static List<String> translateIncludePaths(IProject project, List<String> sumIncludes) {
		// first check if cygpath translation is needed at all
		boolean translationNeeded = false;
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			for (Iterator<String> i = sumIncludes.iterator(); i.hasNext(); ) {
				String include = i.next();
				if (include.startsWith("/")) { //$NON-NLS-1$
					translationNeeded = true;
					break;
				}
			}
		}
		if (!translationNeeded) {
			return sumIncludes;
		}

		CygpathTranslator cygpath = new CygpathTranslator(project);
		boolean useCygPathExtension = cygpath.cygPath != null;
		boolean useCygwinFromPath = !useCygPathExtension;

		String envPath = null;
		if (useCygwinFromPath) {
			IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
			ICProjectDescription prjDes = CCorePlugin.getDefault().getProjectDescription(project, false);
			if (prjDes != null) {
				// we don't know for sure which configuration needs to be used here, so betting on "DefaultSettingConfiguration"
				// considering that scanner discovery uses "DefaultSettingConfiguration" rather than "Active" configuration,
				// see org.eclipse.cdt.build.core.scannerconfig.ScannerConfigBuilder.build(CfgInfoContext context, ...)
				ICConfigurationDescription cfgDes = prjDes.getDefaultSettingConfiguration();
				IEnvironmentVariable envVar = mngr.getVariable(ENV_PATH, cfgDes, true);
				if (envVar != null) {
					envPath = envVar.getValue();
				}
			}
			if (envPath == null) {
				IEnvironmentVariable envVar = mngr.getVariable(ENV_PATH, null, true);
				if (envVar != null) {
					envPath = envVar.getValue();
				}
			}

			useCygwinFromPath = Cygwin.isAvailable(envPath);
		}

		List<String> translatedIncludePaths = new ArrayList<String>();
		for (Iterator<String> i = sumIncludes.iterator(); i.hasNext(); ) {
			String includePath = i.next();
			IPath realPath = new Path(includePath);
			// only allow native pathes if they have a device prefix
			// to avoid matches on the current drive, e.g. /usr/bin = C:\\usr\\bin
			if (realPath.getDevice() != null && realPath.toFile().exists()) {
				translatedIncludePaths.add(includePath);
			}
			else {
				String translatedPath = includePath;

				if (useCygPathExtension) {
					try {
						translatedPath = cygpath.cygPath.getFileName(includePath);
					}
					catch (IOException e) {
						TraceUtil.outputError("CygpathTranslator unable to translate path: ", includePath); //$NON-NLS-1$
					}
				} else if (useCygwinFromPath) {
					try {
						translatedPath = Cygwin.cygwinToWindowsPath(includePath, envPath);
					} catch (Exception e) {
						MakeCorePlugin.log(e);
					}
				} else if (realPath.segmentCount() >= 2) {
					// try default conversions
					//     /cygdrive/x/ --> X:\
					if ("cygdrive".equals(realPath.segment(0))) { //$NON-NLS-1$
						String drive= realPath.segment(1);
						if (drive.length() == 1) {
							translatedPath= realPath.removeFirstSegments(2).makeAbsolute().setDevice(drive.toUpperCase() + ':').toOSString();
						}
					}
				}
				if (!translatedPath.equals(includePath)) {
					// Check if the translated path exists
					if (new File(translatedPath).exists()) {
						translatedIncludePaths.add(translatedPath);
					}
					else if (useCygPathExtension || useCygwinFromPath) {
						// TODO VMIR for now add even if it does not exist
						translatedIncludePaths.add(translatedPath);
					}
					else {
						translatedIncludePaths.add(includePath);
					}
				}
				else {
					// TODO VMIR for now add even if it does not exist
					translatedIncludePaths.add(translatedPath);
				}
			}
		}
		if (useCygPathExtension) {
			cygpath.cygPath.dispose();
		}
		return translatedIncludePaths;
	}

}
