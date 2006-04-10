/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.processes.ui.ProcessesPlugin;
import org.eclipse.rse.processes.ui.SystemProcessesResources;
import org.eclipse.rse.processes.ui.dialogs.SystemKillDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;


public class SystemKillProcessAction extends SystemBaseDialogAction implements IRunnableWithProgress
{

    protected Exception runException = null;
	protected Object[] processesDeathRow;
	protected boolean killedOk = true;
	protected String signalType = null;
    
	/**
	 * Constructor for subclass
	 */
	public SystemKillProcessAction(Shell shell) 
	{
		super(SystemProcessesResources.ACTION_KILLPROCESS_LABEL, 
			  SystemProcessesResources.ACTION_KILLPROCESS_TOOLTIP,
			  ProcessesPlugin.getDefault().getImageDescriptorFromPath("/icons/full/elcl16/killprocessj.gif"), 
			  shell);
		allowOnMultipleSelection(true);
		setProcessAllSelections(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);		
  	    setHelp(ProcessesPlugin.HELPPREFIX+"actn0001"); 
  	    setDialogHelp(ProcessesPlugin.HELPPREFIX+"dkrp0000"); 
	}
	
	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We simply ensure every selected object is an IRemoteProcess
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Iterator e= ((IStructuredSelection) selection).iterator();		
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (!(selectedObject instanceof IRemoteProcess))
			  enable = false;
		}
		return enable;
	}
	
	/**
	 * Required by parent. 
	 * It is up to the caller to call wasCancelled() and if not true, do their own killing.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		SystemKillDialog killDlg = (SystemKillDialog)dlg;		
		if (!killDlg.wasCancelled())
		   signalType = killDlg.getSignal();
		if (signalType.equals(SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT))
			signalType = ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT;
		if (signalType != null)
	    {	    	
    	   IRunnableContext runnableContext = getRunnableContext();
    	   try
    	   {
    	     runnableContext.run(false,false,this); // inthread, cancellable, IRunnableWithProgress
    	   }
    	   catch (java.lang.reflect.InvocationTargetException exc) // unexpected error
    	   {
    	  	 showOperationMessage((Exception)exc.getTargetException(), getShell()); 	    	
    	   }
    	   catch (Exception exc)
    	   {
    	  	 showOperationMessage(exc, getShell());
    	   }    	
		}
		return null;
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
		SystemKillDialog dlg = new SystemKillDialog(shell);
		return dlg;
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
	
    // ----------------------------------
    // INTERNAL METHODS...
    // ----------------------------------
	/**
	 * Method required by IRunnableWithProgress interface.
	 * Allows execution of a long-running operation modally by via a thread.
	 * In our case, it runs the kill operation with a visible progress monitor
	 */
    public void run(IProgressMonitor monitor)
         throws java.lang.reflect.InvocationTargetException,
                java.lang.InterruptedException
	{		
		SystemMessage msg = getKillingMessage();
		runException = null;
		populateSelectedObjects();
		
        try
        {
           int steps = processesDeathRow.length;
	       monitor.beginTask(msg.getLevelOneText(), steps);
	       killedOk = true;
	       IRemoteProcess currentProcess = null;
	       for (int idx=0; killedOk && (idx<steps); idx++)
	       {
	    	  currentProcess = (IRemoteProcess) processesDeathRow[idx];
	    	  if (signalType.equals(SystemProcessesResources.RESID_KILL_SIGNAL_TYPE_DEFAULT))
	    		  signalType = ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT;
	       	  monitor.subTask(getKillingMessage(signalType, currentProcess.getName()).getLevelOneText());
		      killedOk = doKill(monitor, signalType, currentProcess);
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
        killComplete();
	}
    
    /**
     * @param monitor Usually not needed
	 * @param signal the signal to be sent to the remote process
	 * @param process the process to send the signal to
	 */
	protected boolean doKill(IProgressMonitor monitor, String signal, IRemoteProcess process)
		throws Exception 
    {

		RemoteProcessSubSystem ss;
		boolean ok = false;
		ss = process.getParentRemoteProcessSubSystem();
		
		ok = ss.kill(process, signal);
		if (!ok)
		{
			  SystemMessage msg = ProcessesPlugin.getPluginMessage("RSEPG1300");
			  msg.makeSubstitution(process.getName());
			  throw new SystemMessageException(msg); 
		}
		return ok;
    }
    
	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void killComplete() 
	{
		if (processesDeathRow.length == 0)
		  return;

		// refresh all instances of this parent, and all affected filters...
		ISubSystem processSS = ((IRemoteProcess)processesDeathRow[0]).getParentRemoteProcessSubSystem();

		List results = getAffectedFilters(processesDeathRow, processSS);
		
		
		// update the ui
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		for (int i = 0; i < results.size(); i++)
		{
			ISystemFilterReference ref = (ISystemFilterReference)results.get(i);
			ref.markStale(true);
			registry.fireEvent(new SystemResourceChangeEvent(ref, ISystemResourceChangeEvents.EVENT_CHANGE_CHILDREN,ref));
		}
		
				
	}
	
	/**
	 * Returns a list of all the filters that are affected by killing the objects in processesDeathRow.
	 * @param processesDeathRow the objects that will be sent a kill signal
	 * @param subSystem the subsystem to search
	 * @return a list of the affected filters
	 */
	protected List getAffectedFilters(Object[] processesDeathRow, ISubSystem subSystem)
	{
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		List result = new ArrayList();
		for (int i = 0; i < processesDeathRow.length; i++)
		{
			List refs = registry.findFilterReferencesFor(processesDeathRow[i], subSystem);
		
			result.addAll(refs);
		}
		
		return result;
	}
	
    
    protected void populateSelectedObjects()
    {
    	IStructuredSelection selection = getSelection();
		Iterator e = selection.iterator();		
		Vector v = new Vector();
		while (e.hasNext())
		{
			v.add(e.next());
		}
		processesDeathRow = v.toArray();
    }
    
    protected SystemMessage getKillingMessage()
    {
		  return ProcessesPlugin.getPluginMessage("RSEPG1003"); 
    }
	/**
	 * Get the specific "kill" message
	 */
    protected SystemMessage getKillingMessage(String signal, String processName)
    {
    	SystemMessage msg = null;
		msg = ProcessesPlugin.getPluginMessage("RSEPG1004"); 
		msg.makeSubstitution(signal, processName);
		return msg;
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
    	      SystemPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_FAILED).makeSubstitution(msg));
    	  msgDlg.setException(exc);
    	  msgDlg.open();
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
    }
    
    /**
     * Show an error message when the user cancels the operation.
     * Shows a common message by default.
     * Overridable.
     */
    protected void showOperationCancelledMessage(Shell shell)
    {
    	SystemMessageDialog msgDlg = new SystemMessageDialog(shell, SystemPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED));
    	msgDlg.open();
    }	

}