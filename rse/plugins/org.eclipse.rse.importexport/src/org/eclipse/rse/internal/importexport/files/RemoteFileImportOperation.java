/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David McKnight     (IBM)      - [229610] [api] File transfers should use workspace text file encoding
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 * David McKnight   (IBM)        - [276535] File Conflict when Importing Remote Folder with Case-Differentiated-Only Filenames into Project
 * David McKnight   (IBM)        - [191558] [importexport][efs] Import to Project doesn't work with remote EFS projects
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportResources;
import org.eclipse.rse.internal.importexport.RemoteImportExportUtil;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.IOverwriteQuery;
// Similar to org.eclipse.ui.wizards.datatransfer.ImportOperation
/**
 * An operation which does the actual work of copying objects from the local
 * file system into the workspace.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RemoteFileImportOperation extends WorkspaceModifyOperation {
	private static final int POLICY_DEFAULT = 0;
	private static final int POLICY_SKIP_CHILDREN = 1;
	private static final int POLICY_FORCE_OVERWRITE = 2;
	private Object source;
	private IPath destinationPath;
	private IContainer destinationContainer;
	private List selectedFiles;
	private IImportStructureProvider provider;
	private IProgressMonitor monitor;
	protected IOverwriteQuery overwriteCallback;
	private List errorTable = new ArrayList();
	private boolean createContainerStructure = true;
	private RemoteFileImportData importData;
	private boolean saveSettings;
	private String descriptionFilePath;
	//The constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;
	private static final int OVERWRITE_NONE = 1;
	private static final int OVERWRITE_ALL = 2;
	private int overwriteState = OVERWRITE_NOT_SET;
	
	private boolean reviewSynchronize = true;

	/**
	 * Creates a new operation that recursively imports the entire contents of the
	 * specified root file system object.
	 * <p>
	 * The <code>source</code> parameter represents the root file system object to
	 * import. All contents of this object are imported. Valid types for this parameter
	 * are determined by the supplied <code>IImportStructureProvider</code>.
	 * </p>
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 *  <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the contents of the root file system object in their destination.
	 * If <code>setCreateContainerStructure</code> is set to false then the container
	 * structure created is relative to the root file system object.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param source the root file system object to import
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 */
	public RemoteFileImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor) {
		super();
		this.destinationPath = containerPath;
		this.source = source;
		this.provider = provider;
		overwriteCallback = overwriteImplementor;
	}

	/**
	 * Creates a new operation that imports specific file system objects.
	 * In this usage context, the specified source file system object is used by the
	 * operation solely to determine the destination container structure of the file system
	 * objects being imported.
	 * <p>
	 * The <code>source</code> parameter represents the root file system object to
	 * import. Valid types for this parameter are determined by the supplied
	 * <code>IImportStructureProvider</code>. The contents of the source which
	 * are to be imported are specified in the <code>filesToImport</code>
	 * parameter.
	 * </p>
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 * <p>
	 * The <code>filesToImport</code> parameter specifies what contents of the root
	 * file system object are to be imported.
	 * </p>
	 * <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
	 * is set to <code>false</code>, then the container structure created for each of
	 * the file system objects is relative to the supplied root file system object.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param source the root file system object to import from
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 * @param filesToImport the list of file system objects to be imported
	 *  (element type: <code>Object</code>)
	 */
	public RemoteFileImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
		this(containerPath, source, provider, overwriteImplementor);
		setFilesToImport(filesToImport);
	}

	public RemoteFileImportOperation(RemoteFileImportData data, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor) {
		this(data.getContainerPath(), data.getSource(), provider, overwriteImplementor);
		setFilesToImport(data.getElements());
		setOverwriteResources(data.isOverWriteExistingFiles());
		setReviewSynchronize(data.isReviewSynchronize());
		setCreateContainerStructure(data.isCreateDirectoryStructure());
		this.importData = data;
		this.saveSettings = data.isSaveSettings();
		this.descriptionFilePath = data.getDescriptionFilePath();
	}

	/**
	 * Creates a new operation that imports specific file system objects.
	 * <p>
	 * The <code>provider</code> parameter allows this operation to deal with the
	 * source object in an abstract way. This operation calls methods on the provider
	 * and the provider in turn calls specific methods on the source object.
	 * </p>
	 * <p>
	 * The <code>filesToImport</code> parameter specifies what file system objects
	 * are to be imported.
	 * </p>
	 * <p>
	 * The default import behavior is to recreate the complete container structure
	 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
	 * is set to <code>false</code>, then no container structure is created for each of
	 * the file system objects.
	 * </p>
	 *
	 * @param containerPath the full path of the destination container within the
	 *   workspace
	 * @param provider the file system structure provider to use
	 * @param overwriteImplementor the overwrite strategy to use
	 * @param filesToImport the list of file system objects to be imported
	 *  (element type: <code>Object</code>)
	 */
	public RemoteFileImportOperation(IPath containerPath, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
		this(containerPath, null, provider, overwriteImplementor);
		setFilesToImport(filesToImport);
	}

	/**
	 * Creates the folders that appear in the specified resource path.
	 * These folders are created relative to the destination container.
	 *
	 * @param path the relative path of the resource
	 * @return the container resource coresponding to the given path
	 * @exception CoreException if this method failed
	 */
	IContainer createContainersFor(IPath path) throws CoreException {
		IContainer currentFolder = destinationContainer;
		int segmentCount = path.segmentCount();
		//No containers to create
		if (segmentCount == 0) return currentFolder;
		//Needs to be handles differently at the root
		if (currentFolder.getType() == IResource.ROOT) return createFromRoot(path);
		for (int i = 0; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
			if (!currentFolder.exists()) ((IFolder) currentFolder).create(false, true, null);
		}
		return currentFolder;
	}

	/**
	 * Creates the folders that appear in the specified resource path
	 * assuming that the destinationContainer begins at the root. Do not create projects.
	 *
	 * @param path the relative path of the resource
	 * @return the container resource coresponding to the given path
	 * @exception CoreException if this method failed
	 */
	private IContainer createFromRoot(IPath path) throws CoreException {
		int segmentCount = path.segmentCount();
		//Assume the project exists
		IContainer currentFolder = ((IWorkspaceRoot) destinationContainer).getProject(path.segment(0));
		for (int i = 1; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
			if (!currentFolder.exists()) ((IFolder) currentFolder).create(false, true, null);
		}
		return currentFolder;
	}

	/**
	 * Deletes the given resource. If the resource fails to be deleted, adds a
	 * status object to the list to be returned by <code>getResult</code>.
	 *
	 * @param resource the resource
	 */
	void deleteResource(IResource resource) {
		try {
			resource.delete(IResource.KEEP_HISTORY, null);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		}
	}

	/**
	 * Attempts to ensure that the given resource does not already exist in the
	 * workspace. The resource will be deleted if required, perhaps after asking
	 * the user's permission.
	 *
	 * @param targetResource the resource that should not exist
	 * @param policy determines how the resource is imported
	 * @return <code>true</code> if the resource does not exist, and
	 *    <code>false</code> if it does exist
	 */
	boolean ensureTargetDoesNotExist(IResource targetResource, int policy) {
		if (targetResource.exists()) {
			//If force overwrite is on don't bother
			if (policy != POLICY_FORCE_OVERWRITE) {
				if (this.overwriteState == OVERWRITE_NOT_SET && !queryOverwrite(targetResource.getFullPath())) return false;
				if (this.overwriteState == OVERWRITE_NONE) return false;
			}
			deleteResource(targetResource);
		}
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on WorkbenchModifyOperation.
	 * Imports the specified file system objects from the file system.
	 */
	protected void execute(IProgressMonitor progressMonitor) {
		monitor = progressMonitor;
		try {
			if (selectedFiles == null) {
				//Set the amount to 1000 as we have no idea of how long this will take
				String taskMsg = SystemImportExportResources.RESID_FILEIMPORT_IMPORTING;
				monitor.beginTask(taskMsg, 1000);
				ContainerGenerator generator = new ContainerGenerator(destinationPath);
				monitor.worked(50);
				destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
				importRecursivelyFrom(source, POLICY_DEFAULT);
				//Be sure it finishes
				monitor.worked(90);
			} else {
				// Choose twice the selected files size to take folders into account
				int creationCount = selectedFiles.size();
				String taskMsg = SystemImportExportResources.RESID_FILEIMPORT_IMPORTING;
				monitor.beginTask(taskMsg, creationCount + 100);
				ContainerGenerator generator = new ContainerGenerator(destinationPath);
				monitor.worked(50);
				destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
				importFileSystemObjects(selectedFiles);
			}
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
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		} finally {
			monitor.done();
		}
	}

	/**
	 * Saves a description file for the export.
	 * @throws CoreException if an unexpected exception occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void saveDescription() throws CoreException, IOException {
		ByteArrayOutputStream objectStreamOutput = new ByteArrayOutputStream();
		IRemoteFileImportDescriptionWriter writer = importData.createImportDescriptionWriter(objectStreamOutput);
		ByteArrayInputStream fileInput = null;
		try {
			writer.write(importData);
			fileInput = new ByteArrayInputStream(objectStreamOutput.toByteArray());
			IFile descriptionFile = importData.getDescriptionFile();
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
	 * Returns the container resource that the passed file system object should be
	 * imported into.
	 *
	 * @param fileSystemObject the file system object being imported
	 * @return the container resource that the passed file system object should be
	 *     imported into
	 * @exception CoreException if this method failed
	 */
	IContainer getDestinationContainerFor(Object fileSystemObject) throws CoreException {
		IPath pathname = new Path(provider.getFullPath(fileSystemObject));
		if (createContainerStructure)
			return createContainersFor(pathname.removeLastSegments(1));
		else {
			if (source == fileSystemObject) return null;
			IPath sourcePath = new Path(provider.getFullPath(source));
			IPath destContainerPath = pathname.removeLastSegments(1);
			IPath relativePath = destContainerPath.removeFirstSegments(sourcePath.segmentCount()).setDevice(null);
			return createContainersFor(relativePath);
		}
	}

	/**
	 * Returns the status of the import operation.
	 * If there were any errors, the result is a status object containing
	 * individual status objects for each error.
	 * If there were no errors, the result is a status object with error code <code>OK</code>.
	 *
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		// IFS:
		String msg = RemoteImportExportResources.FILEMSG_IMPORT_PROBLEMS;
		return new MultiStatus(RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), IStatus.OK, errors, msg, null);
	}

	private IFile existingFileInDifferentCase(IFile file) throws CoreException {

		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");  //$NON-NLS-1$//$NON-NLS-2$		
		if (!isWindows) // if the system is case sensitive then we're good
			return null;
		
		String newName = file.getName();
		
		//now look for a matching case variant in the tree
		IContainer parent = file.getParent();
		IResource[] members  = parent.members();
		for (int i = 0; i < members.length; i++){
			IResource member = members[i];
			if (member instanceof IFile){
				String memberName = member.getName();
				if (newName.equalsIgnoreCase(memberName)){
					return (IFile)member;
				}
			}
		}
		return null;
	}
	
	/**
	 * Imports the specified file system object into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getResult</code>.
	 *
	 * @param fileObject the file system object to be imported
	 * @param policy determines how the file object is imported
	 */
	void importFile(Object fileObject, int policy) {
		IContainer containerResource;
		try {
			containerResource = getDestinationContainerFor(fileObject);
		} catch (CoreException e) {
			IStatus coreStatus = e.getStatus();
			String newMessage = NLS.bind(RemoteImportExportResources.FILEMSG_IMPORT_ERROR, fileObject, coreStatus.getMessage());
			IStatus status = new Status(coreStatus.getSeverity(), coreStatus.getPlugin(), coreStatus.getCode(), newMessage, null);
			errorTable.add(status);
			return;
		}
		String fileObjectPath = provider.getFullPath(fileObject);
		monitor.subTask(fileObjectPath);
		IFile targetResource = containerResource.getFile(new Path(provider.getLabel(fileObject)));
		monitor.worked(1);
		// ensure that the source and target are not the same
		IPath targetPath = targetResource.getLocation();
		// Use Files for comparison to avoid platform specific case issues
		if (targetPath != null && (targetPath.toFile().equals(new File(fileObjectPath)))) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_IMPORT_SELF, fileObjectPath);
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, null));
			return;
		}
		if (!ensureTargetDoesNotExist(targetResource, policy)) {
			// Do not add an error status because the user
			// has explicitely said no overwrite. Do not
			// update the monitor as it was done in queryOverwrite.
			return;
		}
		try {
			IRemoteFileSubSystem rfss = RemoteFileUtility.getFileSubSystem(((UniFilePlus) fileObject).remoteFile.getHost());
			// 030820: added the following kludge to circumvent problem in
			// artemis.  (artemis 3 will fix this)
			// TODO remove for 6.0
			String encoding = ((UniFilePlus) fileObject).remoteFile.getEncoding();
			if (encoding.startsWith("CP")) //$NON-NLS-1$
			{
				encoding = "Cp" + encoding.substring(2); //$NON-NLS-1$
			}
			
			// check for existing resource		
			IFile existingFile = existingFileInDifferentCase(targetResource);
			if (existingFile != null){
				String msgDetails = NLS.bind(FileResources.FILEMSG_CREATE_FILE_FAILED_EXIST_DETAILS, existingFile.getFullPath());			
				errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(),msgDetails));
				return;			
			}
			
			if (targetResource.getLocation() == null){
				// an EFS file destination				
				String remoteFileName = ((UniFilePlus) fileObject).remoteFile.getName();
				String remoteParentPath = ((UniFilePlus) fileObject).remoteFile.getParentPath();
				
				InputStream instream = rfss.getInputStream(remoteParentPath, remoteFileName, true, monitor);
				if (!targetResource.exists()){
					targetResource.create(instream, IResource.FORCE, monitor);
				}
				else {
					targetResource.setContents(instream, IResource.FORCE, monitor);								
				}
			}
			else {
				rfss.download(((UniFilePlus) fileObject).remoteFile, targetResource.getLocation().makeAbsolute().toOSString(), encoding, null);
			}
			try {
				// refresh workspace with just added resource
				targetResource.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			} catch (CoreException e) {
				errorTable.add(e.getStatus());
			}
		} catch (RemoteFileIOException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_IMPORT_ERROR, fileObjectPath, e.getRemoteException().getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
			return;
		} catch (RemoteFileSecurityException e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_IMPORT_ERROR, fileObjectPath, e.getRemoteException().getLocalizedMessage());
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
			return;
		} catch (Exception e) {
			String msg = NLS.bind(RemoteImportExportResources.FILEMSG_IMPORT_ERROR, fileObjectPath, e.getMessage() == null ? e.toString() : e.getMessage());
			errorTable.add(new Status(IStatus.ERROR, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, e));
			return;
		}
	}

	/**
	 * Imports the specified file system objects into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getStatus</code>.
	 *
	 * @param filesToImport the list of file system objects to import
	 *   (element type: <code>Object</code>)
	 * @exception OperationCanceledException if cancelled
	 */
	void importFileSystemObjects(List filesToImport) {
		Iterator filesEnum = filesToImport.iterator();
		while (filesEnum.hasNext()) {
			Object fileSystemObject = filesEnum.next();
			if (source == null) {
				// We just import what we are given into the destination
				IPath sourcePath = new Path(provider.getFullPath(fileSystemObject)).removeLastSegments(1);
				if (provider.isFolder(fileSystemObject) && sourcePath.isEmpty()) {
					// If we don't have a parent then we have selected the
					// file systems root. Roots can't copied (at least not
					// under windows).
					String msg = RemoteImportExportResources.FILEMSG_COPY_ROOT;
					errorTable.add(new Status(IStatus.INFO, RemoteImportExportPlugin.getDefault().getBundle().getSymbolicName(), 0, msg, null));
					continue;
				}
				source = sourcePath.toFile();
			}
			importRecursivelyFrom(fileSystemObject, POLICY_DEFAULT);
		}
	}

	/**
	 * Imports the specified file system container object into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getResult</code>.
	 *
	 * @param fileObject the file system container object to be imported
	 * @param policy determines how the folder object and children are imported
	 * @return the policy to use to import the folder's children
	 */
	int importFolder(Object folderObject, int policy) {
		IContainer containerResource;
		try {
			containerResource = getDestinationContainerFor(folderObject);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
			return policy;
		}
		if (containerResource == null) return policy;
		monitor.subTask(provider.getFullPath(folderObject));
		IWorkspace workspace = destinationContainer.getWorkspace();
		IPath containerPath = containerResource.getFullPath();
		IPath resourcePath = containerPath.append(provider.getLabel(folderObject));
		// Do not attempt the import if the resource path is unchanged. This may happen
		// when importing from a zip file.
		if (resourcePath.equals(containerPath)) return policy;
		if (workspace.getRoot().exists(resourcePath)) {
			if (policy != POLICY_FORCE_OVERWRITE) {
				if (this.overwriteState == OVERWRITE_NONE || !queryOverwrite(resourcePath)) // Do not add an error status because the user
					// has explicitely said no overwrite. Do not
					// update the monitor as it was done in queryOverwrite.
					return POLICY_SKIP_CHILDREN;
			}
			return POLICY_FORCE_OVERWRITE;
		}
		try {
			workspace.getRoot().getFolder(resourcePath).create(false, true, null);
		} catch (CoreException e) {
			errorTable.add(e.getStatus());
		}
		return policy;
	}

	/**
	 * Imports the specified file system object recursively into the workspace.
	 * If the import fails, adds a status object to the list to be returned by
	 * <code>getStatus</code>.
	 *
	 * @param fileSystemObject the file system object to be imported
	 * @param policy determines how the file system object and children are imported
	 * @exception OperationCanceledException if cancelled
	 */
	void importRecursivelyFrom(Object fileSystemObject, int policy) {
		if (monitor.isCanceled()) throw new OperationCanceledException();
		if (!provider.isFolder(fileSystemObject)) {
			importFile(fileSystemObject, policy);
			return;
		}
		int childPolicy = importFolder(fileSystemObject, policy);
		if (childPolicy != POLICY_SKIP_CHILDREN) {
			Iterator children = provider.getChildren(fileSystemObject).iterator();
			while (children.hasNext())
				importRecursivelyFrom(children.next(), childPolicy);
		}
	}

	/**
	 * Queries the user whether the resource with the specified path should be
	 * overwritten by a file system object that is being imported.
	 *
	 * @param path the workspace path of the resource that needs to be overwritten
	 * @return <code>true</code> to overwrite, <code>false</code> to not overwrite
	 * @exception OperationCanceledException if cancelled
	 */
	boolean queryOverwrite(IPath resourcePath) throws OperationCanceledException {
		String overwriteAnswer = overwriteCallback.queryOverwrite(resourcePath.makeRelative().toString());
		if (overwriteAnswer.equals(IOverwriteQuery.CANCEL)) //throw new OperationCanceledException(UniversalSystemPlugin.getString("customs.emptyString"));
			throw new OperationCanceledException(""); //$NON-NLS-1$
		if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
			return false;
		}
		if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
			this.overwriteState = OVERWRITE_NONE;
			return false;
		}
		if (overwriteAnswer.equals(IOverwriteQuery.ALL)) this.overwriteState = OVERWRITE_ALL;
		return true;
	}

	/**
	 * Sets whether the containment structures that are implied from the full paths
	 * of file system objects being imported should be duplicated in the workbench.
	 *
	 * @param value <code>true</code> if containers should be created, and
	 *  <code>false</code> otherwise
	 */
	public void setCreateContainerStructure(boolean value) {
		createContainerStructure = value;
	}

	/**
	 * Sets the file system objects to import.
	 *
	 * @param filesToImport the list of file system objects to be imported
	 *   (element type: <code>Object</code>)
	 */
	public void setFilesToImport(List filesToImport) {
		this.selectedFiles = filesToImport;
	}

	/**
	 * Sets whether imported file system objects should automatically overwrite
	 * existing workbench resources when a conflict occurs.
	 *
	 * @param value <code>true</code> to automatically overwrite, and
	 *   <code>false</code> otherwise
	 */
	public void setOverwriteResources(boolean value) {
		if (value) this.overwriteState = OVERWRITE_ALL;
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
