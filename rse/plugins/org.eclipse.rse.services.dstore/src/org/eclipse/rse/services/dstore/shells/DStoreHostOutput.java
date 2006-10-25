package org.eclipse.rse.services.dstore.shells;

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
