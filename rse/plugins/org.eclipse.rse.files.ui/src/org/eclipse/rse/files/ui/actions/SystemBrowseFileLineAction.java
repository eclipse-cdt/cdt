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
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;


/**
 * Open a remote file as read-only
 */
public class SystemBrowseFileLineAction extends SystemEditFileLineAction {


	/**
	 * Constructor for SystemBrowseFileAction.
	 * @param text
	 * @param tooltip
	 * @param image
	 * @param parent
	 * @param editorId
	 * @param line
	 */
	public SystemBrowseFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId, 
					IRemoteFile remoteFile, int line, int charStart, int charEnd) {
		super(text, tooltip, image, parent, editorId, remoteFile, line, charStart, charEnd);
	}

	/**
	 * @see org.eclipse.rse.files.ui.actions.SystemEditFileAction#process(IRemoteFile)
	 */
	protected void process(IRemoteFile remoteFile) {
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, _editorId);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell(), true);
		handleGotoLine();
	}

}