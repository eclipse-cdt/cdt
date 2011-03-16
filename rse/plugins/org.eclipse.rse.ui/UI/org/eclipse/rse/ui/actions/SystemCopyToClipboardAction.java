/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * David McKnight   (IBM)        - [248339] [dnd][encodings] Cannot drag&drop / copy&paste files or folders with turkish or arabic names
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 *******************************************************************************/

package org.eclipse.rse.ui.actions;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemDropActionDelegate;
import org.eclipse.rse.internal.ui.view.SystemViewDataDropAdapter;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.internal.model.SystemRegistryUI;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;
import org.eclipse.ui.part.ResourceTransfer;


/**
 * Copy selected objects to clipboard action.
 */
public class SystemCopyToClipboardAction extends SystemBaseAction implements  IValidatorRemoteSelection
{
	private IStructuredSelection _selection;
	private boolean  _doResourceTransfer = false; // determines whether or not to download on copy

	/**
	 * Constructor
	 * -will deprecate this later since we don't use this clipboard now
	 */
	public SystemCopyToClipboardAction(Shell shell, Clipboard clipboard){
		this(shell);
	}
	
	/**
	 * Constructor
	 */
	private SystemCopyToClipboardAction(Shell shell)
	{
		super(SystemResources.ACTION_COPY_LABEL,
			  PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY), 
		      shell);
		setToolTipText(SystemResources.ACTION_COPY_TOOLTIP);
		allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0116"); //$NON-NLS-1$
	}

	public void run()
	{
		if (_selection != null)
		{
			copySelectionToClipboard(_selection);
		}
	}
	
	private IResource getResource(IAdaptable dragObject)
	{
		IResource resource = null;
		ISystemViewElementAdapter viewAdapter = (ISystemViewElementAdapter) dragObject.getAdapter(ISystemViewElementAdapter.class);
		ISystemRemoteElementAdapter remoteAdapter = (ISystemRemoteElementAdapter)dragObject.getAdapter(ISystemRemoteElementAdapter.class);
		
		if (remoteAdapter != null)
		{
			if (remoteAdapter.canEdit(dragObject))
				{				
					ISystemEditableRemoteObject editable = remoteAdapter.getEditableRemoteObject(dragObject);
					// corresponds to a file
					IFile file = editable.getLocalResource();
					if (!file.exists())
					{
						LazyDownloadJob job = new LazyDownloadJob(editable);
						job.schedule();
					}
					resource = file;
				}
			else if (viewAdapter != null)
			{
				if (viewAdapter.hasChildren(dragObject)) 
				{
					IContainer parentFolder = null;
					// corresponds to a folder
					Object[] children = viewAdapter.getChildren(dragObject, new NullProgressMonitor());
					for (int i = 0; i < children.length; i++)
					{
						IAdaptable child = (IAdaptable)children[i];
						IResource childResource = getResource(child);
						if (childResource != null)
						{							
							parentFolder = childResource.getParent();
							if (!parentFolder.exists())
							{
								try
								{
									parentFolder.touch(new NullProgressMonitor());
								}
								catch (Exception e)
								{
									
								}
							
							}
						}
					}
					
					
					resource = parentFolder;
				}				
			}
		}		
		return resource;
	}
	

	private void copySelectionToClipboard(IStructuredSelection ss)
	{
		Iterator iterator = ss.iterator();

		// marshall data
		StringBuffer textStream = new StringBuffer(""); //$NON-NLS-1$
		StringBuffer dataStream = new StringBuffer(""); //$NON-NLS-1$
		
		ArrayList fileNames = new ArrayList();
		ArrayList resources = new ArrayList();
		
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

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
						ISubSystem subSystem = null;
					    if (dragObject instanceof ISubSystem)
					    {
					        subSystem = (ISubSystem)dragObject;
					        String subSystemId = RSECorePlugin.getTheSystemRegistry().getAbsoluteNameForSubSystem(subSystem);
							dataStream.append(subSystemId);
					    }
					    else if (dragObject instanceof IHost)
					    {
					        IHost connection = (IHost)dragObject;
					        String connectionId = RSECorePlugin.getTheSystemRegistry().getAbsoluteNameForConnection(connection);
					        dataStream.append(connectionId);
					    }
					    else
					    {
					    	// get the subsystem id					
							subSystem = adapter.getSubSystem(dragObject);
							String subSystemId = registry.getAbsoluteNameForSubSystem(subSystem);
		
							dataStream.append(subSystemId);
							dataStream.append(":"); //$NON-NLS-1$
		
							String objectId = adapter.getAbsoluteName(dragObject);
							dataStream.append(objectId);
		
							if (iterator.hasNext())
							{
								dataStream.append(SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
							}
		
							if (_doResourceTransfer)
							{
								IResource resource = getResource((IAdaptable)dragObject);
								if (resource != null)
								{
									resources.add(resource);
									
									String fileName = resource.getLocation().toOSString();
									fileNames.add(fileName);
								}							
							}
					    }
					}
				}
			}
		}

		byte[] bytes = null;
		try {
			bytes = dataStream.toString().getBytes("UTF-8"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e){
			bytes = dataStream.toString().getBytes();
		}
		PluginTransferData data = new PluginTransferData(SystemDropActionDelegate.ID, bytes);

		// put data in clipboard
		if (_doResourceTransfer && resources.size() > 0)
		{
			IResource[] ft = new IResource[resources.size()];
			for (int i = 0; i < ft.length; i++)
			{
				ft[i] = (IResource) resources.get(i);								
			}
			
			String[] fn = new String[fileNames.size()];
			for (int j = 0; j < fn.length; j++)
			{
				fn[j] = (String)fileNames.get(j);
			}
			
			Clipboard clipboard = SystemRegistryUI.getInstance().getSystemClipboard();
			clipboard.setContents(new Object[] { data, ft, fn, textStream.toString() }, new Transfer[] { PluginTransfer.getInstance(), ResourceTransfer.getInstance(), FileTransfer.getInstance(), TextTransfer.getInstance()});
			clipboard.dispose();
		}		
		else
		{		
			String[] ft = new String[fileNames.size()];
			for (int i = 0; i < ft.length; i++)
			{
				ft[i] = (String) fileNames.get(i);								
			}

			Clipboard clipboard = SystemRegistryUI.getInstance().getSystemClipboard();

			if (ft.length > 0)
			{
				clipboard.setContents(new Object[] { data, ft, textStream.toString() }, new Transfer[] { PluginTransfer.getInstance(), FileTransfer.getInstance(), TextTransfer.getInstance()});				
			}
			else
			{
				clipboard.setContents(new Object[] { data, textStream.toString() }, new Transfer[] { PluginTransfer.getInstance(), TextTransfer.getInstance()});
			}
			clipboard.dispose();
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
	    return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns the string to append for the selected object for text transfer.
	 * Default implementation returns the system line separator (i.e. the line.separator property).
	 * @param obj the selected object.
	 * @param adapter the adapter of the selected object.
	 * @return the string to append for the selected object.
	 */
	protected String getTextTransferAppend(Object obj, ISystemViewElementAdapter adapter) {
	    return System.getProperty("line.separator"); //$NON-NLS-1$
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
		Iterator e = selection.iterator();
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
