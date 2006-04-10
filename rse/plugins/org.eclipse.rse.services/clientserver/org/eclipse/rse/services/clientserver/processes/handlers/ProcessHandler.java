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

package org.eclipse.rse.services.clientserver.processes.handlers;

import java.util.SortedSet;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;


/**
 * Because process mining is system-specific, this interface abstracts out
 * the work from the UniversalProcessMiner, and allows system-specific classes
 * to take care of the work individually.
 */
public interface ProcessHandler
{

	/**
	 * Given a filter string, returns the results of querying all processes that match the filter.
	 * The results MUST BE SORTED NUMERICALLY BY PID.
	 * @return a list of the results of the query. Does not ever return null!
	 * @param rpfs The filter string to which the objects will be matched.
	 */
	public SortedSet lookupProcesses(IHostProcessFilter rpfs) throws Exception;

	/**
	 * Sends a kill signal to a process.
	 * @return the process after the signal is sent, or null if the process no longer exists.
	 * @param process The process to which the signal will be sent
	 * @param type The type of signal to send to the process, "default" for default
	 */
	public IHostProcess kill(IHostProcess process, String type) throws Exception;
	
}