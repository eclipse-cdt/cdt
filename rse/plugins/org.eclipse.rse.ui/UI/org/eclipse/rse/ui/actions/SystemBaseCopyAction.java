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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.dialogs.SystemSimpleCopyDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;







/**
 * An abstract base class for all copy actions. Defers minimal requirements to 
 *  child classes.
 */
public abstract class SystemBaseCopyAction extends SystemBaseDialogAction 
                implements ISystemMessages, IRunnableWithProgress, ISystemCopyTargetSelectionCallback
{	
	protected ISystemProfileManager mgr;
	protected ISystemRegistry sr;
	protected String[] oldNames;
	protected String[] newNames;
	protected Object[] oldObjects;
	protected Object targetContainer;
	protected boolean copiedOk = true;
	//private boolean makeActive;
    protected Exception runException = null;
    protected int mode = SystemSimpleCopyDialog.MODE_COPY;
    protected int runCount = 0;
	public static final int MODE_COPY = SystemSimpleCopyDialog.MODE_COPY;
	public static final int MODE_MOVE = SystemSimpleCopyDialog.MODE_MOVE;
    
    	
	/**
	 * Constructor when using default action ID
	 * @param parent Owning shell
	 * @param mode Either MODE_COPY or MODE_MOVE from this class
	 */
	public SystemBaseCopyAction(Shell parent, int mode) 
	{
		this(parent, mode==MODE_COPY ? SystemResources.ACTION_COPY_LABEL : SystemResources.ACTION_MOVE_LABEL, mode);
	}

	/**
	 * Constructor
	 * @param parent Owning shell
	 * @param label
	 * @param mode Either MODE_COPY or MODE_MOVE from this class
	 */
	public SystemBaseCopyAction(Shell parent, String label, int mode) 
	{
		super(label, 
		      (mode==MODE_COPY ? 
		            PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY)
		      		//SystemPlugin.getDefault().getImageDescriptor(ISystemConstants.ICON_SYSTEM_COPY_ID) 
		            : SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_MOVE_ID)), 
		      parent);
		this.mode = mode;
		mgr = SystemProfileManager.getSystemProfileManager();
		sr = SystemPlugin.getTheSystemRegistry();
		allowOnMultipleSelection(true);
        setProcessAllSelections(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
	}
	
	/**
	 * Reset. This is a re-run of this action
	 */
	protected void reset()
	{
		oldNames = null;
		newNames = null;
		oldObjects = null;
		targetContainer = null;
		copiedOk = true;
		runException = null;
		//System.out.println("inside base reset()");
	}

	/**
	 * Override of parent.
	 * Return the dialog that will be used to prompt for the copy/move target location
	 * @see #run()
	 */
	protected Dialog createDialog(Shell parent)
	{
		++runCount;
		if (runCount > 1)		
		  reset();
		SystemSimpleCopyDialog copyDlg = new SystemSimpleCopyDialog(parent, getPromptString(), mode, this, getTreeModel(), getTreeInitialSelection());

		// our title now reflects multiple selection. If single change it.
		IStructuredSelection sel = getSelection();
		//System.out.println("size = "+sel.size());
		if (sel.size() == 1)
		{		
			String singleTitle = null;
			if (mode == MODE_COPY)
				singleTitle = SystemResources.RESID_COPY_SINGLE_TITLE;
			else
				singleTitle = SystemResources.RESID_MOVE_SINGLE_TITLE;
			//System.out.println("..."+singleTitle);
			if (!singleTitle.startsWith("Missing")) // TODO: remove test after next mri rev         	
				copyDlg.setTitle(singleTitle);
		}									
		
		return copyDlg; 
	}
	
	protected abstract String[] getOldNames();	
	protected abstract Object[] getOldObjects();	
	/**
	 * Get the verbage prompt to show on line one of the copy dialog
	 */	
	protected String getPromptString()
	{
		if (mode == MODE_COPY)		
		  return SystemResources.RESID_COPY_PROMPT;		
		else
		  return SystemResources.RESID_MOVE_PROMPT;		
	}
	
	protected abstract SystemSimpleContentElement getTreeModel();
	protected abstract SystemSimpleContentElement getTreeInitialSelection();
	
	/**
	 * This method is a callback from the select-target-parent dialog, allowing us to decide whether the current selected
	 * object is a valid parent object. This affects the enabling of the OK button on that dialog.
	 * <p>
	 * The default is to return true if the selected element has no children. This is sufficient for most cases. However, 
	 * in some cases it is not, such as for filter strings where we want to only enable OK if a filter is selected. It is 
	 * possible that filter pools have no filters, so the default algorithm is not sufficient. In these cases the child class
	 * can override this method.
	 */
	public boolean isValidTargetParent(SystemSimpleContentElement selectedElement)
	{
		return !selectedElement.hasChildren();
	}
	
		
	/**
	 * Required by parent. We use it to actually do the work.
	 */
	protected Object getDialogValue(Dialog dlg)
	{		
		targetContainer = getTargetContainer(dlg);
		if (targetContainer != null)
	    {	    	
	       boolean okToProceed = preCheckForCollision();    	
	       if (!okToProceed)
	         return null;
    	   IRunnableContext runnableContext = getRunnableContext();
    	   try
    	   {
    	     runnableContext.run(false,false,this); // inthread, cancellable, IRunnableWithProgress
    	     if (copiedOk)
    	     {
    	     	SystemMessage completeMsg = getCompletionMessage(targetContainer, oldNames, newNames);
    	     	if (completeMsg != null)
    	     	{
    	     	  SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(),completeMsg);
    	     	  msgDlg.open();
    	     	}  
    	     }   
    	   }
    	   catch (java.lang.reflect.InvocationTargetException exc) // unexpected error
    	   {
    	  	 showOperationMessage((Exception)exc.getTargetException(), getShell());
    	     //throw (Exception) exc.getTargetException();    	    	
    	   }
    	   catch (Exception exc)
    	   {
    	  	 showOperationMessage(exc, getShell());
             //throw exc;
    	   }    	
		}
		return null;	
	}
	/**
	 * Get an IRunnable context to show progress in. If there is currently a dialog or wizard up with
	 * a progress monitor in it, we will use this, else we will create a progress monitor dialog.
	 */
	protected IRunnableContext getRunnableContext()
	{
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		IRunnableContext irc = sr.getRunnableContext();
		if (irc == null)
		  irc = new ProgressMonitorDialog(getShell());
		return irc;
	}
	/**
	 * Override this method if you supply your own copy/move target dialog. 
	 * Return the user-selected target or null if cancelled
	 */
	protected Object getTargetContainer(Dialog dlg)
	{
		SystemSimpleCopyDialog cpyDlg = (SystemSimpleCopyDialog)dlg;		
		Object targetContainer = null;
		if (!cpyDlg.wasCancelled())
		   targetContainer = cpyDlg.getTargetContainer();
	    return targetContainer;
	}
	
	/**
	 * Do a pre-check for a collision situation.
	 * This really is only a problem for filter strings, when a name collision is fatal verus
	 *  recoverable via a new-name prompt.
	 */
	protected boolean preCheckForCollision()
	{
		boolean ok = true;
        oldNames = getOldNames();
        oldObjects = getOldObjects();
        int steps = oldObjects.length;
        
	    String oldName = null;
	    Object oldObject = null;
	    for (int idx=0; ok && (idx<steps); idx++)
	    {
	       oldName = oldNames[idx];
	       oldObject = oldObjects[idx];
	       ok = preCheckForCollision(getShell(), targetContainer, oldObject, oldName);
	    }		
		return ok;
	}
	/**
	 * Overridable entry point when you want to prevent any copies/moves if any of the
	 * selected objects have a name collision.
	 * <p>
	 * If you decide to override this, it is your responsibility to issue the error 
	 * message to the user and return false here.
	 * <p>
	 * @return true if there is no problem, false if there is a fatal collision
	 */
	protected boolean preCheckForCollision(Shell shell, Object targetContainer, 
	                                       Object oldObject, String oldName)
	{
		return true;
	}	
	
	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void copyComplete() {}
	
    // ----------------------------------
    // INTERNAL METHODS...
    // ----------------------------------
	/**
	 * Method required by IRunnableWithProgress interface.
	 * Allows execution of a long-running operation modally by via a thread.
	 * In our case, it runs the copy operation with a visible progress monitor
	 */
    public void run(IProgressMonitor monitor)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
	{		
		SystemMessage msg = getCopyingMessage();
		runException = null;
		
        try
        {
           //oldNames = getOldNames();
           //oldObjects = getOldObjects();
           int steps = oldObjects.length;
	       monitor.beginTask(msg.getLevelOneText(), steps);
	       copiedOk = true;
	       String oldName = null;
	       String newName = null;
	       Object oldObject = null;
	       newNames = new String[oldNames.length];
	       for (int idx=0; copiedOk && (idx<steps); idx++)
	       {
	       	  oldName = oldNames[idx];
	       	  oldObject = oldObjects[idx];
	       	  monitor.subTask(getCopyingMessage(oldName).getLevelOneText());
	       	  newName = checkForCollision(getShell(), monitor, targetContainer, oldObject, oldName);
	       	  if (newName == null)
	       	    copiedOk = false;
	       	  else
		        copiedOk = doCopy(monitor, targetContainer, oldObject, newName);
		      newNames[idx] = newName;
		      monitor.worked(1);
	       }
           monitor.done();
        }
        catch(java.lang.InterruptedException exc)
        {
           monitor.done();
           runException = exc;
           throw (java.lang.InterruptedException)runException;
        }						
        catch(Exception exc)
        {
           monitor.done();
           runException = new java.lang.reflect.InvocationTargetException(exc);
           throw (java.lang.reflect.InvocationTargetException)runException;
        }
        copyComplete();
	}
	
	protected abstract String checkForCollision(Shell shell, IProgressMonitor monitor, Object targetContainer, Object oldObject, String oldName);
	/**
	 * Get the generic "Copying..." or "Moving..." message
	 */
    protected SystemMessage getCopyingMessage()
    {
		if (mode == MODE_COPY)		
		  return SystemPlugin.getPluginMessage(MSG_COPYGENERIC_PROGRESS); 
		else
		  return SystemPlugin.getPluginMessage(MSG_MOVEGENERIC_PROGRESS); 
    }
	/**
	 * Get the specific "Copying %1..." or "Moving %1..." message
	 */
    protected SystemMessage getCopyingMessage(String oldName)
    {
    	SystemMessage msg = null;
		if (mode == MODE_COPY)		
		  msg = SystemPlugin.getPluginMessage(MSG_COPYTHINGGENERIC_PROGRESS); 
		else
		  msg = SystemPlugin.getPluginMessage(MSG_MOVETHINGGENERIC_PROGRESS); 
		msg.makeSubstitution(oldName);
		return msg;
    }
    
    /**
     * DO THE ACTUAL COPY OR MOVE. THIS MUST BE IMPLEMENTED BY CHILD CLASSES
     */
	protected abstract boolean doCopy(IProgressMonitor monitor, Object targetContainer, Object oldObject, String newName)
	 throws Exception;
	 
	/**
	 * Return complete message.
	 * Override if you want to popup a completion message after a successful copy/move
	 */
	public SystemMessage getCompletionMessage(Object targetContainer, String[] oldNames, String[] newNames)
	{
		return null;
	}

    /**
     * Helper method to show an error message resulting from the attempted operation.
     */
	protected void showOperationMessage(Exception exc, Shell shell)
	{
		if (exc instanceof java.lang.InterruptedException)
		  showOperationCancelledMessage(shell);
		else if (exc instanceof java.lang.reflect.InvocationTargetException)
		  showOperationErrorMessage(shell, ((java.lang.reflect.InvocationTargetException)exc).getTargetException());
		else
		  showOperationErrorMessage(shell, exc);
	}

    /**
     * Show an error message when the operation fails.
     * Shows a common message by default.
     * Overridable.
     */
    protected void showOperationErrorMessage(Shell shell, Throwable exc)
    {
    	if (exc instanceof SystemMessageException)
    	  showOperationErrorMessage(shell, (SystemMessageException)exc);
    	else
        {
    	  String msg = exc.getMessage();
    	  if ((msg == null) || (exc instanceof ClassCastException))
    	    msg = exc.getClass().getName();
    	  SystemMessageDialog msgDlg = 
    	    new SystemMessageDialog(shell, 
    	      SystemPlugin.getPluginMessage(MSG_OPERATION_FAILED).makeSubstitution(msg));
    	  msgDlg.setException(exc);
    	  msgDlg.open();
          //SystemPlugin.logError("Copy/Move operation failed",exc);
        }
    }	
    /**
     * Show an error message when the operation fails.
     * Shows a SystemMessage that was encapsulated in a SystemMessage exception
     * Overridable.
     */
    protected void showOperationErrorMessage(Shell shell, SystemMessageException exc)
    {
    	SystemMessage msg = exc.getSystemMessage();
    	SystemMessageDialog msgDlg = 
    	  new SystemMessageDialog(shell, msg);
    	msgDlg.open();
        //SystemPlugin.logError("Copy/Move operation failed",exc);
    }	

    /**
     * Show an error message when the user cancels the operation.
     * Shows a common message by default.
     * Overridable.
     */
    protected void showOperationCancelledMessage(Shell shell)
    {
    	SystemMessageDialog msgDlg = new SystemMessageDialog(shell, SystemPlugin.getPluginMessage(MSG_OPERATION_CANCELLED));
    	msgDlg.open();
    }	


}