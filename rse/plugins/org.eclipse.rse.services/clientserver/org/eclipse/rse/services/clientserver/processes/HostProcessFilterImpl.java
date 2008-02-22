/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [219975] Fix implementations of clone()
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.processes;


import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.rse.services.clientserver.NamePatternMatcher;

/**
 * A class representing a remote process filter string. This is a name pattern
 *  for returning lists of remote processes when used as input to the
 *  UniversalProcessMiner class.
 *
 * Valid generic names are names with one or two asterisks
 *  anywhere in the name, as in:
 * <sl>
 *   <code>ABC*</code> or <code>*ABC</code> or <code>A*C</code>
 *   <code>*ABC*</code> or <code>*A*C</code> or <code>A*C*</code>
 * </sl>
 *
 * To get the actual filter string back from objects of this class, just call {@link #toString()}.
 * <p>
 * Clients may use or subclass this class. When subclassing, clients need to
 * ensure that the subclass is always capable of performing a deep clone 
 * operation with the {@link #clone()} method, so if they add fields of 
 * complex type, these need to be dealt with by overriding {@link #clone()}. 
 */
public class HostProcessFilterImpl implements IHostProcessFilter, Cloneable
{

	public static final String ALL = "*"; //$NON-NLS-1$

	protected static final char WILDCARD = '*';

	protected String name, username, gid, ppid, pid;
	protected long minVM, maxVM;
	protected boolean anystatus;
	protected String status;
	protected HashMap states;
	protected boolean _resolveVariables;
	
	/**
	 * Constructor to use when there is no existing filter string.
	 */
	public HostProcessFilterImpl()
	{
		_resolveVariables = false;
		init();
	}
	
	/**
	 * Constructor to use when there is no existing filter string.
	 */
	public HostProcessFilterImpl(boolean resolveVariables)
	{
		_resolveVariables = resolveVariables;
		init();
	}
	
	protected void initStates()
	{
		states = new HashMap();
		for (int i = 0; i < ISystemProcessRemoteConstants.ALL_STATES_STR.length; i++)
		{
			states.put(ISystemProcessRemoteConstants.ALL_STATES_STR[i], new Boolean(false));
		}
	}

	/**
	 * Constructor to use when filter string already exists.
	 */
	public HostProcessFilterImpl(String input)
	{
		_resolveVariables = false;
		initInput(input);
	}
	
	/**
	 * Constructor to use when filter string already exists.
	 */
	public HostProcessFilterImpl(String input, boolean resolveVariables)
	{
		_resolveVariables = resolveVariables;
		initInput(input);
	}
	
	protected void init()
	{
		name = ALL;
		username = ALL;
		gid = ALL;
		ppid = ALL;
		pid = ALL;
		minVM = 0;
		maxVM = -1;
		anystatus = true;
		initStates();
		status = ""; //$NON-NLS-1$
	}
	
	protected void initInput(String input)
	{
		anystatus = true;
		StringTokenizer tz = new StringTokenizer(input, "|"); //$NON-NLS-1$
		String strMinVM = "0"; //$NON-NLS-1$
		String strMaxVM = "-1"; //$NON-NLS-1$

		if (tz.hasMoreTokens())
		{
			name = tz.nextToken();
			
			if (tz.hasMoreTokens())
			{
				username = tz.nextToken();
				
				if (tz.hasMoreTokens())
				{
					gid = tz.nextToken();
					if (tz.hasMoreTokens())
					{
						status = tz.nextToken().trim();
						if (tz.hasMoreTokens())
						{
							ppid = tz.nextToken();
							if (tz.hasMoreTokens())
							{
								pid = tz.nextToken();
								if (tz.hasMoreTokens())
								{
									strMinVM = tz.nextToken();
									if (tz.hasMoreTokens())
									{
										strMaxVM = tz.nextToken();
									}
								}
							}
						}
					}
				}
			}
	    }
		
		if (!(status == null))
		{
			if (!status.equals("")) //$NON-NLS-1$
			{
				String[] allStates = status.split(","); //$NON-NLS-1$
				if (!(allStates == null))
				{
					initStates();
					anystatus = false;
					for (int i = 0; i < allStates.length; i++)
					{
						states.put(allStates[i], new Boolean(true));
					}
				}
			}
		}
		
	    if (name == null || name.equals("")) //$NON-NLS-1$
	      name = ALL;

	    if (username == null || username.equals("")) //$NON-NLS-1$
	      username = ALL;

	    if (gid == null || gid.equals("")) //$NON-NLS-1$
	    	gid = ALL;
	    
	    if (ppid == null || ppid.equals("")) //$NON-NLS-1$
	    	ppid = ALL;

	    if (pid == null || pid.equals("")) //$NON-NLS-1$
	    	pid = ALL;
	    try
	    {
	    	minVM = Long.parseLong(strMinVM);
	    }
	    catch (Exception e)
	    {
	    	minVM = 0;
	    }
	    try
	    {
	    	maxVM = Long.parseLong(strMaxVM);
	    }
	    catch (Exception e)
	    {
	    	maxVM = -1;
	    }
	    if (minVM < 0)
	    {
	    	minVM = 0;
	    }
	    if (maxVM < 0)
	    {
	    	maxVM = -1;
	    }
	}
	
	public HashMap getStates()
	{
		return states;
	}

	/**
	 * Return the process name part of this filter string.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Return the username part of this filter string.
	 */
	public String getUsername()
	{
		if (_resolveVariables && username.equals("${user.id}")) //$NON-NLS-1$
		{
			return ALL;
		}
		else return username;
	}

	/**
	 * Return the process group id (gid) part of this filter string.
	 */
	public String getGid()
	{
		return gid;
	}
	
	/**
	 * Return the process parent id (ppid) part of this filter string.
	 */
	public String getPpid()
	{
		return ppid;
	}
	
	/**
	 * Return the process id (pid) part of this filter string.
	 */
	public String getPid()
	{
		return pid;
	}

	/**
	 * Returns true when all process states are selected. The individal state
	 * queries will return false in this case.
	 */
	public boolean getAnyStatus()
	{
		return anystatus;
	}
	
	/**
	 * Returns the minimum VM size for processes allowed by this filter
	 */
	public String getMinVM()
	{
		return "" + minVM; //$NON-NLS-1$
	}

	/**
	 * Returns the maximum VM size for processes allowed by this filter
	 */
	public String getMaxVM()
	{
		return "" + maxVM; //$NON-NLS-1$
	}


	/**
	 * Set the name part of this filter string. This can be simple or
	 * generic, where generic is a name containing one or two asterisks
	 * anywhere in the name.
	 */
	public void setName(String obj)
	{
		name = obj;
	}

	/**
	 * Set the user id (uid) part of this filter string. This can be simple or
	 * generic, where generic is a uid containing one or two asterisks anywhere
	 * in the name.
	 */
	public void setUsername(String obj)
	{
		username = obj;
	}

	/**
	 * Set the process group id (gid) part of this filter string.
	 */
	public void setGid(String obj)
	{
		gid = obj;
	}
	
	/**
	 * Set the process parent id part of this filter string.
	 */
	public void setPpid(String obj)
	{
		ppid = obj;
	}
	
	/**
	 * Set the process id part of this filter string.
	 */
	public void setPid(String obj)
	{
		pid = obj;
	}

	/**
	 * Select all/any process states
	 */
	public void setAnyStatus()
	{
		anystatus = true;
		initStates();
	}
	
	/**
	 * Sets the minimum VM size for processes allowed by this filter
	 */
	public void setMinVM(String strMinVM)
	{
		try
		{
			minVM = Long.parseLong(strMinVM);
		}
		catch (Exception e)
		{
			minVM = 0;
		}
	}

	/**
	 * Sets the maximum VM size for processes allowed by this filter
	 */
	public void setMaxVM(String strMaxVM)
	{
		try
		{
			maxVM = Long.parseLong(strMaxVM);
		}
		catch (Exception e)
		{
			maxVM = 0;
		}
	}

    /**
     * Convert this filter into a filter string.
     */
	public String toString()
	{
		return name + "|" + username + "|" + gid + "|" + toStateString() + "|" + ppid + "|" + pid + "|" + minVM + "|" + maxVM; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}
	  
	protected String toStateString()
	{
	    String s = " "; //$NON-NLS-1$

	    if (!anystatus)
	    {
	    	for (int i = 0; i < ISystemProcessRemoteConstants.ALL_STATES_STR.length; i++)
	    	{
	    		Boolean currentState = (Boolean) states.get(ISystemProcessRemoteConstants.ALL_STATES_STR[i]);
	    		if (currentState.booleanValue()) s = s + ISystemProcessRemoteConstants.ALL_STATES_STR[i];
	    		if (currentState.booleanValue()) s = s + ","; //$NON-NLS-1$
	    	}
	    	if (!s.trim().equals("")) s = s.trim(); //$NON-NLS-1$
	    	if (s.endsWith(",")) s = s.substring(0, s.length() - 1); //$NON-NLS-1$
	    }
	    return s;
	}
	
	/**
	 * Returns whether this filter allows a process with the status line
	 * <code>status</code> to pass through. The status line contains some of the contents of
	 * the <i>status</i> file contained in the processes numbered directory in
	 * the /proc filesystem. For example, the status line of process 12345 is
	 * the contents of the file <i>/proc/12345/stat</i>.
	 * The status line must be structured as follows:
	 * "pid|name|status|tgid|ppid|tracerpid|uid|username|gid|vmSize|vmRSS"
	 */
	public boolean allows(String status)
	{
		NamePatternMatcher matcher = null;
		String[] tokens = status.split("\\|"); //$NON-NLS-1$
		if (tokens.length < (ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_COUNT -1)) return false;
		
		matcher = new NamePatternMatcher(gid, true, false);
		if (!matcher.matches(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_GID])) return false;
			
		matcher = new NamePatternMatcher(name, true, false);
		if (!matcher.matches(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_EXENAME])) return false;

		String state = tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_STATUS].trim();
		if (!satisfiesState(state)) return false;
		
		if (getUsername().equals("${user.id}")) //$NON-NLS-1$
			matcher = new NamePatternMatcher(ALL, true, false);
		else
			matcher = new NamePatternMatcher(getUsername(), true, false);
		if (!matcher.matches(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_USERNAME])) return false;
		matcher = new NamePatternMatcher(ppid, true, false);
		if (!matcher.matches(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_PPID])) return false;
		matcher = new NamePatternMatcher(pid, true, false);
		if (!matcher.matches(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_PID])) return false;
		long vmSize = 0;
		try
		{
			vmSize = Long.parseLong(tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_VMSIZE]);
		}
		catch (Exception e)
		{
			if (tokens[ISystemProcessRemoteConstants.PROCESS_ATTRIBUTES_INDEX_VMSIZE].trim().equals("")) vmSize = 0; //$NON-NLS-1$
			else return false;
		}
		if (!(vmSize >= minVM)) return false;
		if (maxVM > -1 && !(vmSize <= maxVM)) return false;

		return true;
	}
	
	public boolean getSpecificState(String stateCode)
	{
		if (anystatus) return true;
		Boolean state = (Boolean) states.get(stateCode);
		if (state == null) return false;
		return state.booleanValue();
	}
	
	public void setSpecificState(String stateCode)
	{
		anystatus = false;
		initStates();
		states.put(stateCode, new Boolean(true));
	}
	
	public boolean satisfiesState(String state)
	{
		if (!anystatus)
		{
			String[] allStates = state.split(","); //$NON-NLS-1$
			if (allStates == null) return false;
			if (allStates.length == 0) return false;
			boolean satisfied = false;
			for (int i = 0; i < allStates.length; i++)
			{
				String nextState = allStates[i];		
				satisfied = getSpecificState(nextState);
				if (satisfied) break;
			}
			if (!satisfied) return false;
		}
		return true;
	}
	
	/**
	 * Return an identical (deep) copy of this filter.
	 * 
	 * Subclasses must ensure that such a deep copy operation is always
	 * possible, so their state must always be cloneable. Which should 
	 * always be possible to achieve, since this Object also needs to be
	 * serializable.
	 */
	public Object clone() {
		HostProcessFilterImpl clone = null;
		try {
			clone = (HostProcessFilterImpl)super.clone();
		} catch (CloneNotSupportedException e) {
			//assert false; //can never happen
			throw new RuntimeException(e);
		}
		if (states!=null) {
			clone.states = (HashMap)states.clone();
		}
		return clone;
	}
}
