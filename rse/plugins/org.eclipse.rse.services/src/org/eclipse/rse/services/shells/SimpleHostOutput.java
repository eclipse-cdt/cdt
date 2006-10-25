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
