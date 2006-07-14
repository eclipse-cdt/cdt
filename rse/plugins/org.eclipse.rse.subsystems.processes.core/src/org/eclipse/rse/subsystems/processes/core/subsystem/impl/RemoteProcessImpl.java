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

package org.eclipse.rse.subsystems.processes.core.subsystem.impl;

import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;
import org.eclipse.rse.subsystems.processes.core.subsystem.RemoteProcessSubSystem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * Represents a remote process on the client machine. Wrappers an IRemoteServerProcess,
 * with methods for returning information about the underlying process, as well
 * as more client-oriented methods for returning information about the associated
 * subsystem, connection, and filter string.
 * @author mjberger
 *
 */
public class RemoteProcessImpl implements IRemoteProcess, ISystemProcessRemoteConstants
{
    public static final char CONNECTION_DELIMITER = ':';
	protected IRemoteProcessContext _context;    
    protected IHostProcess _underlyingProcess;
    protected IRemoteProcess _parentProcess;
    
    /**
     * Constructor that takes a context object containing important information.
     * @param context An object holding contextual information about this object
     * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext
     */ 
    public RemoteProcessImpl(IRemoteProcessContext context, IHostProcess process)
    {
    	_context = context;
    	if ((context!=null) && (context.getParentRemoteProcessSubSystem()!=null) &&
    	    !context.getParentRemoteProcessSubSystem().isConnected())
    	  try
    	  {
    	  	// deduce active shell from display
            Shell shell = Display.getCurrent().getActiveShell();
    	    context.getParentRemoteProcessSubSystem().connect(shell);
    	  } catch (Exception exc) {}
    	  
    	  _underlyingProcess = process;
	}
        
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getParentRemoteProcessSubSystem()
	 */
	public RemoteProcessSubSystem getParentRemoteProcessSubSystem()
	{
		return _context.getParentRemoteProcessSubSystem();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getSystemConnection()
	 */
	public IHost getSystemConnection()
	{
    	RemoteProcessSubSystem ss = _context.getParentRemoteProcessSubSystem();
    	if (ss == null)
    	  return null;
    	else
    	  return ss.getHost();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getFilterString()
	 */
	public IHostProcessFilter getFilterString()
	{
		return _context.getFilterString();
	}

    /**
     * Set the filter string resolved to get this object
     */
    public void setFilterString(IHostProcessFilter filterString)
    {
    	_context.setFilterString(filterString);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getAbsolutePathPlusConnection()
	 */
	public String getAbsolutePathPlusConnection()
	{
		IHost conn = getSystemConnection();
    	if (conn == null)
    	  return getAbsolutePath();
    	else
    	  return conn.getSystemProfileName()+'.'+conn.getAliasName() + CONNECTION_DELIMITER + getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getParentRemoteProcess()
	 */
	public IRemoteProcess getParentRemoteProcess()
	{
		if (_parentProcess == null)
    	{
	    	IRemoteProcess parentProcess = _context.getParentRemoteProcess();
	    	if ((parentProcess == null) && getPPid() != -1)
	    	{    	  
	    		RemoteProcessSubSystem ss = _context.getParentRemoteProcessSubSystem();
	    		if (ss != null)
	    		{
	    			try 
	    			{
	    				parentProcess = ss.getRemoteProcessObject(getPPid());
	    			} 
	    			catch (SystemMessageException e) 
	    			{
	    				SystemBasePlugin.logError("UniversalProcessImpl.getParentRemoteProcess()", e);
	    			}
	    			
	    		}
	    	}
	    	_parentProcess = parentProcess;
    	}
    	return _parentProcess;
	}

    /**
     * Return the context associated with this remote process
     */
    public IRemoteProcessContext getContext()
    {
    	return _context;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getAbsolutePath()
	 */
	public String getAbsolutePath()
	{
		return "/proc/" + getPid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getPid()
	 */
	public long getPid()
	{
		return _underlyingProcess.getPid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getPPid()
	 */
	public long getPPid()
	{
		return _underlyingProcess.getPPid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getName()
	 */
	public String getName()
	{
		return _underlyingProcess.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getState()
	 */
	public String getState()
	{
		return _underlyingProcess.getState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getTgid()
	 */
	public long getTgid()
	{
		return _underlyingProcess.getTgid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getTracerPid()
	 */
	public long getTracerPid()
	{
		return _underlyingProcess.getTracerPid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getUid()
	 */
	public long getUid()
	{
		return _underlyingProcess.getUid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getUsername()
	 */
	public String getUsername()
	{
		return _underlyingProcess.getUsername();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getGid()
	 */
	public long getGid()
	{
		return _underlyingProcess.getGid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#isRoot()
	 */
	public boolean isRoot()
	{
		return _underlyingProcess.isRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getAllProperties()
	 */
	public String getAllProperties()
	{
		return _underlyingProcess.getAllProperties();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getVmSizeInKB()
	 */
	public long getVmSizeInKB()
	{
		return _underlyingProcess.getVmSizeInKB();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getVmRSSInKB()
	 */
	public long getVmRSSInKB()
	{
		return _underlyingProcess.getVmRSSInKB();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess#getLabel()
	 */
	public String getLabel()
	{
		return _underlyingProcess.getLabel();
	}
	
	/**
	 * Subclasses must override to return the underlying object,
	 * whether that is a DataElement, or an IRemoteServerProcess
	 */
	public Object getObject()
	{
		return null;
	}

	public Object getAdapter(Class adapter) {
  	    return Platform.getAdapterManager().getAdapter(this, adapter);	

	}

}