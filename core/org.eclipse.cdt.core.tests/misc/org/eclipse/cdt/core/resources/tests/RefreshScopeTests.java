/*******************************************************************************
 *  Copyright (c) 2011, 2012 IBM Corporation and others.
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
import java.util.HashMap;
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
	private String config1,config2;

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
		
		config1 = "Debug";
		config2 = "Release";
		
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
		
		// TEST 1: 
		// add resource "folder1" under config1.
		manager.addResourceToRefresh(fProject, config1, fFolder1);
		// now, check that it was added.
		List<IResource> config1_resources = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resources.size(), 2);
		assertEquals(config1_resources.contains(fProject), true);
		assertEquals(config1_resources.contains(fFolder1), true);
		
		// TEST 2:
		// add resource "folder2" under config1
		manager.addResourceToRefresh(fProject, config1, fFolder2);
		// now check to see that it and "folder1" are still there.
		config1_resources = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resources.size(), 3); // 3 because by default the project is always there.
		assertEquals(config1_resources.contains(fProject), true);
		assertEquals(config1_resources.contains(fFolder1), true);
		assertEquals(config1_resources.contains(fFolder2), true);
		
		// make sure it wasn't added under "Release", which should be empty now, excpet for the default project resource.
		List<IResource> config2_resources = manager.getResourcesToRefresh(fProject, config2);
		assertEquals(config2_resources.size(),1);
		assertEquals(config2_resources.contains(fProject), true);
		
		// and add one under config 2.
		manager.addResourceToRefresh(fProject, config2, fFolder1);
		config2_resources = manager.getResourcesToRefresh(fProject, config2);
		assertEquals(config2_resources.size(),2);
		assertEquals(config2_resources.contains(fProject), true);
		assertEquals(config2_resources.contains(fFolder1), true);
		
		// TEST 3:
		// first try deleting a resource that was never added... folder5
		manager.deleteResourceToRefresh(fProject, config1, fFolder5);
		List<IResource> config1_resourcesAfterDelete = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resourcesAfterDelete.size(), 3);
		assertEquals(config1_resources.contains(fProject), true);
		assertEquals(config1_resources.contains(fFolder1), true);
		assertEquals( config1_resources.contains(fFolder2), true);
		
		// ditto for config2, but this time we did add the resource, to make sure fFolder1 wasn't added.
		manager.deleteResourceToRefresh(fProject, config2, fFolder5);
		List<IResource> config2_resourcesAfterDelete = manager.getResourcesToRefresh(fProject, config2);
		assertEquals(config2_resourcesAfterDelete.size(), 2);
		assertEquals(config2_resources.contains(fProject), true);
		assertEquals(config2_resources.contains(fFolder1), true);
		
	
		// TEST 4:
		// now delete the resources from the manager one by one
		manager.deleteResourceToRefresh(fProject, config1, config1_resources.get(config1_resources.indexOf(fFolder2)));
		config1_resourcesAfterDelete = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resourcesAfterDelete.size(), 2);
		assertEquals(config1_resourcesAfterDelete.contains(fProject), true);
		assertEquals(config1_resourcesAfterDelete.contains(fFolder1), true);
		
		manager.deleteResourceToRefresh(fProject, config1,  config1_resources.get(config1_resources.indexOf(fFolder1)));
		config1_resourcesAfterDelete = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resourcesAfterDelete.size(), 1);
		assertEquals(config1_resourcesAfterDelete.contains(fProject), true);
		
		// and ditto for config2
		manager.deleteResourceToRefresh(fProject, config2, config2_resources.get(config2_resources.indexOf(fFolder1)));
		config2_resourcesAfterDelete = manager.getResourcesToRefresh(fProject, config2);
		assertEquals(config2_resourcesAfterDelete.size(), 1);	
		assertEquals(config2_resourcesAfterDelete.contains(fProject), true);
	}
	

	public void testSetResourcesToExclusionsMapRefresh() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		HashMap<IResource, List<RefreshExclusion>> config1_resourceMap = new HashMap<IResource, List<RefreshExclusion>>();
		config1_resourceMap.put(fFolder1,new LinkedList<RefreshExclusion>());
		config1_resourceMap.put(fFolder2,new LinkedList<RefreshExclusion>());
		manager.setResourcesToExclusionsMap(fProject, config1, config1_resourceMap);
		
		List<IResource> config1_resourcesAfterSet = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(config1_resourcesAfterSet.size(), 2);
		assertEquals(config1_resourcesAfterSet.contains(fFolder1), true);
		assertEquals(config1_resourcesAfterSet.contains(fFolder2), true);
		
		manager.clearResourcesToRefresh(fProject);
		
	}
	
	public void testAddRemoveExclusion() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		IResource config1_resource = fProject;
		
		
		manager.addResourceToRefresh(fProject, config1, config1_resource);
		RefreshExclusion config1_exclusion1 = new TestExclusion();
		manager.addExclusion(fProject, config1, config1_resource, config1_exclusion1);
		RefreshExclusion config1_exclusion2 = new TestExclusion();
		manager.addExclusion(fProject, config1, config1_resource, config1_exclusion2);
		
		// make sure the exclusions are there
		List<RefreshExclusion> exclusionsList = manager.getExclusions(fProject, config1, config1_resource);
		RefreshExclusion[] exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 2);
		assertEquals(exclusionsArray[0], config1_exclusion1);
		assertEquals(exclusionsArray[1], config1_exclusion2);
		
		// remove the exclusions one by one
		manager.removeExclusion(fProject, config1, config1_resource, config1_exclusion2);
		exclusionsList = manager.getExclusions(fProject,config1,config1_resource);
		exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 1);
		assertEquals(exclusionsArray[0], config1_exclusion1);
		
		manager.removeExclusion(fProject, config1, config1_resource, config1_exclusion1);
		exclusionsList = manager.getExclusions(fProject, config1,config1_resource);
		exclusionsArray = exclusionsList.toArray(new RefreshExclusion[0]);
		assertEquals(exclusionsArray.length, 0);
		
	}
	
	public void testPersistAndLoad() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		IResource config1_resource = fProject;
		IResource config2_resource = fFolder1;
		
		//add a resource and two exclusions for config1.
		manager.addResourceToRefresh(fProject, config1, config1_resource); 
		RefreshExclusion config1_exclusion1 = new TestExclusion();
		manager.addExclusion(fProject, config1, config1_resource, config1_exclusion1);
		RefreshExclusion config1_exclusion2 = new TestExclusion();
		manager.addExclusion(fProject, config1, config1_resource, config1_exclusion2);
		
		// add a nested exclusion to the first exclusion
		RefreshExclusion config1_exclusion3 = new TestExclusion();
		config1_exclusion1.addNestedExclusion(config1_exclusion3);
		
		// add an instance to the second exclusion
		ExclusionInstance config1_instance = new ExclusionInstance();
		config1_instance.setDisplayString("foo");
		config1_instance.setResource(fFolder2);
		config1_instance.setExclusionType(ExclusionType.RESOURCE);
		config1_instance.setParentExclusion(config1_exclusion2);
		config1_exclusion2.addExclusionInstance(config1_instance);
		
		//add a resource and two exclusions for config2.
		manager.addResourceToRefresh(fProject, config2, config2_resource); 
		RefreshExclusion config2_exclusion1 = new TestExclusion();
		manager.addExclusion(fProject, config2, config2_resource, config2_exclusion1);
		RefreshExclusion config2_exclusion2 = new TestExclusion();
		manager.addExclusion(fProject, config2, config2_resource, config2_exclusion2);
		
		// add a nested exclusion to the first exclusion
		RefreshExclusion config2_exclusion3 = new TestExclusion();
		config2_exclusion1.addNestedExclusion(config2_exclusion3);
		
		// add an instance to the second exclusion
		ExclusionInstance config2_instance = new ExclusionInstance();
		config2_instance.setDisplayString("foo");
		config2_instance.setResource(fFolder2);
		config2_instance.setExclusionType(ExclusionType.RESOURCE);
		config2_instance.setParentExclusion(config2_exclusion2);
		config2_exclusion2.addExclusionInstance(config1_instance);
		
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
		
		// make sure we got the same stuff we saved for config1
		
		// the project should be set to refresh its root
		List<IResource> config1_resources = manager.getResourcesToRefresh(fProject,config1);
		assertEquals(config1_resources.size(), 1);
		assertEquals(config1_resources.toArray(new IResource[0])[0], config1_resource);
		
		// there should be 2 top-level exclusions
		List<RefreshExclusion> config1_exclusions = manager.getExclusions(fProject, config1,config1_resource);
		assertEquals(2, config1_exclusions.size());
		RefreshExclusion[] config1_exclusionsArray = config1_exclusions.toArray(new RefreshExclusion[0]);
		
		// both exclusions should have parent resource set to the project
		assertEquals(config1_resource, config1_exclusionsArray[0].getParentResource());
		assertEquals(config1_resource, config1_exclusionsArray[1].getParentResource());
		
		// the first exclusion should have one nested exclusion
		List<RefreshExclusion> config1_nestedExclusions1 = config1_exclusionsArray[0].getNestedExclusions();
		assertEquals(config1_nestedExclusions1.size(), 1);
		RefreshExclusion[] config1_nestedExclusionsArray =  config1_nestedExclusions1.toArray(new RefreshExclusion[0]);
		// the nested exclusion should have its parent exclusion set properly
		assertEquals(config1_nestedExclusionsArray[0].getParentExclusion(), config1_exclusionsArray[0]);
		
		// the second exclusion should have no nested exclusions
		List<RefreshExclusion> config1_nestedExclusions2 = config1_exclusionsArray[1].getNestedExclusions();
		assertEquals(config1_nestedExclusions2.size(), 0);
		
		// the second exclusion should have an instance
		List<ExclusionInstance> config1_instances = config1_exclusionsArray[1].getExclusionInstances();
		assertEquals(1, config1_instances.size());
		ExclusionInstance[] config1_instancesArray = config1_instances.toArray(new ExclusionInstance[0]);
		ExclusionInstance config1_loadedInstance = config1_instancesArray[0];
		
		// check the contents of the instance
		assertEquals("foo", config1_loadedInstance.getDisplayString());
		assertEquals(fFolder2, config1_loadedInstance.getResource());
		assertEquals(ExclusionType.RESOURCE, config1_loadedInstance.getExclusionType());
		
		// clear data for config1
		manager.deleteResourceToRefresh(fProject, config1, config1_resource);
				
		// make sure we got the same stuff we saved for config2
		// the project should be set to refresh its root
		List<IResource> config2_resources = manager.getResourcesToRefresh(fProject,config2);
		assertEquals(config2_resources.size(), 2);
		assertEquals(config2_resources.contains(config2_resource), true);
		
		// there should be 2 top-level exclusions
		List<RefreshExclusion> config2_exclusions = manager.getExclusions(fProject,config2,config2_resource);
		assertEquals(2, config2_exclusions.size());
		RefreshExclusion[] config2_exclusionsArray = config2_exclusions.toArray(new RefreshExclusion[0]);
		
		// both exclusions should have parent resource set to the project
		assertEquals(config2_resource, config2_exclusionsArray[0].getParentResource());
		assertEquals(config2_resource, config2_exclusionsArray[1].getParentResource());
		
		// the first exclusion should have one nested exclusion
		List<RefreshExclusion> config2_nestedExclusions1 = config2_exclusionsArray[0].getNestedExclusions();
		assertEquals(config2_nestedExclusions1.size(), 1);
		RefreshExclusion[] config2_nestedExclusionsArray =  config2_nestedExclusions1.toArray(new RefreshExclusion[0]);
		// the nested exclusion should have its parent exclusion set properly
		assertEquals(config2_nestedExclusionsArray[0].getParentExclusion(), config2_exclusionsArray[0]);
		
		// the second exclusion should have no nested exclusions
		List<RefreshExclusion> config2_nestedExclusions2 = config2_exclusionsArray[1].getNestedExclusions();
		assertEquals(config2_nestedExclusions2.size(), 0);
		
		// the second exclusion should have an instance
		List<ExclusionInstance> config2_instances = config2_exclusionsArray[1].getExclusionInstances();
		assertEquals(1, config2_instances.size());
		ExclusionInstance[] config2_instancesArray = config2_instances.toArray(new ExclusionInstance[0]);
		ExclusionInstance config2_loadedInstance = config2_instancesArray[0];
		
		// check the contents of the instance
		assertEquals("foo", config2_loadedInstance.getDisplayString());
		assertEquals(fFolder2, config2_loadedInstance.getResource());
		assertEquals(ExclusionType.RESOURCE, config2_loadedInstance.getExclusionType());
		
		// cleanup
		manager.clearAllData();
	}
	

	public void testResourceExclusion() {
		RefreshScopeManager manager = RefreshScopeManager.getInstance();
		manager.clearAllData();
		
		IResource config1_resource = fProject;
		
		manager.addResourceToRefresh(fProject, config1, config1_resource);
		
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
		manager.addExclusion(fProject, config1, config1_resource, exclusion1);
		
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
		assertEquals(true, manager.shouldResourceBeRefreshed(config1, config1_resource));
		assertEquals(false, manager.shouldResourceBeRefreshed(config1, fFolder1));
		assertEquals(false, manager.shouldResourceBeRefreshed(config1, fFolder2));
		assertEquals(true, manager.shouldResourceBeRefreshed(config1, fFolder3));
		assertEquals(false, manager.shouldResourceBeRefreshed(config1, fFolder4));
		assertEquals(true, manager.shouldResourceBeRefreshed(config1, fFolder5));
		assertEquals(false, manager.shouldResourceBeRefreshed(config1, fFolder6));
		
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
		List<IResource> resourcesToRefresh = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(1, resourcesToRefresh.size());
		assertEquals(resourcesToRefresh.contains(fProject), true);
		
		// there should be no exclusions
		List<RefreshExclusion> exclusions = manager.getExclusions(fProject, config1, fProject);
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
		resourcesToRefresh = manager.getResourcesToRefresh(fProject, config1);
		assertEquals(1, resourcesToRefresh.size());
		assertEquals(resourcesToRefresh.contains(fProject), true);
		
		// there should be no exclusions
		exclusions = manager.getExclusions(fProject, config1, fProject);
		assertEquals(0, exclusions.size());
		
	}

	public static Test suite() {
		return new TestSuite(RefreshScopeTests.class);
	}
	

}
