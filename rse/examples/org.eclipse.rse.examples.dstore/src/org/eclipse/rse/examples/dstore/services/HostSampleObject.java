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
package org.eclipse.rse.examples.dstore.services;

import org.eclipse.dstore.core.model.DataElement;

public class HostSampleObject implements IHostSampleObject {

	private DataElement _element;
	public HostSampleObject(DataElement element){
		_element = element;
	}

	public DataElement getDataElement() {
		return _element;
	}
	
	public String getName(){
		return _element.getName();
	}
	
	public String getType(){
		return _element.getType();
	}
}
