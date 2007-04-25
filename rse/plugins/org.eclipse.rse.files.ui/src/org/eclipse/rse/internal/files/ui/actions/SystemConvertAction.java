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
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.files.ui.dialogs.CombineDialog;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.core.model.ISystemRegistry;
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
public class SystemConvertAction extends SystemExtractToAction {

	public SystemConvertAction(Shell parent)
	{
		super(parent, FileResources.ACTION_CONVERT_LABEL, FileResources.ACTION_CONVERT_TOOLTIP);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0121"); //$NON-NLS-1$
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONVERT_ID));

	}

	public void run()
	{
		for (int i = 0; i < _selected.size(); i++)
		{
			IRemoteFile selection = (IRemoteFile) _selected.get(i);
			String title = FileResources.RESID_CONVERT_TITLE;
			CombineDialog dialog = new CombineDialog(getShell(), title, true);
	
			dialog.setNeedsProgressMonitor(false);
			String message = SystemMessage.sub(FileResources.RESID_CONVERT_PROMPT, "&1", selection.getAbsolutePath()); //$NON-NLS-1$
			dialog.setMessage(message);
			dialog.setShowNewConnectionPrompt(true);
			dialog.setShowPropertySheet(true, false);
			dialog.setSystemTypes(getValidSystemTypes());
	
			dialog.setPreSelection(selection);
	
			dialog.setBlockOnOpen(true);
			dialog.setHelp(RSEUIPlugin.HELPPREFIX + "cnvd0000"); //$NON-NLS-1$
			dialog.setShowLocationPrompt(true);
			dialog.setLocationPrompt(FileResources.RESID_CONVERT_LOCATION);
	  		dialog.setNameAndTypePrompt(FileResources.RESID_CONVERT_NAMEANDTYPE);
	  		dialog.setSelectionValidator(this);
	  		
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

			
			if (ArchiveHandlerManager.isVirtual(destination.getAbsolutePath()))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONVERTTO_VIRTUAL_DEST);
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
			if (selection.isAncestorOf(destination))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DEST_NOT_IN_SOURCE);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				i--;
				continue;
			}
			
			try
			{
				if (destination.exists()) 
				{
					SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_UPLOAD_FILE_EXISTS);
					msg.makeSubstitution(destination.getName());
					SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
					boolean ok = dlg.openQuestionNoException();
					if (ok)
					{
						if (destination.getAbsolutePath().equals(selection.getAbsolutePath()))
						{
							continue;
						}
						destSS.delete(destination, null);
					}
					else
					{
						i--;
						continue;
					}
				}
				destSS.createFile(destination);
			}
			catch (SystemMessageException e)
			{
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), e.getSystemMessage());
				dlg.open();
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
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destination, destination.getParentPath(), destSS, null);
			registry.fireEvent(new SystemResourceChangeEvent(destination, ISystemResourceChangeEvents.EVENT_REFRESH, destination.getParentPath()));
		}
	}

}