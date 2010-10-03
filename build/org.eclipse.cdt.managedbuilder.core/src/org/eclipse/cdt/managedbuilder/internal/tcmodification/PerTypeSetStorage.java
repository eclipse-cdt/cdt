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

import java.util.LinkedHashSet;
import java.util.Set;

public class PerTypeSetStorage<T> implements Cloneable {
	private ObjectTypeBasedStorage<Set<T>> fStorage = new ObjectTypeBasedStorage<Set<T>>();
	
	public Set<T> getSet(int type, boolean create){
		Set<T> set = fStorage.get(type);
		if(set == null && create){
			set = createSet(null);
			fStorage.set(type, set);
		}
		return set;
	}
	
	protected Set<T> createSet(Set<T> set){
		if(set == null)
			return new LinkedHashSet<T>();
		@SuppressWarnings("unchecked")
		Set<T> clone = (Set<T>)((LinkedHashSet<T>)set).clone();
		return clone;
	}

	@Override
	public Object clone(){
		try {
			@SuppressWarnings("unchecked")
			PerTypeSetStorage<T> clone = (PerTypeSetStorage<T>)super.clone();
			@SuppressWarnings("unchecked")
			ObjectTypeBasedStorage<Set<T>> storageClone = (ObjectTypeBasedStorage<Set<T>>)fStorage.clone();
			clone.fStorage = storageClone;
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				Set<T> o = clone.fStorage.get(types[i]); 
				if(o != null){
					clone.fStorage.set(types[i], createSet(o));
				}
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isEmpty(boolean emptySetAsNull){
		if(fStorage.isEmpty())
			return true;
		if(emptySetAsNull){
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				Set<T> o = fStorage.get(types[i]); 
				if(o != null && !o.isEmpty())
					return false;
			}
			return true;
		}
		return false;
	}
}
