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

import java.util.zip.ZipEntry;

/**
 * @author mjberger
 *
 * This is a wrapper class for a ZipEntry object that contains some
 * extra helper methods for processing the ZipEntry that are not included
 * in java.util.zip.ZipEntry.
 */
public class SystemUniversalZipEntry
{

	protected ZipEntry _entry;
	protected String _entryFullName;
	protected String _entryFullPath;
	protected String _entryName;
	protected String _extension;
	protected boolean _nested;
	
	public SystemUniversalZipEntry(ZipEntry entry)
	{
		_entry = entry;
		doNameProcessing();
	}

	/**
     * Returns the ZipEntry associated with this SystemUniversalZipEntry
     */ 
    public ZipEntry getEntry()
    {
    	return _entry; 
    }
 
 	/**
 	 * Sets the ZipEntry associated with this SystemUniversalZipEntry
     */
 	public void setEntry(ZipEntry newEntry)
 	{
 		_entry = newEntry;
 		doNameProcessing();
 	}
 	
 	private void doNameProcessing()
 	{
 		if (_entry.isDirectory())
 		{
 			_entryFullName = _entry.getName().substring(0, _entry.getName().length()-1);
 		}
 		else
 		{
			_entryFullName = _entry.getName();
 		}
		int endOfPathPosition = _entryFullName.lastIndexOf("/");
		if (endOfPathPosition != -1) 
		{
			_entryFullPath = _entryFullName.substring(0,endOfPathPosition);
			_entryName = _entryFullName.substring(endOfPathPosition+1);
		}
		else 
		{
			_entryFullPath = "";
			_entryName = _entryFullName;
		}
		int i = _entryFullName.lastIndexOf(".");
		if (i == -1) 
		{
			_extension = "";
		} else _extension = _entryFullName.substring(i + 1);
		if (_entryFullName.indexOf("/") != -1) _nested = true;
	}
	
	/**
	 * Returns the full path to the entry within the ZipFile file structure.
	 * Note: this is NOT the full path to the ZipFile in the regular file
	 * system.
	 * @return a String containing the full path leading to the ZipEntry within
	 * the ZipFile file structure. Does not include the file name.
	 */	
	public String getFullPath()
	{
		return _entryFullPath;
	}
	
	/**
	 * Returns the full name associated with this entry (including path to the
	 * entry) within the ZipFile file structure. Note: this is NOT the full path to the
	 * ZipFile in the regular file system.
	 * @return a String containing the full name including path of the ZipEntry
	 * within the ZipFile file structure. Includes the file name.
	 */
	public String getFullName()
	{
		return _entryFullName;
	}
	
	/**
	 * Returns only the filename associated with this entry in the ZipFile
	 * file structure.
	 * @return a String containing only the file name of this ZipEntry.
	 */
	public String getName()
	{
		return _entryName;
	}
	
	/**
	 * Returns the extension of this entry, if the entry is a file,
	 * null string otherwise.
	 */
	public String getExtension()
	{
		return _extension;
	}
	
	/**
	 * Returns whether or not this entry is nested within folders within the zip file.
	 */
	public boolean isNested()
	{
		return _nested;
	}
	
	/**
	 * @return Whether or not this zipentry is a directory.
	 */
	public boolean isDirectory()
	{
		return _entry.isDirectory();
	}
}