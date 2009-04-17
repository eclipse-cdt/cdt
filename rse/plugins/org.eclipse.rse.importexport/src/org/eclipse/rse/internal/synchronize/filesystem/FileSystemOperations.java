/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Andreas Voss <av@tonbeller.com> - Bug 181141 [Examples] Team: filesystem provider example can not handle deletions
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / FileSystemOperations
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rse.internal.importexport.files.UniFilePlus;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemResourceVariant;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;
import org.eclipse.ui.dialogs.ContainerGenerator;

/**
 * The get and put operations for the file system provider.
 */
public class FileSystemOperations {
	// A reference to the provider
	// private FileSystemProvider provider;

	FileSystemOperations(FileSystemProvider provider) {
		// this.provider = provider;
	}

	private FileSystemProvider getProvider(IResource resource) {
		return (FileSystemProvider) RepositoryProvider.getProvider(resource.getProject());
	}

	/**
	 * Make the local state of the project match the remote state by getting any
	 * out-of-sync resources. The overrideOutgoing flag is used to indicate
	 * whether locally modified files should also be replaced or left alone.
	 * 
	 * @param resources
	 * 		the resources to get
	 * @param depth
	 * 		the depth of the operation
	 * @param overrideOutgoing
	 * 		whether locally modified resources should be replaced
	 * @param progress
	 * 		a progress monitor
	 * @throws TeamException
	 */
	public void get(IResource[] resources, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		try {
			// ensure the progress monitor is not null
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("GetAction.working"), 100); //$NON-NLS-1$
			// Refresh the subscriber so we have the latest remote state
			FileSystemSubscriber.getInstance().refresh(resources, depth, new SubProgressMonitor(progress, 30));
			internalGet(resources, depth, overrideOutgoing, new SubProgressMonitor(progress, 70));
		} finally {
			progress.done();
		}
	}

	/**
	 * Make the local state of the traversals match the remote state by getting
	 * any out-of-sync resources. The overrideOutgoing flag is used to indicate
	 * whether locally modified files should also be replaced or left alone.
	 * 
	 * @param traversals
	 * 		the traversals that cover the resources to get
	 * @param overrideOutgoing
	 * 		whether locally modified resources should be replaced
	 * @param progress
	 * 		a progress monitor
	 * @throws TeamException
	 */
	public void get(ResourceTraversal[] traversals, boolean overrideOutgoing, IProgressMonitor monitor) throws TeamException {
		try {
			// ensure the progress monitor is not null
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100 * traversals.length);
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				get(traversal.getResources(), traversal.getDepth(), overrideOutgoing, new SubProgressMonitor(monitor, 100));
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Checkout the given resources to the given depth by setting any files to
	 * writable (i.e set read-only to <code>false</code>.
	 * 
	 * @param resources
	 * 		the resources to be checked out
	 * @param depth
	 * 		the depth of the checkout
	 * @param progress
	 * 		a progress monitor
	 * @throws TeamException
	 */
	public void checkout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		try {
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("FileSystemSimpleAccessOperations.1"), resources.length); //$NON-NLS-1$
			for (int i = 0; i < resources.length; i++) {
				Policy.checkCanceled(progress);
				resources[i].accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (resource.getType() == IResource.FILE) {
							// TODO: lock the file on the' server'.
							resource.getResourceAttributes().setReadOnly(false);
						}
						return true;
					}
				}, depth, false /* include phantoms */);
				progress.worked(1);
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		} finally {
			progress.done();
		}
	}

	/**
	 * Check-in the given resources to the given depth by replacing the remote
	 * (i.e. file system) contents with the local workspace contents.
	 * 
	 * @param resources
	 * 		the resources
	 * @param depth
	 * 		the depth of the operation
	 * @param overrideIncoming
	 * 		indicate whether incoming remote changes should be replaced
	 * @param progress
	 * 		a progress monitor
	 * @throws TeamException
	 */
	public void checkin(IResource[] resources, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		try {
			// ensure the progress monitor is not null
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("PutAction.working"), 100); //$NON-NLS-1$
			// Refresh the subscriber so we have the latest remote state
			FileSystemSubscriber.getInstance().refresh(resources, depth, new SubProgressMonitor(progress, 30));
			internalPut(resources, depth, overrideIncoming, new SubProgressMonitor(progress, 70));
		} finally {
			progress.done();
		}
	}

	/**
	 * Check-in the given resources to the given depth by replacing the remote
	 * (i.e. file system) contents with the local workspace contents.
	 * 
	 * @param traversals
	 * 		the traversals that cover the resources to check in
	 * @param overrideIncoming
	 * 		indicate whether incoming remote changes should be replaced
	 * @param progress
	 * 		a progress monitor
	 * @throws TeamException
	 */
	public void checkin(ResourceTraversal[] traversals, boolean overrideIncoming, IProgressMonitor monitor) throws TeamException {
		try {
			// ensure the progress monitor is not null
			monitor = Policy.monitorFor(monitor);
			monitor.beginTask(null, 100 * traversals.length);
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				checkin(traversal.getResources(), traversal.getDepth(), overrideIncoming, new SubProgressMonitor(monitor, 100));
				// update to the latest state
				FileSystemSubscriber.getInstance().refresh(traversal.getResources(), traversal.getDepth(), null);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Return whether the local resource is checked out. A resource is checked
	 * out if it is a file that is not read-only. Folders are always checked
	 * out.
	 * 
	 * @param resource
	 * 		the resource
	 * @return whether the resource is checked out and can be modified
	 */
	public boolean isCheckedOut(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return true;
		}
		return !resource.getResourceAttributes().isReadOnly();
		// return !resource.isReadOnly();
	}

	/*
	 * Get the resource variant for the given resource.
	 */
	private FileSystemResourceVariant getExportResourceVariant(IResource resource) {
		// return
		// (FileSystemResourceVariant)provider.getResourceVariant(resource);
		return (FileSystemResourceVariant) getProvider(resource).getExportResourceVariant(resource);
	}
	
	private FileSystemResourceVariant getImportResourceVariant(IResource resource){
		return (FileSystemResourceVariant) getProvider(resource).getImportResourcevariant(resource);
	}

	private void internalGet(IResource[] resources, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		// Traverse the resources and get any that are out-of-sync
		// System.out.println("get operation");
		progress.beginTask(Policy.bind("GetAction.working"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			Policy.checkCanceled(progress);
			if (resources[i].getType() == IResource.FILE) {
				internalGet((IFile) resources[i], overrideOutgoing, progress);
			} else if (depth != IResource.DEPTH_ZERO) {
				internalGet((IContainer) resources[i], depth, overrideOutgoing, progress);
			}
			progress.worked(1);
		}
	}

	/*
	 * Get the folder and its children to the depth specified.
	 */
	private void internalGet(IContainer container, int depth, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		try {
			ThreeWaySynchronizer synchronizer = FileSystemSubscriber.getInstance().getSynchronizer();
			// Make the local folder state match the remote folder state
			List toDelete = new ArrayList();
			if (container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder) container;
//				FileSystemResourceVariant remote = getExportResourceVariant(container);
				FileSystemResourceVariant remote = getImportResourceVariant(container);
				if (!folder.exists() && remote != null) {
					// Create the local folder
//					folder.create(false, true, progress);
					folder.create(true, true, progress);
					folder.getResourceAttributes().setReadOnly(false);
					synchronizer.setBaseBytes(folder, remote.asBytes());
				} else if (folder.exists() && remote == null) {
					// Schedule the folder for removal but delay in
					// case the folder contains outgoing changes
					toDelete.add(folder);
				}
			}

			// Get the children
			IResource[] children = synchronizer.members(container);
			if (children.length > 0) {
				internalGet(children, depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, overrideOutgoing, progress);
			}

			// Remove any empty folders
			for (Iterator iter = toDelete.iterator(); iter.hasNext();) {
				IFolder folder = (IFolder) iter.next();
				if (folder.members().length == 0) {
					folder.delete(false, true, progress);
					synchronizer.flush(folder, IResource.DEPTH_INFINITE);
				}
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Get the file if it is out-of-sync.
	 */
	private void internalGet(IFile localFile, boolean overrideOutgoing, IProgressMonitor progress) throws TeamException {
		ThreeWaySynchronizer synchronizer = FileSystemSubscriber.getInstance().getSynchronizer();
		IResourceVariantComparator comparator = FileSystemSubscriber.getInstance().getResourceComparator();
//		FileSystemResourceVariant remote = getExportResourceVariant(localFile);
		FileSystemResourceVariant remote = getImportResourceVariant(localFile);
		byte[] baseBytes = synchronizer.getBaseBytes(localFile);
		FileSystemProvider provider = getProvider(localFile);
		IResourceVariant base = provider.getResourceVariant(localFile, baseBytes);
//		
//		if (!synchronizer.hasSyncBytes(localFile) || (isLocallyModified(localFile) && !overrideOutgoing)) {
//			// Do not overwrite the local modification
//			return;
//		}
		if (base != null && remote == null) {
			// The remote no longer exists so remove the local
			try {
				localFile.delete(false, true, progress);
				synchronizer.flush(localFile, IResource.DEPTH_ZERO);
				return;
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		}
		if (!synchronizer.isLocallyModified(localFile) && base != null && remote != null && comparator.compare(base, remote)) {
			// The base and remote are the same and there's no local changes
			// so nothing needs to be done
			return;
		}
		try {
//			UniFilePlus remoteFile = (UniFilePlus) provider.getExportFile(localFile);
			UniFilePlus remoteFile = (UniFilePlus) provider.getImportFile(localFile);
			if(!remoteFile.exists()){
				localFile.delete(true, null);
				synchronizer.flush(localFile, IResource.DEPTH_ZERO);
			}else{
				if(localFile.exists()){
					localFile.delete(true, null);
				}
				try {
					// Copy from the remote file to the local file:
					
//					if(!localFile.getParent().exists()){
//						IPath parentPath = localFile.getFullPath().removeLastSegments(1);
//						ContainerGenerator generator = new ContainerGenerator(parentPath);
//						IContainer container = generator.generateContainer(null);
//						container.getResourceAttributes().setReadOnly(false);
//					}
					createParentFolderRecursively(localFile);
					remoteFile.getRemoteFile().getParentRemoteFileSubSystem().download(remoteFile.getRemoteFile(), localFile.getLocation().toOSString(), localFile.getCharset(), progress);
//					remoteFile.getRemoteFile().getParentRemoteFileSubSystem().download(remoteFile.getRemoteFile(), parent.getLocation().toOSString(), localFile.getCharset(), progress);
					
					// Mark as read-only to force a checkout before editing
					//System.out.println(localFile.getLocation() + " : accessible = " + localFile.isAccessible()+", existing = "+localFile.exists());															
					if(localFile.isAccessible()){
						localFile.getResourceAttributes().setReadOnly(true);
					}
					
					localFile.getParent().refreshLocal(IResource.DEPTH_ONE, progress);
					
					// update sync status
					synchronizer.setBaseBytes(localFile, remote.asBytes());
					
					// update stored timestamp
					SystemIFileProperties properties = new SystemIFileProperties(localFile);
					properties.setRemoteFileTimeStamp(remoteFile.lastModified());
					properties.setDownloadFileTimeStamp(localFile.getLocalTimeStamp());
					
				} catch (SystemMessageException e) {
					e.printStackTrace();
				}
			}
		} catch (CoreException e) {
			throw FileSystemPlugin.wrapException(e);
		}
	}

	// actual put operation for files
	private void internalPut(IResource[] resources, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		try {
			// ensure the progress monitor is not null
			progress = Policy.monitorFor(progress);
			progress.beginTask(Policy.bind("PutAction.working"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			for (int i = 0; i < resources.length; i++) {
				Policy.checkCanceled(progress);
				if (resources[i].getType() == IResource.FILE) {
					internalPut((IFile) resources[i], overrideIncoming, progress);
				} else if (depth > 0) { // Assume that resources are either
					// files or containers.
					internalPut((IContainer) resources[i], depth, overrideIncoming, progress);
				}
				progress.worked(1);
			}
			progress.done();
		} catch (TeamException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Put the file if the sync state allows it.
	 * 
	 * @param localFile
	 * 		the local file
	 * @param overrideIncoming
	 * 		whether incoming changes should be overwritten
	 * @param progress
	 * 		a progress monitor
	 * @return whether the put succeeded (i.e. the local matches the remote)
	 * @throws TeamException
	 */
	private boolean internalPut(IFile localFile, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		ThreeWaySynchronizer synchronizer = FileSystemSubscriber.getInstance().getSynchronizer();
		IResourceVariantComparator comparator = FileSystemSubscriber.getInstance().getResourceComparator();
		FileSystemResourceVariant remote = getExportResourceVariant(localFile);
		byte[] baseBytes = synchronizer.getBaseBytes(localFile);
		FileSystemProvider provider = getProvider(localFile);
		IResourceVariant base = provider.getResourceVariant(localFile, baseBytes);

		overrideIncoming = true; // DKM - test
		// Check whether we are overriding a remote change
		if (base == null && remote != null && !overrideIncoming) {
			// The remote is an incoming (or conflicting) addition.
			// Do not replace unless we are overriding
			return false;
		} else if (base != null && remote == null) {
			// The remote is an incoming deletion
			if (!localFile.exists()) {
				// Conflicting deletion. Clear the synchronizer.
				synchronizer.flush(localFile, IResource.DEPTH_ZERO);
			} else if (!overrideIncoming) {
				// Do not override the incoming deletion
				return false;
			}
		} else if (base != null && remote != null) {
			boolean same = comparator.compare(base, remote);
			if (!isLocallyModified(localFile) && same) {
				// The base and remote are the same and there's no local changes
				// so nothing needs to be done
				return true;
			}
			if (!same && !overrideIncoming) {
				// The remote has changed. Only override if specified
				return false;
			}
		}

		// Handle an outgoing deletion
		UniFilePlus remoteFile = (UniFilePlus) provider.getExportFile(localFile);
		if (!localFile.exists()) {
			remoteFile.delete();
			synchronizer.flush(localFile, IResource.DEPTH_ZERO);
		} else {
			// Otherwise, upload the contents
			try {
				// Copy from the local file to the remote file:
				try {
					if (!remoteFile.getParentFile().exists()) {
						remoteFile.getParentFile().mkdirs();
					}
					remoteFile.getRemoteFile().getParentRemoteFileSubSystem().upload(localFile.getLocation().toOSString(), remoteFile.getRemoteFile(), localFile.getCharset(), progress);
					// Mark the file as read-only to require another checkout
					localFile.getResourceAttributes().setReadOnly(true);
				} catch (Exception e) {
					e.printStackTrace();
				} 
				// Update the synchronizer base bytes
				remote = getExportResourceVariant(localFile);
				synchronizer.setBaseBytes(localFile, remote.asBytes());
				
				// update stored timestamp
				// make sure the remote file is up-to-date
				remoteFile.getRemoteFile().markStale(true);
				IRemoteFile updatedRemoteFile = remoteFile.getRemoteFile().getParentRemoteFileSubSystem().getRemoteFileObject(remoteFile.getRemoteFile().getAbsolutePath(), progress);
				
				SystemIFileProperties properties = new SystemIFileProperties(localFile);
				properties.setRemoteFileTimeStamp(updatedRemoteFile.getLastModified());
				properties.setDownloadFileTimeStamp(localFile.getLocalTimeStamp());
				
			} catch (CoreException e) {
				throw FileSystemPlugin.wrapException(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean isLocallyModified(IFile localFile) throws TeamException {
		ThreeWaySynchronizer synchronizer = FileSystemSubscriber.getInstance().getSynchronizer();
		if (!localFile.exists()) {
			// Extra check for bug 141415
			return synchronizer.getBaseBytes(localFile) != null;
		}
		return synchronizer.isLocallyModified(localFile);
	}

	/*
	 * Get the folder and its children to the depth specified.
	 */
	private void internalPut(IContainer container, int depth, boolean overrideIncoming, IProgressMonitor progress) throws TeamException {
		try {
			ThreeWaySynchronizer synchronizer = FileSystemSubscriber.getInstance().getSynchronizer();
			FileSystemProvider provider = getProvider(container);
			// Make the local folder state match the remote folder state
			List toDelete = new ArrayList();
			if (container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder) container;
				UniFilePlus diskFile = (UniFilePlus) provider.getExportFile(container);
				FileSystemResourceVariant remote = getExportResourceVariant(container);
				if (!folder.exists() && remote != null) {
					// Schedule the folder for removal but delay in
					// case the folder contains incoming changes
					toDelete.add(diskFile);
				} else if (folder.exists() && remote == null) {
					// Create the remote directory and sync up the local
					diskFile.mkdirs();
					
					IResourceVariant variant = provider.getExportResourceVariant(folder);
					if (variant == null){
						// remote directory does not exist
					}
					else {
						synchronizer.setBaseBytes(folder, variant.asBytes());
					}
				}
			} else if (container.getType() == IResource.PROJECT) {
				IProject project = (IProject) container;
				UniFilePlus remoteProjectFolder = (UniFilePlus) provider.getExportFile(project);
				FileSystemResourceVariant remoteResourceVariant = getExportResourceVariant(container);
				if (project.exists() && remoteResourceVariant == null) {
					remoteProjectFolder.mkdir();
					synchronizer.setBaseBytes(project, provider.getExportResourceVariant(project).asBytes());
				}
			}

			// Get the children
			IResource[] children = synchronizer.members(container);
			if (children.length > 0) {
				internalPut(children, depth == IResource.DEPTH_INFINITE ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO, overrideIncoming, progress);
			}

			// Remove any empty folders
			for (Iterator iter = toDelete.iterator(); iter.hasNext();) {
				File diskFile = (File) iter.next();
				if (diskFile.listFiles().length == 0) {
					diskFile.delete();
					synchronizer.flush(container, IResource.DEPTH_INFINITE);
				}
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createParentFolderRecursively(IResource resource) throws CoreException{
		if(!resource.getParent().exists()){
			ContainerGenerator generator = new ContainerGenerator(resource.getParent().getFullPath());
			IContainer parent = generator.generateContainer(null);
			createParentFolderRecursively(parent);
		}
	}
}
