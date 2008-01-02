/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)   - [187711] Link with Editor action for System View
 ********************************************************************************/
package org.eclipse.rse.internal.files.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.ui.IViewLinker;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class LinkWithSystemViewAction implements IViewActionDelegate {

	public class ViewLinker implements IViewLinker
	{
		public void link(IEditorPart editor, SystemView systemView)
		{
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput)
			{
				IFileEditorInput fileInput = (IFileEditorInput) input;
				fileInput.getFile();

				IFile file = fileInput.getFile();
				SystemIFileProperties properties = new SystemIFileProperties(file);
				Object rmtEditable = properties.getRemoteFileObject();
				Object remoteObj = null;
				ISubSystem subSystem = null;
				if (rmtEditable != null && rmtEditable instanceof ISystemEditableRemoteObject)
				{
					ISystemEditableRemoteObject editable = (ISystemEditableRemoteObject) rmtEditable;
					remoteObj = editable.getRemoteObject();
	
				}
				else
				{
					String subsystemId = properties.getRemoteFileSubSystem();
					String path = properties.getRemoteFilePath();
					if (subsystemId != null && path != null)
					{
						subSystem = RSECorePlugin.getTheSystemRegistry().getSubSystem(subsystemId);
						if (subSystem != null)
						{
							if (subSystem.isConnected())
							{
								try
								{
									remoteObj = subSystem.getObjectWithAbsoluteName(path, new NullProgressMonitor());
								}
								catch (Exception e)
								{
									return;
								}
							}
						}
					}
				}
				
				if (remoteObj != null)
				{	
					TreeItem item = (TreeItem)systemView.findFirstRemoteItemReference(remoteObj, null);
					if (item != null){
						systemView.getTree().setSelection(item);
					}
					else {
						// item does not exist in tree
						systemView.expandTo(subSystem, remoteObj);
						
					}
						
				}
			}
		}
	}
	
	private SystemViewPart _systemViewPart;
	private IAction _action;
	private IViewLinker _linker;
	
	public void init(IViewPart view) {
		_systemViewPart = (SystemViewPart)view;
		_linker = new ViewLinker();
	}

	public void run(IAction action) {
		if (_systemViewPart != null){
			_systemViewPart.setLinkingEnabled(action.isChecked(), _linker);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (_action == null) {
			action.setChecked(_systemViewPart.isLinkingEnabled());
			_action= action;
		}

	}

}
