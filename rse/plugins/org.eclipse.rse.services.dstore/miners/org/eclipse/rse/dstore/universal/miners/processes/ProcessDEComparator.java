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

package org.eclipse.rse.dstore.universal.miners.processes;

import java.util.Comparator;

import org.eclipse.dstore.core.model.DataElement;

/**
 * Compares two DataElements representing processes by extracting
 * their PID's and comparing numerically based on the PID's.
 * @author mjberger
 *
 */
public class ProcessDEComparator implements Comparator
{
	public ProcessDEComparator()
	{	
	}
	
	public int compare(Object obj1, Object obj2)
	{
		DataElement de1 = (DataElement) obj1;
		DataElement de2 = (DataElement) obj2;
		long pid1 = 0;
		long pid2 = 0;
		try
		{
			pid1 = Long.parseLong(de1.getName());
			pid2 = Long.parseLong(de2.getName());				
		}
		catch (NumberFormatException e)
		{
			return de1.toString().compareTo(de2.toString());
		}
		return (int) (pid1 - pid2);
	}
	
	public boolean equals(Object obj)
	{
		return this.equals(obj);
	}
}