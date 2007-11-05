/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [190803] Canceling a long-running dstore job prints "InterruptedException" to stdout 
 * David McKnight   (IBM)        - [190010] When status is "cancelled" the wait should complete
 * David McKnight   (IBM)        - [197480] eliminating UI dependencies
 *******************************************************************************/

package org.eclipse.rse.services.dstore.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.dstore.extra.IDomainNotifier;


/*
 * This utility class can be used to monitor the status of one more more status DataElements.
 * Only one instanceof of this class is required per DataStore for use in monitoring statuses.
 * This is intended to be used in place of StatusChangeListeners
 * 
 *  * <p>
 * The following is one example of the use of the StatusMonitor. The code:
 * <blockquote><pre>
 *    		DataElement status = dataStore.command(dsCmd, args, deObj);
 *
 *			StatusMonitor smon = StatusMonitorFactory.getInstance().getStatusMonitorFor(getSystem(), ds);
 *			smon.waitForUpdate(status, monitor);
 * </pre></blockquote>
 */
public class DStoreStatusMonitor implements IDomainListener
{

	protected boolean _networkDown = false;
	
	protected List _workingStatuses;
	protected List _cancelledStatuses;
	protected List _doneStatuses;
	
	protected DataStore _dataStore;

	

	/**
	 * Construct a StatusChangeListener
	 *
	 * @param dataStore the dataStore associated with this monitor
	 */	
	public DStoreStatusMonitor(DataStore dataStore) 
	{
		_dataStore = dataStore;
		reInit();
	}
	

	
	public void reInit()
	{
		_networkDown = false;	
		_workingStatuses = new ArrayList();
		_doneStatuses = new ArrayList();
		_cancelledStatuses = new ArrayList();
		if (_dataStore != null)
		{
		IDomainNotifier notifier = _dataStore.getDomainNotifier();
		if (notifier != null)
		{
			notifier.addDomainListener(this);
		}}
	}
	
	public DataStore getDataStore()
	{
	    return _dataStore;
	}
	
	public void dispose()
	{
	    _workingStatuses.clear();
	    _doneStatuses.clear();
	    _cancelledStatuses.clear();
	    _dataStore.getDomainNotifier().removeDomainListener(this);
	}
	
	/**
	 * @see IDomainListener#listeningTo(DomainEvent)
	 */
	public boolean listeningTo(DomainEvent event) 
	{
		if (_workingStatuses.size() == 0)
		{
			return true;
		}

		DataElement parent = (DataElement)event.getParent();
		if (_workingStatuses.contains(parent))
		{
		    return determineStatusDone(parent);
		}
		
		return false;
	}
	


	/**
	 * @see IDomainListener#domainChanged(DomainEvent)
	 */
	public void domainChanged(DomainEvent event) 
	{
	    if (_workingStatuses.size() == 0)
		{
			return;
		}

		DataElement parent = (DataElement)event.getParent();
		if (_workingStatuses.contains(parent))
		{
		    boolean isStatusDone = determineStatusDone(parent);
		    if (isStatusDone)
		    {
		    	setDone(parent);
		    	notifyUpdate();
		    }
		}
	}
	
    
    /**
     * Determines whether the status is done.
     * @return <code>true</code> if status done, <code>false</code> otherwise.
     */
    public boolean determineStatusDone(DataElement status) 
    {        
        return status.getAttribute(DE.A_VALUE).equals("done") ||  //$NON-NLS-1$
        status.getAttribute(DE.A_NAME).equals("done") ||//$NON-NLS-1$
        status.getAttribute(DE.A_NAME).equals("cancelled"); //$NON-NLS-1$
    }
	
	/**
	 * @return true if the the monitor is passive. In this case it is false.
	 */
	public boolean isPassiveCommunicationsListener() 
	{
		return false;
	}

	/**
	 * setDone(boolean)
	 */
	public synchronized void setDone(DataElement status)
	{	
	    _workingStatuses.remove(status);
	    _doneStatuses.add(status);	   
	}
	
	
	public synchronized void setCancelled(DataElement status)
	{
	    _workingStatuses.remove(status);
	    _cancelledStatuses.add(status);
	    
	    // send a cancel command if possible
		if (status != null)
		{
			DataElement command = status.getParent();
			DataStore dataStore = command.getDataStore();
			DataElement cmdDescriptor = command.getDescriptor();
			DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, "C_CANCEL"); //$NON-NLS-1$

			if (cancelDescriptor != null)
			{
				dataStore.command(cancelDescriptor, command);
			}
		}
	}
	
	public synchronized void setWorking(DataElement status)
	{
	    _workingStatuses.add(status);
	}
	
	
	public boolean wasCancelled(DataElement status) 
	{
	    if (_cancelledStatuses.contains(status))
	    {
	        return true;
	    }
		return false;
	}
	




	/**
	 * Test if the StatusChangeListener returned because the network connection to the 
	 * remote system was broken.
	 */
	public boolean isNetworkDown() 
	{
		return _networkDown;
	}
	
	 public DataElement waitForUpdate(DataElement status) throws InterruptedException
		{	
	        return waitForUpdate(status, null, 0);
		}
	
    public DataElement waitForUpdate(DataElement status, IProgressMonitor monitor) throws InterruptedException
	{	
        return waitForUpdate(status, monitor, 0);
	}
    
    public DataElement waitForUpdate(DataElement status, int wait) throws InterruptedException
	{	
        return waitForUpdate(status, null, wait);
	} 
    
    public synchronized DataElement waitForUpdate(DataElement status, IProgressMonitor monitor, int wait) throws InterruptedException
	{	
    	if (_networkDown && status.getDataStore().isConnected())
    	{
    		reInit();
    	}
        if (determineStatusDone(status))
        {
            setDone(status);
            return status;
        }
        
        setWorking(status);

				
	  // Prevent infinite looping by introducing a threshold for wait 
      int WaitThreshold = 200; //default. sleep(100ms) for 60 times  		
      if ( wait > 0 )
        WaitThreshold = wait*10; // 1 second means 10 sleep(100ms)
      else if ( wait == -1 ) // force a diagnostic
	  		 WaitThreshold = -1;
         
	  int nudges = 0; // nudges used for waking up server with slow connections
	      // nudge up to 12 times before giving up
	 
		{
			// Current thread is not UI thread
			while (_workingStatuses.contains(status))
			{
				boolean statusDone = determineStatusDone(status);
				if (statusDone) 
				{
					setDone(status);
				}
				else 
				{
					if ((monitor != null) && (monitor.isCanceled()))
					{
						setCancelled(status);
						throw new InterruptedException();
					}								
			
					waitForUpdate();
                    //Thread.sleep(200);
                    
                    if (WaitThreshold > 0) // update timer count if
                        // threshold not reached
                        --WaitThreshold; // decrement the timer count

                   if (WaitThreshold == 0)
				    {
                     	wakeupServer(status);
                        
				        // no diagnostic factory but there is a timeout
                    	if (nudges >= 12)
                    		return status;  // returning the undone status object
                    	
                    	nudges++;
				    }
                    else if (_networkDown)
                    {
    					dispose();
    					throw new InterruptedException();
                    }
                }
			}		
		}

	
		return status;
	}

	private void wakeupServer(DataElement status)
	{
		if (status != null)
		{
			// token command to wake up update handler
			DataElement cmdDescriptor = _dataStore.findCommandDescriptor("C_REFRESH"); //$NON-NLS-1$
			DataElement subject = status.getParent().get(0);
			if (cmdDescriptor != null)
			{
				_dataStore.command(cmdDescriptor, subject);
			}
		}
	}

	/**
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
	 */
	public synchronized void waitForUpdate()
	{
		try
		{
			wait(200);		
		}
		catch (InterruptedException e)
		{
			//InterruptedException is used to report user cancellation, so no need to log
			//This should be reviewed (use OperationCanceledException) with bug #190750

			return;
		}
	}
	
	/**
	 * Causes all threads waiting for this class request to be filled
	 * to wake up.
	 */
	public synchronized void notifyUpdate()
	{
		notifyAll();
	}
    

}
