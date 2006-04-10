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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.model.SystemScratchpad;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.PluginTransferData;


/**
 * Drop adapter for dropping objects in the Systems views.
 * 
 */
public class SystemViewDataDropAdapter extends ViewerDropAdapter implements  ISystemMessages
{


	protected Shell shell;
	protected long hoverStart = 0;

	protected static final long hoverThreshold = 1500;
	public static final char CONNECTION_DELIMITER = ':';
	public static final String RESOURCE_SEPARATOR = "|";

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

	private ArrayList getRSESourceObjects(PluginTransferData transferData)
	{
		byte[] result = transferData.getData();

		// get the sources	
		//StringTokenizer tokenizer = new StringTokenizer(new String(result), RESOURCE_SEPARATOR);
		String[] tokens = (new String(result)).split("\\"+SystemViewDataDropAdapter.RESOURCE_SEPARATOR);
		

		ArrayList srcObjects = new ArrayList();
		for (int i = 0;i < tokens.length; i++)
		{
			String srcStr = tokens[i];

			Object srcObject = getObjectFor(srcStr);
			srcObjects.add(srcObject);
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
				srcObjects = getRSESourceObjects(transferData);
				_sourceType = SystemDNDTransferRunnable.SRC_TYPE_RSE_RESOURCE;
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
			else if (data instanceof String[])
			{
				String[] resources = (String[]) data;
				for (int i = 0; i < resources.length; i++)
				{
					String resource = (String)resources[i];
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
			if (target instanceof ISchedulingRule)
			{
				rulesList.add(target);
				j++;
			}
			/** FIXME - can't be coupled with IRemoteFile
			else if (target instanceof IRemoteFile)
			{
				rulesList.add(new RemoteFileSchedulingRule((IRemoteFile)target));
			}
			*/
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
			MultiRule rule = null;
			ISchedulingRule[] rules = (ISchedulingRule[])rulesList.toArray(new ISchedulingRule[rulesList.size()]);
			
			if (j > 0) rule = new MultiRule(rules);
			
			SystemDNDTransferRunnable runnable = new SystemDNDTransferRunnable(target, srcObjects, getViewer(), _sourceType);
			// DKM - rules are causing problems at the moment
			//runnable.setRule(rule);
			
			if (target instanceof SystemScratchpad)
			{
				runnable.run(null);
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
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		// first extract subsystem id
		int connectionDelim = str.indexOf(":");
		if (connectionDelim == -1) // not subsystem, therefore likely to be a connection
		{
		    int profileDelim = str.indexOf(".");
			if (profileDelim != -1) 
			{
			    String profileId = str.substring(0, profileDelim);
			    String connectionId = str.substring(profileDelim + 1, str.length());
			    ISystemProfile profile = registry.getSystemProfile(profileId);
			    return registry.getHost(profile, connectionId);
			}
		}
		
		
		int subsystemDelim = str.indexOf(":", connectionDelim + 1);
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
					SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_FILE_NOTFOUND);
					msg.makeSubstitution(srcKey, subSystem.getHostAliasName());
					return msg;
				}
			}
			else
			{
				SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_CONNECTION_NOTFOUND);
				msg.makeSubstitution(subSystemId);
				return msg;
			}
		}
	}

	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = SystemPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			/*
			// for other cases, use statusbar
        	IWorkbenchWindow win = SystemPlugin.getActiveWorkbenchWindow();
        	if (win != null)
        	{
        		Shell winShell = SystemPlugin.getActiveWorkbenchShell();
               	if (winShell != null && !winShell.isDisposed() && winShell.isVisible())
        		{
        			SystemPlugin.logInfo("Using active workbench window as runnable context");
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
			SystemPlugin.getTheSystemRegistry().setRunnableContext(shell, irc);
			return irc;
		}
	}

	public void dragOver(DropTargetEvent event) 
	{
		super.dragOver(event);
		event.feedback &= ~DND.FEEDBACK_EXPAND;
	}

}