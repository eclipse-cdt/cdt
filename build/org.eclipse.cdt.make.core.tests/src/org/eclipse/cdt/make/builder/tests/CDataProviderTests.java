/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.builder.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class CDataProviderTests extends TestCase {
	/**
	 * @param name
	 */
	public CDataProviderTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CDataProviderTests.class);

//		// Add the relevant tests to the suite
//		suite.addTest(new StandardBuildTests("testProjectCreation"));
//		suite.addTest(new StandardBuildTests("testProjectSettings"));
//		suite.addTest(new StandardBuildTests("testProjectConversion"));
//		suite.addTest(new StandardBuildTests("testProjectCleanup"));
//
//		suite.addTestSuite(ScannerConfigConsoleParserTests.class);
//		suite.addTestSuite(ScannerConfigDiscoveryTests.class);

		return suite;
	}

	public void testCData() throws Exception {
		IProject project = createProject("a1");
		ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
		assertNotNull("project description should not be null", projDes);

		ICConfigurationDescription cfgs[] = projDes.getConfigurations();
		assertEquals(1, cfgs.length);

		int lssNum = cfgs[0].getRootFolderDescription().getLanguageSettings().length;
		int rcDessNum = cfgs[0].getResourceDescriptions().length;
		assertTrue(rcDessNum > 0);
		ICConfigurationDescription cfg2 = projDes.createConfiguration("aasasa", "cfg2", cfgs[0]);
		assertNotNull(cfg2);
		assertEquals(2, projDes.getConfigurations().length);
		assertEquals(lssNum, cfg2.getRootFolderDescription().getLanguageSettings().length);
		assertEquals(rcDessNum, cfg2.getResourceDescriptions().length);

		CCorePlugin.getDefault().setProjectDescription(project, projDes);

		projDes = CCorePlugin.getDefault().getProjectDescription(project);
		assertEquals(2, projDes.getConfigurations().length);
		cfgs = projDes.getConfigurations();
		assertEquals(2, cfgs.length);
		cfg2 = cfgs[0];
		assertNotNull(cfg2);
		assertEquals(2, projDes.getConfigurations().length);
		assertEquals(lssNum, cfg2.getRootFolderDescription().getLanguageSettings().length);
		assertEquals(rcDessNum, cfg2.getResourceDescriptions().length);

		projDes = CCorePlugin.getDefault().getProjectDescription(project, false);
		assertEquals(2, projDes.getConfigurations().length);
		cfgs = projDes.getConfigurations();
		assertEquals(2, cfgs.length);
		cfg2 = cfgs[0];
		assertNotNull(cfg2);
		assertEquals(2, projDes.getConfigurations().length);
		assertEquals(lssNum, cfg2.getRootFolderDescription().getLanguageSettings().length);
		assertEquals(rcDessNum, cfg2.getResourceDescriptions().length);

		project.delete(false, true, new NullProgressMonitor());

		project = ResourcesPlugin.getWorkspace().getRoot().getProject("a1");
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		projDes = CCorePlugin.getDefault().getProjectDescription(project);
		assertNotNull("project description should not be null", projDes);

		cfgs = projDes.getConfigurations();
		assertEquals(2, cfgs.length);
		cfg2 = cfgs[0];
		assertNotNull(cfg2);
		assertEquals(2, projDes.getConfigurations().length);
		assertEquals(lssNum, cfg2.getRootFolderDescription().getLanguageSettings().length);
		assertEquals(rcDessNum, cfg2.getResourceDescriptions().length);

		cfg2 = cfgs[1];
		assertNotNull(cfg2);
		assertEquals(2, projDes.getConfigurations().length);
		assertEquals(lssNum, cfg2.getRootFolderDescription().getLanguageSettings().length);
		assertEquals(rcDessNum, cfg2.getResourceDescriptions().length);
	}

	private IProject createProject(final String name) throws CoreException {
		final Object[] result = new Object[1];
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = root.getProject(name);
				IProjectDescription description = null;

				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}

				if (!project.isOpen()) {
					project.open(null);
				}

				description = project.getDescription();
//				ICommand[] commands = description.getBuildSpec();
//				for (int i = 0; i < commands.length; ++i) {
//					if (commands[i].getBuilderName().equals(ScannerConfigBuilder.BUILDER_ID)) {
//						return;
//					}
//				}
//				ICommand command = description.newCommand();
//				command.setBuilderName(ScannerConfigBuilder.BUILDER_ID);
//				ICommand[] newCommands = new ICommand[commands.length + 1];
//				System.arraycopy(commands, 0, newCommands, 0, commands.length);
//				newCommands[commands.length] = command;
//				description.setBuildSpec(newCommands);
//				project.setDescription(description, null);

				CCorePlugin.getDefault().createCDTProject(description, project, MakeCorePlugin.CFG_DATA_PROVIDER_ID, new NullProgressMonitor());
				result[0] = project;
			}
		}, null);
		return (IProject)result[0];
	}
}
