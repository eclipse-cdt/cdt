/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * Martin Oberhuber (Wind River) - [234726] Update IRemoteFile Javadocs
 * David McKnight   (IBM)        - [246897] Wrong canonical path for a symbolic link
 * David McKnight   (IBM)        - [308783] Value in Properties view remains "Pending..."
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.dstore;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.internal.services.dstore.files.DStoreFileService;
import org.eclipse.rse.internal.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.SystemBasePlugin;

public class DStoreFile extends AbstractRemoteFile
{

	private DStoreFileService getDStoreFileService()
	{
		FileServiceSubSystem ss = (FileServiceSubSystem)_context.getParentRemoteFileSubSystem();
		return (DStoreFileService)ss.getFileService();
	}

	public IRemoteFile getParentRemoteFile()
	{
		// because this can get called by eclipse from the main thread, and dstore can have problems with main-thread queries,
		// this is overridden to provide a parent without doing the actual query
		if (this._parentFile == null)
    	{
    		if (isRoot())
    		{
    			return null;
    		}

	    	IRemoteFile parentFile = null;

	    	String pathOnly = getParentPath();
	    	if (pathOnly != null)
	    	{
	    	  DStoreFileService fileService = getDStoreFileService();
	     	  IRemoteFileSubSystem ss = _context.getParentRemoteFileSubSystem();
	    	  if (ss != null)
	    	  {

	    	  	try {
		    	  	char sep = getSeparatorChar();
		    	  	if (pathOnly.length() == 0)
		    	  	{
		    	  		return null;
		    	  	}
		    	  	else if (pathOnly.equals(getAbsolutePath()))
		    	  	{
		    	  		return null;
		    	  	}
		    	  	else if (pathOnly.length() == 1)
		    	  	{
		    	  		// parentFile is already null
		    	  		//parentFile = null;

		    	  		IHostFile hostParent = fileService.getHostFile(pathOnly);
		    	  		if (hostParent == null)
		    	  		{
		    	  			DataStore ds = _dstoreHostFile.getDataElement().getDataStore();
		    	  			DataElement element = ds.createObject(null, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, ""); //$NON-NLS-1$
		    	  			element.setAttribute(DE.A_VALUE, pathOnly);

		    	  			hostParent = new DStoreHostFile(element);
		    	  		}
		    	  		parentFile = new DStoreFile((FileServiceSubSystem)ss, _context, (IRemoteFile)null, (DStoreHostFile)hostParent);
		    	  	}
		    	  	else if (!(pathOnly.charAt(pathOnly.length()-1)==sep))
		    	  	{
		    	  		DataStore ds = _dstoreHostFile.getDataElement().getDataStore();

		    	  		IHostFile hostParent = fileService.getHostFile(pathOnly);
		    	  		if (hostParent == null)
		    	  		{
		    	  			int nameSep = pathOnly.lastIndexOf(sep);
		    	  			String parentName = pathOnly;
		    	  			String parentPath = pathOnly;
		    	  			if (nameSep > 0)
		    	  			{
		    	  				parentName = pathOnly.substring(nameSep + 1);
		    	  				parentPath = pathOnly.substring(0, nameSep);
		    	  				if (parentPath.endsWith(":")) //$NON-NLS-1$
		    	  				{
		    	  					parentPath = parentPath + sep;
		    	  				}
		    	  			}
		    	  			else
		    	  			{
		    	  				parentName = pathOnly.substring(nameSep + 1);
		    	  				parentPath = "" + sep; //$NON-NLS-1$
		    	  			}

		    	  			DataElement element = ds.createObject(null, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, parentName);
		    	  			element.setAttribute(DE.A_VALUE, parentPath);

		    	  			hostParent = new DStoreHostFile(element);
		    	  		}
		    	  		parentFile = new DStoreFile((FileServiceSubSystem)ss, _context, (IRemoteFile)null, (DStoreHostFile)hostParent);
		              //parentFile = ss.getRemoteFileObject(pathOnly+sep);
		    	  	}
		            else
		            {
		            	DataStore ds = _dstoreHostFile.getDataElement().getDataStore();


		            	IHostFile hostParent = fileService.getHostFile(pathOnly);
		    	  		if (hostParent == null)
		    	  		{
		    	  			int nameSep = pathOnly.lastIndexOf(sep);
		    	  			String parentName = pathOnly;
		    	  			String parentPath = pathOnly;
		    	  			if (nameSep > 0)
		    	  			{
		    	  				parentName = pathOnly.substring(nameSep + 1);
		    	  				parentPath = pathOnly.substring(0, nameSep);
		    	  				if (parentPath.endsWith(":")) //$NON-NLS-1$
		    	  				{
		    	  					parentPath = parentPath + sep;
		    	  				}
		    	  			}
		    	  			else
		    	  			{
		    	  				parentName = pathOnly.substring(nameSep + 1);
		    	  				parentPath = "" + sep; //$NON-NLS-1$
		    	  			}

		    	  			DataElement element = ds.createObject(null, IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR, parentName);
		    	  			element.setAttribute(DE.A_VALUE, parentPath);
		    	  			hostParent = new DStoreHostFile(element);
		    	  		}
		            	parentFile = new DStoreFile((FileServiceSubSystem)ss, _context, (IRemoteFile)null, (DStoreHostFile)hostParent);

		              //parentFile = ss.getRemoteFileObject(pathOnly);
		            }
	    	  	} catch (Exception e) {
	    	  		SystemBasePlugin.logError("RemoteFileImpl.getParentRemoteFile()", e); //$NON-NLS-1$
	    	  	}
	    	  }
	    	}
	    	else
	    	{
	    	}
	    	this._parentFile = parentFile;
    	}
    	return this._parentFile;
	}

	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	protected DStoreHostFile _dstoreHostFile;
	public DStoreFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, DStoreHostFile hostFile)
	{
		super(ss,context, parent, hostFile);
		_dstoreHostFile = hostFile;
	}
	
	

	public void setHostFile(IHostFile hostFile) {
		super.setHostFile(hostFile);
		_dstoreHostFile = (DStoreHostFile)hostFile;
	}

	public boolean isVirtual()
	{
		DataElement element = _dstoreHostFile.getDataElement();
		String type = element.getType();
		if (
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR) ||
				type.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR) )
		{
			return true;
		}
		return false;
	}

	public String getCanonicalPath()
	{
		String classification = getClassification();
		if (classification != null && classification.startsWith("symbolic link")){ //$NON-NLS-1$
			// special path - use path in classification
			int colonIndex = classification.indexOf(":");
			String canonicalPath = classification.substring(colonIndex + 1);
			return canonicalPath;
		}
		return getAbsolutePath();			
	}

	public String getClassification()
	{
		return _dstoreHostFile.getClassification();
	}

}
