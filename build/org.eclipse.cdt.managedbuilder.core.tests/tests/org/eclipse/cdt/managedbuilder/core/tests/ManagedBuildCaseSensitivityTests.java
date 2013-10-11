/*******************************************************************************
 * Copyright (c) 2013 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class ManagedBuildCaseSensitivityTests extends AbstractBuilderTest {
	private static final String PROJ_PATH = "caseSensitiveProjects";
	private static final String MESSAGE_TAIL = " (see .log file for more details).";

	private IProject fInternalBuilderProject, fExternalBuilderProject, fInternalBuilderProject2, fExternalBuilderProject2;

	private IToolChain[] allToolChains;
	
	public ManagedBuildCaseSensitivityTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildCaseSensitivityTests.class.getName());
		
		suite.addTest(new ManagedBuildCaseSensitivityTests("testExternalBuilder"));
		suite.addTest(new ManagedBuildCaseSensitivityTests("testInternalBuilder"));
		suite.addTest(new ManagedBuildCaseSensitivityTests("testExternalBuilder2"));
		suite.addTest(new ManagedBuildCaseSensitivityTests("testInternalBuilder2"));
		return suite;
	}

	private void buildProject(IProject curProject) {	
		try {
			curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,new NullProgressMonitor());
		}
		catch(CoreException e){
			fail(e.getStatus().getMessage());
		}
		catch(OperationCanceledException e){
			fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: " + e.getMessage());
		}
	}


	private IPath getArtifactLocation(IProject project) {
		IConfiguration cfg = null;
		try {
			cfg = getProjectConfiguration(project);
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Error. " + MESSAGE_TAIL); 
		}
		String name = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if((null != ext) && (ext.length() > 0)) {
			name += '.';
			name += ext;
		}
		name = name.replace("${ProjName}", project.getName());
		return project.getLocation().append(cfg.getName()).append(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		allToolChains = ManagedBuildManager.
		getRealToolChains();		
		IWorkspaceDescription wsDescription =	ResourcesPlugin.getWorkspace().getDescription();
		wsDescription.setAutoBuilding(false);
		ResourcesPlugin.getWorkspace().setDescription(wsDescription);
		assertNotNull("Cannot create InternalBuilderProject project", 
				fInternalBuilderProject = ManagedBuildTestHelper.loadProject("InternalBuilderProject", PROJ_PATH));
		assertNotNull("Cannot create ExternalBuilderProject project", 
				fExternalBuilderProject = ManagedBuildTestHelper.loadProject("ExternalBuilderProject", PROJ_PATH));
		assertNotNull("Cannot create InternalBuilderProject2 project", 
				fInternalBuilderProject2 = ManagedBuildTestHelper.loadProject("InternalBuilderProject2", PROJ_PATH));
		assertNotNull("Cannot create ExternalBuilderProject2 project", 
				fExternalBuilderProject2 = ManagedBuildTestHelper.loadProject("ExternalBuilderProject2", PROJ_PATH));
		IToolChain toolChain = setToolChain(fInternalBuilderProject, null);
		assertNotNull("No compatible tool chain.", toolChain);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ManagedBuildTestHelper.removeProject(fInternalBuilderProject.getName());
		ManagedBuildTestHelper.removeProject(fExternalBuilderProject.getName());
		ManagedBuildTestHelper.removeProject(fInternalBuilderProject2.getName());
		ManagedBuildTestHelper.removeProject(fExternalBuilderProject2.getName());
	}


	private IToolChain setToolChain(IProject project, IToolChain setTo) {
		try {
			IConfiguration cfg = getProjectConfiguration(project);
			IToolChain currentToolChain = cfg.getToolChain();

			IToolChainModificationManager mngr = 
				ManagedBuildManager.getToolChainModificationManager();
			IConfigurationModification cfgM = 
				(IConfigurationModification)mngr.
					createModification(cfg.getRootFolderInfo());
			FolderInfo folderInfo = (FolderInfo)cfg.getRootFolderInfo();
			
			if(setTo == null) {
				for(int i = 0; i < allToolChains.length; ++i) {
					// If predefined tool chain supported, leave it alone
					if(allToolChains[i].equals(currentToolChain))
						return currentToolChain;
					// In the same loop try to find compatible tool chain
					if(setTo == null) {
						IBuilder builder = allToolChains[i].getBuilder(); 
						if(cfg.isBuilderCompatible(builder) && 
								folderInfo.isToolChainCompatible(allToolChains[i]))
						setTo = allToolChains[i];
					}					
				}
			}
			if(null != setTo) {
				cfgM.setToolChain(setTo);
				assertEquals(setTo, cfgM.getToolChain());
				assertEquals(setTo.getBuilder(), cfgM.getBuilder());
			}
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Error. " + MESSAGE_TAIL); 
		}
		return setTo;
	}

	private IConfiguration getProjectConfiguration(IProject project) throws CoreException {
		ICProjectDescription cProjDescription = CoreModel.getDefault().
			createProjectDescription(project, true);
		return   ManagedBuildManager.
			getConfigurationForDescription(
				cProjDescription.getConfigurations()[0]);
		
	}
	
	private void cleanBuild(IProject project) {
		try {
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Cannot clean project " + project.getName() + '.' + MESSAGE_TAIL);
		}
		buildProject(project);
		assertTrue("Artifact: " + getArtifactLocation(project).toOSString() + " does not exist", getArtifactLocation(project).toFile().exists());
	}
	
	// Test with a mix of upper case and lower case extensions
	public void testExternalBuilder() {
		cleanBuild(fExternalBuilderProject);
	}
	// Test with upper case extensions
	public void testExternalBuilder2() {
		cleanBuild(fExternalBuilderProject2);
	}
	// Test with a mix of upper case and lower case extensions
	public void testInternalBuilder() {
		cleanBuild(fInternalBuilderProject);
	}
	// Test with upper case extensions
	public void testInternalBuilder2() {
		cleanBuild(fInternalBuilderProject2);
	}
}