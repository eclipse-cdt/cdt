/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David Dykstal (IBM) - [142065] fix drag and drop on Mac OS X
 * Kevin Doyle (IBM) - [187536] Drag & Drop file to Editor launchs file in system editor
 * David McKnight   (IBM)        - [248339] [dnd][encodings] Cannot drag&drop / copy&paste files or folders with turkish or arabic names
 * David McKnight   (IBM)        - [407428] [shells] allow dragging of files in shell
 * David McKnight   (IBM)        - [420190] Dragging anything other than a file causes Null Pointer
 *******************************************************************************/
 
package org.eclipse.rse.internal.ui.view;
 
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.LazyDownloadJob;
import org.eclipse.rse.ui.view.ISystemEditableRemoteObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
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
	private ISelection _selection = null; // set this on dragStart, set to null on dragFinished
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
		_selection = null; // drag has finished, forget the selection
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
	        subSystem = adapter.getSubSystem(dragObject);	   
	        if (adapter instanceof ISystemRemoteElementAdapter){
	        	ISystemRemoteElementAdapter rAdapter = (ISystemRemoteElementAdapter)adapter;
	        	if (rAdapter.canEdit(dragObject)){
	        		ISystemEditableRemoteObject editable = ((ISystemRemoteElementAdapter)adapter).getEditableRemoteObject(dragObject);
	        		if (editable != null){
	        			subSystem = editable.getSubSystem();	   
	        		}
	        	}
	        } 
			if (subSystem != null)
			{
			    String subSystemId = RSECorePlugin.getTheSystemRegistry().getAbsoluteNameForSubSystem(subSystem);
				dataStream.append(subSystemId);
				
			}

		    String objectId = adapter.getAbsoluteName(dragObject);
			dataStream.append(":"); //$NON-NLS-1$
			dataStream.append(objectId);
		}
	}

	public void dragStart(DragSourceEvent event)
	{
		/*
		 * Remember the selection at drag start. This is the only point at which the selection is valid
		 * during the drag operations on all platforms.
		 */
		_selection = _selectionProvider.getSelection();
		if (_selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) _selection;
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
							event.detail = DND.DROP_COPY;
							////FIXME as per bug [142947], drag under feedback now works differently in SWT 
							//event.feedback = DND.FEEDBACK_INSERT_AFTER;
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
	
		/*
		 * We cannot request the selection from the selection provider at this point since
		 * on some platforms (particularly Mac OS X) the selection is forgotten by the underlying
		 * OS control immediately after the drag is started. This call is invoked at the end
		 * of the drag operation but just before the corresponding drop call in the drop adapter.
		 * Thus, we must remember the selection at drag start.
		 */
		if (_selection instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) _selection;

			if (PluginTransfer.getInstance().isSupportedType(event.dataType))
			{
				StringBuffer dataStream = new StringBuffer(""); //$NON-NLS-1$
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

				byte[] bytes = null;
				try {
					bytes = dataStream.toString().getBytes("UTF-8"); //$NON-NLS-1$
				}
				catch (UnsupportedEncodingException e){
					bytes = dataStream.toString().getBytes();
				}
				
				PluginTransferData data = new PluginTransferData("org.eclipse.rse.ui.view.DropActions", bytes); //$NON-NLS-1$
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
					if (dragObject instanceof IAdaptable)
					{
						ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) dragObject).getAdapter(ISystemViewElementAdapter.class);
						if (adapter.canDrag(dragObject))
						{
							IResource resource = getResource((IAdaptable)dragObject);
							if (resource != null)
							{								
								String fileName = resource.getLocation().toOSString();
								fileNames[i] = fileName;
								i++;
							}		
						}
					}
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
				
				IEditorRegistry editRegistry = RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
											
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
										if (preferredEditor == null || preferredEditor.isOpenExternal())
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
					event.data = new EditorInputTransfer.EditorInputData[0];
					event.detail = DND.DROP_NONE;
				}
			}
		}
	}
	
	protected IEditorRegistry getEditorRegistry()
	{
		return RSEUIPlugin.getDefault().getWorkbench().getEditorRegistry();
	}

	protected IEditorDescriptor getDefaultTextEditor()
	{
		IEditorRegistry registry = getEditorRegistry();
		return registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
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
					// this is a drag and drop to windows explorer
					//  because we're dealing with file paths we need to force this to complete before allowing the drop
					//  so instead of doing the job, I'm forcing the transfer on this thread
					LazyDownloadJob job = new LazyDownloadJob(editable);
					job.run(new NullProgressMonitor());
					//job.setPriority(Job.INTERACTIVE);
					//job.schedule();
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
	
}