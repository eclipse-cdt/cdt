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

package org.eclipse.rse.subsystems.files.core.subsystems;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;


public class RemoteFileSchedulingRule implements ISchedulingRule
{
	private IRemoteFile _file;
	
	public RemoteFileSchedulingRule(IRemoteFile file)
	{
		_file = file;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see ISchedulingRule#contains(ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule) {
		if (this == rule)
			return true;
		if (rule instanceof MultiRule) {
			MultiRule multi = (MultiRule) rule;
			ISchedulingRule[] children = multi.getChildren();
			for (int i = 0; i < children.length; i++)
				if (!contains(children[i]))
					return false;
			return true;
		}
		if (!(rule instanceof RemoteFileSchedulingRule))
			return false;
		return getHostName().equals(((RemoteFileSchedulingRule) rule).getHostName()) && 
		       ((RemoteFileSchedulingRule) rule).getAbsolutePath().startsWith(getAbsolutePath());
	}

	/* (non-Javadoc)
	 * @see ISchedulingRule#isConflicting(ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule) 
	{	
		if (!(rule instanceof RemoteFileSchedulingRule))
			return false;
		String otherPath = ((RemoteFileSchedulingRule) rule).getAbsolutePath();
		String path = this.getAbsolutePath();
		String otherHost = ((RemoteFileSchedulingRule) rule).getHostName();
		return getHostName().equals(otherHost) && path.startsWith(otherPath) || otherPath.startsWith(path);
	}
	
	public String getAbsolutePath()
	{
		return _file.getAbsolutePath();
	}
	
	public String getHostName()
	{
		return _file.getSystemConnection().getHostName();
	}
}