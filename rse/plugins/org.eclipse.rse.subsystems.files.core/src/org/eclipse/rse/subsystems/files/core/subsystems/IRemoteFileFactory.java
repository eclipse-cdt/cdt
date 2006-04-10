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

package org.eclipse.rse.subsystems.files.core.subsystems;

/**
 * This interface represents a handle to a remote file object,
 * which is either a universalFileSystem or an ifsFileSystem. 
 */ 

public interface IRemoteFileFactory 
{
	
   /**
    * Get the RemoteFile object
    */
   	public IRemoteFile createRemoteFile(IRemoteFileContext context, Object obj, boolean isRoot);	

   /**
    * Get the RemoteFile object when we know if it is a exists or not
    */
   	public IRemoteFile createRemoteFile(IRemoteFileContext context, Object obj, boolean isRoot, boolean exists);	

   /**
    * Get the RemoteFile object when we know if it is a exists or not and whether it is a directory or not
    */
   	public IRemoteFile createRemoteFile(IRemoteFileContext context, Object obj, boolean isRoot, boolean exists, boolean isDir);	

}