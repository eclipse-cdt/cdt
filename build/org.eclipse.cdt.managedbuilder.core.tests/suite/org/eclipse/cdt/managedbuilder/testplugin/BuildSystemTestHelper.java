/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import junit.framework.Assert;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class BuildSystemTestHelper {

	static public IProject createProject(String name, IPath location, String projTypeId) throws CoreException {
		IProject project = createProject(name, location);

		return createDescription(project, projTypeId);
	}

	static public IProject createDescription(IProject project, String projTypeId) throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		Assert.assertNull("detDescription1 returned not null!", des);

		des = coreModel.createProjectDescription(project, true);
		Assert.assertNotNull("createDescription returned null!", des);

		Assert.assertNull("detDescription2 returned not null!", coreModel.getProjectDescription(project));

		Assert.assertFalse("new des should be not valid", des.isValid());

		Assert.assertEquals(0, des.getConfigurations().length);

		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		IProjectType type = ManagedBuildManager.getProjectType(projTypeId);
		Assert.assertNotNull("project type not found", type);

		ManagedProject mProj = new ManagedProject(project, type);
		info.setManagedProject(mProj);

		IConfiguration cfgs[] = type.getConfigurations();

		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true, false);
			CConfigurationData data = config.getConfigurationData();
			Assert.assertNotNull("data is null for created configuration", data);
			des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		}
		coreModel.setProjectDescription(project, des);
		return project;
	}

	static public IProject createProject(String name) throws CoreException{
		return createProject(name, (IPath)null);
	}

	static public IProject createProject(
			final String name,
			final IPath location) throws CoreException{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;

		if (!newProjectHandle.exists()) {
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			if(location != null)
				description.setLocation(location);
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle, new NullProgressMonitor());
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			project = newProjectHandle;
		}

		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		return project;
	}

	static public void checkDiff(Object[] arr1, Object[] arr2){
		LinkedHashSet<? extends Object> set1 = new LinkedHashSet<Object>(Arrays.asList(arr1));
		LinkedHashSet<? extends Object> set2 = new LinkedHashSet<Object>(Arrays.asList(arr2));
		LinkedHashSet<? extends Object> set1Copy = new LinkedHashSet<Object>(set1);
		set1.removeAll(set2);
		set2.removeAll(set1Copy);

		String set1String = collectionToString(set1);
		String set2String = collectionToString(set2);
		String diffMsg = "array1 entries: " + set1String + ",\n array2 entries: " + set2String + "\n";
		Assert.assertEquals("arrays have different size\n" + diffMsg, arr1.length, arr2.length);
		Assert.assertEquals("arrays have different contents\n" + diffMsg, 0, set1.size());
		Assert.assertEquals("arrays have different contents\n" + diffMsg, 0, set2.size());

		if(!Arrays.equals(arr1, arr2)){
			Assert.fail("different element order, dumping..\n array1 entries: " + arrayToString(arr1) + "\n array2 entries: " + arrayToString(arr2) + "\n");
		}
	}

	static public String collectionToString(Collection<? extends Object> c){
		return arrayToString(c.toArray());
	}

	static public String arrayToString(Object[] arr){
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for(int i = 0; i < arr.length; i++)	{
			if(i != 0)
				buf.append(", ");

			buf.append(arr[i].toString());
		}
		buf.append(']');
		return buf.toString();
	}
}
