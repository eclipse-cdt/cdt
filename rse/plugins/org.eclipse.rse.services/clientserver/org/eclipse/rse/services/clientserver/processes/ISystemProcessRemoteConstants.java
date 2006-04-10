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

package org.eclipse.rse.services.clientserver.processes;


public interface ISystemProcessRemoteConstants
{
	public static final int PROCESS_ATTRIBUTES_INDEX_PID = 0;
	public static final int PROCESS_ATTRIBUTES_INDEX_EXENAME = 1;
	public static final int PROCESS_ATTRIBUTES_INDEX_STATUS = 2;
	public static final int PROCESS_ATTRIBUTES_INDEX_TGID = 3;
	public static final int PROCESS_ATTRIBUTES_INDEX_PPID = 4;
	public static final int PROCESS_ATTRIBUTES_INDEX_TRACERPID = 5;
	public static final int PROCESS_ATTRIBUTES_INDEX_UID = 6;
	public static final int PROCESS_ATTRIBUTES_INDEX_USERNAME = 7;
	public static final int PROCESS_ATTRIBUTES_INDEX_GID = 8;	 
	public static final int PROCESS_ATTRIBUTES_INDEX_VMSIZE = 9;
	public static final int PROCESS_ATTRIBUTES_INDEX_VMRSS = 10;
	public static final int PROCESS_ATTRIBUTES_COUNT = 11;

	public static final char STATE_ACTIVE = 'A';
	public static final char STATE_IDLE = 'I';
	public static final char STATE_NONEXISTENT = 'O';
	public static final char STATE_PAGING = 'W';
	public static final char STATE_RUNNING = 'R';
	public static final char STATE_SLEEPING = 'S';
	public static final char STATE_TRACED = 'T';
	public static final char STATE_WAITING = 'D';
	public static final char STATE_ZOMBIE = 'Z';
	
	public static final char STATE_ZOS_SINGLE = '1';
	public static final char STATE_ZOS_MSGQRECEIVEWAIT = 'A';
	public static final char STATE_ZOS_MSGQSENDWAIT = 'B';
	public static final char STATE_ZOS_COMSYSKERNELWAIT = 'C';
	public static final char STATE_ZOS_SEMAPHOREWAIT = 'D';
	public static final char STATE_ZOS_QUIESCEFROZEN = 'E';
	public static final char STATE_ZOS_FILESYSKERNELWAIT = 'F';
	public static final char STATE_ZOS_MVSPAUSEWAIT = 'G';
	public static final char STATE_ZOS_PTHREADCREATEDTASKS = 'H';
	public static final char STATE_ZOS_SWAPPEDOUT = 'I';
	public static final char STATE_ZOS_PTHREADCREATED = 'J';
	public static final char STATE_ZOS_OTHERKERNELWAIT = 'K';
	public static final char STATE_ZOS_CANCELLED = 'L';
	public static final char STATE_ZOS_MULTITHREAD = 'M';
	public static final char STATE_ZOS_MEDIUMWEIGHTTHREAD = 'N';
	public static final char STATE_ZOS_ASYNCHRONOUSTHREAD = 'O';
	public static final char STATE_ZOS_PTRACEKERNELWAIT = 'P';
	public static final char STATE_ZOS_RUNNING = 'R';
	public static final char STATE_ZOS_SLEEPING = 'S';
	public static final char STATE_ZOS_STOPPED = 'T';
	public static final char STATE_ZOS_INITIALPROCESSTHREAD = 'U';
	public static final char STATE_ZOS_DETACHED = 'V';
	public static final char STATE_ZOS_WAITINGFORCHILD = 'W';
	public static final char STATE_ZOS_FORKING = 'X';
	public static final char STATE_ZOS_MVSWAIT = 'Y';
	public static final char STATE_ZOS_ZOMBIE = 'Z';

	public static final int STATE_STARTING_INDEX = 0;
	public static final int STATE_ACTIVE_INDEX = 0;
	public static final int STATE_IDLE_INDEX = 1;
	public static final int STATE_NONEXISTENT_INDEX = 2;
	public static final int STATE_PAGING_INDEX = 3;
	public static final int STATE_RUNNING_INDEX = 4;
	public static final int STATE_SLEEPING_INDEX = 5;
	public static final int STATE_TRACED_INDEX = 6;
	public static final int STATE_WAITING_INDEX = 7;
	public static final int STATE_ZOMBIE_INDEX = 8;
	public static final int STATE_ENDING_INDEX = 9;
	public static final int STATE_ZOS_STARTING_INDEX = 9;
	public static final int STATE_ZOS_SINGLE_INDEX = 9;
	public static final int STATE_ZOS_MSGQRECEIVEWAIT_INDEX = 10;
	public static final int STATE_ZOS_MSGQSENDWAIT_INDEX = 11;
	public static final int STATE_ZOS_COMSYSKERNELWAIT_INDEX = 12;
	public static final int STATE_ZOS_SEMAPHOREWAIT_INDEX = 13;
	public static final int STATE_ZOS_QUIESCEFROZEN_INDEX = 14;
	public static final int STATE_ZOS_FILESYSKERNELWAIT_INDEX = 15;
	public static final int STATE_ZOS_MVSPAUSEWAIT_INDEX = 16;
	public static final int STATE_ZOS_PTHREADCREATEDTASKS_INDEX = 17;
	public static final int STATE_ZOS_SWAPPEDOUT_INDEX = 18;
	public static final int STATE_ZOS_PTHREADCREATED_INDEX = 19;
	public static final int STATE_ZOS_OTHERKERNELWAIT_INDEX = 20;
	public static final int STATE_ZOS_CANCELLED_INDEX = 21;
	public static final int STATE_ZOS_MULTITHREAD_INDEX = 22;
	public static final int STATE_ZOS_MEDIUMWEIGHTTHREAD_INDEX = 23;
	public static final int STATE_ZOS_ASYNCHRONOUSTHREAD_INDEX = 24;
	public static final int STATE_ZOS_PTRACEKERNELWAIT_INDEX = 25;
	public static final int STATE_ZOS_RUNNING_INDEX = 26;
	public static final int STATE_ZOS_SLEEPING_INDEX = 27;
	public static final int STATE_ZOS_STOPPED_INDEX = 28;
	public static final int STATE_ZOS_INITIALPROCESSTHREAD_INDEX = 29;
	public static final int STATE_ZOS_DETACHED_INDEX = 30;
	public static final int STATE_ZOS_WAITINGFORCHILD_INDEX = 31;
	public static final int STATE_ZOS_FORKING_INDEX = 32;
	public static final int STATE_ZOS_MVSWAIT_INDEX = 33;
	public static final int STATE_ZOS_ZOMBIE_INDEX = 34;
	public static final int STATE_ZOS_ENDING_INDEX = 35;
	
	public static final char[] ALL_STATES = 
	{
		STATE_ACTIVE,
		STATE_IDLE,
		STATE_NONEXISTENT,
		STATE_PAGING,
		STATE_RUNNING,
		STATE_SLEEPING,
		STATE_TRACED,
		STATE_WAITING,
		STATE_ZOMBIE,
		STATE_ZOS_SINGLE,
		STATE_ZOS_MSGQRECEIVEWAIT,
		STATE_ZOS_MSGQSENDWAIT,
		STATE_ZOS_COMSYSKERNELWAIT,
		STATE_ZOS_SEMAPHOREWAIT,
		STATE_ZOS_QUIESCEFROZEN,
		STATE_ZOS_FILESYSKERNELWAIT,
		STATE_ZOS_MVSPAUSEWAIT,
		STATE_ZOS_PTHREADCREATEDTASKS,
		STATE_ZOS_SWAPPEDOUT,
		STATE_ZOS_PTHREADCREATED,
		STATE_ZOS_OTHERKERNELWAIT,
		STATE_ZOS_CANCELLED,
		STATE_ZOS_MULTITHREAD,
		STATE_ZOS_MEDIUMWEIGHTTHREAD,
		STATE_ZOS_ASYNCHRONOUSTHREAD,
		STATE_ZOS_PTRACEKERNELWAIT,
		STATE_ZOS_RUNNING,
		STATE_ZOS_SLEEPING,
		STATE_ZOS_STOPPED,
		STATE_ZOS_INITIALPROCESSTHREAD,
		STATE_ZOS_DETACHED,
		STATE_ZOS_WAITINGFORCHILD,
		STATE_ZOS_FORKING,
		STATE_ZOS_MVSWAIT,
		STATE_ZOS_ZOMBIE 
	};
	
	public static final String[] ALL_STATES_STR = 
	{	
		"ASTATE_ACTIVE",
		"ISTATE_IDLE",
		"OSTATE_NONEXISTENT",
		"WSTATE_PAGING",
		"RSTATE_RUNNING",
		"SSTATE_SLEEPING",
		"TSTATE_TRACED",
		"DSTATE_WAITING",
		"ZSTATE_ZOMBIE",
		"1STATE_ZOS_SINGLE",
		"ASTATE_ZOS_MSGQRECEIVEWAIT",
		"BSTATE_ZOS_MSGQSENDWAIT",
		"CSTATE_ZOS_COMSYSKERNELWAIT",
		"DSTATE_ZOS_SEMAPHOREWAIT",
		"ESTATE_ZOS_QUIESCEFROZEN",
		"FSTATE_ZOS_FILESYSKERNELWAIT",
		"GSTATE_ZOS_MVSPAUSEWAIT",
		"HSTATE_ZOS_PTHREADCREATEDTASKS",
		"ISTATE_ZOS_SWAPPEDOUT",
		"JSTATE_ZOS_PTHREADCREATED",
		"KSTATE_ZOS_OTHERKERNELWAIT",
		"LSTATE_ZOS_CANCELLED",
		"MSTATE_ZOS_MULTITHREAD",
		"NSTATE_ZOS_MEDIUMWEIGHTTHREAD",
		"OSTATE_ZOS_ASYNCHRONOUSTHREAD",
		"PSTATE_ZOS_PTRACEKERNELWAIT",
		"RSTATE_ZOS_RUNNING",
		"SSTATE_ZOS_SLEEPING",
		"TSTATE_ZOS_STOPPED",
		"USTATE_ZOS_INITIALPROCESSTHREAD",
		"VSTATE_ZOS_DETACHED",
		"WSTATE_ZOS_WAITINGFORCHILD",
		"XSTATE_ZOS_FORKING",
		"YSTATE_ZOS_MVSWAIT",
		"ZSTATE_ZOS_ZOMBIE" 
	};
	
	public static final String PROCESS_MINER_ERROR_NO_HANDLER = "No handler for this system type";
	public static final String PROCESS_MINER_SUCCESS = "SUCCESS";
	
	public static final String PROCESS_SIGNAL_TYPE_DEFAULT = "default";
}