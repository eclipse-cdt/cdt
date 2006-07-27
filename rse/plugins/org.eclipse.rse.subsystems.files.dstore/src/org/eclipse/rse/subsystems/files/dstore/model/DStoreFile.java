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

package org.eclipse.rse.subsystems.files.dstore.model;

import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.services.dstore.files.DStoreHostFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.AbstractRemoteFile;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileContext;

public class DStoreFile extends AbstractRemoteFile implements IRemoteFile
{
	protected DStoreHostFile _dstoreHostFile;
	public DStoreFile(FileServiceSubSystem ss, IRemoteFileContext context, IRemoteFile parent, DStoreHostFile hostFile)
	{
		super(ss,context, parent, hostFile);
		_dstoreHostFile = hostFile;
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
		return getAbsolutePath();
	}

	public String getClassification()
	{
		return _dstoreHostFile.getClassification();
	}



	


}