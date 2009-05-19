/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [189130] Move SystemIFileProperties from UI to Core
 * David McKnight   (IBM)        - [209660] check for changed encoding before using cached file
 * David McKnight   (IBM)        - [189873] DownloadJob changed to DownloadAndOpenJob
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * David McKnight   (IBM)        - [276103] Files with names in different cases are not handled properly
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.internal.files.ui.view.DownloadAndOpenJob;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFile;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;



public class SystemEditFilesAction extends SystemBaseAction {

	
   private IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
   
	/**
	 * Constructor for SystemEditFilesAction
	 */
	public SystemEditFilesAction(String text, String tooltip, Shell parent) 
	{
		super(text, tooltip, parent);
		init();
	}
	

	
	/**
	 * Initialize the action
	 */
	private void init() 
	{
		allowOnMultipleSelection(true);
	}

	/**
	 * Get the local cache of the remote file, or <code>null</code> if none.
	 * @param remoteFile the remote file.
	 * @return the local cached resource, or <code>null</code> if none.
	 */
	private IFile getLocalResource(IRemoteFile remoteFile) 
	{
	    return (IFile)UniversalFileTransferUtility.getTempFileFor(remoteFile);
	}

	protected IEditorDescriptor getDefaultEditor(IRemoteFile remoteFile)
	{
		IFile localFile = getLocalResource(remoteFile);
		
		if (localFile == null) {
			return registry.getDefaultEditor(remoteFile.getName());
		}
		else {
			return IDE.getDefaultEditor(localFile);
		}
	}
	
	/**
	 * @see SystemBaseAction#run
	 */
	public void run() {
		
		IStructuredSelection selection = getSelection();
		
		Iterator iterator = selection.iterator();
		while (iterator.hasNext())
		{
		    Object obj = iterator.next();
		    if (obj instanceof IRemoteFile)
		    {
		        process((IRemoteFile)obj);
		    }
		}
	}
	
	private boolean isFileCached(ISystemEditableRemoteObject editable, IRemoteFile remoteFile)
	{
		// DY:  check if the file exists and is read-only (because it was previously opened
		// in the system editor)
		IFile file = editable.getLocalResource();
		SystemIFileProperties properties = new SystemIFileProperties(file);
		boolean newFile = !file.exists();
	
		// detect whether there exists a temp copy already
		if (!newFile && file.exists())
		{
			// we have a local copy of this file, so we need to compare timestamps
	
			// get stored modification stamp
			long storedModifiedStamp = properties.getRemoteFileTimeStamp();
	
			// get updated remoteFile so we get the current remote timestamp
			//remoteFile.markStale(true);
			IRemoteFileSubSystem subsystem = remoteFile.getParentRemoteFileSubSystem();
			try
			{
				remoteFile = subsystem.getRemoteFileObject(remoteFile.getAbsolutePath(), new NullProgressMonitor());
			}
			catch (Exception e)
			{
				
			}
	
			// get the remote modified stamp
			long remoteModifiedStamp = remoteFile.getLastModified();
	
			// get dirty flag
			boolean dirty = properties.getDirty();
	
			boolean remoteNewer = (storedModifiedStamp != remoteModifiedStamp);

			String remoteEncoding = remoteFile.getEncoding();
			String storedEncoding = properties.getEncoding();
			
			boolean encodingChanged = storedEncoding == null || !(remoteEncoding.equals(storedEncoding));

			boolean usedBinary = properties.getUsedBinaryTransfer();
			boolean isBinary = remoteFile.isBinary();
			
			return (!dirty && 
					!remoteNewer && 
					usedBinary == isBinary &&
					!encodingChanged);

		}
		return false;
	}
	
	/**
	 * Process the object: download file, open in editor, etc.
	 */
	protected void process(IRemoteFile remoteFile) {

		String editorId = null;
		IEditorDescriptor des = getDefaultEditor(remoteFile);
		if (des != null)
		{
			editorId = des.getId();
		}
		else
		{
			editorId = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		}
		
		SystemEditableRemoteFile editable = getEditableRemoteObject(remoteFile, des);
		if (editable == null){
			// case for cancelled operation when user was prompted to save file of different case
			return;
		}
		else
		{
			try
			{
				if (editable.checkOpenInEditor() != ISystemEditableRemoteObject.OPEN_IN_SAME_PERSPECTIVE)
				{						
					if (isFileCached(editable, remoteFile))
					{
						editable.openEditor();
					}
					else
					{
						DownloadAndOpenJob oJob = new DownloadAndOpenJob(editable, false);
						oJob.schedule();
					}
				}
				else
				{
					editable.setLocalResourceProperties();
					editable.openEditor();
				}
			}
			catch (Exception e)
			{
			}
					
		}
	}



	private SystemEditableRemoteFile getEditableRemoteObject(Object element, IEditorDescriptor descriptor)
	{
		SystemEditableRemoteFile editable = null;
		RemoteFile remoteFile = (RemoteFile) element;
		if (remoteFile.isFile())
		{
			try
			{
				IFile file = (IFile)UniversalFileTransferUtility.getTempFileFor(remoteFile);
				if (file != null)
				{
					SystemIFileProperties properties = new SystemIFileProperties(file);
					
					Object obj = properties.getRemoteFileObject();
					if (obj != null && obj instanceof SystemEditableRemoteFile)
					{
						editable = (SystemEditableRemoteFile) obj;
						
						String remotePath = remoteFile.getAbsolutePath();
						String replicaRemotePath = editable.getAbsolutePath();
						// first make sure that the correct remote file is referenced (might be difference because of different case)
						if (!replicaRemotePath.equals(remotePath)){ // for bug 276103
							
							IEditorPart editor = editable.getEditorPart();
							boolean editorWasClosed = false;
							if (editor.isDirty()){
								editorWasClosed = editor.getEditorSite().getPage().closeEditor(editor, true);
								if (editorWasClosed)
									editable.doImmediateSaveAndUpload();								
							}
							else {
								editorWasClosed = editor.getEditorSite().getPage().closeEditor(editor, true);
							}
							
							if (!editorWasClosed){
								// use cancelled operation so we need to get out of here
								return null;
							}
							
							try {
								IFile originalFile = editable.getLocalResource();
								originalFile.delete(true, new NullProgressMonitor());												
							}
							catch (CoreException e){
							}
							// fall through and let the new editable get created
						}
						else {					
							return editable;
						}
					}
				}
				
				if (descriptor != null){
					editable = new SystemEditableRemoteFile(remoteFile, descriptor);
				}
				else {
					editable = new SystemEditableRemoteFile(remoteFile);
				}
			}
			catch (Exception e)
			{
			}
		}
		return editable;
	}

}
