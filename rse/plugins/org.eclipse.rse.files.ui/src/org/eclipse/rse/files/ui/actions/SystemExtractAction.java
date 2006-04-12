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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemDragDropAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemExtractAction extends SystemBaseAction 
{
	protected List _selected;
	protected Shell _parent;
	protected String _label;
	
	public SystemExtractAction(Shell parent)
	{
		super(FileResources.ACTION_EXTRACT_LABEL, parent);
		_label = FileResources.ACTION_EXTRACT_LABEL;
		setToolTipText(FileResources.ACTION_EXTRACT_TOOLTIP);
		_selected = new ArrayList();
		_parent = parent;
		allowOnMultipleSelection(true);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0118");
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACT_ID));
	}

	public SystemExtractAction(Shell parent, String label, String tooltip)
	{
		super(label, tooltip, parent);
		_label = label;
		setToolTipText(tooltip);
		_selected = new ArrayList();
		_parent = parent;
		allowOnMultipleSelection(true);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0118");
		setImageDescriptor(RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_EXTRACT_ID));

	}

	public void run() 
	{
		for (int i = 0; i < _selected.size(); i++)
		{
			IRemoteFile selection = (IRemoteFile) _selected.get(i);
			IRemoteFile destinationParent = selection.getParentRemoteFile();
			IRemoteFileSubSystem ss = selection.getParentRemoteFileSubSystem();
			String newName = getExtractedName(selection);
			IRemoteFile destination = null;
			try
			{
				destination = ss.getRemoteFileObject(destinationParent.getAbsolutePath() + destinationParent.getSeparator() + newName);
				ss.createFolder(destination);
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
			//	always refresh
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destination, destinationParent, ss, null, null);
			registry.fireEvent(new SystemResourceChangeEvent(destination, ISystemResourceChangeEvents.EVENT_REFRESH, destinationParent));
		}
	}
	
	protected class ExtractRunnable implements IRunnableWithProgress
	{
		private IRemoteFileSubSystem destSS;
		private IRemoteFileSubSystem sourceSS;
		private IRemoteFile selection;
		private IRemoteFile destination;
		
		public ExtractRunnable(IRemoteFile source, IRemoteFile dest)
		{
			destSS = dest.getParentRemoteFileSubSystem();
			sourceSS = source.getParentRemoteFileSubSystem();
			selection = source;
			destination = dest;
		}
		
		public void run(IProgressMonitor monitor)
		{
			IRemoteFile[] sources = sourceSS.listFoldersAndFiles(selection);
			for (int j = 0; j < sources.length && !monitor.isCanceled(); j++)
			{
				try
				{
					ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter) ((IAdaptable) selection).getAdapter(ISystemDragDropAdapter.class);
					boolean sameSysType = sourceSS.getClass().equals(destSS.getClass());
					boolean sameSys = (sourceSS == destSS);
					if (!sameSys) 
					{
						Object source = srcAdapter.doDrag(sources[j], sameSysType, monitor);
						srcAdapter.doDrop(source, destination, sameSysType, sameSys, 0, monitor);
					}
					else
					{
						srcAdapter.doDrop(sources[j], destination, sameSysType, sameSys, 0, monitor);
					}
				}
				catch (Exception e)
				{
					SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_FAILED);
					SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
					dlg.open();
					System.out.println(e.getMessage());
					System.out.println("Could not extract " + sources[j].getAbsolutePath());
				}
			}
		}
		
	}
	
	/**
	 * Called when the selection changes in the systems view.  This determines
	 * the input object for the command and whether to enable or disable
	 * the action.
	 * 
	 * @param selection the current seleciton
	 * @return whether to enable or disable the action
	 */
	public boolean updateSelection(IStructuredSelection selection) 
	{
		_selected.clear();
		boolean enable = false;
		
		Iterator e = ((IStructuredSelection) selection).iterator();
		while (e.hasNext())
		{
			Object selected = e.next();

			if (selected != null && selected instanceof IRemoteFile)
			{
				IRemoteFile file = (IRemoteFile) selected;
				if (file.isArchive() && file.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement())
				{
					_selected.add(file);
					enable = true;
				}
			}
			else
			{
				return false;
			}
		}
		if (_selected.size() == 1)
		{
			this.setText(getActionLabelForSingleSelection());
		}
		else
		{
			this.setText(_label);
		}
		return enable;
	}
	
	/**
	 * Extending classes must override this method, otherwise
	 * when there is one selection, the action label will be
	 * "Extract to xxxx", where xxxx is a dynamically created directory
	 * name. Note: you can also eliminate this behaviour by overriding
	 * the updateSelection method.
	 * @return Set this to return the normal action label if you don't want
	 * the name dynamically created.
	 */
	protected String getActionLabelForSingleSelection()
	{
		String msg = FileResources.ACTION_EXTRACT_SUB_LABEL;
		return SystemMessage.sub(msg, "%1", getExtractedName((IRemoteFile)_selected.get(0)));
	}
	
	protected String getExtractedName(IRemoteFile selection)
	{
		String newName = selection.getName();
		int k = newName.lastIndexOf(".");
		if (k == -1)
		{
			newName = newName + "_contents";
		}
		else
		{
			newName = newName.substring(0, k);
		}
		return newName;
	}
	
	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}

		return new ProgressMonitorDialog(shell);
	}
}