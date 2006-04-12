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

package org.eclipse.rse.ui.actions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemDeleteDialog;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
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
 * If the input objects do not adapt to {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter} or 
 * {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter}, then you
 * should call {@link #setNameValidator(org.eclipse.rse.core.ui.validators.ISystemValidator)} to 
 * specify a validator that is called to verify the typed new name is valid. Further, to show the type value
 * of the input objects, they should implement {@link org.eclipse.rse.ui.dialogs.ISystemTypedObject}.
 * 
 * @see org.eclipse.rse.ui.dialogs.SystemDeleteDialog
 */
public class SystemCommonDeleteAction
       extends SystemBaseDialogAction
       implements ISystemIconConstants
{
	private String promptLabel; 
	
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
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0021");
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
		SystemDeleteDialog dlg = new SystemDeleteDialog(shell);
		if (promptLabel != null)
			dlg.setPromptLabel(promptLabel);
		Object firstSelection = getFirstSelection();
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
	
	public class DeleteRunnable implements IRunnableWithProgress
	{
		private ISystemDeleteTarget _target;
		public DeleteRunnable(ISystemDeleteTarget target)
		{			
			_target = target;
		}
		
		public void run(IProgressMonitor monitor)
		{
			  _target.doDelete(monitor); // deletes all the currently selected items
		}
	}
	
	/**
	 * Required by parent. 
	 * In our case, we overload it to also perform the deletion, but only if using a delete target,
	 *  else it is up to the caller to call wasCancelled() and if not true, do their own deletion.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		if (!((SystemDeleteDialog)dlg).wasCancelled() && (getDeleteTarget() != null))
		{
			ISystemDeleteTarget target = getDeleteTarget();
			DeleteRunnable delRunnable = new DeleteRunnable(target);
			IRunnableContext runnableContext = getRunnableContext(dlg.getShell());
			try
			{
				runnableContext.run(false, true, delRunnable);
			}
			catch (Exception e)
			{				
			}
			RSEUIPlugin.getTheSystemRegistry().clearRunnableContext();
	      setEnabled(target.canDelete());
		}
		return null;
	}
	
	protected IRunnableContext getRunnableContext(Shell shell)
	{
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistry().getRunnableContext();
		if (irc != null)
		{
			return irc;
		}
		else
		{
			irc = new ProgressMonitorDialog(shell);
			RSEUIPlugin.getTheSystemRegistry().setRunnableContext(shell, irc);
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