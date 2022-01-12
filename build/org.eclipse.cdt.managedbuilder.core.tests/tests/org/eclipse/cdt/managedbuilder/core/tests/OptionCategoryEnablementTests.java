/*******************************************************************************
 * Copyright (c) 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OptionCategoryEnablementTests extends TestCase {

	private static final String testName = "optcaten"; //$NON-NLS-1$
	private static boolean fHandleValueCalled;

	public static Test suite() {
		return new TestSuite(OptionCategoryEnablementTests.class);
	}

	private void resetValueHandler() {
		fHandleValueCalled = false;
	}

	public void testEnablement() {
		resetValueHandler();

		IProject project = ManagedBuildTestHelper.createProject(testName, "cdt.managedbuild.target.enablement.exe"); //$NON-NLS-1$
		IFolder folder = ManagedBuildTestHelper.createFolder(project, "Folder");
		IFile aFile = ManagedBuildTestHelper.createFile(project, "a.c"); //$NON-NLS-1$

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getManagedProject().getConfigurations()[0];
		assertFalse(fHandleValueCalled);

		doTestEnablement(cfg, folder, aFile);

		ManagedBuildTestHelper.removeProject(testName);
	}

	private void doTestEnablement(IBuildObject cfg, IFolder folder, IFile file) {
		final String TOOL_ID = "enablement.this"; //$NON-NLS-1$
		final String OPTION_ID = "enablement.trigger"; //$NON-NLS-1$
		final String CATEGORY_ID = "enablement.category"; //$NON-NLS-1$
		IOption option;
		IOptionCategory optionCategory;

		try {
			ITool tool = getTool(cfg, TOOL_ID);

			option = tool.getOptionBySuperClassId(OPTION_ID);
			assertEquals(option.getBooleanValue(), false);

			// Config Level
			// trigger option is false, so category should not be visible
			optionCategory = tool.getOptionCategory(CATEGORY_ID);
			assertFalse(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));

			// set the trigger option
			((IConfiguration) cfg).setOption(tool, option, true);

			// trigger option is true, so category should be visible
			assertTrue(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));

			// Folder Level
			IResourceInfo folderInfo = ((IConfiguration) cfg).getResourceInfo(folder.getFullPath(), false);
			assertNotNull(folderInfo);

			// unset the trigger option
			option = getOptionForFolder((IFolderInfo) folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			folderInfo.setOption(tool, option, false);
			// category should not be visible
			optionCategory = getOptionCategoryForFolder((IFolderInfo) folderInfo, /*TOOL_ID,*/ CATEGORY_ID);
			assertNotNull(optionCategory);
			assertFalse(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));

			// set the trigger option
			folderInfo.setOption(tool, option, true);

			// category should be visible
			assertTrue(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));

			// File Level
			// set the trigger option
			IResourceConfiguration fileInfo = ((IConfiguration) cfg)
					.getResourceConfiguration(file.getFullPath().toString());
			if (fileInfo == null)
				fileInfo = ((IConfiguration) cfg).createResourceConfiguration(file);
			option = getOptionForFile((IFileInfo) fileInfo, OPTION_ID);
			assertNotNull(option);
			fileInfo.setOption(tool, option, false);
			optionCategory = getOptionCategoryForFile((IFileInfo) fileInfo, CATEGORY_ID);
			assertNotNull(optionCategory);
			// category should not be visible
			assertFalse(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));

			// set the trigger option
			fileInfo.setOption(tool, option, true);

			// category should be visible
			assertTrue(optionCategory.getApplicabilityCalculator().isOptionCategoryVisible(cfg, tool, optionCategory));
		} catch (BuildException e) {
			fail(e.getLocalizedMessage());
		}
	}

	private ITool getTool(IBuildObject cfgBo, String id) {
		IResourceConfiguration rcCfg = null;
		IConfiguration cfg = null;
		ITool tool = null;
		if (cfgBo instanceof IResourceConfiguration) {
			rcCfg = (IResourceConfiguration) cfgBo;
			cfg = rcCfg.getParent();
			ITool tools[] = rcCfg.getTools();
			for (int i = 0; i < tools.length; i++) {
				for (ITool tmp = tools[i]; tmp != null; tmp = tmp.getSuperClass()) {
					if (tmp.getId().equals(id)) {
						tool = tools[i];
						break;
					}
				}
			}
		} else if (cfgBo instanceof IConfiguration) {
			cfg = (IConfiguration) cfgBo;
			tool = cfg.getToolsBySuperClassId(id)[0];
		} else
			fail("wrong argument");
		return tool;
	}

	private IOption getOptionForFolder(IFolderInfo rcInfo, String toolId, String optionId) {
		ITool[] tools = null;
		tools = rcInfo.getToolsBySuperClassId(toolId);
		assertNotNull(tools);
		ITool tool = tools[0];
		assertNotNull(tool);

		IOption option = tool.getOptionBySuperClassId(optionId);
		return option;
	}

	private IOption getOptionForFile(IFileInfo rcInfo, String optionId) {
		ITool[] tools = null;
		tools = rcInfo.getTools();
		assertNotNull(tools);
		ITool tool = tools[0];
		assertNotNull(tool);

		IOption option = tool.getOptionBySuperClassId(optionId);
		return option;
	}

	private IOptionCategory getOptionCategoryForFolder(IFolderInfo rcInfo, String categoryId) {
		ITool[] tools = rcInfo.getTools();
		tools = rcInfo.getTools();
		assertNotNull(tools);
		ITool tool = tools[0];
		assertNotNull(tool);
		IOptionCategory optionCategory = tool.getOptionCategory(categoryId);
		return optionCategory;
	}

	private IOptionCategory getOptionCategoryForFile(IFileInfo rcInfo, String categoryId) {
		ITool[] tools = rcInfo.getTools();
		tools = rcInfo.getTools();
		assertNotNull(tools);
		ITool tool = tools[0];
		assertNotNull(tool);

		IOptionCategory optionCategory = tool.getOptionCategory(categoryId);
		return optionCategory;
	}
}
