/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
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
import java.util.Set;

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.MatchObjectElement;

public class ObjectSet implements IObjectSet {
	private int fObjectType;
	private Set<IRealBuildObjectAssociation> fObjectSet;

	public ObjectSet(int objectType, Set<IRealBuildObjectAssociation> objectSet){
		fObjectType = objectType;
		fObjectSet = objectSet;
	}

	@Override
	public final int getObjectType() {
		return fObjectType;
	}

	@Override
	public IRealBuildObjectAssociation[] getRealBuildObjects() {
		return fObjectSet.toArray(new IRealBuildObjectAssociation[fObjectSet.size()]);
	}

	@Override
	public Collection<IRealBuildObjectAssociation> getRealBuildObjects(Collection<IRealBuildObjectAssociation> set) {
		if(set == null)
			set = new HashSet<IRealBuildObjectAssociation>();

		set.addAll(fObjectSet);

		return set;
	}

	@Override
	public boolean matchesObject(IRealBuildObjectAssociation obj) {
		return fObjectSet.contains(obj.getRealBuildObject());
	}

	@Override
	public boolean retainMatches(Collection<IRealBuildObjectAssociation> collection) {
		return collection.retainAll(fObjectSet);
	}

	@Override
	public int getNumObjects() {
		return fObjectSet.size();
	}

	@SuppressWarnings("nls")
	@Override
	public String toString(){
		StringBuilder buf = new StringBuilder();
		buf.append(MatchObjectElement.TypeToStringAssociation.getAssociation(fObjectType).getString());
		buf.append("[");
		boolean isFirst = true;
		for (IRealBuildObjectAssociation obj : fObjectSet) {
			if(isFirst){
				buf.append(", ");
				isFirst = false;
			}
			buf.append(obj.getId());
		}
		buf.append("]");

		return buf.toString();
	}
}
