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

package org.eclipse.rse.services.clientserver.archiveutils;

/**
 * @author mjberger
 *
 * Represents an absolute virtual path, which contains a real part
 * (the part of the path that locates the containing archive) and a
 * virtual part (the part that locates the file in the virtual file
 * system within the archive).
 */
public class AbsoluteVirtualPath
{

	protected AbsoluteVirtualPath _realPart;
	protected String _realPartName;
	protected String _virtualPart;
	protected boolean _isVirtual;
	protected String _absVirtualPath;

	/**
	 * Sets up a new AbsoluteVirtualPath object, by parsing <code>absolutePath</code>
	 * into a "real" part (the containing archive) and a "virtual" part (the path to
	 * the entry in the archive). Note that the real part is also an AbsoluteVirtualPath
	 * in order to account for nested archives.
	 */	
	public AbsoluteVirtualPath(String absolutePath) 
	{
		String sep = ArchiveHandlerManager.VIRTUAL_SEPARATOR;
		_absVirtualPath = ArchiveHandlerManager.cleanUpVirtualPath(absolutePath);
		int i = _absVirtualPath.lastIndexOf(sep);
		if (i == -1) 
		{
			// no more nesting, this is the actual container archive.
			_absVirtualPath = absolutePath; // fix for defect 51898 and related defects
			_virtualPart = "";
			_realPartName = _absVirtualPath;
			_realPart = this;
			_isVirtual = false;
		}
		else
		{
			// there could be nesting further below, so parse out the real part
			// by recursively calling the constructor.
			_virtualPart = _absVirtualPath.substring(i+sep.length());
			_realPartName = _absVirtualPath.substring(0,i);
			_realPart = new AbsoluteVirtualPath(_realPartName);
			_isVirtual = true;
		}
	}

	/**
	 * @return Whether or not this AbsoluteVirtualPath is virtual.
	 */
	public boolean isVirtual() 
	{
		return _isVirtual;
	}

	/**
	 * @return The AbsoluteVirtualPath of the archive that contains the object specified
	 * by this AbsoluteVirtualPath. Note that the archive can itself be virtual as well.
	 */
	public AbsoluteVirtualPath getContainingArchivePath() 
	{
		return _realPart;
	}

	/**
	 * @return The name of the AbsoluteVirtualPath of the archive that contains the object
	 * specified by this AbsoluteVirtualPath.
	 */
	public String getContainingArchiveString() 
	{
		return _realPartName;
	}

	/**
	 * @return The virtual path to the virtual object referred to by this AbsoluteVirtualPath.
	 */
	public String getVirtualPart() 
	{
		return _virtualPart;
	}
	
	/**
	 * Returns this AbsoluteVirtualPath as a string.
	 */
	public String toString()
	{
		return _absVirtualPath;
	}
	
	/**
	 * Sets the virtual part of this AbsoluteVirtualPath to be newVirtualPart
	 */
	public void setVirtualPart(String newVirtualPart)
	{
		_virtualPart = newVirtualPart;
		if (newVirtualPart == "")
		{
			int i = _absVirtualPath.lastIndexOf(ArchiveHandlerManager.VIRTUAL_SEPARATOR);
			_absVirtualPath = _absVirtualPath.substring(0, i);
			_isVirtual = _realPart._isVirtual;
		}
		else
		{
			_absVirtualPath = _realPartName + ArchiveHandlerManager.VIRTUAL_SEPARATOR + newVirtualPart;
		}
	}
	
	public String getName()
	{
		return _absVirtualPath.substring(_absVirtualPath.lastIndexOf("/") + 1);
	}
	
	public String getPath()
	{
		String path = _absVirtualPath.substring(0, _absVirtualPath.lastIndexOf("/"));
		return path;
	}

}