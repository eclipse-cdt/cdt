/*******************************************************************************
 * Copyright (c) 2008, 2009 Takuya Miyamoto and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Takuya Miyamoto - initial API and implementation
 * David McKnight   (IBM)        - [272708] [import/export] fix various bugs with the synchronization support
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.rse.internal.synchronize.filesystem.subscriber.FileSystemResourceVariant;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.ThreeWayResourceComparator;
import org.eclipse.team.core.variants.ThreeWaySynchronizer;
import org.eclipse.team.internal.core.mapping.LocalResourceVariant;

public class RSEResourceVariantComparator extends ThreeWayResourceComparator {
	private ThreeWaySynchronizer _synchronizer;
	public RSEResourceVariantComparator(ThreeWaySynchronizer synchronizer){
		super(synchronizer);
		_synchronizer = synchronizer;
	}

	
	
	public boolean compare(IResource local, IResourceVariant remote) {
	//	return super.compare(local, remote) && equalSize(local, remote);
		  if(local instanceof IContainer) {      
			  if(remote.isContainer()) {        
				  return true;      
				  }      
			  return false;    
			  }    
		  if(local instanceof IFile && remote instanceof FileSystemResourceVariant) {        
			  FileSystemResourceVariant myE2 = (FileSystemResourceVariant)remote;  
			  myE2.synchRemoteFile(); // make sure we've got the latest remote file
			  
			  SystemIFileProperties properties = new SystemIFileProperties(local);
			  
			  long remoteTimeStamp = myE2.lastModified();		
			  			  
			  if (remoteTimeStamp == 0){
				  // file no longer exists
				properties.setRemoteFileTimeStamp(0);
				properties.setDownloadFileTimeStamp(0);
				return false;
			  }
			  
			  long storedTimeStamp = properties.getRemoteFileTimeStamp();			  
			  long storedLocalTimeStamp = properties.getDownloadFileTimeStamp();
			  long localTimeStamp = local.getLocalTimeStamp();
			  
			  if (storedTimeStamp == 0){
				  // never been stored before
				  // assuming up-to-date file and now marking this timestamp
				properties.setRemoteFileTimeStamp(remoteTimeStamp);
				properties.setDownloadFileTimeStamp(local.getLocalTimeStamp());
				
				storedTimeStamp = remoteTimeStamp;
				storedLocalTimeStamp = localTimeStamp;
			  }
			  			  			 			  
			  boolean result = storedTimeStamp == remoteTimeStamp && storedLocalTimeStamp == localTimeStamp;    			  
			  return result;
		  }   
		  else if (local instanceof IFile && remote instanceof LocalResourceVariant){
			  	return true; // local resource variant is for local
		  }
		  return false;  	
	}
	

	/*
	public boolean compare(IResource local, IResourceVariant remote) {
		// First, ensure the resources are the same gender
		if ((local.getType() == IResource.FILE) == remote.isContainer()) {
			return false;
		}
		try {
			// If the file is locally modified, it cannot be in sync
			if (local.getType() == IResource.FILE && _synchronizer.isLocallyModified(local)) {
		//		return false;
			}
			
			// If there is no base, the local cannot match the remote
			if (_synchronizer.getBaseBytes(local) == null) return false;
			
			// Otherwise, assume they are the same if the remote equals the base
			return equals(_synchronizer.getBaseBytes(local), getBytes(remote));
		} catch (TeamException e) {
			TeamPlugin.log(e);
			return false;
		}
	}
	*/
	
	public boolean compare(IResourceVariant e1, IResourceVariant e2) {    
		  if(e1.isContainer()) {      
			  if(e2.isContainer()) {        
				  return true;      
				  }      
			  return false;    
			  }    
		  if(e1 instanceof FileSystemResourceVariant && e2 instanceof FileSystemResourceVariant) {      
			  FileSystemResourceVariant myE1 = (FileSystemResourceVariant)e1;       
			  FileSystemResourceVariant myE2 = (FileSystemResourceVariant)e2;       
			  return myE1.lastModified() == myE2.lastModified();    			  
		  }    
		  return false;  
	}
	
	private byte[] getBytes(IResourceVariant remote) {
		return remote.asBytes();
	}
	
	private boolean equals(byte[] syncBytes, byte[] oldBytes) {
		if (syncBytes == null || oldBytes == null){
			return false;
		}
		
		if (syncBytes.length != oldBytes.length) return false;
		for (int i = 0; i < oldBytes.length; i++) {
			if (oldBytes[i] != syncBytes[i]) return false;
		}
		return true;
	}
	

	/**
	 * Return if the size of local and remote file are the same.
	 * @param local
	 * @param remote
	 * @return
	 */
	private boolean equalSize(IResource local, IResourceVariant remote){
		long localSize = local.getLocation().toFile().length();
		long remoteSize = 0;
		if(remote instanceof FileSystemResourceVariant){
			remoteSize = ((FileSystemResourceVariant)remote).getFile().length();
		}
		return localSize == remoteSize;
	}
}
