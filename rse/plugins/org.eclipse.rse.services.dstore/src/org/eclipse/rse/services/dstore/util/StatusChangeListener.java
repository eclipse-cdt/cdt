/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.services.dstore.util;


import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/*
 * Utility class for determining when a DataStore command is complete via the status.
 */
public class StatusChangeListener implements IDomainListener
{

	protected DataElement target;
	protected Shell shell;
	protected IProgressMonitor monitor;
	
	protected boolean _networkDown = false;
	protected boolean done = false;
	protected boolean cancelled = false;
	
	protected Vector historyOfTargets;
	
	protected class FindShell implements Runnable {
		private Shell shell;
		
		/**
		 * @see Runnable#run()
		 */
		public void run() {
			try {
				Shell[] shells = Display.getCurrent().getShells();
				for (int loop = 0; loop < shells.length && shell == null; loop++) {
					if (shells[loop].isEnabled()) {
						shell = shells[loop];
					}
				}
			} catch (Exception e) {
				}
		}
	}

	
	/**
	 * Construct a StatusChangeListener
	 *
	 * @param shell A valid Shell object
	 *
	 * @param monitor A progress monitor if you want this object to check if
	 *                the user presses cancel while waiting for the status object
	 * 				  to be updated
	 *
	 * @param target The status DataElement for which you wish to wait for an update
	 */	
	public StatusChangeListener(Shell shell, IProgressMonitor monitor, DataElement target) {
		this.shell = shell;
		this.monitor = monitor;
		this.target = target;
		historyOfTargets = new Vector();				
	}
	

	public StatusChangeListener(Shell shell, IProgressMonitor monitor) {
		this(shell, monitor, null);
	}	

	
//	public StatusChangeListener(Shell shell, DataElement target) {
//		this(shell, null, target);
//	}

	
	/**
	 *
	 */
	public void setStatus(DataElement p_target)
	{
		this.target = p_target;
		
		for (int i = 0; i < historyOfTargets.size(); i++)
		{
			if (target == historyOfTargets.elementAt(i))
			{
				setDone( true );
				historyOfTargets.clear();
				return;
			}
		}
		
		historyOfTargets.clear();
	}
	
	/**
	 *
	 */
	public DataElement getStatus()
	{
		return target;
	}	

	/**
	 * @see IDomainListener#listeningTo(DomainEvent)
	 */
	public boolean listeningTo(DomainEvent event) {
		if (target == null)
		{
			return true;
		}
		
		if (target == event.getParent()) {
			return true;
		}
		
		return false;
	}


	/**
	 * @see IDomainListener#domainChanged(DomainEvent)
	 */
	public void domainChanged(DomainEvent event) {
		if (target == null)
		{
			if (historyOfTargets.size() < 1000)
			{
				historyOfTargets.addElement(event.getParent());
			}
		}
		
		if (target == event.getParent())
		{
		    boolean isStatusDone = determineStatusDone();
		    
		    if (isStatusDone)
		    {
				setDone( true );
		    }
		}
	}


	/**
	 * setDone(boolean)
	 */
	public void setDone(boolean done)
	{
		this.done = done;
	}
	
	/**
	 * @see IDomainListener#getShell()
	 */
	public Shell getShell() {
		// dy:  DomainNotifier (which calls this method) requires the shell not be disposed
		//if (shell == null) {
		if (shell == null || shell.isDisposed())
		{
			FindShell findShell = new FindShell();
			Display.getDefault().syncExec(findShell);
			shell = findShell.shell;
		}
		return shell;
	}

	/**
	 *
	 */
	public boolean wasCancelled() {
		return cancelled;
	}



	
	/**
	 * Wait for the the status DataElement to be refreshed
	 *
	 * @param
	 *    ICommunicationsDiagnosticFactory factory : for creating system specific diagnostic class instance 
	 *    Int wait : threshold for starting diagnostic. Default is 60 seconds; a zero means to use the default.
	 *               -1 means to force a timeout; mainly for testing purpose.  
	 *
	 * @return The status DataElement after it has been updated, or the user
	 *         has pressed cancel
	 *
	 * @throws InterruptedException if the thread was interrupted.
	 */
	public DataElement waitForUpdate() throws InterruptedException
	{
		return waitForUpdate( 0);  //No diagnostic
	}


    public DataElement waitForUpdate(int wait) throws InterruptedException
	{	
    	boolean statusDone = determineStatusDone();
    	if (statusDone)
    	{
    		setDone(true);
    	}
    	else
    	{
			Display display = Display.getCurrent();
					
		  if ( wait > 0 )
		{
		}
		else if ( wait == -1 )
		{
		}
	         
	
	      if (display != null) {
				if (shell == null || shell.isDisposed()) {
					shell = Display.getDefault().getActiveShell();
				}
				// Current thread is UI thread
				while (!done && !cancelled) {
					// Process everything on event queue
					while (display.readAndDispatch()) {
					}
					
					if ((monitor != null) && (monitor.isCanceled())) {
						cancelled = true;
						target.getDataStore().getDomainNotifier().removeDomainListener(this);
						throw new InterruptedException();
					}
					
					statusDone = determineStatusDone();
					
					if (statusDone)
					{
						setDone(true);
					}
					else
					{
					    Thread.sleep(100);
					    if (_networkDown)
	                    {
	    					target.getDataStore().getDomainNotifier().removeDomainListener(this);
	    					throw new InterruptedException();
	                    }
	                }
				}
				
			} else {
				// Current thread is not UI thread
				while (!done && !cancelled)
				{
					
				    if ((monitor != null) && (monitor.isCanceled()))
				    {
						cancelled = true;
						target.getDataStore().getDomainNotifier().removeDomainListener(this);
						throw new InterruptedException();
					}
	
					statusDone = determineStatusDone();
					
					if (statusDone)
					{
						setDone(true);
					}
					else {
	                    Thread.sleep(100);
	
	                    if (_networkDown)
	                    {
	    					target.getDataStore().getDomainNotifier().removeDomainListener(this);
	    					throw new InterruptedException();
	                    }
	                }
				}		
			}
    	}
		
		// Reset done so we can detect the next event if waitForUpdate
		// is called again
		done = false;
		
		return target;
	}
    
    /**
     * Determines whether the status is done.
     * @return <code>true</code> if status done, <code>false</code> otherwise.
     */
    protected boolean determineStatusDone() {
        return getStatus().getAttribute(DE.A_VALUE).equals("done") ||  getStatus().getAttribute(DE.A_NAME).equals("done");
    }
	
	/**
	 * @see org.eclipse.rse.core.subsystems.ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener() {
		return false;
	}

  
	/**
	 * Test if the StatusChangeListener returned because the network connection to the 
	 * remote system was broken.
	 */
	public boolean isNetworkDown() {
		return _networkDown;
	}
}