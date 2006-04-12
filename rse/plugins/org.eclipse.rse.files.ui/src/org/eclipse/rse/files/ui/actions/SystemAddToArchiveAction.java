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
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.resources.AddToArchiveDialog;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
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
public class SystemAddToArchiveAction extends SystemBaseAction 
{

	protected List _selected;
	protected Shell _parent;
	
	public SystemAddToArchiveAction(Shell parent)
	{
		super(FileResources.ACTION_ADDTOARCHIVE_LABEL, parent);
		setToolTipText(FileResources.ACTION_ADDTOARCHIVE_TOOLTIP);
		_selected = new ArrayList();
		_parent = parent;
		allowOnMultipleSelection(true);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0122");
	}

	public SystemAddToArchiveAction(Shell parent, String label, String tooltip)
	{
		super(label, tooltip, parent);
		_selected = new ArrayList();
		_parent = parent;
		allowOnMultipleSelection(true);
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0122");
	}

	public void run() 
	{
		boolean repeat = true;
		while (repeat)
		{
			IRemoteFile firstSelection = (IRemoteFile) _selected.get(0);
			String title = FileResources.RESID_ADDTOARCHIVE_TITLE;
			
			String[] relativePaths = getRelativePaths();
			
			AddToArchiveDialog dialog = new AddToArchiveDialog(getShell(), title, relativePaths);
			if (dialog == null)
			  return;
			dialog.setNeedsProgressMonitor(false);
	
			dialog.setMessage(FileResources.RESID_ADDTOARCHIVE_PROMPT);
			dialog.setShowNewConnectionPrompt(true);
			dialog.setShowPropertySheet(true, false);
			
			dialog.setPreSelection(firstSelection);
	
			dialog.setBlockOnOpen(true);
			dialog.setShowLocationPrompt(true);
			dialog.setLocationPrompt(FileResources.RESID_ADDTOARCHIVE_LOCATION);
			dialog.setNameAndTypePrompt(FileResources.RESID_ADDTOARCHIVE_NAMEANDTYPE);	
	  		//dialog.setSelectionValidator(this);
	  		
			int rc = dialog.open();
	  
				// if (rc != 0) NOT RELIABLE!
			boolean cancelled = false;
			if (dialog.wasCancelled()) cancelled = true;
	
			IRemoteFile destinationArchive = null;
			IRemoteFileSubSystem destSS = null;   		       
			boolean saveFullPathInfo;
			String relativeTo = "";
			
			if (!cancelled)
			{
				destinationArchive = (IRemoteFile) dialog.getOutputObject();
				saveFullPathInfo = dialog.getSaveFullPathInfo();
				if (saveFullPathInfo) relativeTo = dialog.getRelativePath();
				destSS = destinationArchive.getParentRemoteFileSubSystem();
			}
			else
			{
				return;
			}
			if (destinationArchive == null)
			{
				System.out.println("blah");
			}
			
			if (ArchiveHandlerManager.isVirtual(destinationArchive.getAbsolutePath()))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ADDTO_VIRTUAL_DEST);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				continue;
			}
			
			if (destinationInSource(destinationArchive))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_DEST_NOT_IN_SOURCE);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				dlg.open();
				continue;
			}
			
			try
			{
				if (!destinationArchive.exists()) destSS.createFile(destinationArchive);
			}
			catch (SystemMessageException e)
			{
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), e.getSystemMessage());
				dlg.open();
				continue;
			}
			for (int i = 0; i < _selected.size(); i++)
			{
				IRemoteFile selection = (IRemoteFile) _selected.get(i);
				addToArchive(selection, destinationArchive, saveFullPathInfo, relativeTo);
			}
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, destinationArchive, destinationArchive.getParentPath(), destSS, null, null);
			registry.fireEvent(new SystemResourceChangeEvent(destinationArchive, ISystemResourceChangeEvents.EVENT_REFRESH, destinationArchive.getParentPath()));
			repeat = false;
		}
		return;
	}
	
	public void addToArchive(IRemoteFile source, IRemoteFile destinationArchive, boolean saveFullPathInfo, String relativeTo)
	{	
		IRemoteFile destination = null;
		
		if (destinationArchive == null)
		{
			return;
		}
		if (!destinationArchive.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemFactory().supportsArchiveManagement())
		{
			return;
		}
		if (ArchiveHandlerManager.isVirtual(destinationArchive.getAbsolutePath()))
		{
			return;
		}
		if (destinationInSource(destinationArchive))
		{
			return;
		}
		if (!destinationArchive.isArchive())
		{
			return;
		}
		try
		{
			if (!destinationArchive.exists()) destinationArchive.getParentRemoteFileSubSystem().createFile(destinationArchive);
		}
		catch (SystemMessageException e)
		{
			SystemMessageDialog dlg = new SystemMessageDialog(getShell(), e.getSystemMessage());
			dlg.open();
			return;
		}
		if (saveFullPathInfo)
		{
			String selectionPath = source.getAbsolutePath();
			String destinationVirtualDirectory = selectionPath.substring(relativeTo.length());
			int lastSlash = destinationVirtualDirectory.lastIndexOf(source.getSeparatorChar());
			if (lastSlash != -1)
			{
				destinationVirtualDirectory = destinationVirtualDirectory.substring(0, lastSlash);
				destinationVirtualDirectory = ArchiveHandlerManager.cleanUpVirtualPath(destinationVirtualDirectory);
				String newDestinationPath = destinationArchive.getAbsolutePath() + ArchiveHandlerManager.VIRTUAL_SEPARATOR + destinationVirtualDirectory;
				try
				{
					destination = destinationArchive.getParentRemoteFileSubSystem().getRemoteFileObject(newDestinationPath);
					if (!destination.exists())
					{
						destinationArchive.getParentRemoteFileSubSystem().createFolders(destination);
					}
				}
				catch (SystemMessageException e)
				{
					SystemMessageDialog dlg = new SystemMessageDialog(getShell(), e.getSystemMessage());
					dlg.open();
					return;
				}
				
			}
		}
		IRemoteFileSubSystem sourceSS = source.getParentRemoteFileSubSystem();
		
		IRunnableContext runnableContext = getRunnableContext(_parent);
			
		CopyRunnable runnable = new CopyRunnable(source, destination);
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
	
	protected class CopyRunnable implements IRunnableWithProgress
	{
		private IRemoteFileSubSystem destSS;
		private IRemoteFileSubSystem sourceSS;
		private IRemoteFile selection;
		private IRemoteFile destination;
		
		public CopyRunnable(IRemoteFile source, IRemoteFile dest)
		{
			destSS = dest.getParentRemoteFileSubSystem();
			sourceSS = source.getParentRemoteFileSubSystem();
			selection = source;
			destination = dest;
		}
		
		public void run(IProgressMonitor monitor)
		{
			try
			{
				ISystemDragDropAdapter srcAdapter = (ISystemDragDropAdapter) ((IAdaptable) selection).getAdapter(ISystemDragDropAdapter.class);
				boolean sameSysType = sourceSS.getClass().equals(destSS.getClass());
				srcAdapter.doDrag(selection, sameSysType, monitor);
				boolean sameSys = (sourceSS == destSS);
				srcAdapter.doDrop(selection, destination, sameSysType, sameSys, 0, monitor);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				System.out.println("SystemAddToArchiveAction: Could not drag and drop " + selection.getAbsolutePath());
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
				_selected.add(file);
				enable = true;
			}
			else
			{
				return false;
			}
		}
		return enable;
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
	
	protected boolean destinationInSource(IRemoteFile destination)
	{
		boolean insource = false;
		for (int i = 0; i < _selected.size(); i++)
		{
			if (isAncestorOf((IRemoteFile)_selected.get(i), destination)) 
			{
				insource = true;
				break;
			}
		}
		return insource;
	}
	
	protected boolean isAncestorOf(IRemoteFile ancestor, IRemoteFile descendant)
	{
		return ancestor.isAncestorOf(descendant);
	}
	
	protected String[] getRelativePaths()
	{
		IRemoteFile file = (IRemoteFile) _selected.get(0);
		if (file instanceof IVirtualRemoteFile) return null;
		IHost con = file.getSystemConnection();
		String host = con.getHostName();
		String root = file.getRoot();
		String lowestCommonPath = file.getAbsolutePath();
		boolean caseSensitive = file.getParentRemoteFileSubSystem().isCaseSensitive();
		char separator = file.getSeparatorChar();
		
		for (int i = 1; i < _selected.size(); i++)
		{
			file = (IRemoteFile) _selected.get(i);
			if (file instanceof IVirtualRemoteFile) return null;
			IHost nextCon = file.getSystemConnection();
			String nextHost = nextCon.getHostName();
			String nextRoot = file.getRoot();
			String nextPath = file.getAbsolutePath();
			if (nextHost.equals(host) && nextRoot.equals(root))
			{
				lowestCommonPath = getLowestCommonPath(lowestCommonPath, nextPath, caseSensitive);
			}
			else
			{
				return null;
			}
		}
		return getRelativePaths(lowestCommonPath, separator);
	}
	
	protected String[] getRelativePaths(String lcp, char separator)
	{		
		Vector relpaths = new Vector();
		for (int i = lcp.length() - 1; i >= 0; i--)
		{
			if (lcp.charAt(i) == separator)
			{
				relpaths.add(lcp.substring(0, i+1));
			}
		}
		String[] results = new String[relpaths.size()];
		for (int i = 0; i < relpaths.size(); i++)
		{
			results[i] = (String) relpaths.get(i);
		}
		return results;
	}
	
	protected String getLowestCommonPath(String str1, String str2, boolean caseSensitive)
	{
		int maxLength = Math.min(str1.length(), str2.length());
		
		for (int i = maxLength; i >= 0; i--)
		{
			if (str1.regionMatches(!caseSensitive, 0, str2, 0, i))
			{
				return str1.substring(0, i); 
			}
		}
		return "";
	}
}