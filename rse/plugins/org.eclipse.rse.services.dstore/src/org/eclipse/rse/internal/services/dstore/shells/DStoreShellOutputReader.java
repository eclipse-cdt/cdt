/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * David McKnight (IBM) - [286671] return null when status is null
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore.shells;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.rse.services.shells.AbstractHostShellOutputReader;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;
import org.eclipse.rse.services.shells.SimpleHostOutput;

public class DStoreShellOutputReader extends AbstractHostShellOutputReader implements IHostShellOutputReader, IDomainListener
{
	protected DataElement _status;
	protected int _statusOffset = 0;

	public DStoreShellOutputReader(IHostShell hostShell, DataElement status, boolean isErrorReader)
	{
		super(hostShell, isErrorReader);
		 setName("DStoreShellOutputReader"+getName()); //$NON-NLS-1$
		_status = status;
		if (status != null)
		{
			_status.getDataStore().getDomainNotifier().addDomainListener(this);
		}
	}
	
	public String getWorkingDirectory()
	{
		String pwd = _status.getSource();
		return pwd;
	}
	
	protected IHostOutput internalReadLine()
	{
		if (_status != null && _keepRunning)
		{
			int newSize = _status.getNestedSize();

			while (newSize > _statusOffset)
			{
				DataElement line = _status.get(_statusOffset++);
				
			
				
				String type = line.getType();
				boolean isError =  type.equals("error") || type.equals("stderr"); //$NON-NLS-1$ //$NON-NLS-2$
				if (_isErrorReader && isError)
				{
					return new DStoreHostOutput(line);
				}
				else if (!_isErrorReader && !isError)
				{
					return new DStoreHostOutput(line);
				}
			}
			
		
			try
			{
				if (_hostShell.isActive())
				{
					waitForResponse();	
					return internalReadLine();
				}
				else
				{
					return null;
				}
			}
			catch (Exception e)
			{					
				e.printStackTrace();
			}
		}
		if (_status == null){
			return null;
		}
		if (_status.getValue().equals("done")) //$NON-NLS-1$
		{
			if (!_isErrorReader)
			{
				DataElement dummyLine = _status.getDataStore().createObject(_status, "stdout", ""); //$NON-NLS-1$ //$NON-NLS-2$
				return new DStoreHostOutput(dummyLine);
			}
			else
			{
				return null;
			}
		}
		return new SimpleHostOutput(""); //$NON-NLS-1$
	}

	public boolean listeningTo(DomainEvent e)
	{
		return e.getParent() == _status;
	}

	public void domainChanged(DomainEvent event)
	{
		  if (_status.getValue().equals("done")) //$NON-NLS-1$
	        {

	            if (_status == event.getParent())
	            {
	                finish();
	            }
	        }
	        else
	        {
	            // for now, this is pulled via internalReadLine()
	        	notifyResponse();
	        }
		
	}

	/**
	 * Causes the current thread to wait until notified
	 */
	public synchronized void waitForResponse()
	{
		try
		{
			wait();		
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Causes all threads waiting for this 
	 * to wake up.
	 */
	public synchronized void notifyResponse()
	{
		try
		{
			notifyAll();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void finish()
	{
		super.finish();
		notifyResponse();
	}
	
	/*
	private void handleInput()
	{
		 // append new results to existing results
        ArrayList results = _status.getNestedData();
        int totalSize = results.size();
        int currentSize = _linesOfOutput.size();

        for (int loop = currentSize; loop < totalSize; loop++)
        {        	        	        	            
        	DataElement result = (DataElement) results.get(loop);
            addLine(result.getName());
        }
	}
	*/

}
 
