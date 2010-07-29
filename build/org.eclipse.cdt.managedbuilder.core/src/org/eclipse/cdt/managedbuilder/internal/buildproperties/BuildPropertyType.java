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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;


public class BuildPropertyType extends PropertyBase implements IBuildPropertyType{
	private Map<String, BuildPropertyValue> fValuesMap = new HashMap<String, BuildPropertyValue>();
	
	BuildPropertyType(String id, String name){
		super(id, name);
	}

	void addSupportedValue(BuildPropertyValue value){
		fValuesMap.put(value.getId(), value);
	}
	
	public IBuildPropertyValue[] getSupportedValues(){
		return fValuesMap.values().toArray(new BuildPropertyValue[fValuesMap.size()]);
	}
	
	public IBuildPropertyValue getSupportedValue(String id){
		return fValuesMap.get(id);
	}
}
