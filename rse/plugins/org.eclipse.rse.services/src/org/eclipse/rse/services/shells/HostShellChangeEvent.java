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

package org.eclipse.rse.services.shells;

public class HostShellChangeEvent implements IHostShellChangeEvent
{
	protected IHostShell             _hostShell;
	protected IHostShellOutputReader _reader;
	protected int 					 _offset;
	protected int                    _range;
	
	public HostShellChangeEvent(IHostShell shell, IHostShellOutputReader reader, int offset, int range)
	{
		_hostShell = shell;
		_reader = reader;
		_offset = offset;
		_range = range;
	}

	public IHostShell getHostShell()
	{
		return _hostShell;
	}

	public IHostShellOutputReader getReader()
	{
		return _reader;
	}
	
	public Object[] getLines()
	{
		Object[] lines = new Object[_range];
		int r = 0;
		int size = _offset + _range ;
		for (int i= _offset; i < size; i++)
		{
			lines[r] = _reader.readLine(i);
			r++;
		}
		return lines;
	}
	
	public Object[] getLineObjects()
	{
		return getLines();
	}
	
	public boolean isError()
	{
		return _reader.isErrorReader();
	}
		
}