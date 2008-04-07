/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;

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
	public SystemBrowseFileLineAction(String text, String tooltip, ImageDescriptor image, Shell parent, IEditorDescriptor editorDescriptor, 
					IRemoteFile remoteFile, int line, int charStart, int charEnd) {
		super(text, tooltip, image, parent, editorDescriptor, remoteFile, line, charStart, charEnd);
	}

	/**
	 * @see org.eclipse.rse.internal.files.ui.actions.SystemEditFileAction#process(IRemoteFile)
	 */
	protected void process(IRemoteFile remoteFile) {
		SystemEditableRemoteFile editableFile = new SystemEditableRemoteFile(remoteFile, _editorDescriptor);
		editableFile.open(SystemBasePlugin.getActiveWorkbenchShell(), true);
		handleGotoLine();
	}

}
