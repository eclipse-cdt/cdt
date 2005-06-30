/*******************************************************************************
 * Copyright (c) 2004, 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.testplugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
//import org.eclipse.compare.structuremergeviewer.Differencer;
//import org.eclipse.compare.ResourceNode;;

public class ManagedBuildTestHelper {
	
	/* (non-Javadoc)
	 * Create a new project named <code>name</code> or return the project in 
	 * the workspace of the same name if it exists.
	 * 
	 * @param name The name of the project to create or retrieve.
	 * @return 
	 * @throws CoreException
	 */
	static public IProject createProject(
			final String name, 
			final IPath location, 
			final String projectId, 
			final String projectTypeId) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;
		
		if (!newProjectHandle.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (projectId.equals(ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID)) {
				createNewManagedProject(newProjectHandle, name, location, projectId, projectTypeId);
				project = newProjectHandle;
			} else {
				IWorkspaceDescription workspaceDesc = workspace.getDescription();
				workspaceDesc.setAutoBuilding(false);
				workspace.setDescription(workspaceDesc);
				IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
				//description.setLocation(root.getLocation());
				project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
			}
		} else {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			// TODO: Why is this necessary?
			//workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			project = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
				
		return project;	
	}
	
	/**
	 * Remove the <code>IProject</code> with the name specified in the argument from the 
	 * receiver's workspace.
	 *  
	 * @param name
	 */
	static public void removeProject(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(name);
		if (project.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					System.gc();
					System.runFinalization();
					project.delete(true, true, null);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			try {
				workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			} catch (CoreException e2) {
				Assert.assertTrue(false);
			}
		}
	}
	
	static public IProject createProject(String projectName, File zip, IPath location, String projectTypeId) throws CoreException, InvocationTargetException, IOException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject project= root.getProject(projectName);
		if (project.exists()) 
			removeProject(projectName);
		
		IPath destPath = (location != null) ?
				location :
				project.getFullPath();
		if (zip != null) {
			importFilesFromZip(new ZipFile(zip), destPath, null);
		}
		
		return createProject(projectName, location, ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID, projectTypeId);
	}
	
	static public void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {		
		ZipFileStructureProvider structureProvider=	new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new IOverwriteQuery() {
						public String queryOverwrite(String file) {
							return ALL;
						}
			});
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
			Assert.assertTrue(false);
		}
	}

	static public IProject createNewManagedProject(IProject newProjectHandle, 
			final String name, 
			final IPath location, 
			final String projectId, 
			final String projectTypeId) throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject project = newProjectHandle;
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// Create the base project
				IWorkspaceDescription workspaceDesc = workspace.getDescription();
				workspaceDesc.setAutoBuilding(false);
				workspace.setDescription(workspaceDesc);
				IProjectDescription description = workspace.newProjectDescription(project.getName());
				if (location != null) {
					description.setLocation(location);
				}
				CCorePlugin.getDefault().createCProject(description, project, new NullProgressMonitor(), projectId);
				// Add the managed build nature and builder
				addManagedBuildNature(project);
				
				// Find the base project type definition
				IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
				IProjectType projType = ManagedBuildManager.getProjectType(projectTypeId);
				Assert.assertNotNull(projType);
				
				// Create the managed-project (.cdtbuild) for our project that builds an executable.
				IManagedProject newProject = null;
				try {
					newProject = ManagedBuildManager.createManagedProject(project, projType);
				} catch (Exception e) {
					Assert.fail("Failed to create managed project for: " + project.getName());
				}
				Assert.assertEquals(newProject.getName(), projType.getName());
				Assert.assertFalse(newProject.equals(projType));
				ManagedBuildManager.setNewProjectVersion(project);
				// Copy over the configs
				IConfiguration defaultConfig = null;
				IConfiguration[] configs = projType.getConfigurations();
				for (int i = 0; i < configs.length; ++i) {
					// Make the first configuration the default 
					if (i == 0) {
						defaultConfig = newProject.createConfiguration(configs[i], projType.getId() + "." + i);
					} else {
						newProject.createConfiguration(configs[i], projType.getId() + "." + i);
					}
				}
				ManagedBuildManager.setDefaultConfiguration(project, defaultConfig);
			}
		};
		NullProgressMonitor monitor = new NullProgressMonitor();
		try {
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException e2) {
			Assert.assertTrue(false);
		}

		// Initialize the path entry container
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
		if (initResult.getCode() != IStatus.OK) {
			Assert.fail("Initializing build information failed for: " + project.getName() + " because: " + initResult.getMessage());
		}
		return project;
	}

	static public void addManagedBuildNature (IProject project) {
		// Create the buildinformation object for the project
		IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		info.setValid(true);
		
		// Add the managed build nature
		try {
			ManagedCProjectNature.addManagedNature(project, new NullProgressMonitor());
			ManagedCProjectNature.addManagedBuilder(project, new NullProgressMonitor());
		} catch (CoreException e) {
			Assert.fail("Test failed on adding managed build nature or builder: " + e.getLocalizedMessage());
		}

		// Associate the project with the managed builder so the clients can get proper information
		ICDescriptor desc = null;
		try {
			desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
			desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
			desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
		} catch (CoreException e) {
			Assert.fail("Test failed on adding managed builder as scanner info provider: " + e.getLocalizedMessage());
		}
		try {
			desc.saveProjectData();
		} catch (CoreException e) {
			Assert.fail("Test failed on saving the ICDescriptor data: " + e.getLocalizedMessage());		}
	}
	
	static public boolean compareBenchmarks(final IProject project, IPath testDir, IPath[] files) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (Exception e) {
			Assert.fail("File " + files[0].lastSegment() + " - project refresh failed.");
		}
		for (int i=0; i<files.length; i++) {
			IPath testFile = testDir.append(files[i]);
			IPath benchmarkFile = Path.fromOSString("Benchmarks/" + files[i]);
			StringBuffer testBuffer = readContentsStripLineEnds(project, testFile);
			StringBuffer benchmarkBuffer = readContentsStripLineEnds(project, benchmarkFile);
			if (!testBuffer.toString().equals(benchmarkBuffer.toString())) {
				Assert.fail("File " + testFile.lastSegment() + " does not match its benchmark.");
			} 
		}
		return true;
	}

	static public boolean verifyFilesDoNotExist(final IProject project, IPath testDir, IPath[] files) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (Exception e) {
			Assert.fail("File " + files[0].lastSegment() + " - project refresh failed.");
		}
		for (int i=0; i<files.length; i++) {
			IPath testFile = testDir.append(files[i]);
			IPath fullPath = project.getLocation().append(testFile);
			try {
				if (fullPath.toFile().exists()) {
					Assert.fail("File " + testFile.lastSegment() + " unexpectedly found.");
					return false;
				}					
			} catch (Exception e) {
				Assert.fail("File " + fullPath.toString() + " could not be referenced.");
			}
		}
		return true;
	}

	static public StringBuffer readContentsStripLineEnds(IProject project, IPath path) {
		StringBuffer buff = new StringBuffer();
		IPath fullPath = project.getLocation().append(path);
		try {
			FileReader input = null;
			try {
				input = new FileReader(fullPath.toFile());
			} catch (Exception e) {
				Assert.fail("File " + fullPath.toString() + " could not be read.");
			}
			//InputStream input = file.getContents(true);   // A different way to read the file...
			int c;
			do {
				c = input.read();
				if (c == -1) break;
				if (c != '\r' && c != '\n') {
					buff.append((char)c);
				}
			} while (c != -1);
			input.close();
		} catch (Exception e) {
			Assert.fail("File " + fullPath.toString() + " could not be read.");
		}
		return buff;
	}
	
	static public IPath copyFilesToTempDir(IPath srcDir, IPath tmpSubDir, IPath[] files) {
		IPath tmpSrcDir = null;
		String userDirStr = System.getProperty("user.home");
		if (userDirStr != null) {
			IPath userDir = Path.fromOSString(userDirStr);
			tmpSrcDir = userDir.append(tmpSubDir);
			if (userDir.toString().equalsIgnoreCase(tmpSrcDir.toString())) {
				Assert.fail("Temporary sub-directory cannot be the empty string.");				
			} else {
				File tmpSrcDirFile = tmpSrcDir.toFile();
				if (tmpSrcDirFile.exists()) {
					//  Make sure that this is the expected directory before we delete it...
					if (tmpSrcDir.lastSegment().equals(tmpSubDir.lastSegment())) {
						deleteDirectory(tmpSrcDirFile);
					} else {
						Assert.fail("Temporary directory " + tmpSrcDirFile.toString() + " already exists.");
					}
				}
				boolean succeed = tmpSrcDirFile.mkdir();
				if (succeed) {
					for (int i=0; i<files.length; i++) {
						IPath file = files[i];
						IPath srcFile = srcDir.append(file);
						FileReader srcReader = null;
						try {
							srcReader = new FileReader(srcFile.toFile());
						} catch (Exception e) {
							Assert.fail("File " + file.toString() + " could not be read.");
						}
						if (file.segmentCount() > 1) {
							IPath newDir = tmpSrcDir;
							do {
								IPath dir = file.uptoSegment(1);
								newDir = newDir.append(dir);
								file = file.removeFirstSegments(1);
								succeed = newDir.toFile().mkdir();
							} while (file.segmentCount() > 1);
						}
						IPath destFile = tmpSrcDir.append(files[i]);
						FileWriter writer = null;
						try {
							writer = new FileWriter(destFile.toFile());
						} catch (Exception e) {
							Assert.fail("File " + files[i].toString() + " could not be written.");
						}
						try {
							int c;
							do {
								c = srcReader.read();
								if (c == -1) break;
								writer.write(c);
							} while (c != -1);
							srcReader.close();
							writer.close();
						} catch (Exception e) {
							Assert.fail("File " + file.toString() + " could not be copied.");
						}
					}
				}
			}
		}
		return tmpSrcDir;
	}
	
	static public void deleteTempDir(IPath tmpSubDir, IPath[] files) {
		IPath tmpSrcDir = null;
		String userDirStr = System.getProperty("user.home");
		if (userDirStr != null) {
			IPath userDir = Path.fromOSString(userDirStr);
			tmpSrcDir = userDir.append(tmpSubDir);
			if (userDir.toString().equalsIgnoreCase(tmpSrcDir.toString())) {
				Assert.fail("Temporary sub-directory cannot be the empty string.");				
			} else {
				File tmpSrcDirFile = tmpSrcDir.toFile();
				if (!tmpSrcDirFile.exists()) {
					Assert.fail("Temporary directory " + tmpSrcDirFile.toString() + " does not exist.");				
				} else {
					boolean succeed;
					for (int i=0; i<files.length; i++) {
						// Delete the file
						IPath thisFile = tmpSrcDir.append(files[i]);
						succeed = thisFile.toFile().delete();
					}
					// Delete the dir
					succeed = tmpSrcDirFile.delete();
				}
			}
		}
	}

	static private void deleteDirectory(File dir) {
		boolean b;
		File[] toDelete = dir.listFiles();
		for (int i=0; i<toDelete.length; i++) {
			File fileToDelete = toDelete[i];
			if (fileToDelete.isDirectory()) {
				deleteDirectory(fileToDelete);
			}
			b = fileToDelete.delete();
		}
		b = dir.delete();
	}
}
