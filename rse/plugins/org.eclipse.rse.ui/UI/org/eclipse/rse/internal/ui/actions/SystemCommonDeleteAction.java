/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [188637] Handle the caught exception in DeleteJob.run when file fails to be deleted
 * Kevin Doyle (IBM) - [196582] ClassCastException when doing copy/paste with Remote Search view open
 * Xuan Chen   (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * David McKnight   (IBM)        - [226143] [api][breaking] Make RSE rename/delete dialogs internal
 * David McKnight   (IBM)        - [234030] SystemCommonDeleteAction should not fire delete event if deleting is failed
 * David McKnight   (IBM)        - [203361] Deleting multiple target connections misleading
 * David McKnight   (IBM)        - [261367] RSE- Mutiple selection  Delete does not process all items selected
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.dialogs.SystemDeleteDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.model.SystemRemoteElementResourceSet;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * The action that displays the Delete confirmation dialog.There are two ways to use this action:
 * <ol>
 *  <li>When invoking from a class that implements ISystemDeleteTarget. In this case, that class 
 *      will be called back to determine if this action is to be enabled or not, and to do the actual delete for 
 *      each selected object, after the dialog is dismissed.
 *  <li>When used without an ISystemDeleteTarget, in which case you need to call wasCancelled() after
 *      running the action, and then do your own delete.
 * </ol>
 * <p>
 * 
 * @see org.eclipse.rse.internal.ui.dialogs.SystemDeleteDialog
 */
public class SystemCommonDeleteAction
       extends SystemBaseDialogAction
{
	public class DeleteEventRunnable implements Runnable
	{
		private List _localDeletedResources;
		private List _remoteDeletedResources;
		public DeleteEventRunnable(List localDeletedResources, List remoteDeletedResources)
		{
			_localDeletedResources = localDeletedResources;
			_remoteDeletedResources = remoteDeletedResources;
		}
		
		public void run()
		{
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			
			if (_remoteDeletedResources.size() > 0)
			{
				sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, _remoteDeletedResources, null, null, null);
			}
			if (_localDeletedResources.size() > 0)
			{
				Object[] localDeleted = _localDeletedResources.toArray();
				sr.fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(localDeleted, ISystemResourceChangeEvents.EVENT_DELETE_MANY, null/*(getSelectedParent())*/));
			}
		}
	}
	
	
	public class DeleteJob extends Job
	{
		private List _localResources;
		private List _remoteSets;
		public DeleteJob(List localResources, List remoteSets)
		{
			super(SystemResources.ACTION_DELETE_LABEL);
			_localResources = localResources;
			_remoteSets = remoteSets;
			setUser(true);
		}
		
		public IStatus run(IProgressMonitor monitor)
		{
			boolean ok = true;
			List localDeletedObjects = new Vector();
			List remoteDeletedObjects = new Vector();  
			HashMap objectsToDelete = new HashMap();
			
			// local delete is pretty straight-forward
			for (int l = 0; l < _localResources.size() && ok; l++)
			{
				Object element = _localResources.get(l);
				ISystemViewElementAdapter adapter = getViewAdapter(element);
				try
				{
					ok = adapter.doDelete(getShell(), element, monitor);
					if (ok)
					{
						localDeletedObjects.add(element);
					}
				}
				catch (Exception e)
				{	
					SystemBasePlugin.logError(e.getMessage(), e);
				}
			}
			SystemMessageException exception = null;
			
			
			// remote delete is not as straight-forward
			for (int r = 0; r < _remoteSets.size() && ok; r++)
			{
				SystemRemoteElementResourceSet set = (SystemRemoteElementResourceSet)_remoteSets.get(r);
				ISystemViewElementAdapter adapter = set.getViewAdapter();
				try
				{
					for (int i = 0; i < set.size(); i++)
					{
						Object thisObject = set.get(i);
						String objectName = adapter.getName(thisObject);
						objectsToDelete.put(thisObject, objectName);
						remoteDeletedObjects.add(thisObject);
					}
					List resSet = new ArrayList(set.getResourceSet());
					ok = adapter.doDeleteBatch(getShell(), resSet, monitor);
				}
				catch (SystemMessageException e)
				{
					exception = e;
					ok = false;
				}
				catch (Exception e)
				{
					SystemMessageDialog.displayExceptionMessage(getShell(), e);
					return Status.CANCEL_STATUS;
				}				
				
				if (!ok){
					remoteDeletedObjects.clear();
					if (monitor.isCanceled() && set.size() > 1)
					{
						for (int i = 0; i < set.size(); i++)
						{
							Object thisObject = set.get(i);
							if (!(adapter.exists(thisObject)))
							{
								//This object has been deleted
								remoteDeletedObjects.add(thisObject);
							}	
						}
						if (remoteDeletedObjects.size() > 0)
						{
							//Get the moved file names
							Object thisObject = remoteDeletedObjects.get(0);
							String deletedFileNames = null;
							//We could not use adapter.getName(thisObject) here since in dstore case, this name has
							//already been changed to "deleted".  So we need to remember the object and name map, and
							//find the object name in the map here.
							deletedFileNames = (String)objectsToDelete.get(thisObject);  
							for (int i=1; i<(remoteDeletedObjects.size()); i++)
							{
								thisObject = remoteDeletedObjects.get(i);
								deletedFileNames = deletedFileNames + "\n" + (String)objectsToDelete.get(thisObject); //$NON-NLS-1$
							}
							SystemMessage thisMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_DELETE_INTERRUPTED); 
							thisMessage.makeSubstitution(deletedFileNames);
							SystemMessageDialog.displayErrorMessage(shell, thisMessage);
						}
					}
					else
					{
						SystemMessageDialog.displayMessage(exception);
					}
				}

			}
			
			// start a runnable to do the action refresh events
			DeleteEventRunnable fireEvents = new DeleteEventRunnable(localDeletedObjects, remoteDeletedObjects);
			Display.getDefault().asyncExec(fireEvents);
			
			if (ok)
				return Status.OK_STATUS;
			else
				return Status.CANCEL_STATUS;
		}
		
	}
	
	private String promptLabel; 
	private List _setList;
	
	/**
	 * Constructor for SystemDeleteAction when using a delete target
	 * @param parent The Shell of the parent UI for this dialog
	 * @param deleteTarget The UI part that has selectable and deletable parts.
	 */
	public SystemCommonDeleteAction(Shell parent, ISystemDeleteTarget deleteTarget)
	{
		super(SystemResources.ACTION_DELETE_LABEL, SystemResources.ACTION_DELETE_TOOLTIP,
			  PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE)	      
		      , parent);
		setSelectionProvider(deleteTarget);
		allowOnMultipleSelection(true);
		setProcessAllSelections(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);		
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0021"); //$NON-NLS-1$
  	    _setList = new Vector();
	}
	
	/**
	 * Constructor for SystemDeleteAction when not using a delete target
	 * @param parent The Shell of the parent UI for this dialog
	 */
	public SystemCommonDeleteAction(Shell parent)
	{
		this(parent, null);
	}	

	/**
	 * Specify the text to show for the label prompt. The default is 
	 *  "Delete selected resources?"
	 */
	public void setPromptLabel(String text)
	{
		this.promptLabel = text;
	}

	private ISystemDeleteTarget getDeleteTarget()
	{
		return (ISystemDeleteTarget)getSelectionProvider();
	}

    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean updateSelection(IStructuredSelection selection)
	{
		_setList.clear();
		ISystemDeleteTarget deleteTarget = getDeleteTarget();
		if (deleteTarget == null)
		  return true;
		else
		  return deleteTarget.showDelete() && getDeleteTarget().canDelete();	
	}
	
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override run with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected Dialog createDialog(Shell shell)
	{
		Object firstSelection = getFirstSelection();
		if (firstSelection != null)
		{
			SystemDeleteDialog dlg = new SystemDeleteDialog(shell);
			if (promptLabel != null)
				dlg.setPromptLabel(promptLabel);
		
			if (getRemoteAdapter(firstSelection) != null)
			{
	           String warningMsg = null;
	           String warningTip = null;
	
	             warningMsg = SystemResources.RESID_DELETE_WARNING_LABEL;
	             warningTip = SystemResources.RESID_DELETE_WARNING_TOOLTIP;
	           dlg.setWarningMessage(warningMsg,warningTip);
			}
			return dlg;
		}
		else
		{
			return null;
		}
	}
	
	
	/**
	 * New method of doing delete where the physical deletion occurs in a job whereas the refresh is done in a runnable
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		if (!((SystemDeleteDialog)dlg).wasCancelled() && (getDeleteTarget() != null))
		{
			ISystemDeleteTarget target = getDeleteTarget();
			ISelection selection = target.getSelection();
			
			if (selection instanceof IStructuredSelection)
			{				
				// keep track of the current set
				List localSet = new Vector();

				// divide up all objects to delete
				List selectedResources = ((SystemDeleteDialog)dlg).getSelectedResources();
								
				for (int i = 0; i < selectedResources.size(); i++)
				{
					Object object = selectedResources.get(i);
					ISystemViewElementAdapter adapter = SystemAdapterHelpers.getViewAdapter(object);
					if (getRemoteAdapter(object) != null) 
					{
						ISubSystem subSystem = adapter.getSubSystem(object);
						// a remote object so add to remote set
						SystemRemoteElementResourceSet set = getSetFor(subSystem, adapter);
						set.addResource(object);
					}
					else
					{
						localSet.add(object);
					}
				}
				
				// do delete for each set
				DeleteJob job = new DeleteJob(localSet, _setList);
				job.schedule();
			}
			
		}
		return null;
	}


	
	
	protected SystemRemoteElementResourceSet getSetFor(ISubSystem subSystem, ISystemViewElementAdapter adapter) {
		for (int i = 0; i < _setList.size(); i++) {
			SystemRemoteElementResourceSet set = (SystemRemoteElementResourceSet) _setList.get(i);
			if (set.getViewAdapter() == adapter && set.getSubSystem() == subSystem) {
				return set;
			}
		}

		// no existing set - create one
		SystemRemoteElementResourceSet newSet = new SystemRemoteElementResourceSet(subSystem, adapter);
		_setList.add(newSet);
		return newSet;
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
			irc = new ProgressMonitorDialog(shell);
			RSEUIPlugin.getTheSystemRegistryUI().setRunnableContext(shell, irc);
			return irc;
		}
	}
	
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o)
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }
	
}