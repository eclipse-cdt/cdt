/*******************************************************************************
 * Copyright (c) 2018 STMicroelectronics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * STMicroelectronics
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.tests.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.cdt.managedbuilder.ui.properties.ToolListContentProvider;
import org.eclipse.cdt.managedbuilder.ui.properties.ToolListElement;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ToolListContentProviderTests extends TestCase {

	private static final String testName = "ToolListContentProvider"; //$NON-NLS-1$
	private static boolean fHandleValueCalled;

	public static Test suite() {
		return new TestSuite(ToolListContentProviderTests.class);
	}

	private void resetValueHandler() {
		fHandleValueCalled = false;
	}

	public void testToolListContentProvider() {
		resetValueHandler();

		IProject project = ManagedBuildTestHelper.createProject(testName,
				"cdt.managedbuild.target.ToolListContentProvider.exe"); //$NON-NLS-1$

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getManagedProject().getConfigurations()[0];
		assertFalse(fHandleValueCalled);

		doTestToolListContentProvider(cfg);

		ManagedBuildTestHelper.removeProject(testName);
	}

	private void doTestToolListContentProvider(IConfiguration cfg) {
		ToolListContentProvider provider = new ToolListContentProvider(ToolListContentProvider.PROJECT);
		provider.inputChanged(null, null, cfg.getRootFolderInfo());

		ToolListElement[] elements = (ToolListElement[]) provider.getChildren(cfg);

		// Toolchain level
		List<String> toolchainOptCats = getOptionCategories(elements);
		assertTrue(toolchainOptCats.contains("ToolListContentProvider.toolchain.empty.category1"));
		assertFalse(toolchainOptCats.contains("ToolListContentProvider.toolchain.empty.category2"));

		// Tool level
		for (ToolListElement element : elements) {
			ITool tool = element.getTool();
			if (tool != null && tool.getBaseId().equals("ToolListContentProvider.tool.empty1")) {
				List<String> toolOptCats = getOptionCategories(element.getChildElements());
				assertTrue(toolOptCats.contains("ToolListContentProvider.tool.empty.category1"));
				assertFalse(toolOptCats.contains("ToolListContentProvider.tool.empty.category2"));
			}
		}
	}

	private List<String> getOptionCategories(ToolListElement[] elements) {
		List<String> res = new ArrayList<>();
		for (ToolListElement element : elements) {
			IOptionCategory cat = element.getOptionCategory();
			if (cat != null) { // Only list nodes with option category
				String id = cat.getBaseId();
				assertNotNull(id);
				res.add(id);
			}
		}
		return res;
	}
}
