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

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;

public class PerTypeMapStorage<K extends IRealBuildObjectAssociation, V> implements Cloneable {
	private ObjectTypeBasedStorage<Map<K, V>> fStorage = new ObjectTypeBasedStorage<Map<K, V>>();

	public Map<K, V> getMap(int type, boolean create){
		Map<K, V> map = fStorage.get(type);
		if(map == null && create){
			map = createMap(null);
			fStorage.set(type, map);
		}
		return map;
	}
	
	protected Map<K, V> createMap(Map<K, V> map){
		if(map == null) {
			return new HashMap<K, V>();
		}
		@SuppressWarnings("unchecked")
		Map<K, V> clone = (Map<K, V>)((HashMap<K, V>)map).clone();
		return clone;
	}

	@Override
	public Object clone(){
		try {
			@SuppressWarnings("unchecked")
			PerTypeMapStorage<K, V> clone = (PerTypeMapStorage<K, V>)super.clone();
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				Map<K, V> o = clone.fStorage.get(types[i]);
				if(o != null){
					clone.fStorage.set(types[i], clone.createMap(o));
				}
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
