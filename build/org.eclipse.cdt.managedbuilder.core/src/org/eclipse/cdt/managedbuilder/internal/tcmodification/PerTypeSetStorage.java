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

import java.util.HashSet;
import java.util.Set;

public class PerTypeSetStorage {
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
			return new HashSet();
		return (Set)((HashSet)set).clone();
	}

}
