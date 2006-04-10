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

package org.eclipse.rse.files.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.ISystemRemoteEditConstants;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;



public class SystemEditFilesAction extends SystemBaseAction implements ISystemRemoteEditConstants {

	
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
	

	
	/**
	 * Process the object: download file, open in editor, etc.
	 */
	protected void process(IRemoteFile remoteFile) 
	{
		String editorId = null;
		IEditorDescriptor des = getDefaultEditor(remoteFile);
		if (des != null)
		{
			editorId = des.getId();
		}
		
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, editorId);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell());
	}
}