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

package org.eclipse.rse.services.dstore.processes;


import java.util.HashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;

/**
 * This class implements the IRemoteClientProcess interface by wrappering a DataElement
 * and returning process attribute information from the underlying DataElement.
 * It also contains client-specific methods for getting contextual information about
 * the process such as the associated subsystem and connection.
 * @author mjberger
 *
 */
public class DStoreHostProcess implements IHostProcess, org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants, IAdaptable, org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants
{
	public static final boolean ROOT_TRUE = true;
	public static final boolean ROOT_FALSE = false;
		   
    protected String _name, _username;
    protected long _pid = -1;
    protected long _ppid = -1;
    protected long _tgid = -1;
    protected long _tracerPid = -1;
    protected long _uid = -1;
    protected long _gid = -1;
    
    protected String _label = null;
    protected String _fullyQualifiedProcess;

    protected boolean _isRoot = false;
    
    protected Object _remoteObj;
        
    // master hash map
    protected HashMap _contents = new HashMap();
    
    /* container properties */
    protected boolean _isStale = false;
    
    // properties
    protected HashMap _properties = new HashMap();
    protected HashMap _propertyStates = new HashMap();
    
    /**
     * Constructor that takes a dataElement object containing the process information, and
     * a parent process.
     */ 
    public DStoreHostProcess(DataElement dataElementObj)
    {
    	init(dataElementObj);
    }
    
	protected void init(DataElement dataElementObj)
	{
		setProcess(dataElementObj);
		String name = dataElementObj.getName();
		_fullyQualifiedProcess = "/proc/" + name;
					
		// if we already have retrieved file properties
		// set them now
		String s = dataElementObj.getAttribute(DE.A_VALUE);
		if (s != null && s.length() > 0)
		{
			getAttributes(null);	
		}
	}
		
	/**
	 * The Properties of the process are returned from the miner in a string that can be parsed using
	 * the String.split method, with delimiter "|".
	 * You can also set all attributes at once with your own string passed as a parameter, as long
	 * as the string is in the same format as outlined below (pass in null to use the DataElement's string): 
	 * <p> The string contains properties of the object in the following order 
	 * <ul>
	 *    <li>Process Id (pid) - long 
	 *    <li>Executable name - String
	 *    <li>Status - char
	 *    <li>Tgid - long
	 *    <li>Process Parent id (ppid) - long
	 *    <li>Tracer pid - long
	 *    <li>User id (uid) - int
	 *	  <li>Username - String
	 *    <li>Group id (gid) - int
	 *    <li>VM Size - long
	 *    <li>VM RSS - long
	 * </ul>
	 */
	protected String getAttributes(String newAttributes)
	{
		DataElement deObj = (DataElement) this.getObject();

		String s = null;
		
		if (newAttributes == null) s = deObj.getAttribute(DE.A_VALUE);
		else s = newAttributes;
		
		if (s != null && s.length() > 0)
		{
		    String[] str = s.split("\\"+TOKEN_SEPARATOR);
		    int tokens = str.length;
		    if (tokens > 1)
			{
				try
				{
				    setPid(str[PROCESS_ATTRIBUTES_INDEX_PID]);
					
				    setName(str[PROCESS_ATTRIBUTES_INDEX_EXENAME]);
					
					setTgid(str[PROCESS_ATTRIBUTES_INDEX_TGID]);
					
					setPPid(str[PROCESS_ATTRIBUTES_INDEX_PPID]);
					
					if (_ppid == 0) _isRoot = true;
										
					setTracerPid(str[PROCESS_ATTRIBUTES_INDEX_TRACERPID]);
					
					setUid(str[PROCESS_ATTRIBUTES_INDEX_UID]);
					
					setUsername(str[PROCESS_ATTRIBUTES_INDEX_USERNAME]);
					
					setGid(str[PROCESS_ATTRIBUTES_INDEX_GID]);
					
					setLabel(str[PROCESS_ATTRIBUTES_INDEX_EXENAME]);
			    }
			    catch (ArrayIndexOutOfBoundsException e)
			    {
			        // SystemPlugin.logError("Error in UniversalProcessImpl.getAttributes().  Attributes = " + s);
			    }	
			}
		}
		return s;
	}
	
	/**
	 * Set the DataElement for this object
	 */
	public void setProcess(Object dataElementObj)
	{
		_remoteObj = (DataElement) dataElementObj;
	}

	/**
	 * Returns the DataElement for this object
	 */
    public Object getObject()
    {
    	return _remoteObj;
    }
	
	public String getAbsolutePath()
	{
		return _fullyQualifiedProcess;
	}

	public String getLabel()
	{
		return _label;
	}

	public long getPid()
	{
		return _pid;
	}

	public String getName()
	{
		return _name;
	}

	public String getState()
	{
		String state = getSubAttribute(PROCESS_ATTRIBUTES_INDEX_STATUS);
		if (state == null) 
		{
			//SystemPlugin.logError("Error in UniversalProcessImpl.getAttributes(): status of process " + getPid() + " is not given.");
			return " ";
		}
		else return state;
	}

	public long getTgid()
	{
		return _tgid;
	}

	public long getTracerPid()
	{
		return _tracerPid;
	}

	public long getUid()
	{
		return _uid;
	}
	
	public String getUsername()
	{
		return _username;
	}

	public long getGid()
	{
		return _gid;
	}

	protected void setAbsolutePath(String path)
	{
		_fullyQualifiedProcess = path;
	}

	protected void setGid(long gid)
	{
		_gid = gid;
	}
	
	protected void setGid(String newGid)
	{
		try
		{
			_gid = Long.parseLong(newGid);
		}
		catch (NumberFormatException e)
		{
			//SystemPlugin.logError("Error in UniversalProcessImpl.setGid: Could not parse gid into integer.");
			_gid = -1;
		}
	}

	public void setLabel(String newLabel)
	{
		_label = newLabel;
	}

	protected void setName(String exeName)
	{
		_name = exeName;
	}

	protected void setPid(long newPid)
	{
		_pid = newPid;
	}
	
	protected void setPid(String newPid)
	{
		try
		{
			_pid = Long.parseLong(newPid);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setPid: Could not parse pid into integer.");
			_pid = -1;
		}
	}

	protected void setState(String newState)
	{
		setSubAttribute(PROCESS_ATTRIBUTES_INDEX_STATUS, newState);
	}

	protected void setTgid(long tgid)
	{
		_tgid = tgid;
	}
	
	protected void setTgid(String newTgid)
	{
		try
		{
			_tgid = Long.parseLong(newTgid);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setTgid: Could not parse tgid into integer.");
			_tgid = -1;
		}
	}

	protected void setTracerPid(long tracerPid)
	{
		_tracerPid = tracerPid;
	}
	
	protected void setTracerPid(String newTracerPid)
	{
		try
		{
			_tracerPid = Long.parseLong(newTracerPid);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setTracerPid: Could not parse tracerpid into integer.");
			_tracerPid = -1;
		}
	}

	protected void setUid(long uid)
	{
		_uid = uid;
	}
	
	protected void setUid(String newUid)
	{
		try
		{
			_uid = Long.parseLong(newUid);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setUid: Could not parse uid into integer.");
			_uid = -1;
		}
	}
	
	protected void setUsername(String username)
	{
		_username = username;
	}
	
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }

	public long getPPid()
	{
		return _ppid;
	}

	protected void setPPid(long newPPid)
	{
		_ppid = newPPid;
	}
	
	protected void setPPid(String newPPid)
	{
		try
		{
			_ppid = Long.parseLong(newPPid);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setPPid: Could not parse ppid into integer.");
			_ppid = -1;
		}
	}
	
	public boolean isRoot()
	{
		return _isRoot;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.IHostProcess#getAllProperties()
	 */
	public String getAllProperties()
	{
		return getAttributes(null);
	}

	/**
	 * @param allProperties
	 */
	protected void setAllProperties(String allProperties)
	{
		getAttributes(allProperties);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.IHostProcess#getVmSizeInKB()
	 */
	public long getVmSizeInKB()
	{
		String sizeStr = getSubAttribute(PROCESS_ATTRIBUTES_INDEX_VMSIZE);
		if (sizeStr == null) return 0;
		long vmsize = 0;
		try
		{
			vmsize = Long.parseLong(sizeStr);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.getVMSizeInKB: Could not parse VM Size into integer.");
			return 0;
		}
		return vmsize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.IHostProcess#getVmRSSInKB()
	 */
	public long getVmRSSInKB()
	{
		String sizeStr = getSubAttribute(PROCESS_ATTRIBUTES_INDEX_VMRSS);
		if (sizeStr == null) return 0;
		long vmrss = 0;
		try
		{
			vmrss = Long.parseLong(sizeStr);
		}
		catch (NumberFormatException e)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.getVMRSSInKB: Could not parse VM RSS into integer.");
			return 0;
		}
		return vmrss;
	}


	/**
	 * @param size the size to set
	 */
	protected void setVmSizeInKB(long size)
	{
		String sizeStr = "";
		sizeStr = sizeStr + size;
		setSubAttribute(PROCESS_ATTRIBUTES_INDEX_VMSIZE, sizeStr);
	}
	
	/**
	 * @param size the size to set
	 */
	protected void setVmRSSInKB(long size)
	{
		String sizeStr = "";
		sizeStr = sizeStr + size;
		setSubAttribute(PROCESS_ATTRIBUTES_INDEX_VMRSS, sizeStr);
	}

	/**
	 * Returns a subattribute of the A_VALUE of this process's associated
	 * data element.
	 * @param attIndex the index of the desired subattribute
	 * @return a string containing that attribute, or null if that attribute
	 * is not specified, if the dataelement does not exist, or if the attIndex
	 * is out of bounds.
	 */
	protected String getSubAttribute(int attIndex)
	{
		DataElement deObj = (DataElement) this.getObject();
		if (deObj == null) return null;

		String s = deObj.getAttribute(DE.A_VALUE);
		
		if (s != null && s.length() > 0)
		{
		    String[] str = s.split("\\"+TOKEN_SEPARATOR);
		    if (attIndex >= str.length) return null;
		    if (str[attIndex] == null || str[attIndex].equals("")) return null;
		    else return str[attIndex];
		}
		else return null;
	}

	/**
	 * Helper method for setting any attributes of the underlying DataElement.
	 * @param attIndex
	 * @param newSubAttribute
	 */
	protected void setSubAttribute(int attIndex, String newSubAttribute)
	{
		DataElement deObj = (DataElement) this.getObject();
		if (deObj == null)
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setSubAttribute: Could not access dataelement.");
			return;
		}
		String s = deObj.getAttribute(DE.A_VALUE);
		
		if (s != null && s.length() > 0)
		{
		    String[] str = s.split("\\"+TOKEN_SEPARATOR);
		    if (attIndex >= str.length)
		    {
				// SystemPlugin.logError("Error in UniversalProcessImpl.setSubAttribute: Attribute index out of bounds.");		    	
				return;
		    }
		    s = "";
		    str[attIndex] = newSubAttribute;
		    for (int i = 0; i < str.length; i++)
		    {
		    	if (i == str.length - 1) s = s + str[i];
		    	else s = s + str[i] + TOKEN_SEPARATOR;
		    }
		    deObj.setAttribute(DE.A_VALUE, s);
		    DataStore ds = deObj.getDataStore();
		    ds.refresh(deObj);
		}
		else
		{
			// SystemPlugin.logError("Error in UniversalProcessImpl.setSubAttribute: Dataelement did not contain an attribute string.");		    	
			return;
		}
	}
}