/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 * Xuan Chen        (IBM)   - [189041] incorrect file name after rename a file inside a zip file - DStore Windows
 * Xuan Chen        (IBM)   - [187548] Editor shows incorrect file name after renaming file on Linux dstore
 * Kevin Doyle 		(IBM) 	- [191548] Various NPE fixes 
 * David McKnight   (IBM)   - [235471] DStoreHostFile.getParentPath() breaks API contract for Root files
 *******************************************************************************/

package org.eclipse.rse.internal.services.dstore.files;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;

public class DStoreHostFile implements IHostFile, IHostFilePermissionsContainer
{
	public static final int ATTRIBUTE_MODIFIED_DATE=1;
	public static final int ATTRIBUTE_SIZE = 2;
	public static final int ATTRIBUTE_CLASSIFICATION =11;
	public static final int ATTRIBUTE_IS_HIDDEN=3;
	public static final int ATTRIBUTE_CAN_WRITE=4;
	public static final int ATTRIBUTE_CAN_READ=5;

	
	protected DataElement _element;
	protected boolean _isArchive;
	protected String _absolutePath;
	protected IHostFilePermissions _permissions;
	
	public DStoreHostFile(DataElement element)
	{
		_element = element;
		init();
		_isArchive = internalIsArchive();	
	}
	
	public DataElement getDataElement()
	{
		return _element;
	}
	
	public static String getNameFromPath(String path)
	{
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) // account for windows
		{
			lastSlash = path.lastIndexOf('\\');
		}
		if (lastSlash > 0 && lastSlash != path.length() - 1)
		{
			return path.substring(lastSlash + 1);
		}
		return path;
	}
	
	public static String getParentPathFromPath(String path)
	{
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) // acount for windows
		{
			lastSlash = path.lastIndexOf('\\');
		}
		if (lastSlash > 0 && lastSlash != path.length() - 1)
		{
			return path.substring(0, lastSlash);
		}
		return path;
	}
	
	public String getName()
	{
		if (_element.getName() == null) {
			// file was deleted on the host
			return null;
		}
		String type = _element.getType();
		if (type != null && type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			// filter doesn't separate path from name
			String path = _element.getName();
			return getNameFromPath(path);
		}
		else if (isRoot())
		{
			return _element.getValue();
		}
		else
		{
			String name = _element.getName();
			String parentPath = getParentPath();
			if (name.length() == 0 && 
					(parentPath.equals("/")  || parentPath.endsWith(":\\"))) //$NON-NLS-1$ //$NON-NLS-2$
			{
				
				return parentPath;
			}
			if (name.length() == 0)
			{
				String path = _element.getValue();
				int lastSep = path.lastIndexOf('/');
				if (lastSep == -1)
					lastSep = path.lastIndexOf('\\');
				name = path.substring(lastSep + 1);
				return name;
			}
			
			return name;
		}
	}

	public String getParentPath()
	{
		if (_element.getName() == null) {
			// file was deleted on the host
			return null;
		}
		String type = _element.getType();
		if (type != null && type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			// filter doesn't separate path from name
			String path = _element.getName();
			return getParentPathFromPath(path);
		}
		else
		{
			if (isRoot()){ // IFileService.getParentPath() contract states a root must return null
				return null;
			}
			
			if (_element.getName().length() == 0)
			{
				// derive from value
				String fullPath = _element.getValue();
				int sep = fullPath.lastIndexOf('/');
				if (sep == -1)
					sep = fullPath.lastIndexOf('\\');
				
				if (sep == -1)
					return fullPath;
				return fullPath.substring(0, sep);
			}
			else
			{	
				return _element.getValue();
			}
		}
	}

	public boolean isHidden()
	{
		String name = getName();
		if (name == null || name.length() == 0)
		{
			return false;
		}
		else
		{
			
			if  (name.charAt(0) == '.')
			{
				return true;
			}
			else if (isRoot())
			{
				return false;
			}
			else
			{
				String str = getAttribute(_element.getSource(), ATTRIBUTE_IS_HIDDEN);
				return "true".equals(str); //$NON-NLS-1$
			}
		}			
	}
	
	public boolean isDirectory()
	{
		String type = _element.getType();
		if (type != null && (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)))
		{
			return true;
		}
		return false;
		
	}

	public boolean isRoot()
	{
		String parentPath = _element.getValue();
		String name = _element.getName();
		if (name == null) {
			// file was deleted on the host
			return false;
		}
		if (parentPath == null || 
				parentPath.length() == 0 || 
				(name.length() == 0 && (parentPath.equals("/") || parentPath.endsWith(":\\")) || //$NON-NLS-1$ //$NON-NLS-2$
				(name.equals(parentPath) && parentPath.endsWith(":\\"))) //$NON-NLS-1$ 
				)
						
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isFile()
	{
		String type = _element.getType();
		if (type != null && (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR)))
		{
			return true;
		}
		return false;
	}
	
	public boolean exists()
	{
		if (_element.isDeleted())
			return false;
		String type = _element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public String getAbsolutePath()
	{
		return _absolutePath;
	}
	
	private void init()
	{			
		// set the absolute path			
		String name = _element.getName();
		String value = _element.getValue();
		if (name == null)
		{
			// this element is deleted
			_absolutePath = ""; //$NON-NLS-1$
		}
		else if (name.length() == 0)
		{
			_absolutePath = value;
		} 
		else
		{
			String parentPath = getParentPath();	
			String type = _element.getType();
			if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
			{
				_absolutePath = name;
			}
			else if (name.length() == 0)
			{
				_absolutePath = PathUtility.normalizeUnknown(parentPath);
			}
			else if (name == value)
			{
				_absolutePath = name;
			}
			else
			{
				_absolutePath = PathUtility.normalizeUnknown(parentPath + "/" + name); //$NON-NLS-1$
			}
		}
	}

	public long getSize()
	{
		return getFileLength(_element.getSource());
	}

	public long getModifiedDate()
	{
		return getModifiedDate(_element.getSource());

	}
	
	public String getClassification()
	{
		String classification = getClassification(_element.getSource());
		if (classification == null)
		{
			if (isFile())
			{
				classification = "file"; //$NON-NLS-1$
			}
			else
			{
				classification = "directory"; //$NON-NLS-1$
			}
		}
		return classification;
	}

	protected static String getClassification(String attributes) 
	{		
		return getAttribute(attributes, ATTRIBUTE_CLASSIFICATION);
	}
	
	protected static long getFileLength(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_SIZE);
		if (str != null && str.length() > 0)
		{
			return Long.parseLong(str);
		}
		return 0;
	}
	
	protected static long getModifiedDate(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_MODIFIED_DATE);
		if (str != null && str.length() > 0)
		{
			return Long.parseLong(str);
		}
		return 0;
	}
	
	protected static  String getAttribute(String attributes, int index)
	{
		if (attributes != null)
		{
			String[] str = attributes.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
			if (str.length > index){
				return str[index];
			}
		}
		return null;
	}

	public void renameTo(String newAbsolutePath) 
	{
		String current = getName();
		String backupNewAbsolutePath = newAbsolutePath;
		newAbsolutePath = ArchiveHandlerManager.cleanUpVirtualPath(newAbsolutePath);
		int lastSep = newAbsolutePath.lastIndexOf('/');
		if (lastSep == -1)
			lastSep = newAbsolutePath.lastIndexOf('\\');
				
		String newName = null;
		if (lastSep != -1)
		{
			newName = newAbsolutePath.substring(lastSep + 1);
		}
		else
		{
			newName = newAbsolutePath;
		}

		if (newName.equals(current))
		{
			// data element already updated
		}
		else
		{				

			
			_element.setAttribute(DE.A_NAME, newName);
		}
		_absolutePath = backupNewAbsolutePath;
		
		_isArchive = internalIsArchive();
	}
	
	protected boolean internalIsArchive()
	{
		String path = getAbsolutePath();
		return ArchiveHandlerManager.getInstance().isArchive(new File(path)) 
		&& !ArchiveHandlerManager.isVirtual(path);
	}

	public boolean isArchive() 
	{
		return _isArchive;
	}

	public boolean canRead() {
		String str = getAttribute(_element.getSource(), ATTRIBUTE_CAN_READ);
		return "true".equals(str); //$NON-NLS-1$
	}

	public boolean canWrite() {
		String str = getAttribute(_element.getSource(), ATTRIBUTE_CAN_WRITE);
		return "true".equals(str); //$NON-NLS-1$
	}
	
	public void setPermissions(IHostFilePermissions permissions){
		_permissions = permissions;
	}

	public IHostFilePermissions getPermissions() {
		return _permissions;
	}
	
}
