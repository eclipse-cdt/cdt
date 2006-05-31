/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.model.SystemWorkspaceResourceSet;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.ISystemArchiveHandler;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFolderNotEmptyException;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;


/**
 * Utility class for doing file transfers on universal systems
 */
public class UniversalFileTransferUtility
{
	static final boolean doCompressedTransfer = true;//false;

	static final String _rootPath = SystemRemoteEditManager.getDefault().getRemoteEditProjectLocation().makeAbsolute().toOSString();
	
	/**
	 * Transfer a remote file or folder from one remote location to another.
	 * @param srcFileOrFolder the file or folder to copy
	 * @param tgtFolder the folder to copy to
	 * @param monitor the progress monitor
	 * @param shell 
	 */
	public static void transferRemoteResource(IRemoteFile srcFileOrFolder, IRemoteFile tgtFolder, IProgressMonitor monitor)
	{
		Object tempSrc = copyRemoteResourceToWorkspace(srcFileOrFolder, monitor);
		if (tempSrc instanceof IResource)
		{
			copyWorkspaceResourceToRemote((IResource) tempSrc, tgtFolder, monitor);
		}
	}

	/**
	 * replicates a remote file to the temp files project in the workspace
	 * 
	 * @param srcFileOrFolder the file to copy
	 * @param monitor the progress monitor
	 * @param shell 
	 * @return the resulting local replica
	 */
	protected static IFile copyRemoteFileToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{
		IRemoteFileSubSystem srcFS = srcFileOrFolder.getParentRemoteFileSubSystem();
		IResource tempResource = getTempFileFor(srcFileOrFolder);

		IFile tempFile = (IFile) tempResource;
		
		// before we make the transfer to the temp file check whether a temp file already exists
		if (tempFile.exists() && ((Resource)tempFile).getPropertyManager() != null)
		{
			SystemIFileProperties properties = new SystemIFileProperties(tempFile);

			long storedModifiedStamp = properties.getRemoteFileTimeStamp();

			// compare timestamps
			if (storedModifiedStamp > 0)
			{
				// ;if they're the same, just use temp file							
				long remoteModifiedStamp = srcFileOrFolder.getLastModified();
				
				if (storedModifiedStamp == remoteModifiedStamp)
				{
					return tempFile;
				}
			}
		}

		/*
		if (monitor != null)
		{
			SystemMessage copyMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COPYTHINGGENERIC_PROGRESS);
			copyMessage.makeSubstitution(srcFileOrFolder.getName());

			monitor.beginTask(copyMessage.getLevelOneText(), IProgressMonitor.UNKNOWN);
		}
*/
		
		try
		{	
		    // copy remote file to workspace
		    srcFS.download(srcFileOrFolder, tempFile, SystemEncodingUtil.ENCODING_UTF_8, monitor);
		    
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
				// if the file is virtual, set read only if necessary
				// TODO: why set this here? And why for virtual only??
				if (srcFileOrFolder instanceof IVirtualRemoteFile)
				{
					setReadOnly(tempFile, srcFileOrFolder.canWrite());
				}
				
				if (srcFileOrFolder.isText())
				{
					try
					{
						String cset = tempFile.getCharset();
						if (!cset.equals(SystemEncodingUtil.ENCODING_UTF_8))
						{
						
							//System.out.println("charset ="+cset);
							//System.out.println("tempfile ="+tempFile.getFullPath());
							tempFile.setCharset(SystemEncodingUtil.ENCODING_UTF_8, monitor);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		    }
		}
		catch (RemoteFileIOException e)
		{
			SystemMessageDialog dlg = new SystemMessageDialog(SystemBasePlugin.getActiveWorkbenchShell(), e.getSystemMessage());
			dlg.open();
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

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		String subSystemId = registry.getAbsoluteNameForSubSystem(subSystem);
		properties.setRemoteFileSubSystem(subSystemId);
		properties.setRemoteFilePath(remotePath);
		properties.setDownloadFileTimeStamp(tempFile.getLocalTimeStamp());
		
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
	 * Replicates a set of remote files or folders to the workspace
	 * @param remoteSet the objects which are being copied
	 * @param monitor a progress monitor 
	 * @param shell
	 * @return the temporary objects that was created after the download
	 */
	public static SystemWorkspaceResourceSet copyRemoteResourcesToWorkspace(SystemRemoteResourceSet remoteSet, IProgressMonitor monitor)
	{

		
		boolean ok = true;
		SystemWorkspaceResourceSet resultSet = new SystemWorkspaceResourceSet();
		IRemoteFileSubSystem srcFS = (IRemoteFileSubSystem)remoteSet.getSubSystem();

		if (!srcFS.isConnected())
		{
			return null;
		}
	
		boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);


		List set = remoteSet.getResourceSet();
		for (int i = 0; i < set.size() && !resultSet.hasMessage(); i++)
		{
			if (monitor != null && monitor.isCanceled())
			{
				return resultSet;
			}
			
			
			IRemoteFile srcFileOrFolder = (IRemoteFile)set.get(i);
			if (!srcFileOrFolder.exists())
			{
				SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
				errorMessage.makeSubstitution(srcFileOrFolder.getAbsolutePath(), srcFS.getHostAliasName());
				resultSet.setMessage(errorMessage);
			}
			else
			{
				if (srcFileOrFolder.isFile()) // file transfer
				{
					IFile tempFile = copyRemoteFileToWorkspace(srcFileOrFolder, monitor);
					resultSet.addResource(tempFile);
				}
				else // folder transfer
				{
					IResource tempFolder = null;
									
					if (doCompressedTransfer && doSuperTransferProperty && !srcFileOrFolder.isRoot() 
							&& !(srcFileOrFolder.getParentRemoteFileSubSystem().getHost().getSystemType().equals("Local")))
					{
						try
						{
							tempFolder = compressedCopyRemoteResourceToWorkspace(srcFileOrFolder, monitor);
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
						IRemoteFile[] children = srcFS.listFoldersAndFiles(srcFileOrFolder);
						
						
						SystemRemoteResourceSet childSet = new SystemRemoteResourceSet(srcFS, children);
						SystemWorkspaceResourceSet childResults = copyRemoteResourcesToWorkspace(childSet, monitor);
						if (childResults.hasMessage())
						{
							resultSet.setMessage(childResults.getMessage());
						}
						resultSet.addResource(tempFolder);
					}
				}
			}
		}

		
		// refresh and set IFile properties
		for (int r = 0; r < resultSet.size(); r++)
		{
			IResource tempResource = (IResource)resultSet.get(r);
			IRemoteFile rmtFile = (IRemoteFile)remoteSet.get(r);
			
			if (!tempResource.exists())
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


	/**
	 * Replicates a remote file or folder to the workspace
	 * @param srcFileOrFolder the object which is being copied
	 * @param monitor a progress monitor 
	 * @param shell
	 * @return the temporary object that was created after the download
	 */
	public static Object copyRemoteResourceToWorkspace(IRemoteFile srcFileOrFolder, IProgressMonitor monitor)
	{

		boolean ok = true;

		IRemoteFileSubSystem srcFS = srcFileOrFolder.getParentRemoteFileSubSystem();

		if (!srcFS.isConnected())
		{
			return null;
		}
		if (!srcFileOrFolder.exists())
		{
			SystemMessage errorMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
			errorMessage.makeSubstitution(srcFileOrFolder.getAbsolutePath(), srcFS.getHostAliasName());
			return errorMessage;
		}

		if (srcFileOrFolder.isFile())
		{
			IFile tempFile = copyRemoteFileToWorkspace(srcFileOrFolder, monitor);

			if (!tempFile.exists())
			{
				// refresh temp file in project
				try
				{
					if (PlatformUI.isWorkbenchRunning())
					{
						tempFile.refreshLocal(IResource.DEPTH_ONE, monitor);
					}
				}
				catch (CoreException e)
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
			
			boolean doSuperTransferProperty = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);
			
			if (doCompressedTransfer && doSuperTransferProperty && !srcFileOrFolder.isRoot() 
					&& !(srcFileOrFolder.getParentRemoteFileSubSystem().getHost().getSystemType().equals("Local")))
			{
				try
				{
					tempFolder = compressedCopyRemoteResourceToWorkspace(srcFileOrFolder, monitor);
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
				IRemoteFile[] children = srcFS.listFoldersAndFiles(srcFileOrFolder);
				IResource[] childResources = new IResource[children.length];
	
				if (children != null)
				{
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
							childResource = copyRemoteFileToWorkspace(child, monitor);
						}
						else
						{
							childResource = (IResource) copyRemoteResourceToWorkspace(child, monitor);
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
					if (tempFolder.exists() && children != null)
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
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			IRemoteFileSubSystem anFS = RemoteFileUtility.getFileSubSystem(connection);
			if (anFS.getHost().getSystemType().equals("Local"))
			{
				return anFS;
			}
		}

		return null;
	}


	/**
	 * Perform a copy via drag and drop.
	 * @param src the object to be copied.  If the target and source are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param target the object to be copied to.
	 * @param monitor the progress monitor
	 * @param shell
	 * @return the resulting remote object
	 */
	public static Object copyWorkspaceResourceToRemote(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor)
	{
		return copyWorkspaceResourceToRemote(srcFileOrFolder, targetFolder, monitor, true);
	}
	
/**
	 * Perform a copy via drag and drop.
	 * @param workspaceSet the objects to be copied.  If the target and sources are not on the same system, then this is a
	 * temporary object produced by the doDrag.
	 * @param target the object to be copied to.
	 * @param monitor the progress monitor
	 * @param shell
	 * @param indicates whether to check for colllisions or not
	 * @return the resulting remote objects
	 */
	public static SystemRemoteResourceSet copyWorkspaceResourcesToRemote(SystemWorkspaceResourceSet workspaceSet, IRemoteFile targetFolder, IProgressMonitor monitor, Shell shell, boolean checkForCollisions)
	{	
		boolean doSuperTransferPreference = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER)
											&& targetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement();

		IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();
		SystemRemoteResourceSet resultSet = new SystemRemoteResourceSet(targetFS);
		
		if (targetFolder.isStale())
		{
			try
			{
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath());
			}
			catch (Exception e)
			{
			}
		}

		if (!targetFolder.canWrite())
		{
			SystemMessage errorMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR);
			errorMsg.makeSubstitution(targetFS.getHostAliasName());
			resultSet.setMessage(errorMsg);
			return resultSet;
		}

		if (!targetFS.isConnected())
		{
			return null;
		}
		boolean isTargetArchive = targetFolder.isArchive();
		if (isTargetArchive && !targetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement()) return null;
		StringBuffer newPathBuf = new StringBuffer(targetFolder.getAbsolutePath());
		if (isTargetArchive)
		{
			newPathBuf.append(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
		}
		else
		{
			newPathBuf.append(targetFolder.getSeparatorChar());
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
			existingFiles = targetFS.getRemoteFileObjects(newFilePathList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		
		// clear the list so that next time we use renamed names
		newFilePathList.clear();
		
 
		for (int i = 0; i < resources.size() && !resultSet.hasMessage(); i++)
		{
			if (monitor != null && monitor.isCanceled())
			{
				return resultSet;
			}
			
						
			IResource srcFileOrFolder = (IResource)resources.get(i);				
			String name = srcFileOrFolder.getName();
		 
			if (srcFileOrFolder instanceof IFile)
			{
				String oldPath = newPathBuf.toString() + name;
				if (checkForCollisions)
				{
					name = checkForCollision(shell, existingFiles, targetFolder, name, oldPath);
					if (name == null)
					{
						continue;
						//return null;
					}
				}

				String newPath = newPathBuf.toString() + name;

				try
				{
				
					String srcCharSet = null;
					

					try
					{
						srcCharSet = ((IFile)srcFileOrFolder).getCharset(false);
					    if (srcCharSet == null || srcCharSet.length() == 0)
					    {
					        srcCharSet = SystemEncodingUtil.ENCODING_UTF_8;
					    }
					}
					catch (CoreException e)
					{
					    srcCharSet = SystemEncodingUtil.ENCODING_UTF_8;
					}
				

					String srcFileLocation = srcFileOrFolder.getLocation().toOSString();
					targetFS.upload(srcFileLocation, srcCharSet, newPath, targetFS.getRemoteEncoding(),monitor);	
					newFilePathList.add(newPath);
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
			
			else if (srcFileOrFolder instanceof IContainer)
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
				
				String newPath = newPathBuf.toString() + name;
				
				// this is a directory
				// recursively copy
				try
				{
					IRemoteFile newTargetFolder = (IRemoteFile)existingFiles.get(newPath);
					if (!newTargetFolder.exists())
					{
						newTargetFolder = targetFS.createFolder(newTargetFolder);
					}

					
					boolean isTargetLocal = newTargetFolder.getParentRemoteFileSubSystem().getHost().getSystemType().equals("Local");
					boolean destInArchive = (newTargetFolder instanceof IVirtualRemoteFile) || newTargetFolder.isArchive();
					
					if (doCompressedTransfer && doSuperTransferPreference && !destInArchive && !isTargetLocal)
					{
						compressedCopyWorkspaceResourceToRemote(directory, newTargetFolder, monitor);					
					}
					else
					{
						IResource[] children = directory.members();
						SystemWorkspaceResourceSet childSet = new SystemWorkspaceResourceSet(directory.members());			
						SystemRemoteResourceSet childResults = copyWorkspaceResourcesToRemote(childSet, newTargetFolder, monitor, shell, checkForCollisions);																	
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
		
		
		try
		{
			resultSet = targetFS.getRemoteFileObjects(newFilePathList);
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
	 * @param target the object to be copied to.
	 * @param monitor the progress monitor
	 * @param shell
	 * @param indicates whether to check for colllisions or not
	 * @return the result remote object
	 */
	public static Object copyWorkspaceResourceToRemote(IResource srcFileOrFolder, IRemoteFile targetFolder, IProgressMonitor monitor, boolean checkForCollisions)
	{
		Object result = null;

		IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();

		if (targetFolder.isStale())
		{
			try
			{
				targetFolder = targetFS.getRemoteFileObject(targetFolder.getAbsolutePath());
			}
			catch (Exception e)
			{
			}
		}

		if (!targetFolder.canWrite())
		{
			SystemMessage errorMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR);
			errorMsg.makeSubstitution(targetFS.getHostAliasName());
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
			if (isTargetArchive && !targetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement()) return null;
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
				String srcFileLocation = srcFileOrFolder.getLocation().toOSString();

				String srcCharSet = null;
				
				boolean isText = SystemFileTransferModeRegistry.getDefault().isText(newPath);
				if (isText)
				{
					try
					{
						srcCharSet = ((IFile)srcFileOrFolder).getCharset(false);
					    if (srcCharSet == null || srcCharSet.length() == 0)
					    {
					        srcCharSet = SystemEncodingUtil.ENCODING_UTF_8;
					    }
					}
					catch (CoreException e)
					{
					    srcCharSet = SystemEncodingUtil.ENCODING_UTF_8;
					}
				}
				

				targetFS.upload(srcFileLocation, srcCharSet, newPath, targetFS.getRemoteEncoding(), monitor);				
				IRemoteFile copiedFile = targetFS.getRemoteFileObject(targetFolder, name);

				return copiedFile;
			}
		
			catch (RemoteFileIOException e)
			{
				return e.getSystemMessage();
			}
			catch (SystemMessageException e)
			{
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
				IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath);
				if (!newTargetFolder.exists())
				{
					targetFS.createFolder(newTargetFolder);
					newTargetFolder.markStale(true);
					newTargetFolder = targetFS.getRemoteFileObject(newPath);
				}

				directory.refreshLocal(IResource.DEPTH_ONE, monitor);
				
				boolean isTargetLocal = newTargetFolder.getParentRemoteFileSubSystem().getHost().getSystemType().equals("Local");
				boolean destInArchive = (newTargetFolder  instanceof IVirtualRemoteFile) || newTargetFolder.isArchive();
				boolean doSuperTransferPreference = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.DOSUPERTRANSFER);
				
				if (doCompressedTransfer && doSuperTransferPreference && !destInArchive && !isTargetLocal)
				{
					compressedCopyWorkspaceResourceToRemote(directory, newTargetFolder, monitor);					
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
							if (copyWorkspaceResourceToRemote(child, newTargetFolder, monitor, false) == null)
							{
								return null;
							}
						}
					}
				}	
				return newTargetFolder;
		
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

	public static void compressedCopyWorkspaceResourceToRemote(IContainer directory, IRemoteFile newTargetFolder, IProgressMonitor monitor) throws Exception
	{
		if (!newTargetFolder.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement()) return;
		if (ArchiveHandlerManager.isVirtual(newTargetFolder.getAbsolutePath()))
		{
			return;
		}
		IRemoteFile destinationArchive = null;
		String newPath = null;
		try
		{
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_MAIN,IProgressMonitor.UNKNOWN);
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE);
			destinationArchive = getLocalFileSubSystem().getRemoteFileObject(File.createTempFile("supertransfer", getArchiveExtensionFromProperties()).getAbsolutePath());
			FileServiceSubSystem localSS = (FileServiceSubSystem)getLocalFileSubSystem();
			try
			{
			    localSS.delete(destinationArchive, monitor);
			}
			catch (Exception e)
			{
			    
			}
			localSS.createFile(destinationArchive);
			
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
			IRemoteFile sourceDir = localSS.getRemoteFileObject(directory.getLocation().toOSString());
			IRemoteFileSubSystem targetFS = newTargetFolder.getParentRemoteFileSubSystem();
			
			
			// FIXME 
			//localSS.copyToArchiveWithEncoding(sourceDir, destinationArchive, sourceDir.getName(), targetFS.getRemoteEncoding(), monitor);
			localSS.copy(sourceDir, destinationArchive, sourceDir.getName(),  monitor);
			
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER);
			newPath = newTargetParent.getAbsolutePath() + targetFS.getSeparator() + destinationArchive.getName();
			
			// copy local zip to remote
			targetFS.upload(destinationArchive.getAbsolutePath(), newPath, monitor);
			IRemoteFile remoteArchive = targetFS.getRemoteFileObject(newPath);
			
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT);
			String compressedFolderPath = newPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR + directory.getName();
			IRemoteFile compressedFolder = targetFS.getRemoteFileObject(compressedFolderPath);
			
			// extract the compressed folder from the temp archive on remote
			targetFS.copy(compressedFolder, newTargetParent, newTargetFolder.getName(), monitor);
			
			// delete the temp remote archive
			targetFS.delete(remoteArchive, monitor);
			
			monitor.done();
	
		}
		catch (Exception e)
		{
		    e.printStackTrace();
			if (newPath == null) cleanup(destinationArchive, null);
			else cleanup(destinationArchive, new File(newPath));
			throw e;
		}
		if (newPath == null) cleanup(destinationArchive, null);
		else cleanup(destinationArchive, new File(newPath));
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
			target.getParentRemoteFileSubSystem().setLastModified(target, properties.getRemoteFileTimeStamp());
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
					IRemoteFile newtarget = target.getParentRemoteFileSubSystem().getRemoteFileObject(target, child.getName());
					if (!newtarget.exists()) return;
					transferProperties(child, newtarget, monitor);
				}
			}
		}
	}

	protected static String getArchiveExtensionFromProperties()
	{
	
		IPreferenceStore store= RSEUIPlugin.getDefault().getPreferenceStore();
		String archiveType = store.getString(ISystemPreferencesConstants.SUPERTRANSFER_ARC_TYPE);
		if (archiveType == null || !ArchiveHandlerManager.getInstance().isRegisteredArchive("test." + archiveType))
		{
			archiveType = ".zip";
		}
		else
		{
			archiveType = "." + archiveType;
		}
		//String archiveType = ".zip";
		return archiveType;
	}
	
	public static IResource compressedCopyRemoteResourceToWorkspace(IRemoteFile directory, IProgressMonitor monitor) throws Exception
	{
		if (!directory.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement()) return null;
		IRemoteFile destinationArchive = null;
		IRemoteFile cpdest = null;
		File dest = null;
		IResource targetResource = null;
		FileServiceSubSystem localSS = (FileServiceSubSystem)getLocalFileSubSystem();
		try
		{
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_MAIN,IProgressMonitor.UNKNOWN);
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_CREATE);
			File file = File.createTempFile("supertransfer", getArchiveExtensionFromProperties());
			file.delete();
			String separator = "";
			IRemoteFile destinationParent = directory.getParentRemoteFile();
			if (!destinationParent.getAbsolutePath().endsWith(directory.getSeparator()))
				separator = directory.getSeparator();
			
			
			if (destinationParent.canWrite())
			{
				try
				{
				    String destArchPath = destinationParent.getAbsolutePath() + separator + file.getName();
					destinationArchive = directory.getParentRemoteFileSubSystem().getRemoteFileObject(destArchPath);
					if (destinationArchive.exists()) 
					{
					    directory.getParentRemoteFileSubSystem().delete(destinationArchive, monitor);
					}
					directory.getParentRemoteFileSubSystem().createFile(destinationArchive);
				}
				catch (RemoteFileSecurityException e)
				{
				    // can't write to this directory
				}
			}
			if (destinationArchive == null)
			{
			    String homeFolder = directory.getParentRemoteFileSubSystem().getRemoteFileObject("./").getAbsolutePath();
			    String destArchPath = homeFolder + separator + file.getName();
				destinationArchive = directory.getParentRemoteFileSubSystem().getRemoteFileObject(destArchPath);
				if (destinationArchive.exists()) directory.getParentRemoteFileSubSystem().delete(destinationArchive,monitor);
				destinationArchive = directory.getParentRemoteFileSubSystem().createFile(destinationArchive);
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
			IRemoteFile sourceDir = sourceFS.getRemoteFileObject(directory.getAbsolutePath());
		
			// DKM - copy src dir to remote temp archive
			sourceFS.copy(sourceDir, destinationArchive, sourceDir.getName(), monitor);
			destinationArchive.markStale(true);
			
			// reget it so that it's properties (namely "size") are correct
			cpdest = destinationArchive = destinationArchive.getParentRemoteFileSubSystem().getRemoteFileObject(destinationArchive.getAbsolutePath());
			
			monitor.subTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_TRANSFER);
			String name = destinationArchive.getName();
			
			// DKM - use parent folder as dest
			dest = new File(targetResource.getParent().getLocation().toOSString() + File.separator + name);
			sourceFS.download(cpdest, dest, monitor);
			
			
			ISystemArchiveHandler handler = ArchiveHandlerManager.getInstance().getRegisteredHandler(dest);
			String sourceEncoding = sourceFS.getRemoteEncoding();

			VirtualChild[] arcContents = handler.getVirtualChildrenList();
			Display display = Display.getCurrent();
			monitor.beginTask(FileResources.RESID_SUPERTRANSFER_PROGMON_SUBTASK_EXTRACT, arcContents.length);
			
			for (int i = 0; i < arcContents.length; i++)
			{
				if (arcContents[i].isDirectory && handler.getVirtualChildren(arcContents[i].fullName) == null) continue;
				String currentTargetPath = targetResource.getParent().getLocation().toOSString() + localSS.getSeparator() + useLocalSeparator(arcContents[i].fullName);
				IRemoteFile currentTarget = localSS.getRemoteFileObject(currentTargetPath);
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
						IRemoteFile currentSource = localSS.getRemoteFileObject(currentSourcePath);
						boolean shouldExtract = currentSource.isFile();
						
						if (!shouldExtract)
						{
						    // check for empty dir
						    IRemoteFile[] children = localSS.listFiles(currentSource);
						    
						    if (children == null || children.length == 0)
						    {
						        shouldExtract = true;
						    }
						}
						
						if (shouldExtract)
						{
							SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXTRACT_PROGRESS);
							msg.makeSubstitution(currentSource.getName());
							monitor.subTask(msg.getLevelOneText());
							
							boolean isText = currentSource.isText();
							
							while (display.readAndDispatch()) {
							}
							
							boolean canWrite = true;
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
				    else
				    {
				        //return null;
				    }
				    while (display.readAndDispatch()) {
					}
				}
			}
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
		SystemRemoteEditManager editMgr = SystemRemoteEditManager.getDefault();
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
		if (srcFileOrFolder.getSystemConnection().getSystemType().equals("Local"))
		{
			absolutePath = editMgr.getWorkspacePathFor(actualHost, srcFileOrFolder.getAbsolutePath());
		}

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
			actualPath = container.getLocation().append(fileName);
			return actualPath;
		}

		return expectedPath;
	}


	public static String getActualHostFor(ISubSystem subsystem, String remotePath)
		{
			String hostname = subsystem.getHost().getHostName();
			if (subsystem != null && subsystem.getHost().getSystemType().equals("Local"))
			{
				String result = SystemRemoteEditManager.getDefault().getActualHostFor(hostname, remotePath);
				return result;
			}
			return hostname;	
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
		if (subsystem != null && subsystem.getHost().getSystemType().equals("Local"))
		{
			String result = SystemRemoteEditManager.getDefault().getActualHostFor(hostname, remotePath);
			if (!result.equals(hostname))
			{
				return true;
			}
		}
		return false;	
	}
	
	protected static String getWorkspaceRemotePath(ISubSystem subsystem, String remotePath)
		{
			if (subsystem != null && subsystem.getHost().getSystemType().equals("Local"))
			{
				return SystemRemoteEditManager.getDefault().getWorkspacePathFor(subsystem.getHost().getHostName(), remotePath);
			}
			return remotePath;
		}

	protected static String checkForCollision(Shell shell, SystemRemoteResourceSet existingFiles, IRemoteFile targetFolder, String oldName, String oldPath)
	{
		String newName = oldName;

			IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
			IRemoteFile targetFileOrFolder = (IRemoteFile)existingFiles.get(oldPath);

			if (targetFileOrFolder != null && targetFileOrFolder.exists())
			{
				RenameRunnable rr = new RenameRunnable(targetFileOrFolder);
				Display.getDefault().syncExec(rr);
				newName = rr.getNewName();
			}


		return newName;
	}

	public static class RenameRunnable implements Runnable
	{
		private IRemoteFile _targetFileOrFolder;
		private String _newName;
		public RenameRunnable(IRemoteFile targetFileOrFolder)
		{
			_targetFileOrFolder = targetFileOrFolder;
		}
		
		public void run() {
			ValidatorFileUniqueName validator = null; 				
			SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator); // true => copy-collision-mode
			
			dlg.open();
			if (!dlg.wasCancelled())
				_newName = dlg.getNewName();
			else
				_newName = null;
		}
		
		public String getNewName()
		{
			return _newName;
		}
	}
	
	protected static String checkForCollision(IRemoteFile targetFolder, String oldName)
	{
		String newName = oldName;

		try
		{

			IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
			IRemoteFile targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName);

			//RSEUIPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");  		
			//RSEUIPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
				//monitor.setVisible(false); wish we could!

				// we no longer have to set the validator here... the common rename dialog we all now use queries the input
				// object's system view adaptor for its name validator. See getNameValidator in SystemViewRemoteFileAdapter. phil
				ValidatorFileUniqueName validator = null; // new ValidatorFileUniqueName(shell, targetFolder, srcFileOrFolder.isDirectory());
				//SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
				SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(SystemBasePlugin.getActiveWorkbenchShell(), true, targetFileOrFolder, validator); // true => copy-collision-mode

				dlg.open();
				if (!dlg.wasCancelled())
					newName = dlg.getNewName();
				else
					newName = null;
			}
		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e);
		}

		return newName;
	}
}