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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.ExclusionType;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.resources.ResourceExclusion;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author crecoskie
 *
 */
public class RefreshScopeTests extends TestCase {
	
	private IProject fProject;
	private IFolder fFolder1;
	private IFolder fFolder2;
	private IFolder fFolder3;
	private IFolder fFolder4;
	private IFolder fFolder5;
	private IFolder fFolder6;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		
		// create project
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				ICProject cProject = CProjectHelper.createNewStileCProject("testRefreshScope", IPDOMManager.ID_NO_INDEXER, false);
				fProject = cProject.getProject();
			}
		}, null);
		
		
		IWorkspaceRoot root = CTestPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("testRefreshScope");
		
		// create some folders
		// structure is:
		/*
		 * testRefreshScope
		 *    folder1
		 *    folder2
		 *    	folder 3
		 *    		folder 4
		 *    		folder 5
		 *    	folder 6
		 * 
		 */
		final IFolder folder1 = project.getFolder("folder1");
		fFolder1 = folder1;
		final IFolder folder2 = project.getFolder("folder2");
		fFolder2 = folder2;	
		final IFolder folder3 = folder2.getFolder("folder3");
		fFolder3 = folder3;
		final IFolder folder4 = folder3.getFolder("folder4");
		fFolder4 = folder4;
		final IFolder folder5 = folder3.getFolder("folder5");
		fFolder5 = folder5;
		final IFolder folder6 = folder2.getFolder("folder6");
		fFolder6 = folder6;
		
		CTestPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				
				folder1.create(true, true, monitor);
				folder2.create(true, true, monitor);
				folder3.create(true, true, monitor);
				folder4.create(true, true, monitor);
				folder5.create(true, true, monitor);
				folder6.create(true, true, monitor);
			}
		}, null);
		
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
		manager.clearAllData();
		
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
		manager.clearAllData();
		
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
	
	public void testAddRemoveExclusion() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
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
	
	public void testPersistAndLoad() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		manager.addResourceToRefresh(fProject, fProject);
		
		RefreshExclusion exclusion1 = new TestExclusion();
		manager.addExclusion(fProject, exclusion1);
		RefreshExclusion exclusion2 = new TestExclusion();
		manager.addExclusion(fProject, exclusion2);
		
		// add a nested exclusion to the first exclusion
		RefreshExclusion exclusion3 = new TestExclusion();
		exclusion1.addNestedExclusion(exclusion3);
		
		// add an instance to the second exclusion
		ExclusionInstance instance = new ExclusionInstance();
		instance.setDisplayString("foo");
		instance.setResource(fFolder2);
		instance.setExclusionType(ExclusionType.RESOURCE);
		instance.setParentExclusion(exclusion2);
		exclusion2.addExclusionInstance(instance);
		
		ICProjectDescription projectDescription = CCorePlugin.getDefault().getProjectDescription(fProject, true);
		
		try {
			manager.persistSettings(projectDescription);
			CCorePlugin.getDefault().setProjectDescription(fProject, projectDescription);
		} catch (CoreException e) {
			fail();
		}
		
		// now clear all the settings out of the manager
		manager.clearAllData();
		
		// now load the settings
		try {
			manager.loadSettings();
		} catch (CoreException e) {
			fail();
		}
		
		// make sure we got the same stuff we saved
		
		// the project should be set to refresh its root
		List<IResource> resources = manager.getResourcesToRefresh(fProject);
		assertEquals(resources.size(), 1);
		assertEquals(resources.toArray(new IResource[0])[0], fProject);
		
		// there should be 2 top-level exclusions
		List<RefreshExclusion> exclusions = manager.getExclusions(fProject);
		assertEquals(2, exclusions.size());
		RefreshExclusion[] exclusionsArray = exclusions.toArray(new RefreshExclusion[0]);
		
		// both exclusions should have parent resource set to the project
		assertEquals(fProject, exclusionsArray[0].getParentResource());
		assertEquals(fProject, exclusionsArray[1].getParentResource());
		
		// the first exclusion should have one nested exclusion
		List<RefreshExclusion> nestedExclusions1 = exclusionsArray[0].getNestedExclusions();
		assertEquals(nestedExclusions1.size(), 1);
		RefreshExclusion[] nestedExclusionsArray =  nestedExclusions1.toArray(new RefreshExclusion[0]);
		// the nested exclusion should have its parent exclusion set properly
		assertEquals(nestedExclusionsArray[0].getParentExclusion(), exclusionsArray[0]);
		
		// the second exclusion should have no nested exclusions
		List<RefreshExclusion> nestedExclusions2 = exclusionsArray[1].getNestedExclusions();
		assertEquals(nestedExclusions2.size(), 0);
		
		// the second exclusion should have an instance
		List<ExclusionInstance> instances = exclusionsArray[1].getExclusionInstances();
		assertEquals(1, instances.size());
		ExclusionInstance[] instancesArray = instances.toArray(new ExclusionInstance[0]);
		ExclusionInstance loadedInstance = instancesArray[0];
		
		// check the contents of the instance
		assertEquals("foo", loadedInstance.getDisplayString());
		assertEquals(fFolder2, loadedInstance.getResource());
		assertEquals(ExclusionType.RESOURCE, loadedInstance.getExclusionType());
		
		// cleanup
		manager.clearAllData();
	}
	
	public void testResourceExclusion() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		manager.addResourceToRefresh(fProject, fProject);
		
		// create a series of nested exclusions that include/exclude certain folders
		// will be included/excluded as follows
		/*
		 * testRefreshScope - include
		 *    folder1 - exclude
		 *    folder2 - exclude, except,
		 *    	folder 3 - include
		 *    		folder 4 - exclude
		 *    		folder 5 - include
		 *    	folder 6 - exclude
		 * 
		 */
		
		ResourceExclusion exclusion1 = new ResourceExclusion();
		ExclusionInstance instance1 = new ExclusionInstance();
		instance1.setResource(fFolder1);
		exclusion1.addExclusionInstance(instance1);
		ExclusionInstance instance2 = new ExclusionInstance();
		instance2.setResource(fFolder2);
		exclusion1.addExclusionInstance(instance2);
		manager.addExclusion(fProject, exclusion1);
		
		ResourceExclusion exclusion2 = new ResourceExclusion();
		ExclusionInstance instance3 = new ExclusionInstance();
		instance3.setResource(fFolder3);
		exclusion2.addExclusionInstance(instance3);
		exclusion1.addNestedExclusion(exclusion2);
		
		ResourceExclusion exclusion3 = new ResourceExclusion();
		ExclusionInstance instance4 = new ExclusionInstance();
		instance4.setResource(fFolder4);
		exclusion3.addExclusionInstance(instance4);
		exclusion2.addNestedExclusion(exclusion3);
		
		
		// now check and see if the right folders are included/excluded
		assertEquals(true, manager.shouldResourceBeRefreshed(fProject));
		assertEquals(false, manager.shouldResourceBeRefreshed(fFolder1));
		assertEquals(false, manager.shouldResourceBeRefreshed(fFolder2));
		assertEquals(true, manager.shouldResourceBeRefreshed(fFolder3));
		assertEquals(false, manager.shouldResourceBeRefreshed(fFolder4));
		assertEquals(true, manager.shouldResourceBeRefreshed(fFolder5));
		assertEquals(false, manager.shouldResourceBeRefreshed(fFolder6));
		
		// now let's create a bunch of files in these directories using java.io.File (so that we don't get
		// resource deltas happening), and refresh the project according to the policy.  We should only see the files
		// in the same folders above when consulting the resource system
		IPath path = fProject.getLocation();
		createTestFile(path);
		
		path = fFolder1.getLocation();
		createTestFile(path);
		
		path = fFolder2.getLocation();
		createTestFile(path);
		
		path = fFolder3.getLocation();
		createTestFile(path);
		
		path = fFolder4.getLocation();
		createTestFile(path);
		
		path = fFolder5.getLocation();
		createTestFile(path);
		
		path = fFolder6.getLocation();
		createTestFile(path);
		
		// now refresh
		IWorkspaceRunnable runnable = manager.getRefreshRunnable(fProject);
		try {
			ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			fail();
		}
		
		// check if the proper resources exist in the workspace
		IResource resource = fProject.getFile("foo.cpp");
		assertEquals(true, resource.exists());
		resource = fFolder1.getFile("foo.cpp");
		assertEquals(false, resource.exists());
		resource = fFolder2.getFile("foo.cpp");
		assertEquals(false, resource.exists());
		resource = fFolder3.getFile("foo.cpp");
		assertEquals(true, resource.exists());
		resource = fFolder4.getFile("foo.cpp");
		assertEquals(false, resource.exists());
		resource = fFolder5.getFile("foo.cpp");
		assertEquals(true, resource.exists());
		resource = fFolder6.getFile("foo.cpp");
		assertEquals(false, resource.exists());
		
		manager.clearAllData();

	}

	private void createTestFile(IPath path) {
		path = path.append("foo.cpp");
		File file = new File(path.toOSString());
		try {
			file.createNewFile();
		} catch (IOException e) {
			fail();
		}
		

	}
	
	public void testDefaults() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		// by default, a project should refresh its root
		List<IResource> resourcesToRefresh = manager.getResourcesToRefresh(fProject);
		assertEquals(1, resourcesToRefresh.size());
		IResource[] resourceArray = resourcesToRefresh.toArray(new IResource[0]);
		assertEquals(fProject, resourceArray[0]);
		
		// there should be no exclusions
		List<RefreshExclusion> exclusions = manager.getExclusions(fProject);
		assertEquals(0, exclusions.size());
		
		ICProjectDescription projectDescription = CCorePlugin.getDefault().getProjectDescription(fProject);
		
		// now try persisting the data and loading it
		try {
			manager.persistSettings(projectDescription);
		} catch (CoreException e) {
			fail();
		}
		
		manager.clearAllData();
		
		try {
			manager.loadSettings();
		} catch (CoreException e) {
			fail();
		}
		
		// test the defaults again
		// by default, a project should refresh its root
		resourcesToRefresh = manager.getResourcesToRefresh(fProject);
		assertEquals(1, resourcesToRefresh.size());
		resourceArray = resourcesToRefresh.toArray(new IResource[0]);
		assertEquals(fProject, resourceArray[0]);
		// there should be no exclusions
		exclusions = manager.getExclusions(fProject);
		assertEquals(0, exclusions.size());
		
	}

	public static Test suite() {
		return new TestSuite(RefreshScopeTests.class);
	}
	

}
