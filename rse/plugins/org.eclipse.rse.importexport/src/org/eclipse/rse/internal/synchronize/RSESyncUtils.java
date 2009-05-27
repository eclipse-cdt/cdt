/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.synchronize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.RemoteImportExportUtil;
import org.eclipse.rse.internal.importexport.files.IRemoteFileExportDescriptionWriter;
import org.eclipse.rse.internal.importexport.files.IRemoteFileImportDescriptionWriter;
import org.eclipse.rse.internal.importexport.files.RemoteFileExportData;
import org.eclipse.rse.internal.importexport.files.RemoteFileImportData;
import org.eclipse.rse.internal.synchronize.filesystem.FileSystemProvider;
import org.eclipse.rse.internal.synchronize.provisional.ISynchronizeFilter;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class RSESyncUtils {

	/**
	 * Convenience method to get the currently active workbench page. Note that
	 * the active page may not be the one that the user perceives as active in
	 * some situations so this method of obtaining the active page should only
	 * be used if no other method is available.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = RemoteImportExportPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}

	/**
	 * Helper method for export operation.
	 * Calculate the synchronized elements in local side by using ISynchornizeFilter.
	 * The filter contains paths of synchronized elements in local side.
	 * The calculated resources are added in the resources.
	 *
	 * @param local root handle in local side.
	 * @param filter
	 * @param resources calculated resources
	 * @throws CoreException
	 */
	public static void getSynchronizeResources(IResource local, ISynchronizeFilter filter, List<IResource> resources) throws CoreException{
		if(!filter.isExcluded(local.getFullPath())){
			resources.add(local);
		}
		if(local.getType() != IResource.FILE){
			for (IResource resource : ((IContainer)local).members()) {
				getSynchronizeResources(resource, filter,  resources);
			}
		}
	}

	/**
	 * Helper method for import operation.
	 * Calculate the synchronize elements in local side by using ISynchronizeFilter.
	 * The filter contains paths of synchronized elements in remote side.
	 * The calculated resources are added in the resources.
	 *
	 * @param localRoot root handle in local side
	 * @param remote current element in remote side
	 * @param remoteRoot  root handle in remote side
	 * @param filter
	 * @param resources calculated resources
	 * @throws SystemMessageException
	 * @throws CoreException
	 */
	public static void getSynchronizeResources(IResource localRoot, IRemoteFile remote, IRemoteFile remoteRoot, ISynchronizeFilter filter, List<IResource> resources) throws SystemMessageException,  CoreException{

		if(!filter.isExcluded(new Path(remote.getAbsolutePathPlusConnection()))){
			String relativePath = remote.getAbsolutePath().replace(remoteRoot.getAbsolutePath(), "");
			if(remote.isFile()){
//				IFile file = ((IProject)localRoot).getFile(relativePath);
//				IPath filePath = file.getProjectRelativePath();
				resources.add(((IProject)localRoot).getFile(relativePath));
			}else{
//				IFolder folder = ((IProject)localRoot).getFolder(relativePath);
//				IPath folderPath = folder.getProjectRelativePath();
				resources.add(((IProject)localRoot).getFolder(relativePath));
			}

		}
		if(!remote.isFile()){
			for (IRemoteFile remoteFile : remote.getParentRemoteFileSubSystem().list(remote, null)) {
				getSynchronizeResources(localRoot, remoteFile, remoteRoot, filter,  resources);
			}
		}
	}

	// <Copied from org.eclipse.rse.internal.importexport.files.RemoteFileExportOperation>
	/**
	 * Saves a description file for the export.
	 *
	 * @throws CoreException
	 * 		if an unexpected exception occurs.
	 * @throws IOException
	 * 		if an I/O error occurs.
	 */
	public static void saveDescription(RemoteFileExportData exportData) throws CoreException, IOException {
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
				// now have to check if a variant of this file exists (i.e.
				// whether a file exists
				// that has the same path with a different case. For case
				// insensitive file systems
				// such as Windows, this is needed since we can't simply create
				// a file with a different
				// case. Note that isAccessible() above does not check for file
				// paths with different case,
				// so we have to check it explicitly).
				IResource variant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(descriptionFile.getFullPath());
				// if a variant was not found, create the new file
				if (variant == null) {
					// check if a variant of the parent exists
					// we need to do this because at this point we know that the
					// file path does not
					// exist, and neither does its variant. However, it is
					// possible that the parent path
					// has a variant, in which case calling create on the
					// description file with
					// the path as it is given will fail. We need to get the
					// variant path of the parent,
					// append the name of the file to the variant path, and
					// create a file with that path.
					// get parent
					IResource parent = descriptionFile.getParent();
					if (parent != null) {
						// get parent path
						IResource parentVariant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(parent.getFullPath());
						// no parent variant (i.e. in a case sensitive file
						// system)
						if (parentVariant == null) {
							descriptionFile.create(fileInput, true, null);
						}
						// parent variant found (might be same as original
						// parent path)
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
	// </Copied from org.eclipse.rse.internal.importexport.files.RemoteFileExportOperation>

	// <Copied from org.eclipse.rse.internal.importexport.files.RemoteFileExportOperation>
	/**
	 * Saves a description file for the export.
	 *
	 * @throws CoreException
	 * 		if an unexpected exception occurs.
	 * @throws IOException
	 * 		if an I/O error occurs.
	 */
	public static void saveDescription(RemoteFileImportData importData) throws CoreException, IOException {
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
				// now have to check if a variant of this file exists (i.e.
				// whether a file exists
				// that has the same path with a different case. For case
				// insensitive file systems
				// such as Windows, this is needed since we can't simply create
				// a file with a different
				// case. Note that isAccessible() above does not check for file
				// paths with different case,
				// so we have to check it explicitly).
				IResource variant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(descriptionFile.getFullPath());
				// if a variant was not found, create the new file
				if (variant == null) {
					// check if a variant of the parent exists
					// we need to do this because at this point we know that the
					// file path does not
					// exist, and neither does its variant. However, it is
					// possible that the parent path
					// has a variant, in which case calling create on the
					// description file with
					// the path as it is given will fail. We need to get the
					// variant path of the parent,
					// append the name of the file to the variant path, and
					// create a file with that path.
					// get parent
					IResource parent = descriptionFile.getParent();
					if (parent != null) {
						// get parent path
						IResource parentVariant = RemoteImportExportUtil.getInstance().findExistingResourceVariant(parent.getFullPath());
						// no parent variant (i.e. in a case sensitive file
						// system)
						if (parentVariant == null) {
							descriptionFile.create(fileInput, true, null);
						}
						// parent variant found (might be same as original
						// parent path)
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
	// </Copied from org.eclipse.rse.internal.importexport.files.RemoteFileExportOperation>

	public static final String PLUGIN_ID = "org.eclipse.rse.internal.synchronize"; //$NON-NLS-1$
	// Repository provider name
	public static final String PROVIDER_ID = FileSystemProvider.class.getName();

}
