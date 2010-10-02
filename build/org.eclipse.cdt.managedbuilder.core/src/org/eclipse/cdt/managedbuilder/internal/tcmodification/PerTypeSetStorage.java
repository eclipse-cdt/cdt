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

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;

public class PerTypeSetStorage implements Cloneable {
	private ObjectTypeBasedStorage fStorage = new ObjectTypeBasedStorage();
	
	public Set getSet(int type, boolean create){
		Set<IRealBuildObjectAssociation> set = (Set<IRealBuildObjectAssociation>)fStorage.get(type);
		if(set == null && create){
			set = createSet(null);
			fStorage.set(type, set);
		}
		return set;
	}
	
	protected Set<IRealBuildObjectAssociation> createSet(Set<IRealBuildObjectAssociation> set){
		if(set == null)
			return new LinkedHashSet<IRealBuildObjectAssociation>();
		@SuppressWarnings("unchecked")
		Set<IRealBuildObjectAssociation> clone = (Set<IRealBuildObjectAssociation>)((LinkedHashSet<IRealBuildObjectAssociation>)set).clone();
		return clone;
	}

	@Override
	public Object clone(){
		try {
			PerTypeSetStorage clone = (PerTypeSetStorage)super.clone();
			clone.fStorage = (ObjectTypeBasedStorage)fStorage.clone();
			int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
			for(int i = 0; i < types.length; i++){
				@SuppressWarnings("unchecked")
				Set<IRealBuildObjectAssociation> o = (Set<IRealBuildObjectAssociation>) clone.fStorage.get(types[i]); 
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
				@SuppressWarnings("unchecked")
				Set<IRealBuildObjectAssociation> o = (Set<IRealBuildObjectAssociation>) fStorage.get(types[i]); 
				if(o != null && !((Set<IRealBuildObjectAssociation>)o).isEmpty())
					return false;
			}
			return true;
		}
		return false;
	}
}
