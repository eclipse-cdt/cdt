/********************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.files.ui.dialogs.ExtractToDialog;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
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
public class SystemExtractToAction extends SystemExtractAction implements IValidatorRemoteSelection
{	
	private static final String[] validSystemTypesIds = {
		IRSESystemType.SYSTEMTYPE_LOCAL_ID,
		IRSESystemType.SYSTEMTYPE_WINDOWS_ID,
		IRSESystemType.SYSTEMTYPE_LINUX_ID,
		IRSESystemType.SYSTEMTYPE_POWER_LINUX_ID,
		IRSESystemType.SYSTEMTYPE_UNIX_ID,
		IRSESystemType.SYSTEMTYPE_AIX_ID,
		IRSESystemType.SYSTEMTYPE_ISERIES_ID
	};
	private static IRSESystemType[] validSystemTypes = null;
	
	protected SystemMessage targetDescendsFromSrcMsg = null;
	protected int currentlyProcessingSelection = 0;
	
	public SystemExtractToAction(Shell parent)
	{
		super(parent,FileResources.ACTION_EXTRACT_TO_LABEL, FileResources.ACTION_EXTRACT_TO_TOOLTIP);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0119"); //$NON-NLS-1$
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACTTO_ID));

	}

	public SystemExtractToAction(Shell parent, String label, String tooltip)
	{
		super(parent, label, tooltip);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0119"); //$NON-NLS-1$
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACTTO_ID));
	}
	
	protected IRSESystemType[] getValidSystemTypes() {
		if (validSystemTypes==null) {
			validSystemTypes = SystemWidgetHelpers.getValidSystemTypes(validSystemTypesIds);
		}
		return validSystemTypes;
	}

	public void run()
	{
		for (int i = 0; i < _selected.size(); i++)
		{
			currentlyProcessingSelection = i;
			IRemoteFile selection = (IRemoteFile) _selected.get(i);
			//IRemoteFileSubSystem sourceSS = selection.getParentRemoteFileSubSystem();
			String title = FileResources.RESID_EXTRACTTO_TITLE;
			ExtractToDialog dialog = new ExtractToDialog(getShell(), title);

			dialog.setNeedsProgressMonitor(true);
			
			String message = SystemMessage.sub(FileResources.RESID_EXTRACTTO_PROMPT, "&1", selection.getAbsolutePath()); //$NON-NLS-1$
			dialog.setMessage(message);
			dialog.setShowNewConnectionPrompt(true);
			dialog.setShowPropertySheet(true, false);
			dialog.setSystemTypes(getValidSystemTypes());
			
			dialog.setPreSelection(selection);
			
			dialog.setBlockOnOpen(true);
			dialog.setSelectionValidator(this);
			
			/*if (dlgHelpId!=null) 
			{
				dialog.setHelp(dlgHelpId);
			}*/
		  
			dialog.open();
		  
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

			if (selection.isAncestorOf(destination))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DEST_NOT_IN_SOURCE);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				i--;
				continue;
			}
			if (!destination.canWrite())
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DEST_TARGET_READONLY);
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
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destination, destination.getParentPath(), destSS, null);
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
			targetDescendsFromSrcMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOUCE);
		   return targetDescendsFromSrcMsg;
	   }
	   else
		 return null;
   }

}