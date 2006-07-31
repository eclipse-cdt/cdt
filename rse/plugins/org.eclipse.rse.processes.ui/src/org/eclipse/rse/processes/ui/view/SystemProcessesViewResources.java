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

import org.eclipse.osgi.util.NLS;


public class SystemProcessesViewResources extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.processes.ui.view.SystemProcessesViewResources"; 
	
	// PROCESS PROPERTIES
	public static String RESID_PROPERTY_PROCESS_PID_LABEL;
	public static String RESID_PROPERTY_PROCESS_NAME_LABEL; 
	public static String RESID_PROPERTY_PROCESS_UID_LABEL;
	public static String RESID_PROPERTY_PROCESS_USERNAME_LABEL;
	public static String RESID_PROPERTY_PROCESS_PPID_LABEL;
	public static String RESID_PROPERTY_PROCESS_GID_LABEL;
	public static String RESID_PROPERTY_PROCESS_STATE_LABEL;
	public static String RESID_PROPERTY_PROCESS_TGID_LABEL;
	public static String RESID_PROPERTY_PROCESS_TRACERPID_LABEL;
	public static String RESID_PROPERTY_PROCESS_VMSIZE_LABEL;
	public static String RESID_PROPERTY_PROCESS_VMRSS_LABEL;
	
	public static String RESID_PROPERTY_PROCESS_PID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_NAME_TOOLTIP; 
	public static String RESID_PROPERTY_PROCESS_UID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_USERNAME_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_PPID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_GID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_STATE_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_TGID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_TRACERPID_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_VMSIZE_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_VMSIZE_VALUE;
	public static String RESID_PROPERTY_PROCESS_VMRSS_TOOLTIP;
	public static String RESID_PROPERTY_PROCESS_VMRSS_VALUE;
	
	// Property sheet values: Processes
	public static String RESID_PROPERTY_PROCESS_TYPE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ROOT;
	public static String RESID_PROPERTY_PROCESS_TYPE_ERROR_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_RUNNING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_SLEEPING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_WAITING_VALUE;	
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOMBIE_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_TRACED_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_PAGING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ACTIVE_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_IDLE_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_NONEXISTENT_VALUE;

	// zOS states
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_SINGLE_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MSGQRECEIVEWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MSGQSENDWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_COMSYSKERNELWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_SEMAPHOREWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_QUIESCEFROZEN_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_FILESYSKERNELWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MVSPAUSEWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_PTHREADCREATEDTASKS_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_SWAPPEDOUT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_PTHREADCREATED_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_OTHERKERNELWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_CANCELLED_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MULTITHREAD_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MEDIUMWEIGHTTHREAD_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_ASYNCHRONOUSTHREAD_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_PTRACEKERNELWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_RUNNING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_SLEEPING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_STOPPED_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_INITIALPROCESSTHREAD_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_DETACHED_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_WAITINGFORCHILD_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_FORKING_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_MVSWAIT_VALUE;
	public static String RESID_PROPERTY_PROCESS_TYPE_ZOS_ZOMBIE_VALUE;
	
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemProcessesViewResources.class);
	}
}