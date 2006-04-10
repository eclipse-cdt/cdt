/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.messages;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;



public class SystemUIMessage extends SystemMessage
{
	protected static final int displayMask = IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR; // for IStatus substitution variables		

	public SystemUIMessage(String comp, String sub, String number, char ind, String l1, String l2) throws IndicatorException 
	{
		super(comp,sub,number,ind,l1,l2);
	}

/**
	 * used to determine the string value of the object 
	 * it calls toString for all object types except for Exceptions
	 * where the stack is also rendered
	 * @param sub  the substitution object
	 * @return the string value for the object
	 */
	public String getSubValue(Object sub) 
	{
	    if (sub == null)
	      return "";
	      
		if (sub instanceof IStatus)
	    {
	    	return populateList("", (IStatus)sub);
	    }
	    else
	    {
	    	return super.getSubValue(sub);
	    }
	}

/**
	 * Populates the list using this error dialog's status object.
	 * This walks the child stati of the status object and
	 * displays them in a list. The format for each entry is
	 *		status_path : status_message
	 * If the status's path was null then it (and the colon)
	 * are omitted.
	 */
	private static String populateList(String list, IStatus status) {
		java.util.List statusList = Arrays.asList(status.getChildren());		
		Iterator enumer = statusList.iterator();
		while (enumer.hasNext()) {
			IStatus childStatus = (IStatus) enumer.next();
			list = populateList(list, childStatus, 0);
		}
		return list;
	}
	private static String populateList(String list, IStatus status, int nesting) {
		if (!status.matches(displayMask)) {
			return list;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nesting; i++) {
			sb.append(NESTING_INDENT); 
		}
		sb.append(status.getMessage());
		//list.add(sb.toString());
		list = list + sb.toString() + "\n";
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			list = populateList(list, children[i], nesting + 1);
		}
		return list;
	}

}