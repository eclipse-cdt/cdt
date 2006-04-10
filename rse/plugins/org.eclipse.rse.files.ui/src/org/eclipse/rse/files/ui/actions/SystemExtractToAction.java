/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.ISystemTypes;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.ExtractToDialog;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemExtractToAction extends SystemExtractAction implements IValidatorRemoteSelection, ISystemMessages
{	
	protected static final String[] systemTypes = { ISystemTypes.SYSTEMTYPE_LOCAL,
													ISystemTypes.SYSTEMTYPE_WINDOWS,
													ISystemTypes.SYSTEMTYPE_LINUX,
													ISystemTypes.SYSTEMTYPE_POWER_LINUX,
													ISystemTypes.SYSTEMTYPE_UNIX,
													ISystemTypes.SYSTEMTYPE_AIX,
													ISystemTypes.SYSTEMTYPE_ISERIES
												  };
	protected SystemMessage targetDescendsFromSrcMsg = null;
	protected int currentlyProcessingSelection = 0;
	
	public SystemExtractToAction(Shell parent)
	{
		super(parent,FileResources.ACTION_EXTRACT_TO_LABEL, FileResources.ACTION_EXTRACT_TO_TOOLTIP);
		setHelp(SystemPlugin.HELPPREFIX + "actn0119");
		setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACTTO_ID));

	}

	public SystemExtractToAction(Shell parent, String label, String tooltip)
	{
		super(parent, label, tooltip);
		setHelp(SystemPlugin.HELPPREFIX + "actn0119");
		setImageDescriptor(SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACTTO_ID));
	}

	public void run()
	{
		for (int i = 0; i < _selected.size(); i++)
		{
			currentlyProcessingSelection = i;
			IRemoteFile selection = (IRemoteFile) _selected.get(i);
			IRemoteFileSubSystem sourceSS = selection.getParentRemoteFileSubSystem();
			String title = FileResources.RESID_EXTRACTTO_TITLE;
			ExtractToDialog dialog = new ExtractToDialog(getShell(), title);
			if (dialog == null)
			  return;
			dialog.setNeedsProgressMonitor(true);
			
			String message = SystemMessage.sub(FileResources.RESID_EXTRACTTO_PROMPT, "&1", selection.getAbsolutePath());
			dialog.setMessage(message);
			dialog.setShowNewConnectionPrompt(true);
			dialog.setShowPropertySheet(true, false);
			dialog.setSystemTypes(systemTypes);
			
			dialog.setPreSelection(selection);
			
			dialog.setBlockOnOpen(true);
			dialog.setSelectionValidator(this);
			
			/*if (dlgHelpId!=null) 
			{
				dialog.setHelp(dlgHelpId);
			}*/
		  
			int rc = dialog.open();
		  
			// if (rc != 0) NOT RELIABLE!
			boolean cancelled = false;
			if (dialog.wasCancelled()) cancelled = true;
		    
		    IRemoteFile destination = null;
		    IRemoteFileSubSystem destSS = null;   		       
			if (!cancelled)
			{
				destination = (IRemoteFile) dialog.getOutputObject();
				destSS = destination.getParentRemoteFileSubSystem();
			}
			else
			{
				return;
			}
			if (destination == null)
			{
				System.out.println("blah");
			}
			if (selection.isAncestorOf(destination))
			{
				SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_DEST_NOT_IN_SOURCE);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				i--;
				continue;
			}
			if (!destination.canWrite())
			{
				SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_DEST_TARGET_READONLY);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				i--;
				continue;
			}
			IRunnableContext runnableContext = getRunnableContext(_parent);
					
			ExtractRunnable runnable = new ExtractRunnable(selection, destination);
			try
			{
				// currently we don't run this in a thread because
				//  in some cases dialogs are launched in the operation
				//  (widgets can only be legally used on the main thread)
				runnableContext.run(false, true, runnable); // inthread, cancellable, IRunnableWithProgress	
			}				
			catch (java.lang.reflect.InvocationTargetException exc)
			{
			}
			catch (java.lang.InterruptedException e)
			{
			}
			//	always refresh
			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destination, destination.getParentPath(), destSS, null, null);
			registry.fireEvent(new SystemResourceChangeEvent(destination, ISystemResourceChangeEvents.EVENT_REFRESH, destination.getParentPath()));
		}
	}
	
	protected boolean destinationInSource(IRemoteFile destination)
	{
		boolean insource = false;
		for (int i = 0; i < _selected.size(); i++)
		{
			if (destination.isDescendantOf((IRemoteFile) _selected.get(i))) 
			{
				insource = true;
				break;
			}
		}
		return insource;
	}
	
   protected String getActionLabelForSingleSelection()
   {
		return _label;
   }
	
   public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObjects)
   {
	   //if (selectedConnection != sourceConnection) {} // someday, but can't happen today. 
	   Object selectedObject = selectedObjects[0];
	   IRemoteFile currentSelection = (IRemoteFile) _selected.get(currentlyProcessingSelection);
	   if (!(selectedObject instanceof IRemoteFile))
		 return null;
	   IRemoteFile selectedFolder = (IRemoteFile)selectedObject;
	   if (selectedFolder.isDescendantOf(currentSelection))
	   {
		   if (targetDescendsFromSrcMsg == null)
			targetDescendsFromSrcMsg = SystemPlugin.getPluginMessage(FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOUCE);
		   return targetDescendsFromSrcMsg;
	   }
	   else
		 return null;
   }

}