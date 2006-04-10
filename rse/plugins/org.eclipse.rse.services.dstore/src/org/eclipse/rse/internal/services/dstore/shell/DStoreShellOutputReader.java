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

package org.eclipse.rse.internal.services.dstore.shell;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.extra.internal.extra.DomainEvent;
import org.eclipse.dstore.extra.internal.extra.IDomainListener;
import org.eclipse.rse.services.shells.AbstractHostShellOutputReader;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

public class DStoreShellOutputReader extends AbstractHostShellOutputReader implements IHostShellOutputReader, IDomainListener
{
	protected DataElement _status;
	protected int _statusOffset = 0;

	public DStoreShellOutputReader(IHostShell hostShell, DataElement status, boolean isErrorReader)
	{
		super(hostShell, isErrorReader);
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
	
	protected Object internalReadLine()
	{
		if (_status != null)
		{
			int newSize = _status.getNestedSize();
			while (newSize > _statusOffset)
			{
				DataElement line = _status.get(_statusOffset++);
				
			
				
				String type = line.getType();
				boolean isError =  type.equals("error") || type.equals("stderr");
				if (_isErrorReader && isError)
				{
					return line;
				}
				else if (!_isErrorReader && !isError)
				{
					return line;
				}
			}
			
		
			try
			{
				waitForResponse();		
				return internalReadLine();
			}
			catch (Exception e)
			{					
				e.printStackTrace();
			}
		}
		return "";
	}

	public boolean listeningTo(DomainEvent e)
	{
		return e.getParent() == _status;
	}

	public void domainChanged(DomainEvent event)
	{
		  if (_status.getValue().equals("done"))
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
	 * Causes the current thread to wait until this class request has been
	 * fulfilled.
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
	 * Causes all threads waiting for this class request to be filled
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
 