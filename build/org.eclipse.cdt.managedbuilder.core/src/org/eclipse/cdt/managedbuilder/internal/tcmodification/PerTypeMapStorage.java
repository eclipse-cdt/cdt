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
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.HashMap;
import java.util.Map;

public class PerTypeMapStorage implements Cloneable {
	private ObjectTypeBasedStorage fStorage = new ObjectTypeBasedStorage();
	
	public Map getMap(int type, boolean create){
		Map map = (Map)fStorage.get(type);
		if(map == null && create){
			map = createMap(null);
			fStorage.set(type, map);
		}
		return map;
	}
	
	protected Map createMap(Map map){
		if(map == null)
			return new HashMap();
		return (Map)((HashMap)map).clone();
	}

	@Override
	public Object clone(){
		try {
			PerTypeMapStorage clone = (PerTypeMapStorage)super.clone();
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				Object o = clone.fStorage.get(types[i]);
				if(o != null){
					clone.fStorage.set(types[i], clone.createMap((Map)o));
				}
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
