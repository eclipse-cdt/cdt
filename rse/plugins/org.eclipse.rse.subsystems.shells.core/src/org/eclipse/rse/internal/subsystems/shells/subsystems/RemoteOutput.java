/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.internal.subsystems.shells.subsystems;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;


/**
 * The RemoteOuputImpl class is an implementation of IRemoteOutput.
 * It is used for storing information about a particular line of output
 */
public class RemoteOutput implements IAdaptable, IRemoteOutput
{

	protected String _type;
	protected String _text;
	protected String _path;
	protected int _line = 0;
	protected int _startOffset = -1;
	protected int _endOffset = -1;

	private Object _parent;

	/**
	 * Constructor
	 * @param parent container of the output
	 */
	public RemoteOutput(Object parent, String type)
	{
		_parent = parent;
		_type = type;
	}

	/**
	 * Sets the type of remote output
	 * @param type
	 */
	public void setType(String type)
	{
		_type = type;
	}

	/**
	 * Sets the displayable text for the remote output
	 * @param text
	 */
	public void setText(String text)
	{
		_text = text;
	}

	/**
	 * Sets the absolute path for an associated file if applicable
	 * @param path
	 */
	public void setAbsolutePath(String path)
	{
		_path = path;
	}

	/**
	 * Sets the associated line number for a particular source if applicable
	 * @param line
	 */
	public void setLine(int line)
	{
		_line = line;
	}

	/**
	 * Gets the type of this output
	 */
	public String getType()
	{
		return _type;
	}

	/**
	 * Gets the displayable text for this output
	 */
	public String getText()
	{
		return _text;
	}

	/**
	 * Gets the index of this object within a command object
	 */
	public int getIndex()
	{
		if (_parent instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmd = (IRemoteCommandShell) _parent;
			return cmd.getIndexOf(this);
		}
		return -1;
	}

	/**
	 * Gets the absolute path of an associated file if applicable
	 */
	public String getAbsolutePath()
	{
		return _path;
	}

	/**
	 * Gets the associated line within a file if applicable
	 */
	public int getLine()
	{
		return _line;
	}

	/**
	 * Gets the containing object for this output
	 */
	public Object getParent()
	{
		return _parent;
	}

	/**
	 * Gets the associated adapter for this output
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Returns children of the output, if applicable
	 * @return null for this implementation
	 */
	public Object[] getChildren()
	{
		return null;
	}

	/**
	 * Indicates whether this output has children
	 * @return false since there are no children
	 */
	public boolean hasChildren()
	{
		return false;
	}

	/**
	 * Gets the displayable label for this output
	 * @return the label
	 */
	public String getLabel()
	{
		return getText();
	}

	/**
	 * set the start offset for the line
	 * @param offset
	 */
	public void setCharStart(int offset)
	{
		_startOffset = offset;
	}

	/**
	 * set the end offset for the line
	 * @param offset
	 */
	public void setCharEnd(int offset)
	{
		_endOffset = offset;
	}

	/**
	 * Gets the associated char start offset for the line
	 */
	public int getCharStart()
	{
		return _startOffset;
	}

	/**
	 * Gets the associated char end offset for the line
	 */
	public int getCharEnd()
	{
		return _endOffset;
	}

}