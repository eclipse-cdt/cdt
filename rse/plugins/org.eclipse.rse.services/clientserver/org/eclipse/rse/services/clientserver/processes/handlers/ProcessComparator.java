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

import java.util.Comparator;

/**
 * Class for comparing two UniversalServerProcessImpl objects
 * @author mjberger
 *
 */
public class ProcessComparator implements Comparator
{
	/**
	 * Compares two UniversalServerProcessImpl objects by their PID's.
	 */
	public int compare(Object obj1, Object obj2)
	{
		UniversalServerProcessImpl p1 = (UniversalServerProcessImpl) obj1;
		UniversalServerProcessImpl p2 = (UniversalServerProcessImpl) obj2;
		return (int) (p1.getPid() - p2.getPid());
	}
}