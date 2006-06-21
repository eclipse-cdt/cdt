/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreSchema;

/**
 * @author mjberger
 *
 * copyright 2003 IBM Corp.
 */
public class DStoreFileTransferStatusListener extends StatusChangeListener 
{

	protected String _remotePath;
	protected DataElement _log;
	protected DataElement _statusElement;

	public DStoreFileTransferStatusListener(String remotePath, Shell shell, IProgressMonitor monitor, DataStore ds, DataElement uploadLog) throws Exception
	{
		super(shell, monitor);
		_remotePath = remotePath.replace('\\', '/');

		_log = uploadLog;
		if (_log == null) 
		{
			throw new Exception("Could not find log in DataStore.");
		}
		setStatus(findOrCreateUploadStatus(ds)); 
	}
	
	protected DataElement findOrCreateUploadStatus(DataStore ds)
	{		
		DataElement result = ds.find(_log, DE.A_NAME, _remotePath,1);
		
		// first upload, this will always ben null
		// but also need to handle case where it's been uploaded before in the sessoin (i.e. for reseting values)
		if (result == null) 
		{
			result = _log.getDataStore().createObject(_log, "uploadstatus", _remotePath);
			result.setAttribute(DE.A_SOURCE, "running");
			result.setAttribute(DE.A_VALUE, "");
			
			DataElement cmd = ds.findCommandDescriptor(DataStoreSchema.C_SET);
			
//			DataElement setstatus = ds.command(cmd, _log, true);
			ds.command(cmd, _log, true);
			/*
			try
			{
				StatusMonitorFactory.getInstance().getStatusMonitorFor(system, ds).waitForUpdate(setstatus);
			}
			catch (Exception e)
			{				
			}
			*/
			// DKM: no need for this - turns out the problem was that we need to send the LOG not the result - 
		    // since the server needs to know the parent!
			//ds.waitUntil(setstatus, "done");
			
		}
		else
		{
		   
			result.setAttribute(DE.A_SOURCE, "running");
			result.setAttribute(DE.A_VALUE, "");
		}
		_statusElement = result;
		return result;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.rse.services.dstore.util.StatusChangeListener#determineStatusDone()
     */
    protected boolean determineStatusDone() 
    {
        return getStatus().getAttribute(DE.A_SOURCE).equals(IServiceConstants.SUCCESS) || getStatus().getAttribute(DE.A_SOURCE).equals(IServiceConstants.FAILED);
    	
    }
    
	public boolean uploadHasFailed()
	{
		return getStatus().getAttribute(DE.A_SOURCE).equals(IServiceConstants.FAILED);
	}
	
	public String getErrorMessage()
	{
		return getStatus().getAttribute(DE.A_VALUE);
	}

	public String getRemotePath() 
	{
		return _remotePath;
	}

	public void setRemotePath(String remotePath) 
	{
		_remotePath = remotePath;
	}

	
}