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

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.examples.dstore.services.IHostSampleContainer;
import org.eclipse.rse.examples.dstore.services.IHostSampleObject;

public class RemoteSampleObject extends AbstractResource {
	protected IHostSampleObject _hostObject;
	private RemoteSampleObject[] _contents;
	private RemoteSampleObject _parent;
	
	public RemoteSampleObject(RemoteSampleObject parent, IHostSampleObject hostObject, SampleSubSystem ss){
		_hostObject = hostObject;
		_parent = parent;
		setSubSystem(ss);
	}
	
	public IHostSampleObject getHostSampleObject(){
		return _hostObject;
	}
	
	public boolean isContainer(){
		if (_hostObject instanceof IHostSampleContainer){
			return true;
		}
		return false;
	}
	
	public String getName(){
		return _hostObject.getName();
	}
	
	public String getType(){
		return _hostObject.getType();
	}
	
	public RemoteSampleObject getParent(){
		return _parent;
	}
	
	public String getAbsolutePath(){
		if (_parent != null){
			return _parent.getAbsolutePath() + "." + getName();
		}
		else {
			return getName();
		}
	}
	
	public void setContents(RemoteSampleObject[] contents){
		_contents = contents;
	}
	
	public RemoteSampleObject[] getContents(){
		return _contents;
	}
}
