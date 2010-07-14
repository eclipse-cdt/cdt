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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.MatchObjectElement;

public class ObjectSet implements IObjectSet {
	private int fObjectType;
	private Set fObjectSet;
	
	public ObjectSet(int objectType, Set objectSet){
		fObjectType = objectType;
		fObjectSet = objectSet;
	}
	
	public final int getObjectType() {
		return fObjectType;
	}

	public IRealBuildObjectAssociation[] getRealBuildObjects() {
		return (IRealBuildObjectAssociation[])fObjectSet.toArray(new IRealBuildObjectAssociation[fObjectSet.size()]);
	}

	public Collection getRealBuildObjects(Collection set) {
		if(set == null)
			set = new HashSet();
		
		set.addAll(fObjectSet);
			
		return set;
	}

	public boolean matchesObject(IRealBuildObjectAssociation obj) {
		return fObjectSet.contains(obj.getRealBuildObject());
	}

	public boolean retainMatches(Collection collection) {
		return collection.retainAll(fObjectSet);
	}

	public int getNumObjects() {
		return fObjectSet.size();
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(MatchObjectElement.TypeToStringAssociation.getAssociation(fObjectType).getString());
		buf.append("["); //$NON-NLS-1$
		boolean isFirst = true;
		for(Iterator iter = fObjectSet.iterator(); iter.hasNext(); ){
			if(isFirst){
				buf.append(", "); //$NON-NLS-1$
				isFirst = false;
			}
			buf.append(((IRealBuildObjectAssociation)iter.next()).getId());
		}
		buf.append("]"); //$NON-NLS-1$
		
		return buf.toString();
	}
}
