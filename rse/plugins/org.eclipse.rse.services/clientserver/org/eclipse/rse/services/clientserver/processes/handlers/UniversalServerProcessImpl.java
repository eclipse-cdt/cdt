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

package org.eclipse.rse.services.clientserver.processes.handlers;

import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;

/**
 * @author mjberger
 */
public class UniversalServerProcessImpl implements IRemoteServerProcess, IServiceConstants, ISystemProcessRemoteConstants
{
    protected Object[] _properties = new Object[PROCESS_ATTRIBUTES_COUNT+1];
    
    /**
     * create a new UniversalServerProcessImpl with the default property set
     */
    public UniversalServerProcessImpl()
    {
    	_properties[PROCESS_ATTRIBUTES_INDEX_EXENAME] = " ";
    	_properties[PROCESS_ATTRIBUTES_INDEX_GID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_PID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_PPID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_STATUS] = new String(" ");
    	_properties[PROCESS_ATTRIBUTES_INDEX_TGID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_TRACERPID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_UID] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_USERNAME] = " ";
    	_properties[PROCESS_ATTRIBUTES_INDEX_VMSIZE] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_INDEX_VMRSS] = new Long(-1);
    	_properties[PROCESS_ATTRIBUTES_COUNT] = " "; //set the label
    }

    /**
     * create a new UniversalServerProcessImpl with initial Attributes.
     * This is equivalent to constructing the object, then calling setAllProperties(initialAttributes)
     */
    public UniversalServerProcessImpl(String initialAttributes)
    {
    	setAllProperties(initialAttributes);
    }

    protected Long getLongAttribute(String value, long dflt)
    {
    	long result;
    	try
    	{
    		result = Long.parseLong(value);
    	}
    	catch (NumberFormatException e)
    	{
    		return new Long(dflt);
    	}
    	return new Long(result);
    }
    
    protected Integer getIntAttribute(String value, int dflt)
    {
    	int result;
    	try
    	{
    		result = Integer.parseInt(value);
    	}
    	catch (NumberFormatException e)
    	{
    		return new Integer(dflt);
    	}
    	return new Integer(result);
    }
    
	public void setPid(String pid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_PID] = getLongAttribute(pid, -1);
	}

	public void setPPid(String ppid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_PPID] = getLongAttribute(ppid, -1);

	}

	public void setName(String name)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_EXENAME] = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.IRemoteProcess#getLabel()
	 */
	public String getLabel()
	{
		return (String) _properties[PROCESS_ATTRIBUTES_COUNT];
	}
	
	public void setLabel(String label)
	{
		_properties[PROCESS_ATTRIBUTES_COUNT] = label;
	}

	public void setState(String state)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_STATUS] = state;
	}

	public void setTgid(String tgid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_TGID] = getLongAttribute(tgid, -1);
	}

	public void setTracerPid(String tracerpid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_TRACERPID] = getLongAttribute(tracerpid, -1);
	}

	public void setUid(String uid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_UID] = getLongAttribute(uid, -1);
	}

	public void setUsername(String username)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_USERNAME] = username;
	}

	public void setGid(String gid)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_GID] = getLongAttribute(gid, -1);
	}

	public void setVmSizeInKB(String size)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_VMSIZE] = getLongAttribute(size, 0);
	}
	
	public void setVmRSSInKB(String size)
	{
		_properties[PROCESS_ATTRIBUTES_INDEX_VMRSS] = getLongAttribute(size, 0);
	}
	
	/**
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
	 *    <li>User id (uid) - long
	 *	  <li>Username - String
	 *    <li>Group id (gid) - long
	 *    <li>VM Size - long
	 *    <li>VM RSS - long
	 * </ul>
	 */
	public void setAllProperties(String allProperties)
	{
		String s = allProperties;
		
		if (s != null && s.length() > 0)
		{
		    String[] str = s.split("\\"+TOKEN_SEPARATOR);
		    int numOfExpectedTokens = PROCESS_ATTRIBUTES_COUNT;
		    int tokens = str.length;
			if (tokens == numOfExpectedTokens)
			{
				try
				{
				    setPid(str[PROCESS_ATTRIBUTES_INDEX_PID]);
					
				    setName(str[PROCESS_ATTRIBUTES_INDEX_EXENAME]);
					
					setTgid(str[PROCESS_ATTRIBUTES_INDEX_TGID]);
					
					setPPid(str[PROCESS_ATTRIBUTES_INDEX_PPID]);
										
					setTracerPid(str[PROCESS_ATTRIBUTES_INDEX_TRACERPID]);
					
					setUid(str[PROCESS_ATTRIBUTES_INDEX_UID]);
					
					setUsername(str[PROCESS_ATTRIBUTES_INDEX_USERNAME]);
					
					setGid(str[PROCESS_ATTRIBUTES_INDEX_GID]);
					
					setVmSizeInKB(str[PROCESS_ATTRIBUTES_INDEX_VMSIZE]);
					
					setVmRSSInKB(str[PROCESS_ATTRIBUTES_INDEX_VMRSS]);
					
					setState(str[PROCESS_ATTRIBUTES_INDEX_STATUS]);
					
					setLabel(getName());
					
			    }
			    catch (ArrayIndexOutOfBoundsException e)
			    {
			       // SystemPlugin.logError("Error in UniversalProcessImpl.getAttributes(). Attributes in unexpected format. Attributes = " + s);
			    }	
			}
		}
	}

	public long getPid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_PID]).longValue();
	}

	public long getPPid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_PPID]).longValue();
	}

	public String getName()
	{
		return (String) _properties[PROCESS_ATTRIBUTES_INDEX_EXENAME];
	}

	public String getState()
	{
		return (String) _properties[PROCESS_ATTRIBUTES_INDEX_STATUS];
	}

	public long getTgid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_TGID]).longValue();
	}

	public long getTracerPid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_TRACERPID]).longValue();
	}

	public long getUid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_UID]).intValue();
	}

	public String getUsername()
	{
		return (String) _properties[PROCESS_ATTRIBUTES_INDEX_USERNAME];
	}

	public long getGid()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_GID]).intValue();
	}

	public boolean isRoot()
	{
		return (getPPid() == 0);
	}

	public long getVmSizeInKB()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_VMSIZE]).longValue();
	}
	
	public long getVmRSSInKB()
	{
		return ((Long)_properties[PROCESS_ATTRIBUTES_INDEX_VMRSS]).longValue();
	}

	/**
	 * Return all the properties of this data structure in one string.
	 * Properties are separated by IUniversalDataStoreConstants.TOKEN_SEPARATOR;
	 */
	public String getAllProperties()
	{
		String properties = "";
		for (int i = 0; i < PROCESS_ATTRIBUTES_COUNT; i++)
		{
			properties = properties + _properties[i].toString();
			if (i != PROCESS_ATTRIBUTES_COUNT - 1)
				properties = properties + TOKEN_SEPARATOR;
		}
		return properties;
	}
}