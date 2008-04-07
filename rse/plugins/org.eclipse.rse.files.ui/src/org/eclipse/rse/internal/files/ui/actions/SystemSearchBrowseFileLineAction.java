/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteSearchResult;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;


public class SystemSearchBrowseFileLineAction extends SystemSearchEditFileLineAction {

	/**
	 * Constructor to create an edit action that jumps to a file line.
	 * @param text the label for the action.
	 * @param tooltip the tooltip for the action.
	 * @param image the image for the action.
	 * @param parent the parent shell.
	 * @param editorDescriptor the editor id.
	 * @param remoteFile the remote file that is to be opened.
	 * @param searchResult the line number.
	 */
	public SystemSearchBrowseFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, IEditorDescriptor editorDescriptor, IRemoteFile remoteFile, IRemoteSearchResult searchResult) {
		super(text, tooltip, image, parent, editorDescriptor, remoteFile, searchResult);
	}
	
	/**
	 * @see org.eclipse.rse.internal.files.ui.actions.SystemEditFileAction#process(IRemoteFile)
	 */
	protected void process(IRemoteFile remoteFile) {
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, _editorDescriptor);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell(), true);
		handleGotoLine(_remoteFile, _searchResult);
	}
}
