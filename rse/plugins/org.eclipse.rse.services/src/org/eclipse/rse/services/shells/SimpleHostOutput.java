/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.services.shells;

public class SimpleHostOutput implements IHostOutput
{
	private String _line;
	public SimpleHostOutput(String line)
	{
		_line = line;
	}
	
	public String getString()
	{
		return _line;
	}
	
	public String toString()
	{
		return _line;
	}
}
