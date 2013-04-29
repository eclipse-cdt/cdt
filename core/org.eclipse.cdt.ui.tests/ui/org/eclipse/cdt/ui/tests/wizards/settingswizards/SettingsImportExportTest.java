/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.wizards.settingswizards;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.ui.wizards.settingswizards.ISettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.IncludePathsSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.MacroSettingsProcessor;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsExportStrategy;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsImportStrategy;

public class SettingsImportExportTest extends BaseUITestCase {

	private static ICLanguageSettingEntry[] EXPORTED_MACROS = new ICLanguageSettingEntry[] {
		new CMacroEntry("MAC1", "value1", 0),
		new CMacroEntry("anothermacro", "", 0),
		new CMacroEntry("smac", "blah", 0)
	};
		
	private static ICLanguageSettingEntry[] EXPORTED_INCLUDES = new ICLanguageSettingEntry[] {
		new CIncludePathEntry("/path/to/somewhere", 0),
		new CIncludePathEntry("/blah/blah/blah", 0),
		new CIncludePathEntry("pantera/is/awesome", 0)
	};
		
		

	public SettingsImportExportTest() {}
	
	public SettingsImportExportTest(String name) {
		super(name);
	}
	


	// This could be replaced with an extension point
	private static final List<ISettingsProcessor> processors = Arrays.<ISettingsProcessor>asList(
		new IncludePathsSettingsProcessor(),
		new MacroSettingsProcessor()
	);
	
	
	private static void createFile(String contents, String filePath) throws Exception {
		IPath path = new Path(filePath);
		FileWriter writer = new FileWriter(path.toFile());
		writer.write(contents);
		writer.close();
	}
	
	private static void deleteFile(String filePath) {
		new Path(filePath).toFile().delete();
	}
	
	private static String getFilePath(String fileName) {
		IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		return workspaceLocation.toOSString() + IPath.SEPARATOR + fileName;
	}
	
	
	private void setUpProjectSettings(ICProject cProject) throws Exception {
		IProject project = cProject.getProject();
		ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project, true);
		ICConfigurationDescription config = desc.getActiveConfiguration();
		ICFolderDescription folder = config.getRootFolderDescription();
		ICLanguageSetting[] languageSettings = folder.getLanguageSettings();
		ICLanguageSetting languageSetting = languageSettings[0];
		languageSetting.setSettingEntries(ICSettingEntry.MACRO, Arrays.asList(EXPORTED_MACROS));
		languageSetting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, EXPORTED_INCLUDES);
		CoreModel.getDefault().setProjectDescription(project, desc);
	}
	
	
	public void testNormalExportImport() throws Exception {
		ICProject exportProject = CProjectHelper.createCCProject("TempProject1", "unused");
		ICProject importProject = CProjectHelper.createCCProject("TempProject2", "unused");
		setUpProjectSettings(exportProject);
		
		ProjectSettingsWizardPageMock page = new ProjectSettingsWizardPageMock() {
			@Override public void setMessage(String message, int flag) {
				if(flag == IMessageProvider.ERROR)
					fail("there should be no error message displayed");
			}
			@Override public void showErrorDialog(String dialogTitle, String message) {
				fail("the error dialog should not be displayed"); 
			}
		};
		
		page.setDestinationFilePath(getFilePath("settings.xml"));
		page.setSettingsProcessors(processors);
		page.setSelectedSettingsProcessors(processors);
		ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(exportProject.getProject(), false);
		ICConfigurationDescription config = desc.getActiveConfiguration();
		page.setSelectedConfiguration(config);
		
		ProjectSettingsExportStrategy exporter = new ProjectSettingsExportStrategy();
		exporter.finish(page);

		
		// now import into another project
		
		desc = CoreModel.getDefault().getProjectDescription(importProject.getProject(), true);
		config = desc.getActiveConfiguration();
		page.setSelectedConfiguration(config);
		
		ProjectSettingsImportStrategy importer = new ProjectSettingsImportStrategy();
		importer.finish(page);

		desc = CoreModel.getDefault().getProjectDescription(importProject.getProject(), true);
		config = desc.getActiveConfiguration();
		ICFolderDescription folder = config.getRootFolderDescription();
		ICLanguageSetting languageSetting = folder.getLanguageSettings()[0];
		
		ICLanguageSettingEntry[] importedMacros = languageSetting.getSettingEntries(ICSettingEntry.MACRO);
		
		assertEquals(EXPORTED_MACROS.length, importedMacros.length);
		for(int i = 0; i < importedMacros.length; i++) {
			assertEquals(EXPORTED_MACROS[i].getName(), importedMacros[i].getName());
			assertEquals(EXPORTED_MACROS[i].getValue(), importedMacros[i].getValue());
		}

		ICLanguageSettingEntry[] importedIncludes = languageSetting.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		
		assertEquals(EXPORTED_INCLUDES.length, importedIncludes.length);
		for(int i = 0; i < importedIncludes.length; i++) {
			assertTrue(importedIncludes[i].getName().endsWith(EXPORTED_INCLUDES[i].getName()));
		}
		
		CProjectHelper.delete(importProject);
		CProjectHelper.delete(exportProject);
	}
	
	
	public static void vaidateCorrectErrorHandling(String xmlContent) throws Exception {
		String filePath = getFilePath("test.txt");
		createFile(xmlContent, filePath);
		
		ICProject project = CProjectHelper.createCCProject("VaidateProject", "unused");
		
		ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project.getProject(), false);
		ICConfigurationDescription config = desc.getActiveConfiguration();
		
		final boolean[] errorDialogShown = new boolean[] {false};
		ProjectSettingsWizardPageMock page = new ProjectSettingsWizardPageMock() {
			@Override public void setMessage(String message, int flag) {
				fail();
			}
			@Override public void showErrorDialog(String dialogTitle, String message) {
				errorDialogShown[0] = true;
			}
		};
		page.setDestinationFilePath(filePath);
		page.setSettingsProcessors(processors);
		page.setSelectedSettingsProcessors(processors);
		page.setSelectedConfiguration(config);
		
		
		ProjectSettingsImportStrategy importer = new ProjectSettingsImportStrategy();
		importer.finish(page);
		
		assertTrue(xmlContent, errorDialogShown[0]);
		
		assertNoSettingsImported(project.getProject());
		
		// TODO test that no macros or includes were imported
		CProjectHelper.delete(project);
		deleteFile(filePath);
	}
	
	private static void assertNoSettingsImported(IProject project) {
		ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project, true);
		ICConfigurationDescription config = desc.getActiveConfiguration();
		ICFolderDescription folder = config.getRootFolderDescription();
		ICLanguageSetting[] languageSettings = folder.getLanguageSettings();
		for(ICLanguageSetting languageSetting : languageSettings) {
			ICLanguageSettingEntry[] entries = languageSetting.getSettingEntries(ICSettingEntry.MACRO);
			for(ICLanguageSettingEntry entry : entries) {
				assertTrue(entry.isBuiltIn());
			}
			entries = languageSetting.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
			for(ICLanguageSettingEntry entry : entries) {
				assertTrue(entry.isBuiltIn());
			}
		}
	}
	
	// {badXML1}
	// blah blah blah
	//
	// {badXML2}
	// <cdtprojectproperties></cdtprojectproperties>
	//
	// {badXML3}
	// <cdtprojectproperties>
	// <section name="invalidsectionname">
	// </section>
	// </cdtprojectproperties>
	//
	// {badXML4}
	// <cdtprojectproperties>
	// <section name="org.eclipse.cdt.internal.ui.wizards.settingswizards.Macros">
	// <invalidtag>
	// </invalidtag>
	// </section>
	// </cdtprojectproperties>
	// 
	// {badXML5}
	// <cdtprojectproperties>
	// <section name="org.eclipse.cdt.internal.ui.wizards.settingswizards.Macros">
	// <macro>
	// <name>aaaa</name><value></value>
	// </macro>
	// </section>
	// </cdtprojectproperties>
	// 
	// {badXML6}
	// <cdtprojectproperties>
	// <section name="org.eclipse.cdt.internal.ui.wizards.settingswizards.Macros">
	// <language name="GNU C++">
	// <macro>
	// <name>aaaa</name>
	// </macro>
	// </language>
	// </section>
	// </cdtprojectproperties>
	//
	// {badXML7}
	// <cdtprojectproperties>
	// <section name="org.eclipse.cdt.internal.ui.wizards.settingswizards.Macros">
	// <language name="GNU C++">
	// <macro>
	// <name>aaaa</name><value></value><value></value>
	// </macro>
	// </language>
	// </section>
	// </cdtprojectproperties>
	//
	// {badXML8}
	// <cdtprojectproperties>
	// <section name="org.eclipse.cdt.internal.ui.wizards.settingswizards.IncludePaths">
	// <language name="GNU C++">
	// <includepath>C:\WINDOWS</includepath><invalid></invalid>
	// </language>
	// </section>
	// </cdtprojectproperties>
	public void testNotValid() throws Exception {
		vaidateCorrectErrorHandling(readTaggedComment("badXML1"));
		vaidateCorrectErrorHandling(readTaggedComment("badXML2"));
		vaidateCorrectErrorHandling(readTaggedComment("badXML3"));
		vaidateCorrectErrorHandling(readTaggedComment("badXML4"));
		vaidateCorrectErrorHandling(readTaggedComment("badXML5")); // missing <language> tag
		vaidateCorrectErrorHandling(readTaggedComment("badXML6")); // missing <value> tag
		vaidateCorrectErrorHandling(readTaggedComment("badXML7")); // extra <value> tag
		vaidateCorrectErrorHandling(readTaggedComment("badXML8"));
	}
}
