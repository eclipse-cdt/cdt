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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestConfigurationLeak extends TestCase {

	private static final String testName = "TestConfigurationLeak"; //$NON-NLS-1$

	public static Test suite() {
		return new TestSuite(TestConfigurationLeak.class);
	}

	public void testToolListContentProvider() throws Exception {
		int N = 100;
		for (int i = 0; i < N; ++i) {
			long start = System.currentTimeMillis();
			ManagedBuildTestHelper.createProject(testName, "cdt.managedbuild.target.ToolListContentProvider.exe"); //$NON-NLS-1$
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(testName);
			project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration cfg = info.getManagedProject().getConfigurations()[0];
			Preferences prefs = InstanceScope.INSTANCE.getNode(ManagedBuilderCorePlugin.getUniqueIdentifier());
			if (prefs != null) {
				prefs = prefs.node("properties");
				String[] childrenNames = prefs.childrenNames();
				System.out.println("Properties node children count: " + childrenNames.length);
				System.out.println("Properties node children:");
				for (String name : childrenNames) {
					System.out.println(name);
				}
			}
			ManagedBuildTestHelper.removeProject(testName);
			long end = System.currentTimeMillis();
			long elapsed = end - start;
			System.out.println("iteration " + i + " took " + elapsed + "ms");
		}
	}
}
