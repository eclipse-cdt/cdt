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

package org.eclipse.rse.eclipse.filesystem;



import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;


public class FileStoreConversionUtility 
{
	public static IFileStore convert(IFileStore parent, IRemoteFile remoteFile)
	{
		return new RSEFileStoreRemoteFileWrapper(parent, remoteFile);
	}
	
	public static IFileStore[] convert(IFileStore parent, IRemoteFile[] remoteFiles)
	{
		IFileStore[] converted = new IFileStore[remoteFiles.length];
		for (int i = 0; i < remoteFiles.length; i++)
		{
			converted[i] = convert(parent, remoteFiles[i]);
		}
		return converted;
	}
}