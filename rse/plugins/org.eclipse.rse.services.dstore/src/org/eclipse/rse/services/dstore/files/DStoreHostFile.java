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
		if (lastSlash > 0 && lastSlash != path.length() - 1)
		{
			return path.substring(lastSlash);
		}
		return path;
	}
	
	public static String getParentPathFromPath(String path)
	{
		int lastSlash = path.lastIndexOf('/');
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
		else
		{
			String name = _element.getName();
			String parentPath = getParentPath();
			if (name.length() == 0 && 
					(parentPath.equals("/")  || parentPath.endsWith(":\\")))
			{
				return getParentPath();
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
			return _element.getValue();
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
			
			return name.charAt(0) == '.';
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
		String parentPath = getParentPath();
		String name = _element.getName();
		if (parentPath == null || parentPath.length() == 0 || 
				(name.length() == 0 && (parentPath.equals("/") || parentPath.endsWith(":\\")))
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
		String parentPath = getParentPath();
		String name = _element.getName();
		
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
			return PathUtility.normalizeUnknown(parentPath + "/" + name);
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
				classification = "file";
			}
			else
			{
				classification = "directory";
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
		String[] str = attributes.split("\\"+IServiceConstants.TOKEN_SEPARATOR);
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
		_element.setAttribute(DE.A_NAME, newAbsolutePath);
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
}