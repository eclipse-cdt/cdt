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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;


public class SystemDoubleClickEditAction extends SystemBaseAction
{



	private Object element;

	/**
	 * Constructor for SystemDoubleClickEditAction
	 */
	public SystemDoubleClickEditAction(Object element)
	{
		super(null, null);
		this.element = element;
	}
	
	protected IEditorRegistry getEditorRegistry()
	{
		return RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
	}
	
	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
	/**
	 * @see SystemBaseAction#run()
	 */
	public void run()
	{

		IRemoteFile remoteFile = null;

		if ((element == null) || !(element instanceof IRemoteFile))
			return;
		else
			remoteFile = (IRemoteFile) element;

		/* DKM - use Eclipse default, instead
		// open LPEX editor on double click if remote file is a text file
		if (remoteFile.isText()) {
			IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();
			IEditorDescriptor descriptor = registry.findEditor(ISystemTextEditorConstants.SYSTEM_TEXT_EDITOR_ID);
			String id = descriptor.getId();
			SystemEditFileAction editAction = new SystemEditFileAction(null, null, null, null, id);
			editAction.setSelection(new StructuredSelection(element));
			editAction.run();
		}
		else {			// open the system editor on double click if remote file is a text file
			SystemEditFilePlatformAction platformEditAction = new SystemEditFilePlatformAction(null, null, null, null);
			platformEditAction.setSelection(new StructuredSelection(element));
			platformEditAction.run();
		}
		*/

		// DKM, use Eclipse default
		String fileName = remoteFile.getName();
		IEditorRegistry registry = getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(fileName);
		if (descriptor == null)
		{
			descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			
		}
		String id = descriptor.getId();
		SystemEditFileAction editAction = new SystemEditFileAction(null, null, null, null, id);
		editAction.setSelection(new StructuredSelection(element));
		editAction.run();
	}
}