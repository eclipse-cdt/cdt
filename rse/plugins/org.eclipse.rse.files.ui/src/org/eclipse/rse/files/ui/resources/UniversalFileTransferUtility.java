/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Michael Scharf (Wind River) - Fix 163844: InvalidThreadAccess in checkForCollision
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [183824] Forward SystemMessageException from IRemoteFileSubsystem
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * Xuan Chen        (IBM)        - [187548] Editor shows incorrect file name after renaming file on Linux dstore
 * David McKnight   (IBM)        - [191472] should not use super transfer with SSH/FTP Folder Copy and Paste
 * Xuan Chen (IBM)               - [191367] with supertransfer on, Drag & Drop Folder from DStore to DStore doesn't work
 * Xuan Chen (IBM)               - [201790] [dnd] Copy and Paste across connections to a Drive doesn't work
 * Xuan Chen (IBM)               - [202668] [supertransfer] Subfolders not copied when doing first copy from dstore to Local
 * Xuan Chen (IBM)               - [202670] [supertransfer] After doing a copy to a directory that contains folders some folders name's display "deleted"
 * Xuan Chen (IBM)               - [202949] [archives] copy a folder from one connection to an archive file in a different connection does not work
 * David McKnight   (IBM)        - [205819] Need to use input stream copy when EFS files are the src
 * David McKnight   (IBM)        - [195285] mount path mapper changes
 * Kevin Doyle (IBM)	         - [203014] Copy/Paste Across Connections doesn't display Overwrite dialog when folder already exists
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * David McKnight   (IBM)        - [209375] new API copyRemoteResourcesToWorkspaceMultiple to optimize downloads
 * Rupen Mardirossian (IBM)      - [208435] added constructor to nested RenameRunnable class to take in names that are previously used as a parameter for multiple renaming instances, passed through check collision as well through overloading.
 * Xuan Chen          (IBM)      - [160775] [api] [breaking] [nl] rename (at least within a zip) blocks UI thread
 * David McKnight     (IBM)      - [203114] don't treat XML files specially (no hidden prefs for bin vs text)
 * David McKnight     (IBM)      - [209552] get rid of copy APIs to be clearer with download and upload
 * David McKnight     (IBM)      - [143503] encoding and isBinary needs to be stored in the IFile properties
 * Xuan Chen          (IBM)        - [191370] [dstore] supertransfer zip not deleted when canceling copy
 * Xuan Chen          (IBM)      - [210816] Archive testcases throw ResourceException if they are run in batch
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Rupen Mardirossian (IBM)      - [210682] Collisions when doing a copy operation across systems will us the SystemCopyDialog
 * Xuan Chen        (IBM)        - [229093] set charset of the temp file of the text remote file to its remote encoding
 * Rupen Mardirossian (IBM)      - [198728] downloadResourcesToWorkspace now creates empty folders for copying across connections via createEmptyFolders method
 * David McKnight     (IBM)      - [229610] [api] File transfers should use workspace text file encoding
 * Kevin Doyle		  (IBM)		 - [227391] Saving file in Eclipse does not update remote file
 * David McKnight     (IBM)      - [234924] [ftp][dnd][Refresh] Copy/Paste file from Package Explorer doesn't refresh folder
 * David McKnight     (IBM)      - [236723] UniversalFileTransferUtility..uploadResourcesFromWorkspace should query remote folder encoding
 * Radoslav Gerganov (ProSyst)   - [231428] [files] NPE on canceling copy operation from remote host
 * David McKnight     (IBM)      - [262092] Special characters are missing when pasting a file on a different connection
 * David McKnight     (IBM)      - [271831] Set the readonly file attribute when download the file
 * David McKnight     (IBM)      - [251136] Error copying local file to remote system when temp file is readonly
 * David McKnight   (IBM)        - [276103] Files with names in different cases are not handled properly
 * David McKnight     (IBM)      - [276534] Cache Conflict After Synchronization when Browsing Remote System with Case-Differentiated-Only Filenames
 * David McKnight     (IBM)      - [281712] [dstore] Warning message is needed when disk is full
 * David McKnight     (IBM)      - [234258] [dnd] Drag&Drop a folder silently ignores elements without permissions
 * David McKnight   (IBM)        - [299140] Local Readonly file can't be copied/pasted twice
 * David McKnight     (IBM)      - [298440] jar files in a directory can't be pasted to another system properly
 * David McKnight     (IBM)      - [311218] Content conflict dialog pops up when it should not
 * David McKnight     (IBM)      - [228743] [usability][dnd] Paste into read-only folder fails silently
 * David McKnight     (IBM)      - [376410] cross-system copy/paste operation doesn't transfer remote encodings for binary files
 * David McKnight     (IBM)      - [386486] when the original timestamp of a file is 0 don't set it after an upload
 * David McKnight   (IBM)        - [389838] Fast folder transfer does not account for code page
 * Samuel Wu        (IBM)        - [402533] UniversalFileTransferUtility threw NPE
 * David McKnight     (IBM)      - [413178] UniversalFileTransferUtility isn't setting read-only bit if encoding and timestamp are unchanged
 *******************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemRemoteResourceSet;
import org.eclipse.rse.core.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.files.ui.resources.SystemFileNameHelper;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.internal.ui.dialogs.CopyRunnable;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.clientserver.messages.SystemUnsupportedOperationException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.services.files.RemoteFolderNotEmptyException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for doing file transfers on universal systems.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UniversalFileTransferUtility {
	static final boolean doCompressedTransfer = true;//false;

	static final String _rootPath = SystemRemoteEditManager.getInstance().getRemoteEditProjectLocation().makeAbsolute().toOSString();

	public static class RenameStatus extends Status {

		private static final int CANCEL_ALL = 16;

		/**
		 * Creates a new RenameStatus object. The created status has no
		 * children.
		 *
		 * @param severity the severity; one of <code>OK</code>,
		 *            <code>ERROR</code>, <code>INFO</code>,
		 *            <code>WARNING</code>, or <code>CANCEL</code>
		 * @param pluginId the unique identifier of the relevant plug-in
		 * @param code the plug-in-specific status code, or <code>OK</code>
		 * @param message a human-readable message, localized to the current
		 *            locale
		 * @param exception a low-level exception, or <code>null</code> if not
		 *            applicable
		 */
		public RenameStatus(int severity, String pluginId, int code, String message, Throwable exception) {
			super(severity, pluginId, code, message, exception);
		}
	}

	/**
	 * Indicates whether super transfer should be used for a particular file transfer.  This will return true if both
	 * the preference for super transfer is turned on and the subsystem configuration supports archives
	 * @param subsystem the subsystem used to transfer files and folders
	 * @return true if super transfer should be used
	 */
	private static boolean doSuperTransfer(IRemoteFileSubSystem subsystem)
	{
		//boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.DOSUPERTRANSFER) &&
		boolean doSuperTransferProperty = false; // disabling due to potential corruption
		subsystem.getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();
		return doSuperTransferProperty;
	}

	/**
	 * Transfer a remote file or folder from one remote location to another.
	 * @param srcFileOrFolder the file or folder to copy
	 * @param tgtFolder the folder to copy to
	 * @param monitor the progress monitor
	 */
	public static void transferRemoteResource(IRemoteFile srcFileOrFolder, IRemoteFile tgtFolder, IProgressMonitor monitor)
	{
		Object tempSrc = downloadResourceToWorkspace(srcFileOrFolder, monitor);
		if (tempSrc instanceof IResource)
		{
			uploadResourceFromWorkspace((IResource) tempSrc, tgtFolder, monitor);
		}
	}

	private static boolean tempFileAvailable(IFile tempFile, IRemoteFile remoteFile) throws RemoteFileIOException
	{
		// before we make the transfer to the temp file check whether a temp file already exists
		if (tempFile.exists() && ((Resource)tempFile).getPropertyManager() != null)
		{
			SystemIFileProperties properties = new SystemIFileProperties(tempFile);
			
			String replicaRemoteFilePath = properties.getRemoteFilePath();
			String remoteFilePath = remoteFile.getAbsolutePath();
			
			if (!remoteFilePath.equals(replicaRemoteFilePath)){
				// this temp file is for a file of different case		
				Exception e = new Exception(FileResources.FILEMSG_CREATE_FILE_FAILED_EXIST);
				throw new RemoteFileIOException(e);
			}
		

			long storedModifiedStamp = properties.getRemoteFileTimeStamp();

			// compare timestamps
			if (storedModifiedStamp > 0)
			{
				// ;if they're the same, just use temp file
				long remoteModifiedStamp = remoteFile.getLastModified();

				boolean usedBin = properties.getUsedBinaryTransfer();
				boolean shouldUseBin = remoteFile.isBinary();

				// changed encodings matter too
				String remoteEncoding = remoteFile.getEncoding();
				String lastEncoding = properties.getEncoding();
				
						
				boolean usedReadOnly = properties.getReadOnly();
				boolean isReadOnly = !remoteFile.canWrite();
				
				if (storedModifiedStamp == remoteModifiedStamp &&
						usedBin == shouldUseBin &&
						remoteEncoding.equals(lastEncoding) &&						
						usedReadOnly == isReadOnly
						){					
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * replicates a remote file to the temp files project in the workspace
	 *
	 * @param srcFileOrFolder the file to copy
	 * @param monitor the progress monitor
	 * @return the resulting local replica, or <code>null</code> if the
	 *         operation was cancelled before the download was complete
	 * @since 3.0
	 */
	protected static IFile downloadFileToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{
		IRemoteFileSubSystem srcFS = srcFileOrFolder.getParentRemoteFileSubSystem();
		IResource tempResource = getTempFileFor(srcFileOrFolder);

		IFile tempFile = (IFile) tempResource;

		boolean available = true;
		try {
			available = tempFileAvailable(tempFile, srcFileOrFolder);
		}
		catch (RemoteFileIOException e){
			// this is the case where a temp file exists for a file of a different case
			// bug 276534
			SystemIFileProperties properties = new SystemIFileProperties(tempFile);

			Object obj = properties.getRemoteFileObject();
			if (obj != null && obj instanceof SystemEditableRemoteFile)
			{
				SystemEditableRemoteFile editable = (SystemEditableRemoteFile) obj;
				if (editable.checkOpenInEditor() != ISystemEditableRemoteObject.NOT_OPEN){								
					// editor open for this file
					// for now, best we may be able to do is just keep this one and warn
					String remotePath = editable.getAbsolutePath();
					String msgTxt = NLS.bind(FileResources.FILEMSG_COPY_FILE_FAILED, remotePath);
					String msgDetails = FileResources.FILEMSG_COPY_FILE_FAILED_DETAILS;

					final SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
							ISystemFileConstants.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR,
							IStatus.WARNING, msgTxt, msgDetails);

					runInDisplayThread(new Runnable() {
						public void run() {
							SystemMessageDialog dlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), message);
							dlg.open();
						}});
					return null;
				}
				else {
					// get rid of the current temp file
					try {
						tempFile.delete(true, monitor);
					}
					catch (CoreException ex){}
					tempResource = getTempFileFor(srcFileOrFolder);
					tempFile = (IFile) tempResource;
					
					available = false;				
				}
			}	
			else {
				// file not being edited, so overwrite it
				available = false;
			}
		}
		if (available){
			return tempFile;
		}

		try
		{
			// copy remote file to workspace
			SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
			listener.addIgnoreFile(tempFile);
			String remoteEncoding = srcFileOrFolder.getEncoding();
			srcFS.download(srcFileOrFolder, tempFile.getLocation().makeAbsolute().toOSString(), remoteEncoding, monitor);
			
			if (!tempFile.exists() && !tempFile.isSynchronized(IResource.DEPTH_ZERO))
			{
				// eclipse doesn't like this if the resource appears to be from another project
				try
				{
					//tempFile.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					tempFile.refreshLocal(IResource.DEPTH_ZERO, null/*monitor*/);
				}
				catch (Exception e)
				{

				}
			}
			if (tempFile.exists())
			{
				SystemIFileProperties properties = new SystemIFileProperties(tempFile);
				
				// set the appropriate readonly flag
				boolean readOnly = !srcFileOrFolder.canWrite();
				setReadOnly(tempFile, readOnly);
		
				// set file properties
				properties.setRemoteFileTimeStamp(srcFileOrFolder.getLastModified());
				properties.setDownloadFileTimeStamp(tempFile.getLocation().toFile().lastModified());
				properties.setReadOnly(readOnly);
				properties.setDirty(false);
	
				if (remoteEncoding != null)
				{
					if (srcFileOrFolder.isBinary())
					{
						if (!tempFile.isSynchronized(IResource.DEPTH_ZERO))
						{
							tempFile.refreshLocal(IResource.DEPTH_ZERO, null/*monitor*/);
						}
						if (!tempFile.getCharset().equals(remoteEncoding))
						{
							tempFile.setCharset(remoteEncoding, null);
						}
					}
					else
					{
						// using text mode so the char set needs to be local						
						if (properties.getLocalEncoding() != null){
							String localEncoding = properties.getLocalEncoding();
							tempFile.setCharset(localEncoding, null);
						}
						// otherwise, the default charset is inherited so no need to set
					}
				}
			}
			listener.removeIgnoreFile(tempFile);
		}
		catch (SystemOperationCancelledException soce) {
			return null;
		}
		catch (final SystemMessageException e)
		{
			runInDisplayThread(new Runnable() {
				public void run() {
					SystemMessageDialog dlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), e.getSystemMessage());
					dlg.open();
				}});
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return (IFile) tempResource;
	}

	protected static void setIFileProperties(IFile tempFile, IRemoteFile remoteFile, IRemoteFileSubSystem subSystem)
	{
		// set it's properties for use later
		SystemIFileProperties properties = new SystemIFileProperties(tempFile);

		// set remote properties
		properties.setRemoteFileTimeStamp(remoteFile.getLastModified());
		properties.setDirty(false);

		String remotePath = remoteFile.getAbsolutePath();

		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		String subSystemId = registry.getAbsoluteNameForSubSystem(subSystem);
		properties.setRemoteFileSubSystem(subSystemId);
		properties.setRemoteFilePath(remotePath);


		properties.setEncoding(remoteFile.getEncoding());
		properties.setUsedBinaryTransfer(remoteFile.isBinary());

		// get the modified timestamp from the File, not the IFile
		// for some reason, the modified timestamp from the IFile does not always return
		// the right value. There is a Javadoc comment saying the value from IFile might be a
		// cached value and that might be the cause of the problem.
		properties.setDownloadFileTimeStamp(tempFile.getLocation().toFile().lastModified());

		boolean isMounted = isRemoteFileMounted(subSystem, remotePath);
		properties.setRemoteFileMounted(isMounted);
		if (isMounted)
		{
			String actualRemoteHost = getActualHostFor(subSystem, remotePath);
			String actualRemotePath = getWorkspaceRemotePath(subSystem, remotePath);
			properties.setResolvedMountedRemoteFileHost(actualRemoteHost);
			properties.setResolvedMountedRemoteFilePath(actualRemotePath);
		}
	}

	/**
	 * Used for local files - special case!
	 * @param tempFile
	 * @param remoteFile
	 * @param hostname
	 * @param userId
	 * @since 3.0
	 */
	protected static void setIFileProperties(IFile tempFile, File remoteFile, String hostname, String userId)
	{
		// set it's properties for use later
		SystemIFileProperties properties = new SystemIFileProperties(tempFile);

		// set remote properties
		properties.setRemoteFileTimeStamp(remoteFile.lastModified());
		properties.setDirty(false);

		String remotePath = remoteFile.getAbsolutePath();
		properties.setRemoteFilePath(remotePath);
		try
		{
			properties.setEncoding(tempFile.getCharset());
		}
		catch (CoreException e){
		}


		// get the modified timestamp from the File, not the IFile
		// for some reason, the modified timestamp from the IFile does not always return
		// the right value. There is a Javadoc comment saying the value from IFile might be a
		// cached value and that might be the cause of the problem.
		properties.setDownloadFileTimeStamp(tempFile.getLocation().toFile().lastModified());

		boolean isMounted = isRemoteFileMounted(hostname, remotePath, null); // no subsystem
		properties.setRemoteFileMounted(isMounted);
		if (isMounted)
		{
			String actualRemoteHost = getActualHostFor(hostname, remotePath, null);	// no subsystem
			String actualRemotePath = getWorkspaceRemotePath(hostname, remotePath, null); // no subsystem
			properties.setResolvedMountedRemoteFileHost(actualRemoteHost);
			properties.setResolvedMountedRemoteFilePath(actualRemotePath);
		}
	}

	/**
	 * This method downloads a set of remote resources to the workspace. It uses
	 * the downloadMultiple() API of the remote file subsystem and service
	 * layers so for some service implementations, this is a big optimization
	 * 
	 * @param remoteSet the set of resources to download
	 * @param monitor the progress monitor
	 * @return the set of temporary files created as a result of the download.
	 *         This may contain fewer files than requested in case the operation
	 *         was cancelled.
	 * @since 3.0
	 */
	public static SystemWorkspaceResourceSet downloadResourcesToWorkspaceMultiple(SystemRemoteResourceSet remoteSet, IProgressMonitor monitor)
	{
		IContainer broadestContainer = null;
		SystemWorkspaceResourceSet resultSet = new SystemWorkspaceResourceSet();
		List set = remoteSet.getResourceSet();
		IRemoteFileSubSystem srcFS = (IRemoteFileSubSystem)remoteSet.getSubSystem();

		SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();


		List remoteFilesForDownload = new ArrayList();
		List tempFilesForDownload = new ArrayList();
		List remoteEncodingsForDownload = new ArrayList();
		List emptyFolders = new ArrayList();

		// step 1: pre-download processing
		for (int i = 0; i < set.size() && !resultSet.hasMessage(); i++){

			if (monitor != null && monitor.isCanceled())
			{
				return resultSet;
			}

			IRemoteFile srcFileOrFolder = (IRemoteFile)set.get(i);
			// first check for existence
			if (!srcFileOrFolder.exists()){
				String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, srcFileOrFolder.getAbsolutePath(), srcFS.getHostAliasName());

				SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
						IStatus.ERROR, msgTxt);
				resultSet.setMessage(errorMessage);

			}
			else
			{
				if (srcFileOrFolder.isFile()) // file transfer only
				{
					IResource tempResource = getTempFileFor(srcFileOrFolder);

					IFile tempFile = (IFile) tempResource;

					boolean problem = false;
					boolean available = true;
					try {		
						available =	tempFileAvailable(tempFile, srcFileOrFolder);
					}
					catch (RemoteFileIOException e){
						// this is the case where a temp file exists for a file of a different case
						// bug 276534
						SystemIFileProperties properties = new SystemIFileProperties(tempFile);

						Object obj = properties.getRemoteFileObject();
						if (obj != null && obj instanceof SystemEditableRemoteFile)
						{
							SystemEditableRemoteFile editable = (SystemEditableRemoteFile) obj;
							if (editable.checkOpenInEditor() != ISystemEditableRemoteObject.NOT_OPEN){								
								// editor open for this file
								// for now, best we may be able to do is just keep this one and warn
								String remotePath = srcFileOrFolder.getAbsolutePath();
								String msgTxt = NLS.bind(FileResources.FILEMSG_COPY_FILE_FAILED, remotePath);
								String msgDetails = FileResources.FILEMSG_COPY_FILE_FAILED_DETAILS;
								SystemMessage message = new SimpleSystemMessage(Activator.PLUGIN_ID,
										ISystemFileConstants.MSG_DOWNLOAD_ALREADY_OPEN_IN_EDITOR,
										IStatus.WARNING, msgTxt, msgDetails);
	
								resultSet.setMessage(message);
								problem = true;
							}
							else {
								// get rid of the current temp file
								try {
									tempFile.delete(true, monitor);
								}
								catch (CoreException ex){}
								tempResource = getTempFileFor(srcFileOrFolder);
								tempFile = (IFile) tempResource;
							}
							available = false;
						}	
						else {
							// file not being edited, so overwrite it
							available = false;
						}
					}
					
					if (available){
						resultSet.addResource(tempFile);
					}
					else if (!problem){
						listener.addIgnoreFile(tempFile);

						remoteFilesForDownload.add(srcFileOrFolder);
						tempFilesForDownload.add(tempFile);
						remoteEncodingsForDownload.add(srcFileOrFolder.getEncoding());

						IContainer parent = tempFile.getParent();
						if (broadestContainer == null || parent.contains(broadestContainer)){
							broadestContainer = parent;
						}
						else {
							if (!broadestContainer.contains(parent)) { // siblings?
								broadestContainer = broadestContainer.getParent();
							}
						}
					}
				}
				else if (srcFileOrFolder.isDirectory()) // recurse for empty folders and add to our consolidated resource set
				{
					IResource tempFolder = getTempFileFor(srcFileOrFolder);
					try
					{
						//get contents of folder
						IRemoteFile[] children = srcFS.list(srcFileOrFolder,IFileService.FILE_TYPE_FILES_AND_FOLDERS,monitor);
						//check for empty folder and add to set
						if(children==null || children.length==0)
						{
							emptyFolders.add(tempFolder);
						}
						//get all subfolders						
						children=srcFS.list(srcFileOrFolder, IFileService.FILE_TYPE_FOLDERS, monitor);
						
						if(!(children==null) && !(children.length==0))
						{
							// make sure children are not archives!
							ArrayList fcs = new ArrayList();
							for (int c = 0; c < children.length; c++){
								IRemoteFile child = children[c];
								if (!child.isArchive()){
									fcs.add(child);								
								}
							}
							if (fcs.size() > 0){
								
								SystemRemoteResourceSet childSet = new SystemRemoteResourceSet(srcFS, fcs);
													
								//recurse with subfolders to check for empty folders
								SystemWorkspaceResourceSet childResults = downloadResourcesToWorkspaceMultiple(childSet, monitor);
								if (childResults.hasMessage())
								{
									resultSet.setMessage(childResults.getMessage());
								}
								resultSet.addResource(tempFolder);
							}
						}
					}
					catch (SystemMessageException e)
					{
						SystemBasePlugin.logError(e.getMessage(), e);
						SystemMessageDialog.displayMessage(e);
					}
				}
			}
		}

		// step 2: downloading
		IRemoteFile[] sources = (IRemoteFile[])remoteFilesForDownload.toArray(new IRemoteFile[remoteFilesForDownload.size()]);

		String[] encodings = (String[])remoteEncodingsForDownload.toArray(new String[remoteEncodingsForDownload.size()]);

		// destinations
		String[] destinations = new String[remoteFilesForDownload.size()];
		for (int t = 0; t < tempFilesForDownload.size(); t++){
			IFile destFile = (IFile)tempFilesForDownload.get(t);
			
			// make sure the file isn't read-only during the download
			if (destFile.isReadOnly()){
				setReadOnly(destFile, false);
			}
			
			destinations[t] = destFile.getLocation().toOSString();
		}

		if (sources.length > 0){
			try {
				srcFS.downloadMultiple(sources, destinations, encodings, monitor);
			}
			catch (SystemMessageException e){
				resultSet.setMessage(e.getSystemMessage());
			}
		}

		// step 2.1: refresh the broadest container (keep it down to 1 big refresh)
		try
		{
			if (broadestContainer != null && !broadestContainer.isSynchronized(IResource.DEPTH_INFINITE)){
				broadestContainer.refreshLocal(IResource.DEPTH_INFINITE, null);//monitor);
			}
		}
		catch (Exception e)
		{

		}
		//Create empty folders
		try
		{
			createEmptyFolders(monitor, emptyFolders);
		}
		catch(CoreException e)
		{
			SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.FILEMSG_CREATE_FILE_FAILED,
					IStatus.ERROR, FileResources.FILEMSG_CREATE_FILE_FAILED, e);
			resultSet.setMessage(errorMessage);
		}

		// step 3: post download processing
		if (!resultSet.hasMessage())
		{

			for (int p = 0; p < remoteFilesForDownload.size(); p++) {

				IRemoteFile srcFileOrFolder = (IRemoteFile)remoteFilesForDownload.get(p);
				IFile tempFile = (IFile)tempFilesForDownload.get(p);
				resultSet.addResource(tempFile);
				String remoteEncoding = (String)remoteEncodingsForDownload.get(p);
				listener.removeIgnoreFile(tempFile);

				SystemIFileProperties properties = new SystemIFileProperties(tempFile);
				long storedTime = properties.getRemoteFileTimeStamp();
				long currentTime = srcFileOrFolder.getLastModified();
				String storedEncoding = properties.getEncoding();
				String currentEncoding = srcFileOrFolder.getEncoding();

				if (tempFile.exists())
				{
					// set the appropriate readonly flag
					boolean readOnly = !srcFileOrFolder.canWrite();
					setReadOnly(tempFile, readOnly);
					if (storedTime != currentTime || (storedEncoding == null || !storedEncoding.equals(currentEncoding)))
					{				
						// deal with encoding properties
						if (storedEncoding == null || !storedEncoding.equals(currentEncoding)){							
							try
							{
								if (remoteEncoding != null)
								{
									if (srcFileOrFolder.isBinary())
									{
										if (!tempFile.isSynchronized(IResource.DEPTH_ZERO))
										{
											tempFile.refreshLocal(IResource.DEPTH_ZERO, null/*monitor*/);
										}
										if (!tempFile.getCharset().equals(remoteEncoding))
										{
											tempFile.setCharset(remoteEncoding, null);
										}
									}
									else
									{
										// using text mode so the char set needs to be local
										if (properties.getLocalEncoding() != null){
											String localEncoding = properties.getLocalEncoding();
											tempFile.setCharset(localEncoding, null);
										}
										// otherwise, the default charset is inherited so no need to set
									}
								}
							}
							catch (Exception e)
							{
								SimpleSystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
												ICommonMessageIds.MSG_OPERATION_FAILED,
												IStatus.ERROR, "", e);  //$NON-NLS-1$
								resultSet.setMessage(errorMessage);
								return null;
							}
						}

						try
						{
							// set all properties
							setIFileProperties(tempFile, srcFileOrFolder, srcFS);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		return resultSet;
	}

	private static void createEmptyFolders(IProgressMonitor monitor, List emptyFolders) throws CoreException
	{
		IContainer empty;
		IFolder emptyFolder;
		List emptyParent;
		boolean go=false;
		for(int i=0; i<emptyFolders.size();i++)
		{
			emptyParent = new ArrayList();
			empty = (IContainer) emptyFolders.get(i);
			go=true;
			//check to see which parent folders need to be created
			while(go)
			{
				empty = empty.getParent();
				if(!empty.exists() && empty instanceof IFolder)
				{
					emptyParent.add(empty);
				}
				else
				{
					go=false;
				}
			}
			//create empty parent folders
			for(int j=emptyParent.size()-1;j>=0;j--)
			{
				emptyFolder = (IFolder) emptyParent.get(j);
				if(!emptyFolder.exists())
				{
					emptyFolder.create(true, true, monitor);
				}
			}
			//create empty folders
			emptyFolder = (IFolder) emptyFolders.get(i);
			if(!emptyFolder.exists())
			{
				emptyFolder.create(true, true, monitor);
			}
		}
	}


	/**
	 * Replicates a set of remote files or folders to the workspace
	 * @param remoteSet the objects which are being copied
	 * @param monitor a progress monitor
	 * @return the temporary objects that was created after the download
	 * @since 3.0
	 */
	public static SystemWorkspaceResourceSet downloadResourcesToWorkspace(SystemRemoteResourceSet remoteSet, IProgressMonitor monitor)
	{
		boolean ok = true;
		SystemWorkspaceResourceSet resultSet = new SystemWorkspaceResourceSet();
		IRemoteFileSubSystem srcFS = (IRemoteFileSubSystem)remoteSet.getSubSystem();

		if (!srcFS.isConnected())
		{
			return null;
		}

		boolean doSuperTransferProperty = doSuperTransfer(srcFS);

		List set = remoteSet.getResourceSet();
		List emptyFolders = new ArrayList();

		for (int i = 0; i < set.size() && !resultSet.hasMessage(); i++)
		{
			if (monitor != null && monitor.isCanceled())
			{
				return resultSet;
			}


			IRemoteFile srcFileOrFolder = (IRemoteFile)set.get(i);
			if (!srcFileOrFolder.exists())
			{
				String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, srcFileOrFolder.getAbsolutePath(), srcFS.getHostAliasName());

				SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
						ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
						IStatus.ERROR, msgTxt);
				resultSet.setMessage(errorMessage);

			}
			else
			{
				if (srcFileOrFolder.isFile()) // file transfer
				{

					IFile tempFile = downloadFileToWorkspace(srcFileOrFolder, monitor);
					if (monitor != null && monitor.isCanceled())
					{
						return resultSet;
					}
					resultSet.addResource(tempFile);
				}
				else // folder transfer
				{
					IResource tempFolder = null;

					if (doCompressedTransfer && doSuperTransferProperty && !srcFileOrFolder.isRoot()
							&& !(srcFileOrFolder.getParentRemoteFileSubSystem().getHost().getSystemType().isLocal()))
					{
						try
						{
							tempFolder = compressedDownloadToWorkspace(srcFileOrFolder, monitor);
						}
						catch (Exception e)
						{
							e.printStackTrace();
							ok = false;
						}
						ok = tempFolder != null;
						if (ok)
						{
							resultSet.addResource(tempFolder);
						}
					}
					else
					{
						tempFolder = getTempFileFor(srcFileOrFolder);
						try
						{
							IRemoteFile[] children = srcFS.list(srcFileOrFolder,monitor);
							//check for empty folder and add to set
							if(children==null || children.length==0)
							{
								emptyFolders.add(tempFolder);
							}

							SystemRemoteResourceSet childSet = new SystemRemoteResourceSet(srcFS, children);
							SystemWorkspaceResourceSet childResults = downloadResourcesToWorkspace(childSet, monitor);
							if (childResults.hasMessage())
							{
								resultSet.setMessage(childResults.getMessage());
							}
							resultSet.addResource(tempFolder);
						}
						catch (SystemMessageException e)
						{
							SystemBasePlugin.logError(e.getMessage(), e);
							SystemMessageDialog.displayMessage(e);
						}
					}
				}
			}
		}

		//Create empty folders
		try
		{
			createEmptyFolders(monitor, emptyFolders);
		}
		catch(CoreException e)
		{
			SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.FILEMSG_CREATE_FILE_FAILED,
					IStatus.ERROR, FileResources.FILEMSG_CREATE_FILE_FAILED, e);
			resultSet.setMessage(errorMessage);
		}

		// refresh and set IFile properties
		for (int r = 0; r < resultSet.size(); r++)
		{
			IResource tempResource = (IResource)resultSet.get(r);
			IRemoteFile rmtFile = (IRemoteFile)remoteSet.get(r);

			if (tempResource != null && !tempResource.exists()) // need to check for null resource
				// because it's possible to be null when the download fails
			{
				// refresh temp file in project
				try
				{
					tempResource.refreshLocal(IResource.DEPTH_ONE, null /*monitor*/);
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
			if (tempResource instanceof IFile)
			{
				try
				{
					setIFileProperties((IFile)tempResource, rmtFile, srcFS);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			/*
			else
			{
				// refresh temp file in project
				try
				{
					tempResource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
			}
			 */
		}


		return resultSet;
	}

	public static void discardReplicasOfDeletedFiles(IRemoteFileSubSystem ss, IContainer folder)
	{
		try
		{
			IResource[] members = folder.members();
			for (int i = members.length -1; i >= 0; i--)
			{
				IResource member = members[i];
				if (member instanceof IFile)
				{
					// is this a valid replica?
					SystemIFileProperties properties = new SystemIFileProperties(member);
					String path = properties.getRemoteFilePath();
					if (path != null)
					{
						IRemoteFile remoteFile = null;
						if (ss instanceof RemoteFileSubSystem)
						{
							// mark any cached remote file stale so we know for sure
							remoteFile = ((RemoteFileSubSystem)ss).getCachedRemoteFile(path);
							if (remoteFile != null)
							{
								remoteFile.markStale(true);
							}
						}
						remoteFile = ss.getRemoteFileObject(path, new NullProgressMonitor());
						if (remoteFile != null && !remoteFile.exists())
						{
							// this must be old so we should delete this
							member.delete(true, new NullProgressMonitor());
						}
					}
				}
				else if (member instanceof IContainer)
				{
					discardReplicasOfDeletedFiles(ss, (IContainer)member);
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * @since 3.0
	 */
	public static Object downloadResourceToWorkspace(File srcFileOrFolder, IProgressMonitor monitor) {

		if (!srcFileOrFolder.exists()) {
			String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, srcFileOrFolder.getAbsolutePath(), "LOCALHOST"); //$NON-NLS-1$

			SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
					IStatus.ERROR, msgTxt);
			return errorMessage;
		}

		if (srcFileOrFolder.isFile()) {
			IFile tempFile = downloadFileToWorkspace(srcFileOrFolder, monitor);

			if (!tempFile.exists())
			{
				// refresh temp file in project
				try
				{
					if (PlatformUI.isWorkbenchRunning())
					{
						if (!tempFile.isSynchronized(IResource.DEPTH_ZERO))
							tempFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
					}
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			tempFile = (IFile) getTempFileFor(srcFileOrFolder);
			if (tempFile.exists())
			{
				try
				{
					setIFileProperties(tempFile, srcFileOrFolder, "LOCALHOST", System.getProperty("user.name"));		 //$NON-NLS-1$ //$NON-NLS-2$
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return tempFile;
		}
		else {
			return null;
		}
	}

	/**
	 * Replicates a local file to the temporary files project in the workspace.
	 * @param srcFileOrFolder the file to copy.
	 * @param monitor the progress monitor.
	 * @return the resulting local replica.
	 * @since 3.0
	 */
	protected static IFile downloadFileToWorkspace(File srcFileOrFolder, IProgressMonitor monitor)
	{
		IResource tempResource = getTempFileFor(srcFileOrFolder);

		IFile tempFile = (IFile) tempResource;

		// before we make the transfer to the temp file check whether a temp file already exists
		if (tempFile.exists())
		{
			SystemIFileProperties properties = new SystemIFileProperties(tempFile);

			long storedModifiedStamp = properties.getRemoteFileTimeStamp();

			// compare timestamps
			if (storedModifiedStamp > 0)
			{
				// if they're the same, just use temp file
				long remoteModifiedStamp = srcFileOrFolder.lastModified();

				boolean usedBin = properties.getUsedBinaryTransfer();
				boolean shouldUseBin = RemoteFileUtility.getSystemFileTransferModeRegistry().isBinary(srcFileOrFolder);
				if (storedModifiedStamp == remoteModifiedStamp && (usedBin == shouldUseBin))
				{
					// set the appropriate readonly flag
					boolean readOnly = !srcFileOrFolder.canWrite();
					setReadOnly(tempFile, readOnly);
					
					return tempFile;
				}
			}
		}

		try
		{
			// copy remote file to workspace
			SystemUniversalTempFileListener listener = SystemUniversalTempFileListener.getListener();
			listener.addIgnoreFile(tempFile);
			String encoding = tempFile.getParent().getDefaultCharset();

			download(srcFileOrFolder, tempFile, encoding, monitor);
			listener.removeIgnoreFile(tempFile);
			if (!tempFile.exists() && !tempFile.isSynchronized(IResource.DEPTH_ZERO))
			{
				// eclipse doesn't like this if the resource appears to be from another project
				try
				{
					tempFile.refreshLocal(IResource.DEPTH_ZERO, null);
				}
				catch (Exception e)
				{

				}
			}
			if (tempFile.exists())
			{
				// set the appropriate readonly flag
				boolean readOnly = !srcFileOrFolder.canWrite();
				setReadOnly(tempFile, readOnly);
				
				if (RemoteFileUtility.getSystemFileTransferModeRegistry().isText(srcFileOrFolder))
				{
					try
					{
						if (!tempFile.isSynchronized(IResource.DEPTH_ZERO))
						{
							tempFile.refreshLocal(IResource.DEPTH_ZERO, null/*monitor*/);
						}
						String cset = tempFile.getCharset();
						if (!cset.equals(encoding))
						{
							tempFile.setCharset(encoding, monitor);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e)
		{
			SystemBasePlugin.logError("An exception occured " + e.getMessage(), e); //$NON-NLS-1$
			return null;
		}

		return (IFile)tempResource;
	}

	protected static boolean download(File file, IFile tempFile, String hostEncoding, IProgressMonitor monitor) {

		FileInputStream inputStream = null;
		BufferedInputStream bufInputStream = null;
		FileOutputStream outputStream = null;
		BufferedOutputStream bufOutputStream = null;
		OutputStreamWriter outputWriter = null;
		BufferedWriter bufWriter = null;
		boolean isCancelled = false;

		File destinationFile = tempFile.getLocation().toFile();

		try
		{

			if (!destinationFile.exists())
			{
				File parentDir = destinationFile.getParentFile();
				parentDir.mkdirs();
			}
			else {
				// make sure the temp file can be written to
				if (tempFile.isReadOnly()){
					setReadOnly(tempFile, false);
				}
			}

			// encoding conversion required if it a text file but not an xml file
			boolean isBinary = RemoteFileUtility.getSystemFileTransferModeRegistry().isBinary(file);
			boolean isEncodingConversionRequired = !isBinary;

			inputStream = new FileInputStream(file);
			bufInputStream = new BufferedInputStream(inputStream);
			outputStream = new FileOutputStream(destinationFile);

			if (isEncodingConversionRequired)
			{
				outputWriter = new OutputStreamWriter(outputStream, hostEncoding);
				bufWriter = new BufferedWriter(outputWriter);
			}
			else
			{
				bufOutputStream = new BufferedOutputStream(outputStream);
			}


			byte[] buffer = new byte[512000];
			long totalSize = file.length();
			int totalRead = 0;

			while (totalRead < totalSize && !isCancelled)
			{

				int available = bufInputStream.available();
				available = (available < 512000) ? available : 512000;

				int bytesRead = bufInputStream.read(buffer, 0, available);

				if (bytesRead == -1) {
					break;
				}

				// need to convert encoding, i.e. text file, but not xml
				// ensure we read in file using the encoding for the file system
				// which can be specified by user as text file encoding in preferences
				if (isEncodingConversionRequired)
				{
					String s = new String(buffer, 0, bytesRead, hostEncoding);
					if (bufWriter != null)
						bufWriter.write(s);
				}
				else
				{
					if (bufOutputStream != null)
						bufOutputStream.write(buffer, 0, bytesRead);
				}

				totalRead += bytesRead;

				if (monitor != null)
				{
					monitor.worked(bytesRead);
					isCancelled = monitor.isCanceled();
				}
			}
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{

			try
			{
				if (bufWriter != null)
					bufWriter.close();

				if (bufInputStream != null)
					bufInputStream.close();

				if (bufOutputStream != null)
					bufOutputStream.close();

				if (isCancelled)
				{
					return false;
				}
				else if (destinationFile != null && file.exists()) {
					destinationFile.setLastModified(file.lastModified());

					if (destinationFile.length() != file.length()) {
						return false;
					}
				}
			}
			catch (IOException e)
			{
			}
		}

		return true;
	}

	/**
	 * Replicates a remote file or folder to the workspace
	 *
	 * @param srcFileOrFolder the object which is being copied
	 * @param monitor a progress monitor
	 * @return the temporary object that was created after the download
	 * @since 3.0
	 */
	public static Object downloadResourceToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{

		boolean ok = true;

		IRemoteFileSubSystem srcFS = srcFileOrFolder.getParentRemoteFileSubSystem();

		if (!srcFS.isConnected())
		{
			return null;
		}
		if (!srcFileOrFolder.exists())
		{
			String msgTxt = NLS.bind(FileResources.MSG_ERROR_FILE_NOTFOUND, srcFileOrFolder.getAbsolutePath(), srcFS.getHostAliasName());

			SystemMessage errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.MSG_ERROR_FILE_NOTFOUND,
					IStatus.ERROR, msgTxt);
			return errorMessage;
		}

		if (srcFileOrFolder.isFile())
		{
			IFile tempFile = downloadFileToWorkspace(srcFileOrFolder, monitor);

			if (!tempFile.exists())
			{
				// refresh temp file in project
				try
				{
					if (PlatformUI.isWorkbenchRunning())
					{
						if (!tempFile.isSynchronized(IResource.DEPTH_ZERO))
							tempFile.refreshLocal(IResource.DEPTH_ZERO, monitor);
					}
				}
				catch (CoreException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			tempFile = (IFile) getTempFileFor(srcFileOrFolder);
			if (tempFile.exists() && ((Resource)tempFile).getPropertyManager() != null)
			{
				try
				{
					setIFileProperties(tempFile, srcFileOrFolder, srcFS);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return tempFile;
		}
		else
		{
			IResource tempFolder = null;

			boolean doSuperTransferProperty = doSuperTransfer(srcFileOrFolder.getParentRemoteFileSubSystem());

			if (doCompressedTransfer && doSuperTransferProperty && !srcFileOrFolder.isRoot()
					&& !(srcFileOrFolder.getParentRemoteFileSubSystem().getHost().getSystemType().isLocal()))
			{
				try
				{
					tempFolder = compressedDownloadToWorkspace(srcFileOrFolder, monitor);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					ok = false;
				}
				ok = tempFolder != null;
				if (ok)
				{
					return tempFolder;
				}
			}
			else
			{
				tempFolder = getTempFileFor(srcFileOrFolder);
				IRemoteFile[] children = null;
				try
				{
					children = srcFS.list(srcFileOrFolder, monitor);
				}
				catch (SystemMessageException e)
				{
					SystemBasePlugin.logError(e.getMessage(), e);
					SystemMessageDialog.displayMessage(e);
				}
				IResource[] childResources = null;

				if (children != null)
				{
					childResources = new IResource[children.length];
					if (children.length == 0)
					{
						File tempFolderFile = tempFolder.getLocation().toFile();
						tempFolderFile.mkdirs();
					}

					for (int i = 0; i < children.length && ok; i++)
					{
						IRemoteFile child = children[i];
						IResource childResource = null;
						if (child.isFile())
						{
							childResource = downloadFileToWorkspace(child, monitor);
						}
						else
						{
							childResource = (IResource) downloadResourceToWorkspace(child, monitor);
						}
						if (childResource == null)
						{
							ok = false;
						}
						if (monitor != null && monitor.isCanceled())
						{
							ok = false;
						}
						childResources[i] = childResource;
					}
				}

				if (ok)
				{
					refreshResourceInWorkspace(tempFolder);

					// set properties of files
					if (tempFolder.exists() && children != null && childResources != null)
					{
						for (int i = 0; i < childResources.length; i++)
						{
							IResource tempFile = childResources[i];

							if (tempFile.exists() && tempFile instanceof IFile)
							{
								IRemoteFile child = children[i];
								setIFileProperties((IFile)tempFile, child, srcFS);
							}
						}
					}

					return tempFolder;
				}
			}
		}

		return null;
	}

	/**
	 * Helper method to get the local file subsystem.
	 * @return the local file subsystem
	 */
	private static IRemoteFileSubSystem getLocalFileSubSystem()
	{
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			IRemoteFileSubSystem anFS = RemoteFileUtility.getFileSubSystem(connection);
			if (anFS.getHost().getSystemType().isLocal())
			{
				return anFS;
			}
		}

		return null;
	}


	/**
	 * Perform a copy via drag and drop.
	 * @param srcFileOrFolder the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @return the resulting remote object
	 * @since 3.0
	 */
	public static Object uploadResourceFromWorkspace(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor)
	{
		return uploadResourceFromWorkspace(srcFileOrFolder, targetFolder, monitor, true);
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param workspaceSet the objects to be copied.  If the target and sources are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @param checkForCollisions indicates whether to check for colllisions or not
	 * @return the resulting remote objects
	 * @since 3.0
	 */
	public static SystemRemoteResourceSet uploadResourcesFromWorkspace(SystemWorkspaceResourceSet workspaceSet, IRemoteFile targetFolder, IProgressMonitor monitor, boolean checkForCollisions)
	{


		IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();
		boolean isWindows = !targetFS.getParentRemoteFileSubSystemConfiguration().isUnixStyle();
		boolean doSuperTransferPreference = doSuperTransfer(targetFS);
		SystemRemoteResourceSet resultSet = new SystemRemoteResourceSet(targetFS);

		if (targetFolder.isStale())
		{
			try
			{
				IRemoteFile currentTargetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath(), monitor);
				if (currentTargetFolder != null)
					targetFolder = currentTargetFolder;
			}
			catch (Exception e)
			{
			}
		}

		if (!targetFolder.canWrite() && !isWindows) // windows check for bug 228743
		{
			String msgTxt = FileResources.FILEMSG_SECURITY_ERROR;
			String msgDetails = NLS.bind(FileResources.FILEMSG_SECURITY_ERROR_DETAILS, targetFS.getHostAliasName());

			SystemMessage errorMsg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.FILEMSG_SECURITY_ERROR,
					IStatus.ERROR, msgTxt, msgDetails);
			resultSet.setMessage(errorMsg);

			return resultSet;
		}

		if (!targetFS.isConnected())
		{
			return null;
		}
		boolean isTargetArchive = targetFolder.isArchive();
		boolean isTargetVirtual = ArchiveHandlerManager.isVirtual(targetFolder.getAbsolutePath());
		if (isTargetArchive && !targetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement()) return null;
		StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
		if (isTargetArchive)
		{
			newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		}
		else if (isTargetVirtual)
		{
			//if the target is a virtual folder, we need to append ArchiveHandlerManager.VIRTUAL_FOLDER_SEPARATOR
			//instead of the file separator of the file subsystem.
			newPathBuf.append(ArchiveHandlerManager.VIRTUAL_FOLDER_SEPARATOR);
		}
		else
		{
			int newPathBufLenth = newPathBuf.length();
			if (newPathBufLenth > 0 && !((newPathBuf.charAt(newPathBufLenth - 1) == targetFolder.getSeparatorChar())))
			{
				newPathBuf.append(targetFolder.getSeparatorChar());
			}
		}

		List resources = workspaceSet.getResourceSet();
		List newFilePathList = new ArrayList();


		// query what we're going to create
		for (int n = 0; n < resources.size(); n++)
		{
			IResource srcFileOrFolder = (IResource)resources.get(n);
			newFilePathList.add(newPathBuf.toString() + srcFileOrFolder.getName());
		}
		// one big query
		SystemRemoteResourceSet existingFiles = null;
		try
		{
			String[] folderAndFilePaths = (String[])newFilePathList.toArray(new String[newFilePathList.size()]);
			IRemoteFile[] results = targetFS.getRemoteFileObjects(folderAndFilePaths, monitor);
			existingFiles = new SystemRemoteResourceSet(targetFS, results);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


		// clear the list so that next time we use renamed names
		newFilePathList.clear();
		//List toCopyNames = new ArrayList(); //was used for rename operation (no longer needed)
		List copyFilesOrFolders = new ArrayList();
		List existingFilesOrFolders = new ArrayList();

		for (int i = 0; i < resources.size() && !resultSet.hasMessage(); i++)
		{
			if (monitor != null && monitor.isCanceled())
			{
				try
				{
					IRemoteFile[] results = targetFS.getRemoteFileObjects((String[])newFilePathList.toArray(new String[newFilePathList.size()]), monitor);
					resultSet = new SystemRemoteResourceSet(targetFS, results);
					if (workspaceSet.hasMessage())
					{
						resultSet.setMessage(workspaceSet.getMessage());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return resultSet;
			}


			IResource srcFileOrFolder = (IResource)resources.get(i);
			String name = srcFileOrFolder.getName();

			if (srcFileOrFolder instanceof IFile)
			{
				String oldPath = newPathBuf.toString() + name;
				if (checkForCollisions)
				{
					if(existingFiles!=null)
					{
						if(checkForCollision(existingFiles, targetFolder, oldPath))
						{
							existingFilesOrFolders.add(existingFiles.get(oldPath));
						}
					}
					//below code is used for renaming operation, which is no longer needed
					/*int severity = status.getSeverity();

					if (severity == IStatus.OK) {
						name = status.getMessage();
						toCopyNames.add(name);
					}
					else if (severity == IStatus.CANCEL) {

						int code = status.getCode();

						if (code == IStatus.CANCEL) {
							continue;
						}
						else if (code == RenameStatus.CANCEL_ALL) {
							break;
						}
					}*/
				}
				copyFilesOrFolders.add(srcFileOrFolder);
			}

			else if (srcFileOrFolder instanceof IContainer)
			{
				String oldPath = newPathBuf.toString() + name;
				if (checkForCollisions)
				{
					if(existingFiles!=null)
					{
						if(checkForCollision(existingFiles, targetFolder, oldPath))
						{
							existingFilesOrFolders.add(existingFiles.get(oldPath));
						}
					}
					//below code is used for renaming operation, which is no longer needed
					/*
					RenameStatus status = checkForCollision(existingFiles, targetFolder, name, oldPath, toCopyNames);
					int severity = status.getSeverity();

					if (severity == IStatus.OK) {
						name = status.getMessage();
						toCopyNames.add(name);
					}
					else if (severity == IStatus.CANCEL) {

						int code = status.getCode();

						if (code == IStatus.CANCEL) {
							continue;
						}
						else if (code == RenameStatus.CANCEL_ALL) {
							break;
						}
					}
					 */

				}
				copyFilesOrFolders.add(srcFileOrFolder);
			}
		}
		boolean overwrite=false;
		if(existingFilesOrFolders.size()>0)
		{
			CopyRunnable cr = new CopyRunnable(existingFilesOrFolders);
			Display.getDefault().syncExec(cr);
			overwrite = cr.getOk();
		}
		if(existingFilesOrFolders.size()==0 || overwrite)
		{
			for (int i = 0; i < copyFilesOrFolders.size() && !resultSet.hasMessage(); i++)
			{

				IResource srcFileOrFolder = (IResource)copyFilesOrFolders.get(i);
				String name = srcFileOrFolder.getName();

				String newPath = newPathBuf.toString() + name;

				if (srcFileOrFolder instanceof IFile)
				{
					try
					{
						String srcCharSet = RemoteFileUtility.getSourceEncoding((IFile)srcFileOrFolder);

						String srcFileLocation = srcFileOrFolder.getLocation().toOSString();

						// for bug 236723, getting remote encoding for target instead of default for target fs
						String remoteEncoding = targetFolder.getEncoding();
						String systemEncoding = targetFS.getRemoteEncoding();

						targetFS.upload(srcFileLocation, srcCharSet, newPath, remoteEncoding,monitor);
						newFilePathList.add(newPath);

						
						IRemoteFile newFile = targetFS.getRemoteFileObject(newPath, monitor);
						if (newFile.isBinary() && newFile instanceof RemoteFile){ // after a binary upload, we need to mark the encoding of the remote file
							((RemoteFile)newFile).setEncoding(srcCharSet);
						}		
						// should check preference first
						if (RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.PRESERVETIMESTAMPS))
						{
							SystemIFileProperties properties = new SystemIFileProperties(srcFileOrFolder);
							long ts = properties.getRemoteFileTimeStamp();
							if (ts != 0){ // don't set 0 timestamp
								try {
									targetFS.setLastModified(newFile, ts, monitor);
								}
								catch (SystemUnsupportedOperationException e){
									// service doesn't support setLastModified
									SystemBasePlugin.logError("Unable to set last modified", e); //$NON-NLS-1$
								}
							}
						}
					}

					catch (RemoteFileIOException e)
					{
						resultSet.setMessage(e.getSystemMessage());
					}
					catch (SystemMessageException e)
					{
						resultSet.setMessage(e.getSystemMessage());
					}

					if (resultSet.hasMessage())
					{
						return resultSet;
					}
				}
				if(srcFileOrFolder instanceof IContainer)
				{
					IContainer directory = (IContainer) srcFileOrFolder;
					if (!directory.exists())
					{
						try
						{
							directory.refreshLocal(IResource.DEPTH_ONE, monitor);
						}
						catch (Exception e)
						{

						}
					}
					try
					{
						if (existingFiles != null)
						{
							IRemoteFile newTargetFolder = (IRemoteFile)existingFiles.get(newPath);
							// newTargetFolder will be null if user chose to do a rename
							if (newTargetFolder == null) {
								newTargetFolder = targetFS.getRemoteFileObject(newPath, monitor);
							}
							if (newTargetFolder != null && !newTargetFolder.exists())
							{
								newTargetFolder = targetFS.createFolder(newTargetFolder, monitor);
							}

							boolean isTargetLocal = newTargetFolder.getParentRemoteFileSubSystem().getHost().getSystemType().isLocal();
							boolean destInArchive = (newTargetFolder instanceof IVirtualRemoteFile) || newTargetFolder.isArchive();

							if (doCompressedTransfer && doSuperTransferPreference && !destInArchive && !isTargetLocal)
							{
								compressedUploadFromWorkspace(directory, newTargetFolder, monitor);
							}
							else
							{
								//sometimes, IContainer#members does not return the right members under
								//this folder.  We need to call refreshLocal() first to overcome this problem
								directory.refreshLocal(IResource.DEPTH_ONE, monitor);
								IResource[] children = directory.members();
								SystemWorkspaceResourceSet childSet = new SystemWorkspaceResourceSet(children);
								SystemRemoteResourceSet childResults = uploadResourcesFromWorkspace(childSet, newTargetFolder, monitor, false);
								if (childResults == null)
								{
									return null;
								}
								if (childResults.hasMessage())
								{
									resultSet.setMessage(childResults.getMessage());
								}
							}

							newFilePathList.add(newPath);
						}
					}
					catch (SystemMessageException e)
					{
						workspaceSet.setMessage(e.getSystemMessage());
					}
					catch (CoreException e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		try
		{
			IRemoteFile[] results = targetFS.getRemoteFileObjects((String[])newFilePathList.toArray(new String[newFilePathList.size()]), monitor);
			resultSet = new SystemRemoteResourceSet(targetFS, results);
			if (workspaceSet.hasMessage())
			{
				resultSet.setMessage(workspaceSet.getMessage());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return resultSet;
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param srcFileOrFolder the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @param checkForCollisions indicates whether to check for colllisions or not
	 * @return the result remote object
	 * @since 3.0
	 */
	public static Object uploadResourceFromWorkspace(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor, boolean checkForCollisions)
	{
		Object result = null;

		IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();

		if (targetFolder.isStale())
		{
			try
			{
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath(), monitor);
			}
			catch (Exception e)
			{
			}
		}

		if (!targetFolder.canWrite())
		{
			String msgTxt = FileResources.FILEMSG_SECURITY_ERROR;
			String msgDetails = NLS.bind(FileResources.FILEMSG_SECURITY_ERROR_DETAILS, targetFS.getHostAliasName());
			SystemMessage errorMsg = new SimpleSystemMessage(Activator.PLUGIN_ID,
					ISystemFileConstants.FILEMSG_SECURITY_ERROR,
					IStatus.ERROR, msgTxt, msgDetails);
			return errorMsg;
		}

		if (!targetFS.isConnected())
		{
			return null;
		}

		/*
		SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPY_PROGRESS);
		copyMessage.makeSubstitution(srcFileOrFolder.getName(), targetFolder.getAbsolutePath());
		 */

		String name = srcFileOrFolder.getName();

		if (srcFileOrFolder instanceof IFile)
		{
			if (checkForCollisions)
			{
				name = checkForCollision(targetFolder, name);
				if (name == null)
				{
					return null;
				}
			}

			boolean isTargetArchive = targetFolder.isArchive();
			if (isTargetArchive && !targetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement()) return null;
			StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
			if (isTargetArchive)
			{
				newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
			}
			else
			{
				newPathBuf.append(targetFolder.getSeparatorChar());
			}
			newPathBuf.append(name);

			String newPath = newPathBuf.toString();

			try
			{

				String srcCharSet = RemoteFileUtility.getSourceEncoding((IFile)srcFileOrFolder);

				boolean isText = RemoteFileUtility.getSystemFileTransferModeRegistry().isText(newPath);
				IPath location = srcFileOrFolder.getLocation();
				IRemoteFile copiedFile = null;
				if (location == null) // remote EFS file?
				{
					if (srcFileOrFolder instanceof IFile)
					{
						// copy using input stream
						try
						{
							InputStream inStream = ((IFile)srcFileOrFolder).getContents();

							if (targetFS instanceof FileServiceSubSystem)
							{
								/*
								OutputStream outStream = targetFS.getOutputStream(targetFolder.getAbsolutePath(), name, IFileService.NONE, monitor);
			
								byte[] buffer = new byte[1024];
								int readCount;
								while( (readCount = inStream.read(buffer)) > 0)
								{
									outStream.write(buffer, 0, readCount);
								}
								outStream.close();
								*/							
								IFileService fileService = ((FileServiceSubSystem)targetFS).getFileService();

								// for bug 236723, getting remote encoding for target instead of default for target fs
								String remoteEncoding = targetFolder.getEncoding();
								fileService.upload(inStream, targetFolder.getAbsolutePath(), name, !isText, remoteEncoding, monitor);
							}
						}
						catch (Exception e)
						{
						}

					}
				}
				else
				{
					// just copy using local location
					String srcFileLocation = location.toOSString();

					// for bug 236723, getting remote encoding for target instead of default for target fs
					String remoteEncoding = targetFolder.getEncoding();
					targetFS.upload(srcFileLocation, srcCharSet, newPath, remoteEncoding, monitor);
				}

				copiedFile = targetFS.getRemoteFileObject(targetFolder, name, monitor);

				if (copiedFile.isBinary() && copiedFile instanceof RemoteFile){ // after a binary upload, we need to mark the encoding of the remote file
					((RemoteFile)copiedFile).setEncoding(srcCharSet);
				}	
				
				// should check preference first
				if (RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.PRESERVETIMESTAMPS))
				{
					SystemIFileProperties properties = new SystemIFileProperties(srcFileOrFolder);
					long timestamp = properties.getRemoteFileTimeStamp();

					// srcFileOrFolder may not be a file from the RemoteSystemTempFiles folder in which
					// case there will be no stored property for the remote timestamp.
					if (timestamp == 0)
						timestamp = srcFileOrFolder.getLocalTimeStamp();

					if (timestamp != 0){ // don't set 0 timestamps
						try {
							targetFS.setLastModified(copiedFile, timestamp, monitor);
						}
						catch (SystemUnsupportedOperationException e){
							// service doesn't support setLastModified
							SystemBasePlugin.logError("Unable to set last modified", e); //$NON-NLS-1$
						}
					}
	  		    }

				return copiedFile;
			}

			catch (RemoteFileIOException e)
			{
				SystemMessageDialog.displayMessage(e);
				return e.getSystemMessage();
			}
			catch (SystemMessageException e)
			{
				SystemMessageDialog.displayMessage(e);
				return e.getSystemMessage();
			}
		}
		else if (srcFileOrFolder instanceof IContainer)
		{
			if (checkForCollisions)
			{
				name = checkForCollision(targetFolder, name);
				if (name == null)
				{
					return null;
				}
			}

			boolean isTargetArchive = targetFolder.isArchive();
			StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
			if (isTargetArchive)
			{
				newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
			}
			else
			{
				newPathBuf.append(targetFolder.getSeparatorChar());
			}
			newPathBuf.append(name);

			String newPath = newPathBuf.toString();

			IContainer directory = (IContainer) srcFileOrFolder;

			// this is a directory
			// recursively copy
			try
			{
				IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath, monitor);
				if (!newTargetFolder.exists())
				{
					targetFS.createFolder(newTargetFolder, monitor);
					newTargetFolder.markStale(true);
					newTargetFolder = targetFS.getRemoteFileObject(newPath, monitor);
				}


				if (!directory.isSynchronized(IResource.DEPTH_ONE))
				{
					try
					{
						directory.refreshLocal(IResource.DEPTH_ONE, monitor);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}


				boolean isTargetLocal = newTargetFolder.getParentRemoteFileSubSystem().getHost().getSystemType().isLocal();
				boolean destInArchive = (newTargetFolder  instanceof IVirtualRemoteFile) || newTargetFolder.isArchive();
				boolean doSuperTransferPreference = doSuperTransfer(targetFS);
				if (doCompressedTransfer && doSuperTransferPreference && !destInArchive && !isTargetLocal)
				{
					compressedUploadFromWorkspace(directory, newTargetFolder, monitor);
				}
				else
				{
					IResource[] children = directory.members();
					for (int i = 0; i < children.length; i++)
					{
						if (monitor.isCanceled())
						{
							return null;
						}
						else
						{
							IResource child = children[i];
							if (uploadResourceFromWorkspace(child, newTargetFolder, monitor, false) == null)
							{
								return null;
							}
						}
					}
				}
				return newTargetFolder;

			}
			catch (SystemMessageException e)
			{
				SystemMessageDialog.displayMessage(e);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}

		return result;
	}

	/**
	 * @since 3.0
	 */
	public static void compressedUploadFromWorkspace(IContainer directory, IRemoteFile newTargetFolder, IProgressMonitor monitor) throws Exception
	{
		if (!newTargetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement()) return;
		if (ArchiveHandlerManager.isVirtual(newTargetFolder.getAbsolutePath()))
		{
			return;
		}
		IRemoteFile destinationArchive = null;
		String newPath = null;
		IRemoteFileSubSystem targetFS = null;
		IRemoteFile remoteArchive = null;

		try
		{
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_MAIN,IProgressMonitor.UNKNOWN);
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE);
			destinationArchive = getLocalFileSubSystem().getRemoteFileObject(File.createTempFile("supertransfer", getArchiveExtensionFromProperties()).getAbsolutePath(), monitor); //$NON-NLS-1$
			FileServiceSubSystem localSS = (FileServiceSubSystem)getLocalFileSubSystem();
			try
			{
				localSS.delete(destinationArchive, monitor);
			}
			catch (Exception e)
			{

			}
			localSS.createFile(destinationArchive, monitor);

			if (destinationArchive == null)
			{
				return;
			}
			if (!destinationArchive.isArchive())
			{
				return;
			}
			IRemoteFile newTargetParent = newTargetFolder.getParentRemoteFile();
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_POPULATE);
			IRemoteFile sourceDir = localSS.getRemoteFileObject(directory.getLocation().toOSString(), monitor);
			targetFS = newTargetFolder.getParentRemoteFileSubSystem();


			// FIXME
			//localSS.copyToArchiveWithEncoding(sourceDir, destinationArchive, sourceDir.getName(), targetFS.getRemoteEncoding(), monitor);
			localSS.copy(sourceDir, destinationArchive, sourceDir.getName(),  monitor);

			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER);
			newPath = newTargetParent.getAbsolutePath() + targetFS.getSeparator() + destinationArchive.getName();

			// copy local zip to remote
			targetFS.upload(destinationArchive.getAbsolutePath(), SystemEncodingUtil.ENCODING_UTF_8, newPath, System.getProperty("file.encoding"), monitor); //$NON-NLS-1$
			remoteArchive = targetFS.getRemoteFileObject(newPath, monitor);

			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT);
			String compressedFolderPath = newPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR + directory.getName();
			IRemoteFile compressedFolder = targetFS.getRemoteFileObject(compressedFolderPath, monitor);

			// extract the compressed folder from the temp archive on remote
			targetFS.copy(compressedFolder, newTargetParent, newTargetFolder.getName(), monitor);

		}
		catch (SystemMessageException e)
		{
			if (monitor.isCanceled())
			{
				//If this operation if cancelled, and if the destination has already been created (partially)
				//in the host, we need to delete it.
				if (newTargetFolder.exists())
				{
					targetFS.delete(newTargetFolder, null);
				}
			}
			throw e;
			//SystemMessageDialog.displayMessage(e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally {
			if (newPath == null) cleanup(destinationArchive, null);
			else cleanup(destinationArchive, new File(newPath));

			// delete the temp remote archive
			// now, DStoreFileService#getFile() (which is invoked by getRemoteFileObject() call)
			// has been updated to also put the query object into the dstore file map,
			// we don't need to do the query on the remoteArchive object before the
			// delete.
			if (remoteArchive != null && remoteArchive.exists())
			{
				targetFS.delete(remoteArchive, null);
			}
			monitor.done();
		}
	}

	protected static void setReadOnly(IFile file, boolean flag)
	{
		ResourceAttributes attrs = file.getResourceAttributes();
		attrs.setReadOnly(flag);
		try
		{
			file.setResourceAttributes(attrs);
		}
		catch (CoreException e)
		{
		}
	}
	public static void transferProperties(IResource source, IRemoteFile target, IProgressMonitor monitor) throws CoreException, RemoteFileSecurityException, RemoteFileIOException, SystemMessageException
	{
		if (monitor.isCanceled()) return;
		if (source instanceof IFile)
		{
			SystemIFileProperties properties = new SystemIFileProperties(source);
			try {
				long ts = properties.getRemoteFileTimeStamp();
				if (ts != 0){ // don't set 0 timestamps
					target.getParentRemoteFileSubSystem().setLastModified(target, ts, monitor);
				}
			}
			catch (SystemUnsupportedOperationException e){
				// service doesn't support setLastModified
				SystemBasePlugin.logError("Unable to set last modified", e); //$NON-NLS-1$
			}
		}
		else if (source instanceof IContainer)
		{
			source.refreshLocal(IResource.DEPTH_ONE, null);
			IResource[] children = ((IContainer)source).members();
			for (int i = 0; i < children.length; i++)
			{
				if (monitor.isCanceled())
				{
					return;
				}
				else
				{
					IResource child = children[i];
					IRemoteFile newtarget = target.getParentRemoteFileSubSystem().getRemoteFileObject(target, child.getName(), monitor);
					if (!newtarget.exists()) return;
					transferProperties(child, newtarget, monitor);
				}
			}
		}
	}

	protected static String getArchiveExtensionFromProperties()
	{

		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		String archiveType = store.getString(ISystemFilePreferencesConstants.SUPERTRANSFER_ARC_TYPE);
		if (archiveType == null || !ArchiveHandlerManager.getInstance().isRegisteredArchive("test." + archiveType)) //$NON-NLS-1$
		{
			archiveType = ".zip"; //$NON-NLS-1$
		}
		else
		{
			archiveType = "." + archiveType; //$NON-NLS-1$
		}
		//String archiveType = ".zip";
		return archiveType;
	}

	/**
	 * @since 3.0
	 */
	public static IResource compressedDownloadToWorkspace(IRemoteFile directory, IProgressMonitor monitor) throws Exception
	{
		if (!directory.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement()) return null;
		IRemoteFile destinationArchive = null;
		IRemoteFile cpdest = null;
		File dest = null;
		IResource targetResource = null;
		FileServiceSubSystem localSS = (FileServiceSubSystem)getLocalFileSubSystem();
		try
		{
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_MAIN,IProgressMonitor.UNKNOWN);
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE);
			File file = File.createTempFile("supertransfer", getArchiveExtensionFromProperties()); //$NON-NLS-1$
			file.delete();
			String separator = ""; //$NON-NLS-1$
			IRemoteFile destinationParent = directory.getParentRemoteFile();
			if (!destinationParent.getAbsolutePath().endsWith(directory.getSeparator()))
				separator = directory.getSeparator();


			if (destinationParent.canWrite())
			{
				try
				{
					String destArchPath = destinationParent.getAbsolutePath() + separator + file.getName();
					destinationArchive = directory.getParentRemoteFileSubSystem().getRemoteFileObject(destArchPath, monitor);
					if (destinationArchive.exists())
					{
						directory.getParentRemoteFileSubSystem().delete(destinationArchive, monitor);
					}
					directory.getParentRemoteFileSubSystem().createFile(destinationArchive, monitor);
				}
				catch (RemoteFileSecurityException e)
				{
					// can't write to this directory
				}
			}
			if (destinationArchive == null)
			{
				String homeFolder = directory.getParentRemoteFileSubSystem().getRemoteFileObject("./", monitor).getAbsolutePath(); //$NON-NLS-1$
				String destArchPath = homeFolder + separator + file.getName();
				destinationArchive = directory.getParentRemoteFileSubSystem().getRemoteFileObject(destArchPath, monitor);
				if (destinationArchive.exists()) directory.getParentRemoteFileSubSystem().delete(destinationArchive,monitor);
				destinationArchive = directory.getParentRemoteFileSubSystem().createFile(destinationArchive, monitor);
			}

			targetResource = getTempFileFor(directory);

			if (destinationArchive == null)
			{
				return null;
			}
			if (!destinationArchive.isArchive())
			{
				return null;
			}

			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_POPULATE);
			IRemoteFileSubSystem sourceFS = directory.getParentRemoteFileSubSystem();
			IRemoteFile sourceDir = sourceFS.getRemoteFileObject(directory.getAbsolutePath(), monitor);

			// DKM - copy src dir to remote temp archive
			try
			{
				sourceFS.copy(sourceDir, destinationArchive, sourceDir.getName(), monitor);
			}
			catch (SystemMessageException e)
			{
				if (monitor.isCanceled())
				{
					cleanup(destinationArchive, null);
					return targetResource;
				}
			}
			destinationArchive.markStale(true);

			// reget it so that it's properties (namely "size") are correct
			cpdest = destinationArchive = destinationArchive.getParentRemoteFileSubSystem().getRemoteFileObject(destinationArchive.getAbsolutePath(), monitor);

			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER);
			String name = destinationArchive.getName();

			// DKM - use parent folder as dest
			dest = new File(targetResource.getParent().getLocation().toOSString() + File.separator + name);
			sourceFS.download(cpdest, dest.getAbsolutePath(), System.getProperty("file.encoding"), monitor); //$NON-NLS-1$


			ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(dest);

			VirtualChild[] arcContents = handler.getVirtualChildrenList(null);
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT, arcContents.length);

			for (int i = 0; i < arcContents.length; i++)
			{
				if (arcContents[i].isDirectory && handler.getVirtualChildren(arcContents[i].fullName, null) == null) continue;
				String currentTargetPath = targetResource.getParent().getLocation().toOSString() + localSS.getSeparator() + useLocalSeparator(arcContents[i].fullName);
				IRemoteFile currentTarget = localSS.getRemoteFileObject(currentTargetPath, monitor);
				boolean replace = false;

				if (currentTarget != null && currentTarget.exists())
				{
					IResource currentTargetResource = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(new Path(currentTarget.getAbsolutePath()));
					SystemIFileProperties properties = new SystemIFileProperties(currentTargetResource);

					if (properties.getRemoteFileTimeStamp() != arcContents[i].getTimeStamp())
					{
						replace = true;
					}
				}
				else
				{
					replace = true;
				}

				if (replace)
				{

					if (!monitor.isCanceled())
					{
						String currentSourcePath = dest.getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + arcContents[i].fullName;
						IRemoteFile currentSource = localSS.getRemoteFileObject(currentSourcePath, monitor);
						boolean shouldExtract = currentSource.isFile();

						if (!shouldExtract)
						{
							// check for empty dir
							IRemoteFile[] children = localSS.list(currentSource, monitor);

							if (children == null || children.length == 0)
							{
								shouldExtract = true;
							}
						}

						if (shouldExtract)
						{
							String msgTxt = NLS.bind(FileResources.MSG_EXTRACT_PROGRESS, currentSource.getName());
							monitor.subTask(msgTxt);


							boolean canWrite = true;
							if (currentTarget != null)
							{
								IResource currentTargetResource = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(new Path(currentTarget.getAbsolutePath()));
								if (currentTargetResource != null && currentTargetResource.exists())
								{
									try
									{
										currentTargetResource.delete(false, monitor);
									}
									catch (Exception e)
									{
										// don't extract this one
										canWrite = false;
									}
								}

								if (canWrite)
								{
									localSS.copy(currentSource, currentTarget.getParentRemoteFile(), currentSource.getName(),  monitor);
									// FIXME localSS.copyFromArchiveWithEncoding(currentSource, currentTarget.getParentRemoteFile(), currentSource.getName(), sourceEncoding, isText, monitor);

									SystemIFileProperties properties = new SystemIFileProperties(currentTargetResource);
									properties.setRemoteFileTimeStamp(arcContents[i].getTimeStamp());
									monitor.worked(1);
								}
							}
						}
					}
					else
					{
						//return null;
					}

				}
			}
		}
		catch (SystemMessageException e)
		{
			SystemMessageDialog.displayMessage(e);
			cleanup(cpdest, dest);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			cleanup(cpdest, dest);
			throw e;
		}

		cleanup(cpdest, dest);
		monitor.done();
		return targetResource;
	}

	protected static void cleanup(IRemoteFile arc1, File arc2) throws RemoteFileIOException, RemoteFileSecurityException, RemoteFolderNotEmptyException
	{
		if (arc1 != null)
		{
			try
			{
				arc1.getParentRemoteFileSubSystem().delete(arc1, null);
			}
			catch (SystemMessageException e)
			{
				SystemMessageDialog.displayMessage(e);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (arc2 != null && arc2.exists())
			arc2.delete();
	}
	/**
	 *
	 */
	protected static String useLocalSeparator(String virtualPath)
	{
		return virtualPath.replace('/', getLocalFileSubSystem().getSeparatorChar());
	}

	/**
	 * Returns the corresponding temp file location for a remote file or folder
	 * @param srcFileOrFolder the remote file or folder
	 * @return the local replica location
	 */
	public static IResource getTempFileFor(IRemoteFile srcFileOrFolder)
	{
		SystemRemoteEditManager editMgr = SystemRemoteEditManager.getInstance();
		if (!editMgr.doesRemoteEditProjectExist())
		{
			editMgr.getRemoteEditProject();
		}

		//char separator = IFileConstants.PATH_SEPARATOR_CHAR_WINDOWS;
		char separator = '/';
		StringBuffer path = new StringBuffer(editMgr.getRemoteEditProjectLocation().makeAbsolute().toOSString());

		String actualHost = getActualHostFor(srcFileOrFolder.getParentRemoteFileSubSystem(), srcFileOrFolder.getAbsolutePath());
		path = path.append(separator + actualHost + separator);

		String absolutePath = srcFileOrFolder.getAbsolutePath();

		if (srcFileOrFolder.getHost().getSystemType().isLocal())
		{
			absolutePath = editMgr.getWorkspacePathFor(actualHost, srcFileOrFolder.getAbsolutePath(), srcFileOrFolder.getParentRemoteFileSubSystem());
		}

		IPath remote = new Path(absolutePath);
		absolutePath = SystemFileNameHelper.getEscapedPath(remote.toOSString());

		int colonIndex = absolutePath.indexOf(IPath.DEVICE_SEPARATOR);

		if (colonIndex != -1)
		{
			if (colonIndex == 0)
			{
				absolutePath = absolutePath.substring(1);
			}
			else if (colonIndex == (absolutePath.length() - 1))
			{
				absolutePath = absolutePath.substring(0, colonIndex);
			}
			else
			{
				absolutePath = absolutePath.substring(0, colonIndex).toLowerCase() + absolutePath.substring(colonIndex + 1);
			}
		}

		path = path.append(absolutePath);
		String pathstr = normalizePath(path.toString(), srcFileOrFolder.getParentRemoteFileSubSystem().getSeparatorChar());

		IPath workspacePath = getLocalPathObject(pathstr);

		IResource result = null;
		if (srcFileOrFolder.isDirectory())
		{
			result = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(workspacePath);
		}
		else
		{
			result = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(workspacePath);
		}

		return result;
	}

	protected static String normalizePath(String localpath, char rmtSeparator)
	{
		char localSeparator = File.separatorChar;
		if (localSeparator != rmtSeparator)
		{
			return localpath.replace(rmtSeparator, localSeparator);
		}
		return localpath;
	}

	/**
	 * Returns the corresponding temp file location for a local file or folder.
	 * @param srcFileOrFolder the local file or folder.
	 * @return the local replica location.
	 */
	public static IResource getTempFileFor(File srcFileOrFolder)
	{
		SystemRemoteEditManager editMgr = SystemRemoteEditManager.getInstance();
		if (!editMgr.doesRemoteEditProjectExist())
		{
			editMgr.getRemoteEditProject();
		}

		//char separator = IFileConstants.PATH_SEPARATOR_CHAR_WINDOWS;
		char separator = '/';
		StringBuffer path = new StringBuffer(editMgr.getRemoteEditProjectLocation().makeAbsolute().toOSString());

		String actualHost = "LOCALHOST";	 //$NON-NLS-1$
		path = path.append(separator + actualHost + separator);

		// this is only for local, so no remote name required
		String absolutePath = editMgr.getWorkspacePathFor(actualHost, srcFileOrFolder.getAbsolutePath(), null); // no subsystem

		int colonIndex = absolutePath.indexOf(IPath.DEVICE_SEPARATOR);

		if (colonIndex != -1)
		{
			if (colonIndex == 0)
			{
				absolutePath = absolutePath.substring(1);
			}
			else if (colonIndex == (absolutePath.length() - 1))
			{
				absolutePath = absolutePath.substring(0, colonIndex);
			}
			else
			{
				absolutePath = absolutePath.substring(0, colonIndex).toLowerCase() + absolutePath.substring(colonIndex + 1);
			}
		}

		path = path.append(absolutePath);
		IPath workspacePath = getLocalPathObject(path.toString());

		IResource result = null;
		if (srcFileOrFolder.isDirectory())
		{
			result = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(workspacePath);
		}
		else
		{
			result = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(workspacePath);
		}

		return result;
	}


	private static IPath getLocalPathObject(String localPath)
	{
		IPath actualPath = null;
		IPath expectedPath = new Path(localPath);

		IPath rootPath = new Path(_rootPath);
		IContainer container = SystemBasePlugin.getWorkspaceRoot().getContainerForLocation(rootPath);

		if (container != null && container.exists())
		{
			IResource lastMatch = null;
			for (int i = rootPath.segmentCount(); i < expectedPath.segmentCount() - 1; i++)
			{
				String expectedFolder = expectedPath.segment(i).toLowerCase();
				IResource match = null;

				try
				{
					IResource[] resources = container.members();

					for (int r = 0; r < resources.length && match == null; r++)
					{
						IResource resource = resources[r];
						if (resource instanceof IContainer)
						{
							String resName = resource.getName().toLowerCase();
							if (expectedFolder.equals(resName))
							{
								match = resource;
								lastMatch = match;
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (match != null)
				{
					container = (IContainer) match;
				}
				else if (lastMatch != null)
				{
					IPath newPath = lastMatch.getLocation();
					while (i < expectedPath.segmentCount())
					{
						newPath = newPath.append(expectedPath.segment(i));
						i++;
					}

					return newPath;
				}
				else
				{
					return expectedPath;
				}
			}

			String fileName = expectedPath.segment(expectedPath.segmentCount() - 1);
			try {
				IResource[] resources = container.members();
				boolean found = false;
				for (int r = 0; r < resources.length && !found; r++){
					IResource resource = resources[r];
					if (resource instanceof IFile){
						String resourceName = resource.getName();
						if (resourceName.toLowerCase().equals(fileName.toLowerCase())){
							found = true;
							fileName = resourceName;
						}
					}
				}
			}
			catch (CoreException e){}
			
			actualPath = container.getLocation().append(fileName);
			return actualPath;
		}

		return expectedPath;
	}


	/**
	 * @since 3.0
	 */
	public static String getActualHostFor(IRemoteFileSubSystem subsystem, String remotePath)
	{
		String hostname = subsystem.getHost().getHostName();
		if (subsystem.getHost().getSystemType().isLocal())
		{
			String result = SystemRemoteEditManager.getInstance().getActualHostFor(hostname, remotePath, subsystem);
			return result;
		}
		return hostname;
	}

	/**
	 * @since 3.0
	 */
	public static String getActualHostFor(String hostname, String remotePath, IRemoteFileSubSystem subsystem)
	{
		return SystemRemoteEditManager.getInstance().getActualHostFor(hostname, remotePath, subsystem);
	}

	private static void refreshResourceInWorkspace(IResource parent)
	{
		if (!parent.exists())
		{
			refreshResourceInWorkspace(parent.getParent());
		}
		else
		{
			try
			{
				parent.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
			catch (Exception e)
			{
			}
		}
	}

	protected static boolean isRemoteFileMounted(ISubSystem subsystem, String remotePath)
	{
		String hostname = subsystem.getHost().getHostName();
		if (subsystem.getHost().getSystemType().isLocal())
		{
			String result = SystemRemoteEditManager.getInstance().getActualHostFor(hostname, remotePath, (IRemoteFileSubSystem)subsystem);
			if (!result.equals(hostname))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @since 3.0
	 */
	protected static boolean isRemoteFileMounted(String hostname, String remotePath, IRemoteFileSubSystem subsystem)
	{
		String result = SystemRemoteEditManager.getInstance().getActualHostFor(hostname, remotePath, subsystem);

		if (!result.equals(hostname)) {
			return true;
		}

		return false;
	}

	/**
	 * @since 3.0
	 */
	protected static String getWorkspaceRemotePath(IRemoteFileSubSystem subsystem, String remotePath) {

		if (subsystem != null) {
			return SystemRemoteEditManager.getInstance().getWorkspacePathFor(subsystem.getHost().getHostName(), remotePath, subsystem);
		}

		return remotePath;
	}

	/**
	 * @since 3.0
	 */
	protected static String getWorkspaceRemotePath(String hostname, String remotePath, IRemoteFileSubSystem subsystem) {
		return SystemRemoteEditManager.getInstance().getWorkspacePathFor(hostname, remotePath, subsystem);
	}

	protected static RenameStatus checkForCollision(SystemRemoteResourceSet existingFiles, IRemoteFile targetFolder, String oldName, String oldPath)
	{
		return checkForCollision(existingFiles, targetFolder, oldName, oldPath, null);
	}
	/**
	 * @since 3.0
	 */
	protected static RenameStatus checkForCollision(SystemRemoteResourceSet existingFiles, IRemoteFile targetFolder, String oldName, String oldPath, List NamesInUse)
	{
		String newName = oldName;

		IRemoteFile targetFileOrFolder = (IRemoteFile) existingFiles.get(oldPath);

		RenameStatus status = new RenameStatus(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, newName, null);

		if (targetFileOrFolder != null && targetFileOrFolder.exists()) {
			RenameRunnable rr = new RenameRunnable(targetFileOrFolder, NamesInUse);
			Display.getDefault().syncExec(rr);
			newName = rr.getNewName();

			if (newName == null) {

				int state = rr.getCancelStatus();

				if (state == RenameRunnable.RENAME_DIALOG_CANCELLED_ALL) {
					status = new RenameStatus(IStatus.CANCEL, Activator.getDefault().getBundle().getSymbolicName(), RenameStatus.CANCEL_ALL, "", null); //$NON-NLS-1$
				}
				else if (state == RenameRunnable.RENAME_DIALOG_CANCELLED) {
					status = new RenameStatus(IStatus.CANCEL, Activator.getDefault().getBundle().getSymbolicName(), IStatus.CANCEL, "", null); //$NON-NLS-1$
				}
			}
			else {
				status = new RenameStatus(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, newName, null);
			}
		}


		return status;
	}

	/**
	 * @since 3.0
	 */
	protected static boolean checkForCollision(SystemRemoteResourceSet existingFiles, IRemoteFile targetFolder, String oldPath)
	{

		IRemoteFile targetFileOrFolder = (IRemoteFile) existingFiles.get(oldPath);


		if (targetFileOrFolder != null && targetFileOrFolder.exists())
			return true;
		else
			return false;
	}

	/**
	 * @since 3.0
	 */
	public static class RenameRunnable implements Runnable
	{
		private IRemoteFile _targetFileOrFolder;
		private String _newName;
		private List _namesInUse = new ArrayList();
		private int cancelStatus;

		/**
		 * @since 3.0
		 */
		public static int RENAME_DIALOG_NOT_CANCELLED = -1;
		/**
		 * @since 3.0
		 */
		public static int RENAME_DIALOG_CANCELLED = 0;
		/**
		 * @since 3.0
		 */
		public static int RENAME_DIALOG_CANCELLED_ALL = 1;

		public RenameRunnable(IRemoteFile targetFileOrFolder)
		{
			_targetFileOrFolder = targetFileOrFolder;
			cancelStatus = RENAME_DIALOG_NOT_CANCELLED;
		}

		/**
		 * @since 3.0
		 */
		public RenameRunnable(IRemoteFile targetFileOrFolder, List namesInUse)
		{
			_targetFileOrFolder = targetFileOrFolder;
			cancelStatus = RENAME_DIALOG_NOT_CANCELLED;
			_namesInUse=namesInUse;
		}

		public void run() {
			ValidatorFileUniqueName validator = null;
			SystemRenameSingleDialog dlg;
			if(_namesInUse!=null && _namesInUse.size()>0)
			{
				dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator, _namesInUse); // true => copy-collision-mode
			}
			else
			{
				dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator); // true => copy-collision-mode
			}
			dlg.setShowCancelAllButton(true);

			dlg.open();
			if (!dlg.wasCancelled() && !dlg.wasCancelledAll())
				_newName = dlg.getNewName();
			else {
				_newName = null;

				if (dlg.wasCancelledAll()) {
					cancelStatus = RENAME_DIALOG_CANCELLED_ALL;
				}
				else {
					cancelStatus = RENAME_DIALOG_CANCELLED;
				}
			}
		}

		public String getNewName()
		{
			return _newName;
		}

		public int getCancelStatus() {
			return cancelStatus;
		}
	}

	protected static String checkForCollision(final IRemoteFile targetFolder, String oldName)
	{
		final String[] newName = new String[]{oldName};

		try
		{

			IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
			final IRemoteFile targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName, new NullProgressMonitor());

			//RSEUIPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
				//monitor.setVisible(false); wish we could!
				// we no longer have to set the validator here... the common rename dialog we all now use queries the input
				// object's system view adaptor for its name validator. See getNameValidator in SystemViewRemoteFileAdapter. phil
				runInDisplayThread(new Runnable() {
					public void run() {
						ValidatorFileUniqueName validator = null; // new ValidatorFileUniqueName(shell, targetFolder, srcFileOrFolder.isDirectory());
						//SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
						SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(SystemBasePlugin.getActiveWorkbenchShell(), true, targetFileOrFolder, validator); // true => copy-collision-mode

						dlg.open();
						if (!dlg.wasCancelled())
							newName[0] = dlg.getNewName();
						else
							newName[0] = null;
					}
				});
			}
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e); //$NON-NLS-1$
		}

		return newName[0];
	}

	private static void runInDisplayThread(Runnable runnable) {
		Display display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		if(Thread.currentThread()==display.getThread()) {
			// if we are in the display thread we can call the method directly
			runnable.run();
		} else {
			// we execute it in the Display Thread but we wait for the result...
			display.syncExec(runnable);
		}
	}




	/**
	 * replicates a remote file to the temp files project in the workspace
	 *
	 * @param srcFileOrFolder the file to copy
	 * @param monitor the progress monitor
	 * @return the resulting local replica
	 *
	 * @deprecated use downloadFileToWorkspace
	 */
	protected static IFile copyRemoteFileToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{
		return downloadFileToWorkspace(srcFileOrFolder, monitor);
	}




	/**
	 * This method downloads a set of remote resources to the workspace.  It uses
	 * the downloadMultiple() API of the remote file subsystem and service layers so
	 * for some service implementations, this is a big optimization
	 *
	 * @param remoteSet the set of resources to download
	 * @param monitor the progress monitor
	 * @return the set of temp files created as a result of the download.
	 *
	 * @deprecated use downloadResourcesToWorkspaceMultiple
	 * @since 3.0
	 */
	public static SystemWorkspaceResourceSet copyRemoteResourcesToWorkspaceMultiple(SystemRemoteResourceSet remoteSet, IProgressMonitor monitor)
	{
		return downloadResourcesToWorkspaceMultiple(remoteSet, monitor);
	}


	/**
	 * Replicates a set of remote files or folders to the workspace
	 * @param remoteSet the objects which are being copied
	 * @param monitor a progress monitor
	 * @return the temporary objects that was created after the download
	 *
	 * @deprecated use downloadResourcesToWorkspace
	 */
	public static SystemWorkspaceResourceSet copyRemoteResourcesToWorkspace(SystemRemoteResourceSet remoteSet, IProgressMonitor monitor)
	{
		return downloadResourcesToWorkspace(remoteSet, monitor);
	}

	/**
	 *
	 * @deprecated use downloadResourceToWorkspace
	 */
	public static Object copyRemoteResourceToWorkspace(File srcFileOrFolder, IProgressMonitor monitor) {
		return downloadResourceToWorkspace(srcFileOrFolder, monitor);
	}

	/**
	 * Replicates a local file to the temp files project in the workspace.
	 * @param srcFileOrFolder the file to copy.
	 * @param monitor the progress monitor.
	 * @return the resulting local replica.
	 *
	 * @deprecated use downloadFileToWorkspace
	 */
	protected static IFile copyRemoteFileToWorkspace(File srcFileOrFolder, IProgressMonitor monitor)
	{
		return downloadFileToWorkspace(srcFileOrFolder, monitor);
	}



	/**
	 * Replicates a remote file or folder to the workspace
	 * @param srcFileOrFolder the object which is being copied
	 * @param monitor a progress monitor
	 * @return the temporary object that was created after the download
	 *
	 * @deprecated use downloadResourceToWorkspace
	 */
	public static Object copyRemoteResourceToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{
		return downloadResourceToWorkspace(srcFileOrFolder, monitor);
	}
	/**
	 * Perform a copy via drag and drop.
	 * @param srcFileOrFolder the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @return the resulting remote object
	 *
	 * @deprecated use uploadResourceFromWorkspace
	 */
	public static Object copyWorkspaceResourceToRemote(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor)
	{
		return uploadResourceFromWorkspace(srcFileOrFolder, targetFolder, monitor);
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param workspaceSet the objects to be copied.  If the target and sources are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @param checkForCollisions indicates whether to check for collisions or not
	 * @return the resulting remote objects
	 *
	 * @deprecated use uploadResourcesFromWorkspace
	 */
	public static SystemRemoteResourceSet copyWorkspaceResourcesToRemote(SystemWorkspaceResourceSet workspaceSet, IRemoteFile targetFolder, IProgressMonitor monitor, boolean checkForCollisions)
	{
		return uploadResourcesFromWorkspace(workspaceSet, targetFolder, monitor, checkForCollisions);
	}

	/**
	 * Perform a copy via drag and drop.
	 * @param srcFileOrFolder the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param targetFolder the object to be copied to.
	 * @param monitor the progress monitor
	 * @param checkForCollisions indicates whether to check for collisions or not
	 * @return the result remote object
	 *
	 * @deprecated use uploadResourceFromWorkspace
	 */
	public static Object copyWorkspaceResourceToRemote(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor, boolean checkForCollisions)
	{
		return uploadResourceFromWorkspace(srcFileOrFolder, targetFolder, monitor, checkForCollisions);
	}

	/**
	 * @param directory
	 * @param newTargetFolder
	 * @param monitor
	 * @throws Exception
	 *
	 * @deprecated use compressedUploadFromWorkspace
	 */
	public static void compressedCopyWorkspaceResourceToRemote(IContainer directory, IRemoteFile newTargetFolder, IProgressMonitor monitor) throws Exception
	{
		compressedUploadFromWorkspace(directory, newTargetFolder, monitor);
	}

	/**
	 *
	 * @param directory
	 * @param monitor
	 * @return
	 * @throws Exception
	 *
	 * @deprecated use compressedDownloadToWorkspace
	 */
	public static IResource compressedCopyRemoteResourceToWorkspace(IRemoteFile directory, IProgressMonitor monitor) throws Exception
	{
		return compressedDownloadToWorkspace(directory, monitor);
	}

}




