/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * David McKnight (IBM) - [192704] work around drag&drop issues from Project Explorer
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [234924] [ftp][dnd][Refresh] Copy/Paste file from Package Explorer doesn't refresh folder
 * David McKnight   (IBM)        - [248339] [dnd][encodings] Cannot drag&drop / copy&paste files or folders with turkish or arabic names
 ********************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISystemDragDropAdapter;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.internal.model.SystemScratchpad;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.PluginTransferData;


/**
 * Drop adapter for dropping objects in the Systems views.
 * 
 */
public class SystemViewDataDropAdapter 
//extends PluginDropAdapter
extends ViewerDropAdapter
{
	protected Shell shell;
	protected long hoverStart = 0;

	protected static final long hoverThreshold = 1500;
	public static final char CONNECTION_DELIMITER = ':';
	public static final String RESOURCE_SEPARATOR = "|"; //$NON-NLS-1$

	protected int _sourceType = SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE;
	/**
	 * Constructor for the drop adapter
	 * 
	 */
	public SystemViewDataDropAdapter(StructuredViewer viewer)
	{
		super(viewer);
		setFeedbackEnabled(true);
	}

	/**
	 * Method for determining whether the drop target is a valid target.
	 * We could do extra validation on the source (drag) object(s), although
	 * that could get expensive.  The SWT drag and drop framework calls this
	 * method.
	 * 
	 */
	public boolean validateDrop(Object target, int operation, TransferData transferType)
	{
	    if (target == null)
	    {
	        target = this.getViewer().getInput();
	    }
		if (target instanceof IAdaptable)
		{
			ISystemDragDropAdapter adapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);
			if (adapter != null)
			{
				return adapter.canDrop(target);
			}
		}

		return false;
	}
	
	
	// DKM - hack to see if project explorer resources can be dropped in RSE
	private boolean isLocalSelectionResources(PluginTransferData transferData)
	{
		byte[] result = transferData.getData();

		// get the sources	
		String[] tokens = (new String(result)).split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR); //$NON-NLS-1$
		
		ArrayList srcObjects = new ArrayList();
		for (int i = 0;i < tokens.length; i++)
		{
			String srcStr = tokens[i];
			if (srcStr.equals("org.eclipse.ui.navigator.ProjectExplorer")) //$NON-NLS-1$
			{
				return true;
			}
		}
		return false;
	}

	private ArrayList getRSESourceObjects(PluginTransferData transferData)
	{
		byte[] result = transferData.getData();

		// get the sources	
		String str = null;
		try {
			str = new String(result, "UTF-8"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			str = new String(result);
		}
		
		String[] tokens = str.split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR); //$NON-NLS-1$
		

		ArrayList srcObjects = new ArrayList();
		for (int i = 0;i < tokens.length; i++)
		{
			String srcStr = tokens[i];

			{
				Object srcObject = getObjectFor(srcStr);
				srcObjects.add(srcObject);
			}
		}
		return srcObjects;
	}
	
	private ArrayList getSourceObjects(Object data)
	{
		ArrayList srcObjects = new ArrayList();

		if (srcObjects.isEmpty())
		{
			if (data instanceof PluginTransferData)
			{
				PluginTransferData transferData = (PluginTransferData) data;
				if (isLocalSelectionResources(transferData))
				{
					IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
					Iterator selIt = selection.iterator();
					while (selIt.hasNext())
					{
						srcObjects.add(selIt.next());
					}
					_sourceType = SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE;
				}
				else
				{				
					srcObjects = getRSESourceObjects(transferData);
					_sourceType = SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE;
				}
			}
			// different kind of data            
			else if (data instanceof IResource[])
			{
				IResource[] resources = (IResource[]) data;
				for (int i = 0; i < resources.length; i++)
				{
					srcObjects.add(resources[i]);
				}
				_sourceType = SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE;
			}
			else if (data instanceof EditorInputTransfer.EditorInputData[])
			{
				EditorInputTransfer.EditorInputData[] editorInput = (EditorInputTransfer.EditorInputData[])data;
				for (int i = 0; i < editorInput.length; i++)
				{
					IPersistableElement inData = editorInput[i].input.getPersistable();
					if (inData instanceof FileEditorInput){
						IFile file = ((FileEditorInput)inData).getFile();
						srcObjects.add(file);
					}
				}	
				_sourceType = SystemDNDTransferRunnable.SRC_TYPE_ECLIPSE_RESOURCE;
			}
			else if (data instanceof String[])
			{
				String[] resources = (String[]) data;
				for (int i = 0; i < resources.length; i++)
				{
					String resource = resources[i];
					srcObjects.add(resource);
				}
				_sourceType = SystemDNDTransferRunnable.SRC_TYPE_OS_RESOURCE;
			}
		}

		return srcObjects;
	}


	/**
	 * Called by SWT after the drop have been validated to perform the
	 * drop transfer.
	 * 
	 */
	public boolean performDrop(Object data)
	{
		boolean ok = true;

		ArrayList srcObjects = getSourceObjects(data);

		if (srcObjects.size() > 0)
		{
		    Object target = getCurrentTarget();
		    if (target == null)
		    {
		        target = getViewer().getInput();
		    }
			
			List rulesList = new ArrayList();
			int j = 0;
			for (int i = 0; i < srcObjects.size(); i++)
			{
				if (srcObjects.get(i) instanceof ISchedulingRule)
				{
					rulesList.add(srcObjects.get(i));
					j++;
				}
				/** FIXME - can't be coupled with IRemoteFile
				else if (srcObjects.get(i) instanceof IRemoteFile)
				{
					rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)srcObjects.get(i)));
					j++;
				}
				*/
			}
			/*
			if (target instanceof ISchedulingRule)
			{
				rulesList.add(target);
				j++;
			}
			*/
			/** FIXME - can't be coupled with IRemoteFile
			else if (target instanceof IRemoteFile)
			{
				rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)target));
			}
			
			else if (target instanceof IAdaptable)
			{
				ISystemDragDropAdapter targetAdapter = (ISystemDragDropAdapter) ((IAdaptable) target).getAdapter(ISystemDragDropAdapter.class);

				if (targetAdapter != null)
				{
					ISubSystem targetSubSystem = targetAdapter.getSubSystem(target);
					rulesList.add(targetSubSystem);
					j++;
				}
			}
			*/
			MultiRule rule = null;
			ISchedulingRule[] rules = (ISchedulingRule[])rulesList.toArray(new ISchedulingRule[rulesList.size()]);
			
			if (j > 0) rule = new MultiRule(rules);
			
			SystemDNDTransferRunnable runnable = new SystemDNDTransferRunnable(target, srcObjects, getViewer(), _sourceType);
			// DKM - rules are causing problems at the moment
			runnable.setRule(rule);
			
			if (target instanceof SystemScratchpad)
			{
				runnable.run(new NullProgressMonitor());
			}
			else
			{				
				runnable.schedule();
			}
			//ok = runnable.dropOkay();
			ok = true;
		}
		return ok;
	}
	

	/**
	 * Method for decoding an source object ID to the actual source object.
	 * We determine the profile, connection and subsystem, and then
	 * we use the SubSystem.getObjectWithKey() method to get at the
	 * object.
	 *
	 */
	private Object getObjectFor(String str)
	{
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		// first extract subsystem id
		int connectionDelim = str.indexOf(":"); //$NON-NLS-1$
		if (connectionDelim == -1) // not subsystem, therefore likely to be a connection
		{
		    int profileDelim = str.indexOf("."); //$NON-NLS-1$
			if (profileDelim != -1) 
			{
			    String profileId = str.substring(0, profileDelim);
			    String connectionId = str.substring(profileDelim + 1, str.length());
			    ISystemProfile profile = registry.getSystemProfile(profileId);
			    return registry.getHost(profile, connectionId);
			}
		}
		
		
		int subsystemDelim = str.indexOf(":", connectionDelim + 1); //$NON-NLS-1$
		if (subsystemDelim == -1) // not remote object, therefore likely to be a subsystem
		{
		    return registry.getSubSystem(str);
		}
		else
		{
			String subSystemId = str.substring(0, subsystemDelim);
			String srcKey = str.substring(subsystemDelim + 1, str.length());
	
		
			ISubSystem subSystem = registry.getSubSystem(subSystemId);
			if (subSystem != null)
			{
				Object result = null;
				try
				{
					result = subSystem.getObjectWithAbsoluteName(srcKey);
				}
				catch (SystemMessageException e)
				{
					return e.getSystemMessage();
				}
				catch (Exception e)
				{
				}
				if (result != null)
				{
					return result;
				}
				else
				{
					SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
					msg.makeSubstitution(srcKey, subSystem.getHostAliasName());
					return msg;
				}
			}
			else
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_CONNECTION_NOTFOUND);
				msg.makeSubstitution(subSystemId);
				return msg;
			}
		}
	}

	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistryUI().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			/*
			// for other cases, use statusbar
        	IWorkbenchWindow win = RSEUIPlugin.getActiveWorkbenchWindow();
        	if (win != null)
        	{
        		Shell winShell = RSEUIPlugin.getActiveWorkbenchShell();
               	if (winShell != null && !winShell.isDisposed() && winShell.isVisible())
        		{
        			RSEUIPlugin.logInfo("Using active workbench window as runnable context");
        			shell = winShell;
        			return win;	
        		}	
        		else
        		{
        			win = null;	
        		}
        	}	  
     */
      
			irc = new ProgressMonitorDialog(shell);
			RSEUIPlugin.getTheSystemRegistryUI().setRunnableContext(shell, irc);
			return irc;
		}
	}

	public void dragOver(DropTargetEvent event) 
	{
		super.dragOver(event);
		event.feedback &= ~DND.FEEDBACK_EXPAND;
		event.detail = DND.DROP_COPY;
	}

}