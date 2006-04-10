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

package org.eclipse.rse.processes.ui.view;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;


/**
 * Content provider for any widget that wishes to get the names of all possible
 * states of a process. Also contains a utility methods for getting the translated
 * information about individual process states.
 * @author mjberger
 *
 */
public class SystemProcessStatesContentProvider implements ISystemProcessRemoteConstants, IStructuredContentProvider
{
	private HashMap strIndices;
	
	/**
	 * Constructor
	 */
	public SystemProcessStatesContentProvider()
	{
		strIndices = new HashMap();
		// construct a mapping from unique state names to integers. Each integer
		// is the index of the associated state name in the array of translated
		// state name strings.
		for (int i = 0; i < ALL_STATES_STR.length; i++)
		{
			strIndices.put(ALL_STATES_STR[i], new Integer(i));
		}
	}
	
	/**
	 * @return a String array containing the translated names of all the process states.
	 */
	public static String[] getStates()
	{
		return new String[]
		    {
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ACTIVE_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_IDLE_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_NONEXISTENT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_PAGING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_RUNNING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_SLEEPING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_TRACED_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_WAITING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOMBIE_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_SINGLE_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MSGQRECEIVEWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MSGQSENDWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_COMSYSKERNELWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_SEMAPHOREWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_QUIESCEFROZEN_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_FILESYSKERNELWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MVSPAUSEWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_PTHREADCREATEDTASKS_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_SWAPPEDOUT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_PTHREADCREATED_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_OTHERKERNELWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_CANCELLED_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MULTITHREAD_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MEDIUMWEIGHTTHREAD_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_ASYNCHRONOUSTHREAD_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_PTRACEKERNELWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_RUNNING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_SLEEPING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_STOPPED_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_INITIALPROCESSTHREAD_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_DETACHED_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_WAITINGFORCHILD_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_FORKING_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_MVSWAIT_VALUE,
				SystemProcessesViewResources.RESID_PROPERTY_PROCESS_TYPE_ZOS_ZOMBIE_VALUE
		    };
		
	}
	
	/**
	 * Given the unique name/code representing a process state, returns
	 * the translated string for the process state.
	 * @param state the unique name/code for a process state.
	 * @return the associated translated name, or "" if a matching one cannot be found
	 */
	public String getStateString(String state)
	{
		Integer index = (Integer) strIndices.get(state);
		if (index == null) return "";
		String[] resources = getStates();
		if (index.intValue() >= resources.length) return "";
		return resources[index.intValue()];
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement)
	{
		return getStates();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
	}
}