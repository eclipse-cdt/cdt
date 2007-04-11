package org.eclipse.rse.internal.useractions.ui.uda.util;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.Vector;

import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeManager;

public class UDAResolvedTypes {
	protected Vector previousTypes = null;

	public UDAResolvedTypes() {
		previousTypes = null;
	}

	protected void addTypesToVector(Vector v, Object[] objElems) {
		for (int i = 0; i < objElems.length; i++) {
			SystemUDTypeElement typeElem = (SystemUDTypeElement) objElems[i];
			String name = typeElem.toString();
			resolveType(name, v, objElems);
		}
	}

	protected String resolveTypes(String types, Vector v, Object[] objElems) {
		int i = types.indexOf("<"); //$NON-NLS-1$
		if (i < 0) return types;
		int j = types.indexOf(">"); //$NON-NLS-1$
		if (i >= j) return types;
		String type = types.substring(i + 1, j);
		String resolvedType = resolveType(type, v, objElems);
		return types.substring(0, i) + " " + resolvedType + " " + resolveTypes(types.substring(j + 1), v, objElems); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String resolveType(String type, Vector v, Object[] objElems) {
		if (previousTypes.contains(type)) {
			return ""; //$NON-NLS-1$
		}
		String resolvedTypes = resolveType(type, v);
		if (resolvedTypes != null) return resolvedTypes;
		for (int i = 0; i < objElems.length; i++) {
			SystemUDTypeElement typeElem = (SystemUDTypeElement) objElems[i];
			if (type.equals(typeElem.toString())) {
				previousTypes.addElement(type);
				resolvedTypes = resolveTypes(typeElem.getTypes(), v, objElems);
				previousTypes.remove(type);
				v.addElement(new UDAFileTypesForName(type, resolvedTypes));
				return resolvedTypes;
			}
		}
		return ""; //$NON-NLS-1$
	}

	protected String resolveType(String type, Vector v) {
		for (int i = 0; i < v.size(); i++) {
			UDAFileTypesForName typesForName = (UDAFileTypesForName) v.elementAt(i);
			if (type.equals(typesForName.getName())) {
				return typesForName.getTypes();
			}
		}
		return null;
	}

	public String getFileTypesForTypeName(String name, int type, SystemUDTypeManager typeMgr) {
		//  ??  Implement for non NFS types???
		return null;
	}
}
