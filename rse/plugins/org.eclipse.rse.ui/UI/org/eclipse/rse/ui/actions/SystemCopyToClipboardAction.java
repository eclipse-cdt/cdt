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

package org.eclipse.rse.ui.actions;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemDropActionDelegate;
import org.eclipse.rse.ui.view.SystemViewDataDropAdapter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;


/**
 * Copy selected objects to clipboard action.
 */
public class SystemCopyToClipboardAction extends SystemBaseAction implements  ISystemMessages, IValidatorRemoteSelection
{


	private IStructuredSelection _selection;
	private Clipboard _clipboard;

	/**
	 * Constructor
	 */
	public SystemCopyToClipboardAction(Shell shell, Clipboard clipboard)
	{
		super(SystemResources.ACTION_COPY_LABEL,
			  PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY), 
		      shell);

		_clipboard = clipboard;

		allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(SystemPlugin.HELPPREFIX+"actn0116");
	}

	public void run()
	{
		if (_selection != null)
		{
			copySelectionToClipboard(_selection);
		}
	}

	private void copySelectionToClipboard(IStructuredSelection ss)
	{
		Iterator iterator = ss.iterator();

		// marshall data
		StringBuffer textStream = new StringBuffer("");
		StringBuffer dataStream = new StringBuffer("");
		ArrayList fileNames = new ArrayList();
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();

		while (iterator.hasNext())
		{
			Object dragObject = iterator.next();

			if (dragObject instanceof IAdaptable)
			{
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) dragObject).getAdapter(ISystemViewElementAdapter.class);
				
				if (adapter != null)
				{					
					String text = adapter.getAlternateText(dragObject);
					textStream.append(getTextTransferPrepend(dragObject, adapter));
					textStream.append(text);	
					textStream.append(getTextTransferAppend(dragObject, adapter));
					
					if (adapter.canDrag(dragObject))
					{
						// get the subsystem id					
						ISubSystem subSystem = adapter.getSubSystem(dragObject);
						String subSystemId = registry.getAbsoluteNameForSubSystem(subSystem);

						dataStream.append(subSystemId);
						dataStream.append(":");

						String objectId = adapter.getAbsoluteName(dragObject);
						dataStream.append(objectId);

						if (iterator.hasNext())
						{
							dataStream.append(SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
						}

						/** FIXME - files can't be coupled to systems.core!
						// support for external copy for local files
						if (dragObject instanceof IRemoteFile)
						{
							IRemoteFile file = (IRemoteFile) dragObject;

							String connectionType = file.getParentRemoteFileSubSystem().getHost().getSystemType();
							
							if (connectionType.equals("Local"))
							{
								fileNames.add(file.getAbsolutePath());
							}
						}
						*/
					}
				}
			}
		}

		PluginTransferData data = new PluginTransferData(SystemDropActionDelegate.ID, dataStream.toString().getBytes());

		// put data in clipboard
		if (fileNames.size() == 0)
		{
			_clipboard.setContents(new Object[] { data, textStream.toString() }, new Transfer[] { PluginTransfer.getInstance(), TextTransfer.getInstance()});
		}
		else
		{		
			String[] ft = new String[fileNames.size()];
			for (int i = 0; i < ft.length; i++)
			{
				ft[i] = (String) fileNames.get(i);								
			}

			_clipboard.setContents(new Object[] { data, ft, textStream.toString() }, new Transfer[] { PluginTransfer.getInstance(), FileTransfer.getInstance(), TextTransfer.getInstance()});
		}
	}
	
	/**
	 * Returns the string to prepend for the selected object for text transfer.
	 * Default implementation returns the empty string.
	 * @param obj the selected object.
	 * @param adapter the adapter of the selected object.
	 * @return the string to prepend for the selected object.
	 */
	protected String getTextTransferPrepend(Object obj, ISystemViewElementAdapter adapter) {
	    return "";
	}
	
	/**
	 * Returns the string to append for the selected object for text transfer.
	 * Default implementation returns the system line separator (i.e. the line.separator property).
	 * @param obj the selected object.
	 * @param adapter the adapter of the selected object.
	 * @return the string to append for the selected object.
	 */
	protected String getTextTransferAppend(Object obj, ISystemViewElementAdapter adapter) {
	    return System.getProperty("line.separator");
	}

	/**
	 * The user has selected a remote object. Return null if OK is to be enabled, or a SystemMessage
	 *  if it is not to be enabled. The message will be displayed on the message line.
	 * <p>
	 * This is overridden in SystemMoveRemoteFileAction
	 */
	public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObjects)
	{
		return null;
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We simply ensure every selected object has a system view element adapter.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Iterator e = ((IStructuredSelection) selection).iterator();
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (selectedObject instanceof IAdaptable)
			{
				IAdaptable adaptable = (IAdaptable) selectedObject;
				ISystemViewElementAdapter va = (ISystemViewElementAdapter) (adaptable.getAdapter(ISystemViewElementAdapter.class));
				if (va != null)
				{
					enable = va.canDrag(selectedObject);
				}
				else
				{
					enable = false;
				}
			}
			else
			{
				enable = false;
			}
		}

		if (enable)
		{
			_selection = selection;
		}
		return enable;
	}

}