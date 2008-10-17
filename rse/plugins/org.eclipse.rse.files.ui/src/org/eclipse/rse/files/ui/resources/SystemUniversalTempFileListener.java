/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [205297] Editor upload should not be on main thread
 * David McKnight   (IBM)        - [195285] mount path mapper changes
 * Kevin Doyle	    (IBM) 		 - [197976] Synch up Read-Only attribute when performing save based on local copy
 * Kevin Doyle 		(IBM)		 - [204810] Saving file in Eclipse does not update remote file
 * Kevin Doyle 		(IBM)		 - [210389] Display error dialog when setting file not read-only fails when saving
 * David McKnight   (IBM)        - [235221] Files truncated on exit of Eclipse
 * David McKnight   (IBM)        - [249544] Save conflict dialog appears when saving files in the editor
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.actions.SystemUploadConflictAction;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.DisplaySystemMessageAction;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;

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
	* @param subsystem the remote file subsystem of the remote file
	* @param tempFile the temporary file
	* @param resourceId the remote file
	* @param monitor progress monitor
	*/
	protected void doResourceSynchronization(ISubSystem subsystem, IFile tempFile, String resourceId, IProgressMonitor monitor)
	{
		if (subsystem instanceof IRemoteFileSubSystem)
		{
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
				//if (fs.getHost().getSystemType().isLocal())
				{
					boolean isMounted = properties.getRemoteFileMounted();
					if (isMounted)
					{				
						String mappedHostPath = properties.getResolvedMountedRemoteFilePath();
						String mappedHostName = properties.getResolvedMountedRemoteFileHost();
						String systemRemotePath = SystemRemoteEditManager.getInstance().getMountPathFor(mappedHostName, mappedHostPath, (IRemoteFileSubSystem)subsystem);	
						
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
				IRemoteFile remoteFile = fs.getRemoteFileObject(resourceId, monitor);		

				if (remoteFile != null)
				{
					
					// make sure we have uptodate file
					remoteFile.markStale(true);
					remoteFile = fs.getRemoteFileObject(resourceId, monitor);	

					// get modification stamp and dirty state         
					long storedModifiedStamp = properties.getRemoteFileTimeStamp();

					// If remote file is read-only make it writable as the local
					// copy has changed to be writable
					if (remoteFile.exists() && !remoteFile.canWrite() && !tempFile.isReadOnly()) {
						remoteFile.getParentRemoteFileSubSystem().setReadOnly(
								remoteFile, false, new NullProgressMonitor());
					}	
					
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

						final SystemEditableRemoteFile fEditable = editable;
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								
									// defect - we get a save event when saving during a close
									// in that case, we shouldn't reopen the editor
									// I think this was originally here so that, if a save is done on
									// a file that hasn't yet been wrapped with an editable, we can
									// set the editor member
									// now call check method before
									if (fEditable.checkOpenInEditor() != ISystemEditableRemoteObject.NOT_OPEN)
									{
										try {
											fEditable.openEditor();
										}
										catch (PartInitException e) {
										}
									}			
									
									fEditable.addAsListener();
								} 				
						});
						editable.setLocalResourceProperties();
					}

					upload(fs, remoteFile, tempFile, properties, storedModifiedStamp, editable, monitor);				
				}
			} 
			catch (SystemMessageException e) {
				DisplaySystemMessageAction msgAction = new DisplaySystemMessageAction(e.getSystemMessage());
				Display.getDefault().syncExec(msgAction);
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
					String srcEncoding = tempFile.getCharset(true);
					
					if (srcEncoding == null) {
						srcEncoding = remoteFile.getEncoding();
					}
					
					fs.upload(tempFile.getLocation().makeAbsolute().toOSString(), remoteFile, srcEncoding, monitor);
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

				
				IRemoteFile parent = remoteFile.getParentRemoteFile();
	
				ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				// refresh
				if (parent != null)
				{
					registry.fireEvent(new SystemResourceChangeEvent(parent, ISystemResourceChangeEvents.EVENT_REFRESH, null));
				}

				// waiting to make sure the file's timestamp is uptodate
				remoteFile = waitForTimestampToBeUpToDate(remoteFile,remoteModifiedStamp, monitor);
				
				
							
				registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, remoteFile));
			
				long ts = remoteFile.getLastModified();
				
				// set the stored timestamp to be the same as the remote timestamp
				properties.setRemoteFileTimeStamp(ts);

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
				final boolean remoteNewer = remoteModifiedStamp > storedModifiedStamp;

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
				
				final SystemEditableRemoteFile remoteEdit = editable;
				final IFile tFile = tempFile;
				final IRemoteFile rFile = remoteFile;
				
				// upload is run in a job, so the conflict action/dialog needs to run in UI thread
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						Shell shell = RSEUIPlugin.getTheSystemRegistryUI().getShell();

						SystemUploadConflictAction conflictAction = new SystemUploadConflictAction(shell, tFile, rFile, remoteNewer);
						conflictAction.run();
						remoteEdit.updateDirtyIndicator();
					}
				});
				
				
				
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private IRemoteFile waitForTimestampToBeUpToDate(IRemoteFile remoteFile, long originalTimestamp, IProgressMonitor monitor)
	{
		IRemoteFileSubSystem fs = remoteFile.getParentRemoteFileSubSystem();
		String path = remoteFile.getAbsolutePath();
		try {
			long timestamp = originalTimestamp;

			boolean fileUpdated = false;
			boolean timestampChanging = true;
			
			int MAX_TIMES_CHECKED = 100; // make sure we don't wait indefinitely
			int timesChecked = 0;  
			
			while ((timestampChanging || !fileUpdated) && !monitor.isCanceled()){	// wait until the timestamp stops changing AND timestamp did change at least once		
				try {
					Thread.sleep(500); // sleep 
				}
				catch (InterruptedException e){				
				}
			
				// query the remote file again
				remoteFile.markStale(true);
				remoteFile = fs.getRemoteFileObject(path, monitor);

				// what's the timestamp now?
				long nextTimestamp = remoteFile.getLastModified();		

				timestampChanging = (timestamp != nextTimestamp);

				if (!fileUpdated){	// indicate the file has changed if the timestamp has			
					fileUpdated = timestampChanging;
				}
				
				timestamp = nextTimestamp;
				timesChecked++;
				
				if (timesChecked >= MAX_TIMES_CHECKED){ // we're not expecting this, but it's better to timeout than to hang on this
					SystemBasePlugin.logError("timeout waiting for timestamp after upload of "+ path); //$NON-NLS-1$
					return remoteFile;
				}
			}
		}
		catch (SystemMessageException e){
			
		}
		
		return remoteFile;
	}
}