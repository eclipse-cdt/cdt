/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.examples.dstore.subsystems;

import org.eclipse.rse.examples.dstore.services.IHostSampleObject;



public class SampleRootResource extends RemoteSampleObject {

	public SampleRootResource(SampleSubSystem ss) {
		super(null, null, ss);
	}

	public boolean isContainer(){
		return true;
	}
	
	public String getName(){
		return "Root";
	}
	

	
	public String getType(){
		return "Root";
	}
	
	public RemoteSampleObject getParent(){
		return null;
	}
	
	public String getAbsolutePath(){
		return getName();
	}
	
	public IHostSampleObject getHostSampleObject(){
		return _hostObject;
	}
	
	public void setHostSampleObject(IHostSampleObject hostObject){
		_hostObject = hostObject;
	}
}
