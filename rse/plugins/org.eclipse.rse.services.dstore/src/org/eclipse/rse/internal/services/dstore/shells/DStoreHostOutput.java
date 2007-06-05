/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed the initial implementation:
 * David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore.shells;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.services.shells.IHostOutput;

public class DStoreHostOutput implements IHostOutput 
{

	private DataElement _element;
	
	public DStoreHostOutput(DataElement element)
	{
		_element = element;
	}
	
	public String getString() 
	{
		return _element.getName();
	}
	
	public DataElement getElement()
	{
		return _element;
	}

	public String toString()
	{
		return getString();
	}
}
