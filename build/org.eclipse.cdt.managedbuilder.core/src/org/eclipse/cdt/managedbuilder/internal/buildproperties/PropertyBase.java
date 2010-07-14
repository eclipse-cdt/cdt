/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildproperties;



public abstract class PropertyBase {
	private String fId;
	private String fName;
	
	PropertyBase(String id, String name){
		fId = id;
		fName = name;
	}

	public String getId(){
		return fId;
	}
	
	public String getName(){
		return fName;
	}
	
	@Override
	public String toString(){
		return getId();
	}
	
	@Override
	public boolean equals(Object o){
		if(!o.getClass().equals(getClass()))
			return false;
		
		return fId.equals(((PropertyBase)o).getId());
	}
	
	@Override
	public int hashCode(){
		return fId.hashCode();
	}
}
