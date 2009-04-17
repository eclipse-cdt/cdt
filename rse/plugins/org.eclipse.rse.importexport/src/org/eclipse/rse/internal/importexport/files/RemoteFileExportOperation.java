/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [191479] refreshing destination directory after export
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.internal.importexport.RemoteImportExportUtil;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

/**
 *	Operation for exporting the contents of a resource to the local file system.
 */
class RemoteFileExportOperation implements IRunnableWithProgress {
	private IHost conn;
	private IPath path;
	private IProgressMonitor monitor;
	private RemoteExporter exporter;
	private List resourcesToExport;
	private IOverwriteQuery overwriteCallback;
	private IResource resource;
	private List errorTable = new ArrayList(1);
	private RemoteFileExportData exportData;
	private boolean saveSettings;
	private String descriptionFilePath;
	
	private boolean reviewSynchronize = true;
	
	// the constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;
	private static final int OVERWRITE_NONE = 1;
	private static final int OVERWRITE_ALL = 2;
	private int overwriteState = OVERWRITE_NOT_SET;
	private boolean createLeadupStructure = true;
	private boolean createContainerDirectories = true;

	/**
	 *  Create an instance of this class.  Use this constructor if you wish to
	 *  export specific resources with a common parent resource (affects container
	 *  directory creation).
	 */
	private RemoteFileExportOperation(IHost conn, IResource resource, List resources, String destinationPath, IOverwriteQuery overwriteImplementor) {
		this.conn = conn;
		this.resource = resource;
		this.resourcesToExport = resources;
		this.path = new Path(destinationPath);
		this.overwriteCallback = overwriteImplementor;
		this.exporter = new RemoteExporter(conn);		
	}

	public RemoteFileExportOperation(RemoteFileExportData data, IOverwriteQuery overwriteImplementor) {
		this(Utilities.parseForSystemConnection(data.getDestination()), null, data.getElements(), Utilities.parseForPath(data.getDestination()), overwriteImplementor);
		this.exportData = data;
		this.saveSettings = data.isSaveSettings();
		this.descriptionFilePath = data.getDescriptionFilePath();
		setCreateLeadupStructure(data.isCreateDirectoryStructure());
		setReviewSynchronize(data.isReviewSynchronize());
		setOverwriteFiles(data.isOverWriteExistingFiles());
	}

	/**
	 * Add a new entry to the error table with the passed information
	 */
	protected void addError(String message, Throwable e) {
		errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, e));
	}

	/**
	 *  Answer the total number of file resources that exist at or below self in the
	 *  resources hierarchy.
	 *
	 *  @return int
	 *  @param resource org.eclipse.core.resources.IResource
	 */
	protected int countChildrenOf(IResource resource) throws CoreException {
		if (resource.getType() == IResource.FILE) return 1;
		int count = 0;
		if (resource.isAccessible()) {
			IResource[] children = ((IContainer) resource).members();
			for (int i = 0; i < children.length; i++)
				count += countChildrenOf(children[i]);
		}
		return count;
	}

	/**
	 *	Answer a boolean indicating the number of file resources that were
	 *	specified for export
	 *
	 *	@return int
	 */
	protected int countSelectedResources() throws CoreException {
		int result = 0;
		Iterator resources = resourcesToExport.iterator();
		while (resources.hasNext())
			result += countChildrenOf((IResource) resources.next());
		return result;
	}

	/**
	 *  Create the directories required for exporting the passed resource,
	 *  based upon its container hierarchy
	 *
	 *  @param resource org.eclipse.core.resources.IResource
	 */
	protected void createLeadupDirectoriesFor(IResource resource) {
		IPath resourcePath = resource.getFullPath().removeLastSegments(1);
		for (int i = 0; i < resourcePath.segmentCount(); i++) {
			path = path.append(resourcePath.segment(i));
			exporter.createFolder(path);
		}
	}

	/**
	 *	Recursively export the previously-specified resource
	 */
	protected void exportAllResources() throws InterruptedException {
		if (resource.getType() == IResource.FILE)
			exportFile((IFile) resource, path);
		else {
			try {
				exportChildren(((IContainer) resource).members(), path);
			} catch (CoreException e) {
				// not safe to show a dialog
				// should never happen because the file system export wizard ensures that the
				// single resource chosen for export is both existent and accessible
				errorTable.add(e);
			}
		}
	}

	/**
	 *	Export all of the resources contained in the passed collection
	 *
	 *	@param children java.util.Enumeration
	 *	@param currentPath IPath
	 */
	protected void exportChildren(IResource[] children, IPath currentPath) throws InterruptedException {
		for (int i = 0; i < children.length; i++) {
			IResource child = children[i];
			if (!child.isAccessible()) continue;
			if (child.getType() == IResource.FILE)
				exportFile((IFile) child, currentPath);
			else {
				IPath destination = currentPath.append(child.getName());
				try {
					exporter.createFolder(destination);
				} catch (Exception e) {
					String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, destination,  e.getLocalizedMessage() == null ? e.toString() : e.getMessage());
					errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
				}
				try {
					exportChildren(((IContainer) child).members(), destination);
				} catch (CoreException e) {
					// not safe to show a dialog
					// should never happen because:
					// i.  this method is called recursively iterating over the result of #members,
					//		which only answers existing children
					// ii. there is an #isAccessible check done before #members is invoked
					errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
					//errorTable.add(e.getStatus());
				}
			}
		}
	}

	/**
	 *  Export the passed file to the specified location
	 *
	 *  @param file org.eclipse.core.resources.IFile
	 *  @param location org.eclipse.core.runtime.IPath
	 */
	protected void exportFile(IFile file, IPath location) throws InterruptedException {
		IPath fullPath = location.append(file.getName());
		String destination = fullPath.toString();
		// flag to indicate whether export is required
		boolean exportRequired = false;
		monitor.subTask(file.getFullPath().toString());
		String properPathString = fullPath.toOSString();
		File targetFile = null;
		if (conn == null) {
			targetFile = new File(properPathString);
		} else {
			try {
				targetFile = new UniFilePlus(Utilities.getIRemoteFile(conn, fullPath.toString()));
			} catch (NullPointerException e) {
				String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, RemoteImportExportResources.MSG_IMPORT_EXPORT_UNABLE_TO_USE_CONNECTION);
				
				// Assume that communication has failed.  
				errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
				throw e;
			}
		}
		if (targetFile.exists()) {
			exportRequired = isExportRequired(file, destination);
			// if export is not required, no need to do anything
			if (!exportRequired) {
				return;
			}
			if (!targetFile.canWrite()) {
				String msg = NLS.bind(RemoteImportExportResources.FILEMSG_NOT_WRITABLE, targetFile.getAbsolutePath());
				errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, null));
				monitor.worked(1);
				return;
			}
			if (overwriteState == OVERWRITE_NONE) {
				return;
			} else if (overwriteState != OVERWRITE_ALL) {
				String overwriteAnswer = overwriteCallback.queryOverwrite(properPathString);
				if (overwriteAnswer.equals(IOverwriteQuery.CANCEL)) {
					throw new InterruptedException();
				} else if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
					monitor.worked(1);
					return;
				} else if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
					monitor.worked(1);
					overwriteState = OVERWRITE_NONE;
					return;
				} else if (overwriteAnswer.equals(IOverwriteQuery.ALL)) {
					overwriteState = OVERWRITE_ALL;
				}
			}
		} else if (!targetFile.exists()) {
			// need to do an export if target file does not exist, even if the local
			// file has not changed. This is for the scenario where a file may have been
			// exported, and the server copy was later deleted. The next export should put
			// the local copy back on the server, even if the local file was not changed.
			exportRequired = true;
		}
		try {
			exporter.write(file, fullPath);
			// if there are no exceptions, we should be here and the export should have completed fine
			// so we update the modification time at the time of export
			SystemIFileProperties props = new SystemIFileProperties(file);
			long modTime = file.getModificationStamp();
			props.setModificationStampAtExport(conn.getHostName(), destination, modTime);
		} catch (IOException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, e.getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		} catch (CoreException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, e.getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		} catch (RemoteFileIOException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, e.getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
		} catch (RemoteFileSecurityException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, e.getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
		} catch (Exception e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_EXPORT_ERROR, fullPath, e.getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, e));
		}
		monitor.worked(1);
		ModalContext.checkCanceled(monitor);
	}

	protected boolean isExportRequired(IFile file, String destinationPath) {
		if (conn != null) {
			// get the host name of the connection
			String hostName = conn.getHostName();
			SystemIFileProperties props = new SystemIFileProperties(file);
			// check if we have a modification time stored for the hostname/destination path combination
			boolean hasModTime = props.hasModificationStampAtExport(hostName, destinationPath);
			// if not, that means we are exporting for the first time
			if (!hasModTime) {
				return true;
			}
			// otherwise, check if the modification time stored is different to the modification time
			// of the file
			else {
				long modTime = props.getModificationStampAtExport(hostName, destinationPath);
				long currentModTime = file.getModificationStamp();
				// if the modification timestamps are different, then the file has changed
				// since the last export to the destination, so we need export it again
				if (modTime != currentModTime) {
					return true;
				}
				// otherwise, do not export
				else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 *	Export the resources contained in the previously-defined
	 *	resourcesToExport collection
	 */
	protected void exportSpecifiedResources() throws InterruptedException {
		Iterator resources = resourcesToExport.iterator();
		IPath initPath = (IPath) path.clone();
		while (resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			if (!currentResource.isAccessible()) continue;
			path = initPath;
			if (resource == null) {
				// No root resource specified and creation of containment directories
				// is required.  Create containers from depth 2 onwards (ie.- project's
				// child inclusive) for each resource being exported.
				if (createLeadupStructure) createLeadupDirectoriesFor(currentResource);
			} else {
				// Root resource specified.  Must create containment directories
				// from this point onwards for each resource being exported
				IPath containersToCreate = currentResource.getFullPath().removeFirstSegments(resource.getFullPath().segmentCount()).removeLastSegments(1);
				for (int i = 0; i < containersToCreate.segmentCount(); i++) {
					path = path.append(containersToCreate.segment(i));
					exporter.createFolder(path);
				}
			}
			if (currentResource.getType() == IResource.FILE)
				exportFile((IFile) currentResource, path);
			else {
				if (createContainerDirectories) {
					path = path.append(currentResource.getName());
					exporter.createFolder(path);
				}
				try {
					exportChildren(((IContainer) currentResource).members(), path);
				} catch (CoreException e) {
					// should never happen because #isAccessible is called before #members is invoked,
					// which implicitly does an existence check
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	/**
	 * Returns the status of the export operation.
	 * If there were any errors, the result is a status object containing
	 * individual status objects for each error.
	 * If there were no errors, the result is a status object with error code <code>OK</code>.
	 *
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		String msg = RemoteImportExportResources.FILEMSG_EXPORT_PROBLEMS;
		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, msg, null);
	}

	/**
	 *  Answer a boolean indicating whether the passed child is a descendent
	 *  of one or more members of the passed resources collection
	 *
	 *  @return boolean
	 *  @param resources java.util.List
	 *  @param child org.eclipse.core.resources.IResource
	 */
	protected boolean isDescendent(List resources, IResource child) {
		if (child.getType() == IResource.PROJECT) return false;
		IResource parent = child.getParent();
		if (resources.contains(parent)) return true;
		return isDescendent(resources, parent);
	}

	/**
	 *	Export the resources that were previously specified for export
	 *	(or if a single resource was specified then export it recursively)
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException {
		this.monitor = monitor;
		IPath parentPath = (IPath)path.clone();
		if (resource != null) {
			if (createLeadupStructure) createLeadupDirectoriesFor(resource);
			if (createContainerDirectories && resource.getType() != IResource.FILE) { // ensure it's a container
				path = path.append(resource.getName());
				exporter.createFolder(path);
			}
		}
		try {
			int totalWork = IProgressMonitor.UNKNOWN;
			try {
				if (resourcesToExport == null)
					totalWork = countChildrenOf(resource);
				else
					totalWork = countSelectedResources();
			} catch (CoreException e) {
				// Should not happen
				errorTable.add(e.getStatus());
			}
			String taskMsg = SystemImportExportResources.RESID_FILEEXPORT_EXPORTING;
			monitor.beginTask(taskMsg, totalWork);
			if (resourcesToExport == null) {
				exportAllResources();
			} else {
				exportSpecifiedResources();
			}
			
			// fire event to update RSE
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			IRemoteFile destination = getRemoteFile(conn, parentPath);
			
			sr.fireEvent(new SystemResourceChangeEvent(destination, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));
			
			if (saveSettings) {
				try {
					saveDescription();
				} catch (CoreException e) {
					SystemBasePlugin.logError("Error occured trying to save description " + descriptionFilePath, e); //$NON-NLS-1$
					errorTable.add(e.getStatus());
				} catch (IOException e) {
					SystemBasePlugin.logError("Error occured trying to save description " + descriptionFilePath, e); //$NON-NLS-1$
					errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getSymbolicName(), 0, e.getLocalizedMessage(), e));
				}
			}
		} finally {
			monitor.done();
		}
	}

	private IRemoteFile getRemoteFile(IHost conn, IPath path)
	{
		return Utilities.getIRemoteFile(conn, path.toString());
	}
	
	/**
	 * Saves a description file for the export.
	 * @throws CoreException if an unexpected exception occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void saveDescription() throws CoreException, IOException {
		ByteArrayOutputStream objectStreamOutput = new ByteArrayOutputStream();
		IRemoteFileExportDescriptionWriter writer = exportData.createExportDescriptionWriter(objectStreamOutput);
		ByteArrayInputStream fileInput = null;
		try {
			writer.write(exportData);
			fileInput = new ByteArrayInputStream(objectStreamOutput.toByteArray());
			IFile descriptionFile = exportData.getDescriptionFile();
			// check if resource exists
			if (descriptionFile.isAccessible()) {
				descriptionFile.setContents(fileInput, true, true, null);
			}
			// if resource does not exist
			else {
				// now have to check if a variant of this file exists (i.e. whether a file exists
				// that has the same path with a different case. For case insensitive file systems
				// such as Windows, this is needed since we can't simply create a file with a different
				// case. Note that isAccessible() above does not check for file paths with different case,
				// so we have to check it explicitly).
				IResource variant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(descriptionFile.getFullPath());
				// if a variant was not found, create the new file
				if (variant == null) {
					// check if a variant of the parent exists
					// we need to do this because at this point we know that the file path does not
					// exist, and neither does its variant. However, it is possible that the parent path
					// has a variant, in which case calling create on the description file with
					// the path as it is given will fail. We need to get the variant path of the parent,
					// append the name of the file to the variant path, and create a file with that path.
					// get parent
					IResource parent = descriptionFile.getParent();
					if (parent != null) {
						// get parent path
						IResource parentVariant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(parent.getFullPath());
						// no parent variant (i.e. in a case sensitive file system)
						if (parentVariant == null) {
							descriptionFile.create(fileInput, true, null);
						}
						// parent variant found (might be same as original parent path)
						else {
							IPath newPath = parentVariant.getFullPath().append(descriptionFile.getName());
							IFile newDescriptionFile = SystemBasePlugin.getWorkspace().getRoot().getFile(newPath);
							newDescriptionFile.create(fileInput, true, null);
						}
					}
				}
				// otherwise, simply set the contents of the variant file
				else {
					if (variant instanceof IFile) {
						((IFile) variant).setContents(fileInput, true, true, null);
					}
				}
			}
		} finally {
			if (fileInput != null) {
				fileInput.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 *	Set this boolean indicating whether a directory should be created for
	 *	Folder resources that are explicitly passed for export
	 *
	 *	@param value boolean
	 */
	public void setCreateContainerDirectories(boolean value) {
		createContainerDirectories = value;
	}

	/**
	 *	Set this boolean indicating whether each exported resource's complete path should
	 *	include containment hierarchies as dictated by its parents
	 *
	 *	@param value boolean
	 */
	public void setCreateLeadupStructure(boolean value) {
		createLeadupStructure = value;
	}

	/**
	 *	Set this boolean indicating whether exported resources should automatically
	 *	overwrite existing files when a conflict occurs
	 *
	 *	@param value boolean
	 */
	public void setOverwriteFiles(boolean value) {
		if (value) {
			overwriteState = OVERWRITE_ALL;
		}
	}
	
	/**
	 *	Set this boolean indicating whether exported resources should automatically
	 *	be reviewed/synchronized
	 *
	 *	@param value boolean
	 */
	public void setReviewSynchronize(boolean value) {
		reviewSynchronize = value;
	}
}
