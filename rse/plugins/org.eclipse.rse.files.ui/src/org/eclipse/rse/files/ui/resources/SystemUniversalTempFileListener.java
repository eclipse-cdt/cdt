/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.actions.SystemUploadConflictAction;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.ui.actions.DisplaySystemMessageAction;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * This class manages listening for resource changes within our temp file project
 * It is used for listening to saves made in the editor so that we can upload 
 * changes to the remote files.   This class specifically handles universal files
 * and doesn't do anything for iSeries.  For iSeries members we need to subclass this.
 */
public class SystemUniversalTempFileListener extends SystemTempFileListener
{

	private static SystemUniversalTempFileListener _instance = null;

	private ArrayList _editedFiles = new ArrayList();

	/**
	 * Return singleton
	 */
	public static SystemUniversalTempFileListener getListener()
	{
		if (_instance == null)
		{
			_instance = new SystemUniversalTempFileListener();
		}
		return _instance;
	}

	public void registerEditedFile(SystemEditableRemoteFile editMember)
	{
		_editedFiles.add(editMember);
	}

	public void unregisterEditedFile(SystemEditableRemoteFile editMember)
	{
		_editedFiles.remove(editMember);
	}

	public SystemEditableRemoteFile getEditedFile(IRemoteFile file)
	{
		for (int i = 0; i < _editedFiles.size(); i++)
		{
			SystemEditableRemoteFile efile = (SystemEditableRemoteFile) _editedFiles.get(i);
			if (efile != null)
			{
				IRemoteFile editedFile = efile.getRemoteFile();
				if (editedFile.getAbsolutePathPlusConnection().equals(file.getAbsolutePathPlusConnection()))
				{
					return efile;
				}
			}
		}

		return null;
	}

	/**
	 * Indicate whether this tempfile listener handles the specified
	 * @param subsystem the subsystem to check
	 * @return whether it handles this or not
	 */
	protected boolean doesHandle(ISubSystem subsystem)
	{
		if (subsystem instanceof IRemoteFileSubSystem)
		{
			return true;
		}
		return false;
	}

	/**
	* Synchronize the specified remote file with the temporary local file using the 
	* specified remote file subsystem.
	* 
	* @param fs the remote file subsystem of the remote file
	* @param tempFile the temporary file
	* @param remoteFile the remote file
	*/
	protected void doResourceSynchronization(ISubSystem subsystem, IFile tempFile, String resourceId, IProgressMonitor monitor)
	{
		if (subsystem instanceof IRemoteFileSubSystem)
		{
			Shell shell = SystemPlugin.getTheSystemRegistry().getShell();
			IRemoteFileSubSystem fs = (IRemoteFileSubSystem) subsystem;
			
			// first we need to get the stored timestamp property and the actual remote timestamp
			SystemIFileProperties properties = new SystemIFileProperties(tempFile);
			
			// make sure we're working online - not offline
			if (fs.isOffline())
			{			
				properties.setDirty(true);		
				return;
			}
			else
			{
				// for mounting...
				if (fs.getHost().getSystemType().equals("Local"))
				{
					boolean isMounted = properties.getRemoteFileMounted();
					if (isMounted)
					{				
						String mappedHostPath = properties.getResolvedMountedRemoteFilePath();
						String mappedHostName = properties.getResolvedMountedRemoteFileHost();
						String systemRemotePath = SystemRemoteEditManager.getDefault().getMountPathFor(mappedHostName, mappedHostPath);	
						
						if (systemRemotePath == null)
						{
							// mount no longer exists - just return for now
							return;
						}
						if (!systemRemotePath.equals(resourceId))
						{
							// remote path
							resourceId = systemRemotePath;
							properties.setRemoteFilePath(systemRemotePath);
						}								
					}									
				}
			}
			
			try
			{				
				IRemoteFile remoteFile = fs.getRemoteFileObject(resourceId);		

				if (remoteFile != null)
				{
					
					// make sure we have uptodate file
					remoteFile.markStale(true);
					remoteFile = fs.getRemoteFileObject(resourceId);	

					// get modification stamp and dirty state         
					long storedModifiedStamp = properties.getRemoteFileTimeStamp();

					// get associated editable
					SystemEditableRemoteFile editable = getEditedFile(remoteFile);
					if (editable != null && storedModifiedStamp == 0)
					{
						return;

					}
					else if (editable == null)
					{
						Object remoteObject = properties.getRemoteFileObject();
						if (remoteObject != null && remoteObject instanceof SystemEditableRemoteFile)
						{
							editable = (SystemEditableRemoteFile) remoteObject;
						}
						else
						{
							editable = new SystemEditableRemoteFile(remoteFile);
						}

						// defect - we get a save event when saving during a close
						// in that case, we shouldn't reopen the editor
						// I think this was originally here so that, if a save is done on
						// a file that hasn't yet been wrapped with an editable, we can
						// set the editor member
						// now call check method before
						if (editable.checkOpenInEditor() != ISystemEditableRemoteObject.NOT_OPEN)
						{
							editable.openEditor();
						}
						editable.addAsListener();
						editable.setLocalResourceProperties();
					}

					upload(fs, remoteFile, tempFile, properties, storedModifiedStamp, editable, monitor);				
				}
			}
			catch (Exception e)
			{
				SystemBasePlugin.logError(e.getMessage());
			}
		}

	}

	public void upload(IRemoteFileSubSystem fs, IRemoteFile remoteFile, IFile tempFile, SystemIFileProperties properties, 
				long storedModifiedStamp, SystemEditableRemoteFile editable, IProgressMonitor monitor)
	{
		try
		{
			// get the remote modified timestamp
			long remoteModifiedStamp = remoteFile.getLastModified();

			boolean remoteFileDeleted = !remoteFile.exists();
			// compare timestamps
			if (remoteFileDeleted || (storedModifiedStamp == remoteModifiedStamp))
			{
				// timestamps are the same, so the remote file hasn't changed since our last download
				try
				{
					// upload our pending changes to the remote file
					fs.uploadUTF8(tempFile, remoteFile,monitor);
				}

				catch (RemoteFileSecurityException e)
				{
					DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(e.getSystemMessage());
					Display.getDefault().syncExec(msgAction);
				}
				catch (RemoteFileIOException e)
				{
					DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(e.getSystemMessage());
					Display.getDefault().syncExec(msgAction);
				}
				catch (Exception e)
				{
					RemoteFileIOException exc = new RemoteFileIOException(e);
					DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(exc.getSystemMessage());
					Display.getDefault().syncExec(msgAction);
				}

				// get the remote file object again so that we have a fresh remote timestamp
				remoteFile.markStale(true);
				
				
				IRemoteFile parent = remoteFile.getParentRemoteFile();
	
				ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
				// refresh
				if (parent != null)
				{
					registry.fireEvent(new SystemResourceChangeEvent(parent, ISystemResourceChangeEvents.EVENT_REFRESH, null));
				}

				remoteFile = fs.getRemoteFileObject(remoteFile.getAbsolutePath());
				
				registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, remoteFile));
			
				
				// set the stored timestamp to be the same as the remote timestamp
				properties.setRemoteFileTimeStamp(remoteFile.getLastModified());

				// indicate that the temp file is no longer dirty
				properties.setDirty(false);
				editable.updateDirtyIndicator();
				
				}
			else if (storedModifiedStamp == -1)
			{
				// hack because Eclipse send out event after replacing local file with remote
				//	we don't want to save this!
				// set the stored timestamp to be the same as the remote timestamp
				properties.setRemoteFileTimeStamp(remoteFile.getLastModified());
			}
			else
			{
				// conflict            	
				// determine which file has a newer timestamp
				boolean remoteNewer = remoteModifiedStamp > storedModifiedStamp;

				// case 1: the remote file has changed since our last download
				//			it's new timestamp is newer than our stored timestamp (file got
				//			updated)

				// case 2: the remote file has changed since our last download
				// 			it's new timestamp is older than our stored timestamp (file got
				//			replaced with an older version)   

				// prompt user with dialog **** Prompt Dialog 1
				//			1) Overwrite local
				//			2) Overwrite remote
				//			3) Save as...
				//			4) Cancel
				Shell shell = SystemPlugin.getTheSystemRegistry().getShell();

				SystemUploadConflictAction conflictAction = new SystemUploadConflictAction(shell, tempFile, remoteFile, remoteNewer);
				conflictAction.run();
				editable.updateDirtyIndicator();
				
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}