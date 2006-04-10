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

package org.eclipse.rse.files.ui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult;
import org.eclipse.swt.widgets.Shell;


public class SystemSearchBrowseFileLineAction extends SystemSearchEditFileLineAction {

	/**
	 * Constructor to create an edit action that jumps to a file line.
	 * @param text the label for the action.
	 * @param tooltip the tooltip for the action.
	 * @param image the image for the action.
	 * @param parent the parent shell.
	 * @param editorId the editor id.
	 * @param remoteFile the remote file that is to be opened.
	 * @param line the line number.
	 */
	public SystemSearchBrowseFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, String editorId, IRemoteFile remoteFile, IRemoteSearchResult searchResult) {
		super(text, tooltip, image, parent, editorId, remoteFile, searchResult);
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.actions.SystemSearchEditFileAction#process(IRemoteFile)
	 */
	protected void process(IRemoteFile remoteFile) {
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, _editorId);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell(), true);
		handleGotoLine(_remoteFile, _searchResult);
	}
}