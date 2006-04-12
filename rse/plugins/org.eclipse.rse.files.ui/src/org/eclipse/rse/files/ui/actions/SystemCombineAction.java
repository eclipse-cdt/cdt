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
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.CombineDialog;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemCombineAction extends SystemExtractToAction {

	public SystemCombineAction(Shell parent)
	{
		super(parent, FileResources.ACTION_COMBINE_LABEL, FileResources.ACTION_COMBINE_TOOLTIP);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0120");
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_COMBINE_ID));

	}

	public void run()
	{
		boolean repeat = true;
		while (repeat)
		{
			IRemoteFile firstSelection = (IRemoteFile) _selected.get(0);
			String title = FileResources.RESID_COMBINE_TITLE;
			CombineDialog dialog = new CombineDialog(getShell(), title);
			if (dialog == null)
			  return;
			dialog.setNeedsProgressMonitor(false);
	
			dialog.setMessage(FileResources.RESID_COMBINE_PROMPT);
			dialog.setShowNewConnectionPrompt(true);
			dialog.setShowPropertySheet(true, false);
			dialog.setSystemTypes(systemTypes);
	
			dialog.setPreSelection(firstSelection);
	
			dialog.setBlockOnOpen(true);
	  
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
			
			if (ArchiveHandlerManager.isVirtual(destination.getAbsolutePath()))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMBINETO_VIRTUAL_DEST);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				continue;
			}
			
			if (destinationInSource(destination))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DEST_NOT_IN_SOURCE);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				continue;
			}
			
			try
			{
				if (!destination.exists()) destSS.createFile(destination);
			}
			catch (SystemMessageException e)
			{
				System.out.println(e.getMessage());
			}
			for (int i = 0; i < _selected.size(); i++)
			{
				IRemoteFile selection = (IRemoteFile) _selected.get(i);
				IRemoteFileSubSystem sourceSS = selection.getParentRemoteFileSubSystem();
				
				IRunnableContext runnableContext = getRunnableContext(_parent);
				String nextName = selection.getName();
				int j = nextName.lastIndexOf(".");
				if (j != -1)
				{
					nextName = nextName.substring(0, j);
				}
				String nextDest = destination.getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + nextName;
				IRemoteFile nextDestination = null;
				try
				{
					while (destSS.getRemoteFileObject(nextDest).exists())
					{
						nextDest = nextDest + "1";
					}
					nextDestination = destSS.getRemoteFileObject(nextDest);
					destSS.createFolder(nextDestination);
				}
				catch (SystemMessageException e)
				{
					SystemMessageDialog dlg = new SystemMessageDialog(getShell(), e.getSystemMessage());
					dlg.open();
					return;
				}
						
				ExtractRunnable runnable = new ExtractRunnable(selection, nextDestination);
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
			}
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destination, destination.getParentPath(), destSS, null, null);
			registry.fireEvent(new SystemResourceChangeEvent(destination, ISystemResourceChangeEvents.EVENT_REFRESH, destination.getParentPath()));
			repeat = false;
		}
		return;
	}
}