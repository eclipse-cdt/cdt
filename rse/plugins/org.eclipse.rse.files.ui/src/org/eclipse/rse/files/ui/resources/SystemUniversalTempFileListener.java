/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * David McKnight   (IBM)        - [256048] Saving a member open in Remote LPEX editor while Working Offline doesn't set the dirty property
 * David McKnight   (IBM)        - [191284] Confusing behaviour when editing a Readonly file.
 * David McKnight   (IBM)        - [334839] File Content Conflict is not handled properly
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
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.Workbench;

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
			
			// make sure we're working online 
			// also as per bug 256048 - comment#6 if we're not connected we still need to do the same thing
			if (fs.isOffline() || !fs.isConnected())
			{			
				// offline mode - make sure the file stays dirty
				properties.setDirty(true);
								
				// try to reset the dirty indicator for the editor if it's open
				// will only work for lpex right now
				SystemEditableRemoteFile editable = null;
				if (properties.getRemoteFileObject() instanceof SystemEditableRemoteFile){
					editable = (SystemEditableRemoteFile)properties.getRemoteFileObject();						
					editable.updateDirtyIndicator();
				}

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
						IRemoteFileSubSystem ss = remoteFile.getParentRemoteFileSubSystem();
						ss.setReadOnly(remoteFile, false, monitor);
						
						// the remote file is still marked read-only, we need to requery the file (i.e. for SSH and FTP)
						if (!remoteFile.canWrite()){
							remoteFile.markStale(true);
							remoteFile = ss.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
						}
					}	
					
					boolean openEditorAfterUpload = false;
					
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
						
						openEditorAfterUpload = true;
						editable.setLocalResourceProperties();
					}

					upload(fs, remoteFile, tempFile, properties, storedModifiedStamp, editable, monitor);	
					
					if (openEditorAfterUpload){ 
						// moving this to after the upload because otherwise it queries the remote file and that messes up the timestamps needed by upload						
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
											fEditable.openEditor(); // open e
										}
										catch (PartInitException e) {
										}
									}			
									
									fEditable.addAsListener();
								} 				
						});
					}
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

	/**
	 * This method attempts to upload a temporary file in the workspace to a corresponding remote file location.  It
	 * checks whether the timestamp of the remote file has changed since the temporary file was last known to
	 * be in synch with the remote file.  If the timestamp has not changed, then it is assumed that the remote
	 * file has not changed and therefore it is safe to do an upload.  If the timestamp has changed, then the remote
	 * file must have changed independently and there is a conflict and the upload conflict action is invoked.
	 * 
	 * <p>
	 * <b>Warning</b> It is important to make sure that the remoteFile that gets passed in is up-to-date AND is the 
	 * current cached version.  If the remoteFile is not up-to-date then the timestamp of the actual remote file may 
	 * be wrong and lead to the following problems:
	 * 
	 * <ul>
	 *   <li> If the detected remote timestamp is not the actual remote timestamp but it is the same as the storedModifiedStamp, an 
	 *   upload without detecting a conflict will cause lost data on the remote side!
	 *   <li> If the detected remote timestamp is not the actual remote timestamp and the actual timestamp is the same as the 
	 *   storedModifiedStamp, a conflict will be indicated that doesn't actually exist
	 * </ul>
	 * 
	 * If the remoteFile is not the current cached version then the following problem occurs.  After the upload, the remote file is
	 * marked stale so that the up-to-date remote file can be retrieved with the updated actual timestamp.  Because the remoteFile 
	 * that was passed into this method is not the cached version, marking it stale will not mark the cached version stale and 
	 * thus, when a re-query of the file is done after the upload, the original cached version gets returned as opposed to a fresh
	 * version with the correct timestamp. 
	 * 
	 * <p>
	 * Because of these problems, it is recommended that, before calling upload(), the remoteFile is retrieved from the cache and is
	 * marked stale like the following example:
	 * 
	 * <code>
	 *    ...
	 *    // get the remote file from the cache
	 *    IRemoteFile remoteFile = fs.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
	 *    
	 *    // mark it stale
	 *    remoteFile.markStale(true);
	 *    
	 *    // re-query the remote file to make sure you have the latest
	 *    remoteFile = fs.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
	 *    
	 *    // call upload
	 *    upload(fs, remoteFile, ...);
	 *    .... 
	 * </code>
	 * 
	 * 
	 * @param fs the file subsystem that corresponds with the file to upload
	 * @param remoteFile the remote file location to upload to 
	 * @param tempFile the source temp file to upload
	 * @param properties the remote file properties of the file to upload
	 * @param storedModifiedStamp the last timestamp of the remote file for which a temp file was in synch with the remote file
	 * @param editable the wrapper that associates the remote file, temp file and editor together
	 * @param monitor the progress monitor
	 */
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

				// requery the file so get the new timestamp
				remoteFile.markStale(true);
				remoteFile =fs.getRemoteFileObject(remoteFile.getAbsolutePath(), monitor);
				
				IRemoteFile parent = remoteFile.getParentRemoteFile();


				long ts = remoteFile.getLastModified();
				
				// set the stored timestamp to be the same as the remote timestamp
				properties.setRemoteFileTimeStamp(ts);

				ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				// refresh
				if (parent != null)
				{
					registry.fireEvent(new SystemResourceChangeEvent(parent, ISystemResourceChangeEvents.EVENT_REFRESH, null));
				}
			
				registry.fireEvent(new SystemResourceChangeEvent(remoteFile, ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, remoteFile));
			
				
				// indicate that the temp file is no longer dirty
				properties.setDirty(false);
				if (editable.isDirty())
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
				final SystemIFileProperties fProperties = properties;
				
				// upload is run in a job, so the conflict action/dialog needs to run in UI thread
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						boolean closing = Workbench.getInstance().isClosing();						
						Shell shell = null;
						if (closing){
							shell = Display.getDefault().getActiveShell();
						}
						else {
							shell = RSEUIPlugin.getTheSystemRegistryUI().getShell();
						}
						SystemUploadConflictAction conflictAction = new SystemUploadConflictAction(shell, tFile, rFile, remoteNewer);
						conflictAction.run();
						if (fProperties.getDirty()){
							remoteEdit.updateDirtyIndicator();
						}
					}
				});
				
				
				
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}