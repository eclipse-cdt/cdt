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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.ISystemRemoteEditConstants;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;



public class SystemEditFileAction extends SystemBaseAction implements ISystemRemoteEditConstants {

	
	protected String _editorId;
	
	/**
	 * Constructor for SystemEditFileAction
	 */
	public SystemEditFileAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId) {
		super(text, tooltip, null, image, parent);
		init();
		_editorId = editorId;
	}
	
	/**
	 * Constructor for SystemEditFileAction
	 */
	public SystemEditFileAction(String text, String tooltip, ImageDescriptor image, int style, Shell parent, String editorId) {
		super(text, tooltip, null, image, style, parent);
		init();
		_editorId = editorId;
	}
	
	/**
	 * Initialize the action
	 */
	private void init() {
		allowOnMultipleSelection(false);
	}

	
	/**
	 * @see SystemBaseAction#run
	 */
	public void run() {
		IStructuredSelection selection = getSelection();
		
		if (selection.size() != 1)
			return; 
		
		Object element = getFirstSelection();
		
		if (element == null)
			return;
		else if (!(element instanceof IRemoteFile))
			return;
	
		process((IRemoteFile)element);
	}
	
	
	/**
	 * Process the object: download file, open in editor, etc.
	 */
	protected void process(IRemoteFile remoteFile) {
			
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, _editorId);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell());
	}
}