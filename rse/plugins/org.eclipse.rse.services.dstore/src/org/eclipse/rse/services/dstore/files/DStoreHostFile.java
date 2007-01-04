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

package org.eclipse.rse.services.dstore.files;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.IHostFile;

public class DStoreHostFile implements IHostFile
{
	public static final int ATTRIBUTE_MODIFIED_DATE=1;
	public static final int ATTRIBUTE_SIZE = 2;
	public static final int ATTRIBUTE_CLASSIFICATION =11;
	public static final int ATTRIBUTE_IS_HIDDEN=3;
	public static final int ATTRIBUTE_CAN_WRITE=4;
	public static final int ATTRIBUTE_CAN_READ=5;

	
	protected DataElement _element;
	protected boolean _isArchive;
	
	public DStoreHostFile(DataElement element)
	{
		_element = element;
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
		String type = _element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
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
		String type = _element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			// filter doesn't separate path from name
			String path = _element.getName();
			return getParentPathFromPath(path);
		}
		else
		{
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
				return(str.equals("true")); //$NON-NLS-1$
			}
		}			
	}
	
	public boolean isDirectory()
	{
		String type = _element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR))
		{
			return true;
		}
		return false;
		
	}

	public boolean isRoot()
	{
		String parentPath = _element.getValue();
		String name = _element.getName();
		if (parentPath == null || parentPath.length() == 0 || 
				(name.length() == 0 && (parentPath.equals("/") || parentPath.endsWith(":\\"))) //$NON-NLS-1$ //$NON-NLS-2$
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
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
				|| type.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR))
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
		String name = _element.getName();
		if (name == null)
		{
			// this element is deleted
			return ""; //$NON-NLS-1$
		}
		if (name.length() == 0)
		{
			return _element.getValue();
		} 
		String parentPath = getParentPath();

		
		String type = _element.getType();
		if (type.equals(IUniversalDataStoreConstants.UNIVERSAL_FILTER_DESCRIPTOR))
		{
			return name;
		}
		
		if (name.length() == 0)
		{
			return PathUtility.normalizeUnknown(parentPath);
		}
		else
		{
			return PathUtility.normalizeUnknown(parentPath + "/" + name); //$NON-NLS-1$
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
		return Long.parseLong(str);
	}
	
	protected static long getModifiedDate(String attributes) 
	{
		String str = getAttribute(attributes, ATTRIBUTE_MODIFIED_DATE);
		return Long.parseLong(str);
	}
	
	protected static String getAttribute(String attributes, int index)
	{
		String[] str = attributes.split("\\"+IServiceConstants.TOKEN_SEPARATOR); //$NON-NLS-1$
		if (str.length > index)
		{
			return str[index];
		}
		else
		{
			return null;
		}
	}

	public void renameTo(String newAbsolutePath) 
	{
		String current = getName();
		if (newAbsolutePath.endsWith(current))
		{
			// data element already updated
		}
		else
		{				
			int lastSep = newAbsolutePath.lastIndexOf('/');
			if (lastSep == -1)
				lastSep = newAbsolutePath.lastIndexOf('\\');
			
			String newName = newAbsolutePath.substring(lastSep + 1);
			
			_element.setAttribute(DE.A_NAME, newName);
		}
		
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
		return(str.equals("true")); //$NON-NLS-1$
	}

	public boolean canWrite() {
		String str = getAttribute(_element.getSource(), ATTRIBUTE_CAN_WRITE);
		return(str.equals("true")); //$NON-NLS-1$
	}
	
}