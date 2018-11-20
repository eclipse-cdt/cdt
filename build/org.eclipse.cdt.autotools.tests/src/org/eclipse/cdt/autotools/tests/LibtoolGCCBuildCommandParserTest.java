/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation adapted from GCCBuildCommandParserTest
 *******************************************************************************/

package org.eclipse.cdt.autotools.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.Test;

/**
 * Test cases to test libtool build command parser.
 */
public class LibtoolGCCBuildCommandParserTest {
	// ID of the parser taken from the extension point
	private static final String BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.autotools.core.LibtoolGCCBuildCommandParser"; //$NON-NLS-1$

	/**
	 * Helper method to fetch configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, false);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	/**
	 * Test possible variations of libtool/compiler command.
	 */
	@Test
	public void testProcessLine() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = "testProcessLine";
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file1 = ResourceHelper.createFile(project, "file1.cpp");
		IFile file2 = ResourceHelper.createFile(project, "file2.cpp");
		IFile file3 = ResourceHelper.createFile(project, "file3.cpp");
		String languageId = LanguageSettingsManager.getLanguages(file1, cfgDescription).get(0);

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("libtool: compile:  gcc -I/path0 file1.cpp");
		parser.processLine("libtool: compile:  g++ -I/path0 file2.cpp");
		parser.processLine("libtool: compile:  cc -I/path0 file3.cpp");
		parser.shutdown();
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
		assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		entries = parser.getSettingEntries(cfgDescription, file2, languageId);
		assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		entries = parser.getSettingEntries(cfgDescription, file3, languageId);
		assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
	}

}