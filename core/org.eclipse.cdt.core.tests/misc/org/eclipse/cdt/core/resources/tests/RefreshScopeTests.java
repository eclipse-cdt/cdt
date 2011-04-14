/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources.tests;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author crecoskie
 *
 */
public class RefreshScopeTests extends TestCase {
	
	private IProject fProject;
	private IResource fFolder1;
	private IResource fFolder2;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		
		// create project
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
				IProject project = root.getProject("testRefreshScope");
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
				}
				fProject = project;
			}
		}, null);
		
		
		IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("testRefreshScope");
		
		// create a couple folders
		final IFolder folder1 = project.getFolder("folder1");
		fFolder1 = folder1;
		final IFolder folder2 = project.getFolder("folder2");
		fFolder2 = folder2;
		
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				
				folder1.create(true, true, monitor);
				folder2.create(true, true, monitor);
			}
		}, null);
		
	}

	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fProject.delete(true, true, null);
	}
	
	public void testAddDeleteResource() throws CoreException {
		
		
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.addResourceToRefresh(fProject, fFolder1);
		
		IResource[] resources = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resources.length, 1);
		assertEquals(fFolder1, resources[0]);
		
		manager.addResourceToRefresh(fProject, fFolder2);
		resources = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resources.length, 2);
		assertEquals(fFolder1, resources[0]);
		assertEquals(fFolder2, resources[1]);
		
		// first try deleting a resource that was never added... the project
		manager.deleteResourceToRefresh(fProject, fProject);
		IResource[] resourcesAfterDelete = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resourcesAfterDelete.length, 2);
		assertEquals(fFolder1, resources[0]);
		assertEquals(fFolder2, resources[1]);
		
		
		// now delete the resources from the manager one by one
		manager.deleteResourceToRefresh(fProject, resources[1]);
		resourcesAfterDelete = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resourcesAfterDelete.length, 1);
		assertEquals(resourcesAfterDelete[0], resources[0]);
		
		manager.deleteResourceToRefresh(fProject, resources[0]);
		resourcesAfterDelete = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resourcesAfterDelete.length, 0);
		
	}
	
	public void testSetResourcesToRefresh() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		List<IResource> resources = new LinkedList<IResource>();
		resources.add(fFolder1);
		resources.add(fFolder2);
		manager.setResourcesToRefresh(fProject, resources);
		
		IResource[] resourcesAfterSet = manager.getResourcesToRefresh(fProject).toArray(new IResource[0]);
		assertEquals(resourcesAfterSet.length, 2);
		assertEquals(fFolder1, resourcesAfterSet[0]);
		assertEquals(fFolder2, resourcesAfterSet[1]);
		
		manager.clearResourcesToRefresh(fProject);
		
	}
	
	public class TestExclusion extends RefreshExclusion {

		@Override
		public String getName() {
			return "TestExclusion";
		}

		@Override
		public boolean testExclusion(IResource resource) {
			// if the resource name ends in a 2, then we pass
			String name = resource.getName();
			return name.endsWith("2");
		}
		
	}
	
	public void testAddRemoveExclusion() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.addResourceToRefresh(fProject, fProject);
		RefreshExclusion exclusion1 = new TestExclusion();
		manager.addExclusion(fProject, exclusion1);
		RefreshExclusion exclusion2 = new TestExclusion();
		manager.addExclusion(fProject, exclusion2);
		
		// make sure the exclusions are there
		List<RefreshExclusion> exclusionsList = manager.getExclusions(fProject);
		RefreshExclusion[] exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 2);
		assertEquals(exclusionsArray[0], exclusion1);
		assertEquals(exclusionsArray[1], exclusion2);
		
		// remove the exclusions one by one
		manager.removeExclusion(fProject, exclusion2);
		exclusionsList = manager.getExclusions(fProject);
		exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 1);
		assertEquals(exclusionsArray[0], exclusion1);
		
		manager.removeExclusion(fProject, exclusion1);
		exclusionsList = manager.getExclusions(fProject);
		exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 0);
		
	}
	

}
