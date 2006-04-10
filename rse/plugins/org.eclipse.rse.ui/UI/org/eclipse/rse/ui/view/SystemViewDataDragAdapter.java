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

package org.eclipse.rse.ui.view;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;


/**
 *  Drag adapter for dragging objects in the Systems views
 * 
 */
public class SystemViewDataDragAdapter extends DragSourceAdapter
{


	ISelectionProvider _selectionProvider;
	public static final char CONNECTION_DELIMITER = ':';
	public static final char RESOURCE_SEPARATOR = '|';

	/**
	 * Constructor for drag adapter
	 * 
	 */
	public SystemViewDataDragAdapter(ISelectionProvider provider)
	{
		_selectionProvider = provider;
	}

	public void dragFinished(DragSourceEvent event)
	{
		if (event.doit == false)
		{
			return;
		}
	}
	

	private void serializeObject(Object dragObject, ISystemDragDropAdapter adapter, StringBuffer dataStream)
	{
		// get the subsystem id		
	    ISubSystem subSystem = null;
	    if (dragObject instanceof ISubSystem)
	    {
	        subSystem = (ISubSystem)dragObject;
	        String subSystemId = SystemPlugin.getTheSystemRegistry().getAbsoluteNameForSubSystem(subSystem);
			dataStream.append(subSystemId);
	    }
	    else if (dragObject instanceof IHost)
	    {
	        IHost connection = (IHost)dragObject;
	        String connectionId = SystemPlugin.getTheSystemRegistry().getAbsoluteNameForConnection(connection);
	        dataStream.append(connectionId);
	    }
	    else
	    {
	        subSystem = adapter.getSubSystem(dragObject);	   
	    
			if (subSystem != null)
			{
			    String subSystemId = SystemPlugin.getTheSystemRegistry().getAbsoluteNameForSubSystem(subSystem);
				dataStream.append(subSystemId);
				
			}

		    String objectId = adapter.getAbsoluteName(dragObject);
			dataStream.append(":");
			dataStream.append(objectId);
		}
	}

	public void dragStart(DragSourceEvent event)
	{
		ISelection selection = _selectionProvider.getSelection();
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) selection;
			Iterator iterator = ss.iterator();
			while (iterator.hasNext())
			{
				Object dragObject = iterator.next();

				if (dragObject instanceof IAdaptable)
				{
					ISystemDragDropAdapter adapter = (ISystemDragDropAdapter) ((IAdaptable) dragObject).getAdapter(ISystemDragDropAdapter.class);
					if (adapter == null || !adapter.canDrag(dragObject))
					{
						event.doit = false;
						event.detail = DND.DROP_NONE;
					}
					else
					{
						if (EditorInputTransfer.getInstance().isSupportedType(event.dataType))
						{
							if (adapter instanceof ISystemRemoteElementAdapter)
							{
								if (((ISystemRemoteElementAdapter)adapter).canEdit(dragObject))
								{
									event.doit = true;									
								}
								else
								{
									event.doit = false;
									event.detail = DND.DROP_NONE;
									return;
								}
							}
						}
						else
						{
							event.doit = true;
						}
					}
				}
				else
				{
					event.doit = false;
					event.detail = DND.DROP_NONE;
					return;
				}
			}
		}

		//event.doit = true;
	}


	/**
	 * Method for determining the source (drag) object(s) and
	 * encoding those objects in a byte[].  We encode the profile,
	 * connection and subsystem, and then we use ISystemDragDropAdapter.getAbsoluteName()
	 * to determine the ID for the object within it's subsystem.
	 */
	public void dragSetData(DragSourceEvent event)
	{
		ISelection selection = _selectionProvider.getSelection();
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) selection;

			if (PluginTransfer.getInstance().isSupportedType(event.dataType))
			{
				StringBuffer dataStream = new StringBuffer("");
				Iterator iterator = ss.iterator();
				while (iterator.hasNext())
				{
					Object dragObject = iterator.next();
					
					if (dragObject instanceof IAdaptable)
					{
						ISystemDragDropAdapter adapter = (ISystemDragDropAdapter) ((IAdaptable) dragObject).getAdapter(ISystemDragDropAdapter.class);
						if (adapter != null)
						{
							if (adapter.canDrag(dragObject))
							{
								serializeObject(dragObject, adapter, dataStream);

								if (iterator.hasNext())
								{
									dataStream.append(RESOURCE_SEPARATOR);
								}
							}
						}
					}
				}

				PluginTransferData data = new PluginTransferData("org.eclipse.rse.ui.view.DropActions", dataStream.toString().getBytes());
				event.data = data;
				if (dataStream.length() > 0)
				{
					event.doit = true;
					event.detail = DND.DROP_COPY;
				}
				else
				{
					event.doit = false;
					event.detail = DND.ERROR_CANNOT_INIT_DRAG;
				}
			}
			else if (FileTransfer.getInstance().isSupportedType(event.dataType))
			{
				// external drag and drop
				String[] fileNames = new String[ss.size()];
				Iterator iterator = ss.iterator();
				int i = 0;
				while (iterator.hasNext())
				{
					Object dragObject = iterator.next();
					/** FIXME - IREmoteFile is systems.core independent now
					if (dragObject instanceof IRemoteFile)
					{
						IRemoteFile file = (IRemoteFile) dragObject;

						String connectionType = file.getParentRemoteFileSubSystem().getHost().getSystemType();
						if (connectionType.equals("Local"))
						{
							fileNames[i] = file.getAbsolutePath();
							i++;
						}
					}
					*/
				}
				if (i > 0)
				{
					event.data = fileNames;
				}
			}
			else if (TextTransfer.getInstance().isSupportedType(event.dataType))
			{
				String[] texts = new String[ss.size()];
				Iterator iterator = ss.iterator();
				int i = 0;
				while (iterator.hasNext())
				{
					Object dragObject = iterator.next();
					if (dragObject instanceof IAdaptable)
					{
						ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) dragObject).getAdapter(ISystemViewElementAdapter.class);
						if (adapter != null)
						{
							texts[i] = adapter.getText(dragObject);
							i++;
						}
					}
				}
				if (i > 0)
				{
					event.data = texts;
				}
			}
			else if (EditorInputTransfer.getInstance().isSupportedType(event.dataType))
			{
				EditorInputTransfer.EditorInputData[] inputData = new EditorInputTransfer.EditorInputData[ss.size()];
				Iterator iterator = ss.iterator();
				int i = 0;
				
				IEditorRegistry editRegistry = SystemPlugin.getDefault().getWorkbench().getEditorRegistry();
											
				while (iterator.hasNext())
				{
					Object dragObject = iterator.next();
					
					Object adapterObj = ((IAdaptable)dragObject).getAdapter(ISystemRemoteElementAdapter.class);					
					if (adapterObj != null)										
					{
						ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)adapterObj;
						if (adapter.canEdit(dragObject))
						{
							ISystemEditableRemoteObject editable = adapter.getEditableRemoteObject(dragObject);
							if (editable != null)
							{
								try
								{		
									Shell shell = event.display.getActiveShell();
									if (editable.download(shell))
									{
										editable.addAsListener();
										editable.setLocalResourceProperties();
									
										IFile theFile = editable.getLocalResource();	
									
										IEditorDescriptor preferredEditor = editRegistry.getDefaultEditor(theFile.getName()); // may be null
										if (preferredEditor == null)
										{
											preferredEditor = getDefaultTextEditor();
																						
										}
									
										FileEditorInput fileInput = new FileEditorInput(theFile);
										inputData[i] = EditorInputTransfer.createEditorInputData(preferredEditor.getId(), fileInput);
										i++;																
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				if (i > 0)
				{
					event.data = inputData;
				}
				else
				{
					event.detail = DND.DROP_NONE;
				}
			}
		}
	}
	
	protected IEditorRegistry getEditorRegistry()
	{
		return SystemPlugin.getDefault().getWorkbench().getEditorRegistry();
	}

	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor");
	}
}