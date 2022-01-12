/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.debug.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class BuildOptionsParser implements IWorkspaceRunnable, IMarkerGenerator {

	private final IProject project;
	private final File buildLog;
	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$

	public BuildOptionsParser(IProject project, File buildLog) {
		this.project = project;
		this.buildLog = buildLog;
	}

	@Override
	public void run(IProgressMonitor monitor) {
		monitor.beginTask(Messages.GetBuildOptions, 10);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(buildLog));
			// Calculate how many source files we have to process and use that as a basis
			monitor.beginTask(Messages.GetBuildOptions, 10);

			// Find the GCCBuildCommandParser for the configuration.
			ICProjectDescriptionManager projDescManager = CCorePlugin.getDefault().getProjectDescriptionManager();
			ICProjectDescription projDesc = projDescManager.getProjectDescription(project, false);
			ICConfigurationDescription ccdesc = projDesc.getActiveConfiguration();
			GCCBuildCommandParser parser = null;
			if (ccdesc instanceof ILanguageSettingsProvidersKeeper) {
				ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper) ccdesc;
				List<ILanguageSettingsProvider> list = keeper.getLanguageSettingProviders();
				for (ILanguageSettingsProvider p : list) {
					//						System.out.println("language settings provider " + p.getId());
					if (p.getId().equals(GCC_BUILD_OPTIONS_PROVIDER_ID)) {
						parser = (GCCBuildCommandParser) p;
					}
				}
			}
			ErrorParserManager epm = new ErrorParserManager(project, this,
					new String[] { "org.eclipse.cdt.core.CWDLocator" }); //$NON-NLS-1$
			// Start up the parser and process lines generated from the .debug_macro section.
			parser.startup(ccdesc, epm);
			monitor.beginTask(Messages.GetBuildOptions, 10);
			String line = br.readLine();
			while (line != null) {
				parser.processLine(line);
				line = br.readLine();
			}
			parser.shutdown();
			if (br != null)
				br.close();

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		monitor.done();
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
		// do nothing
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		// do nothing
	}

}
