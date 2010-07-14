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

public class PerTypeSetStorage implements Cloneable {
	private ObjectTypeBasedStorage fStorage = new ObjectTypeBasedStorage();
	
	public Set getSet(int type, boolean create){
		Set set = (Set)fStorage.get(type);
		if(set == null && create){
			set = createSet(null);
			fStorage.set(type, set);
		}
		return set;
	}
	
	protected Set createSet(Set set){
		if(set == null)
			return new LinkedHashSet();
		return (Set)((LinkedHashSet)set).clone();
	}

	@Override
	public Object clone(){
		try {
			PerTypeSetStorage clone = (PerTypeSetStorage)super.clone();
			clone.fStorage = (ObjectTypeBasedStorage)fStorage.clone();
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				Object o = clone.fStorage.get(types[i]); 
				if(o != null){
					clone.fStorage.set(types[i], createSet((Set)o));
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
				Object o = fStorage.get(types[i]); 
				if(o != null && !((Set)o).isEmpty())
					return false;
			}
			return true;
		}
		return false;
	}
}
