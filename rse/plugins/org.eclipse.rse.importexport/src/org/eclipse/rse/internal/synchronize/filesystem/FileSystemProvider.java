/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemProvider
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem;

import java.io.File;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.importexport.files.UniFilePlus;
import org.eclipse.rse.internal.importexport.files.Utilities;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemResourceVariant;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * This example illustrates how to create a concrete implementation of a
 * <code>RepositoryProvider</code> that uses the file system to act as the
 * repository. See the plugin.xml file for the xml required to register this
 * provider with the Team extension point
 * <code>org.eclipse.team.core.repository</code>. The plugin.xml file also
 * contains examples of how to filter menu items using a repository provider's
 * ID.
 * 
 * <p>
 * This example provider illustrates the following:
 * <ol>
 * <li>simple working implementation of <code>RepositoyProvider</code>
 * <li>storage of a persistent property with the project (which provides the
 * target location for the provider)
 * <li>access to an instance of <code>SimpleAccessOperations</code> for
 * performing simple file operations
 * </ol>
 * 
 * <p>
 * Additional functionality that will be illustrated in the future include:
 * <ol>
 * <li>Validate Save/Validate Edit
 * <li>Move/Delete Hook
 * <li>Project Sets
 * <li>Use of the workspace synchronizer (ISynchronizOperation)
 * <li>Use of decorators
 * <li>combining streams and progress monitors to get responsive UI
 * </ol>
 * 
 */
public class FileSystemProvider extends RepositoryProvider {

	/*
	 * Create a custom rule factory to allow more optimistic concurrency
	 */
	private static final ResourceRuleFactory RESOURCE_RULE_FACTORY = new ResourceRuleFactory() {
		// Just need a subclass to instantiate
	};

	// The location of the folder on file system where the repository is stored.
	private IPath rootPath;
	private UniFilePlus remoteRoot;

	// The QualifiedName that is used to persist the location across workspace
	// as a persistent property on a resource
	private static QualifiedName FILESYSTEM_REPO_LOC = new QualifiedName(RSESyncUtils.PLUGIN_ID, "disk_location"); //$NON-NLS-1$

	/**
	 * Create a new FileSystemProvider.
	 */
	public FileSystemProvider() {
		super();
	}

	/**
	 * This method is invoked when the provider is mapped to a project. Although
	 * we have access to the project at this point (using
	 * <code>getProject()</code>, we don't know the root location so there is
	 * nothing we can do yet.
	 * 
	 * @see org.eclipse.team.core.RepositoryProvider#configureProject()
	 */
	@Override
	public void configureProject() throws CoreException {
		FileSystemSubscriber.getInstance().handleRootChanged(getProject(), true /* added */);
	}

	/**
	 * This method is invoked when the provider is unmapped from its project.
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// Clear the persistant property containing the location
		getProject().setPersistentProperty(FILESYSTEM_REPO_LOC, null);
		FileSystemSubscriber.getInstance().handleRootChanged(getProject(), false /* removed */);
	}

	/**
	 * Return the provider ID as specified in the plugin.xml
	 * 
	 * @see RepositoryProvider#getID()
	 */
	@Override
	public String getID() {
		return RSESyncUtils.PROVIDER_ID;
	}

	/**
	 * Set the file system location for the provider. This mist be invoked after
	 * the provider is mapped and configured but before the provider is used to
	 * perform any operations.
	 * 
	 * @param location
	 * 		the path representing the location where the project contents will
	 * 		be stored.
	 * @throws TeamException
	 */
	public void setTargetLocation(String location) throws TeamException {
		// location = transformRSEtoNormal(location);

		// set the instance variable to the provided path
		rootPath = new Path(location);

		// ensure that the location is a folder (if it exists)
		File file = new File(location);
		if (file.exists() && !file.isDirectory()) {
			throw new TeamException(Policy.bind("FileSystemProvider.mustBeFolder", location)); //$NON-NLS-1$
		}

		// record the location as a persistent property so it will be remembered
		// across platform invocations
		try {
			getProject().setPersistentProperty(FILESYSTEM_REPO_LOC, location);
		} catch (CoreException e) {
			throw FileSystemPlugin.wrapException(e);
		}
	}

	/**
	 * Returns the folder in the file system to which the provider is connected.
	 * Return <code>null</code> if there is no location or there was a problem
	 * determining it.
	 * 
	 * @return IPath The path to the root of the repository.
	 */
	public IPath getRoot() {
		if (rootPath == null) {
			try {
				String location = getProject().getPersistentProperty(FILESYSTEM_REPO_LOC);
				if (location == null) {
					return null;
				}
				rootPath = new Path(location);
			} catch (CoreException e) {
				// log the problem and carry on
				FileSystemPlugin.log(e);
				return null;
			}
		}
		// System.out.println(root);
		return rootPath;
	}

	/**
	 * Return an object that provides the operations for transferring data to
	 * and from the provider's location.
	 */
	public FileSystemOperations getOperations() {
		return new FileSystemOperations(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.RepositoryProvider#getFileModificationValidator()
	 */
	@Override
	public IFileModificationValidator getFileModificationValidator() {
		return getFileModificationValidator2();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.RepositoryProvider#getFileModificationValidator2()
	 */
	@Override
	public FileModificationValidator getFileModificationValidator2() {
		return new org.eclipse.rse.internal.synchronize.filesystem.FileModificationValidator(this);
	}

	/**
	 * Return the resource variant for the local resource using the bytes to
	 * identify the variant.
	 * 
	 * @param resource
	 * 		the resource
	 * @param bytes
	 * 		the bytes that identify the resource variant
	 * @return the resource variant handle
	 */
	public IResourceVariant getResourceVariant(IResource resource, byte[] bytes) {
		// Takuya: it's important if resource exist or not.
		if (bytes == null)
			return null;
		UniFilePlus file = (UniFilePlus) getExportFile(resource);
		if (file == null)
			return null;
		return new FileSystemResourceVariant(file, bytes);
	}

	/**
	 * Return the resource variant for the local resource.
	 * 
	 * @param resource
	 * 		the resource
	 * @return the resource variant
	 */
	public IResourceVariant getExportResourceVariant(IResource resource) {
		// Takuya: it's important if resource exist or not.
		UniFilePlus file = (UniFilePlus) getExportFile(resource);
		if (file == null || !file.exists())
			return null;
		return new FileSystemResourceVariant(file);
	}
	
	public IResourceVariant getImportResourcevariant(IResource resource){
		UniFilePlus file = (UniFilePlus) getImportFile(resource);
		if (file == null || !file.exists())
			return null;
		return new FileSystemResourceVariant(file);
	}

	/**
	 * Return the <code>java.io.File</code> that the given resource maps to.
	 * Return <code>null</code> if the resource is not a child of this
	 * provider's project.
	 * The path of return file is targetRootPath/project/relativePath.
	 * 
	 * @param resource
	 * 		the resource
	 * @return the file that the resource maps to.
	 */
	public File getExportFile(IResource resource) {
		UniFilePlus file = null;
		try {
			if (resource.getProject().equals(getProject())) {
				UniFilePlus root = getRemoteRootFolder();
				String relativePath = transformInDependency(root.getRemoteFile().getHost(), resource.getFullPath().toString());
				// MOB BUGBUG//IRemoteFile remoteFile =
				// root.getRemoteFile().getParentRemoteFileSubSystem
				// ().getRemoteFileObject(root.getRemoteFile(),relativePath,
				// null);
				IRemoteFile remoteFile = root.getRemoteFile().getParentRemoteFileSubSystem().getRemoteFileObject(root.getRemoteFile().getAbsolutePath() + relativePath, null);
				file = new UniFilePlus(remoteFile);

			}
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
	
	public File getImportFile(IResource resource){
		UniFilePlus file = null;
		try {
			if (resource.getProject().equals(getProject())) {
				UniFilePlus root = getRemoteRootFolder();
				String relativePath = transformInDependency(root.getRemoteFile().getHost(), IPath.SEPARATOR + resource.getProjectRelativePath().toString());
				IRemoteFile remoteFile = root.getRemoteFile().getParentRemoteFileSubSystem().getRemoteFileObject(root.getRemoteFile().getAbsolutePath() + relativePath, null);
				file = new UniFilePlus(remoteFile);

			}
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
	

	/**
	 * return whether the resource is file or not
	 * 
	 * @param resource
	 * @return
	 */
	private boolean isFile(IResource resource) {
		return new File(this.transformRSEtoNormal(resource.getLocation().toString())).isFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.RepositoryProvider#getRuleFactory()
	 */
	@Override
	public IResourceRuleFactory getRuleFactory() {
		return RESOURCE_RULE_FACTORY;
	}

	@Override
	public IFileHistoryProvider getFileHistoryProvider() {
		return null;
	}

	private String transformRSEtoNormal(String original) {
		if (original.indexOf(":") == -1) {
			return original;
		}

		String transformed = "";
		String[] str = original.split(":");

		// Windows
		if (str.length > 2) {
			transformed += str[1] + ":" + str[2];
		}
		// Linux
		else {
			transformed += str[1];
		}

		return transformed;

	}

	/**
	 * get the handle fo remote root directory which is specified by IHost and
	 * absolute path as the member value of this class.
	 * 
	 * @return
	 */
	public UniFilePlus getRemoteRootFolder() {
		if (remoteRoot == null) {
			IPath remoteRootDir = getRoot();
			String remoteRootDirString = transformRSEtoNormal(remoteRootDir.toString());
			IHost conn = Utilities.parseForSystemConnection(remoteRootDir.toString());
			String absolutePath = transformInDependency(conn, remoteRootDirString);
			return /* remoteRoot = */new UniFilePlus(Utilities.getIRemoteFile(conn, absolutePath));
		}

		return remoteRoot;
	}

	private String transformInDependency(IHost host, String original) {
		String ret = "";
		IRSESystemType type = host.getSystemType();
		if (host.getSystemType().isWindows()) {
			ret = original.replace("/", "\\");
		} else {
			ret = original.replace("\\", "/");
		}
		return ret;
	}

}
