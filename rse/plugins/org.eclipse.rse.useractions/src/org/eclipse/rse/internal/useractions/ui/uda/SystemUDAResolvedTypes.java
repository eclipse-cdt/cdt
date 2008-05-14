package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [232106] - NPE when resolving types   
 *******************************************************************************/
import java.util.Vector;

/**
 * 
 */
public class SystemUDAResolvedTypes {
	protected Vector previousTypes = null;
	protected Vector[] UDAFileTypesForNameByDomain;
	protected Vector UDAFileTypesForNameNoDomain;

	//private    char     delimiter = ' ';
	// I have done a bit of work in here, but I really have not analyzed this logic
	// to see what its doing, and if its doing it all correctly. Phil.
	/**
	 * Constructor when using blank as the type delimiter
	 */
	public SystemUDAResolvedTypes() {
		previousTypes = null;
	}

	/* 
	 * Constructor when specifying your own character as the type delimiter
	 *
	 public UDAResolvedTypes(char delimiter)
	 {
	 previousTypes = null;			
	 this.delimiter = delimiter;
	 }*/
	/**
	 * 
	 */
	protected void addTypesToVector(Vector v, Object[] objElems) {
		for (int i = 0; i < objElems.length; i++) {
			SystemUDTypeElement typeElem = (SystemUDTypeElement) objElems[i];
			String name = typeElem.toString();
			resolveType(name, v, objElems);
		}
	}

	/**
	 * 
	 */
	protected String resolveTypes(String types, Vector v, Object[] objElems) {
		int i = types.indexOf("<"); //$NON-NLS-1$
		if (i < 0) return types;
		int j = types.indexOf(">"); //$NON-NLS-1$
		if (i >= j) return types;
		String type = types.substring(i + 1, j);
		String resolvedType = resolveType(type, v, objElems);
		return types.substring(0, i) + " " + resolvedType + " " + resolveTypes(types.substring(j + 1), v, objElems); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * 
	 */
	protected String resolveType(String type, Vector v, Object[] objElems) {
		if (previousTypes.contains(type)) return ""; //$NON-NLS-1$
		String resolvedTypes = resolveType(type, v);
		if (resolvedTypes != null) return resolvedTypes;
		for (int i = 0; i < objElems.length; i++) {
			SystemUDTypeElement typeElem = (SystemUDTypeElement) objElems[i];
			if (type.equals(typeElem.toString())) {
				previousTypes.addElement(type);
				String types = typeElem.getTypes();
				if (types != null) {
					resolvedTypes = resolveTypes(types, v, objElems);
				}
				previousTypes.remove(type);
				v.addElement(new SystemUDAFileTypesForName(type, resolvedTypes));
				return resolvedTypes;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * 
	 */
	protected String resolveType(String type, Vector v) {
		for (int i = 0; i < v.size(); i++) {
			SystemUDAFileTypesForName typesForName = (SystemUDAFileTypesForName) v.elementAt(i);
			if (type.equals(typesForName.getName())) return typesForName.getTypes();
		}
		return null;
	}

	/**
	 * Given a named type, return all the types that this typed name represents,
	 *  concatenated as a single string.
	 * @param name - the named type to be resolved
	 * @param domain - the domain, expressed in its integer form
	 */
	public String getFileTypesForTypeName(String name, int domain, SystemUDTypeManager typeMgr) {
		Vector typesVector = null;
		boolean supportsDomains = typeMgr.getActionSubSystem().supportsDomains();
		boolean needToPopulate = false;
		if (supportsDomains) {
			if (UDAFileTypesForNameByDomain == null) {
				int nbrDomains = typeMgr.getActionSubSystem().getMaximumDomain() + 1;
				UDAFileTypesForNameByDomain = new Vector[nbrDomains];
			}
			typesVector = UDAFileTypesForNameByDomain[domain];
			if (typesVector == null) {
				typesVector = new Vector();
				UDAFileTypesForNameByDomain[domain] = typesVector;
				needToPopulate = true;
			}
		} else {
			typesVector = UDAFileTypesForNameNoDomain;
			if (typesVector == null) {
				typesVector = new Vector();
				UDAFileTypesForNameNoDomain = typesVector;
				needToPopulate = true;
			}
		}
		if (needToPopulate) {
			previousTypes = new Vector(); // what's this for?
			addTypesToVector(typesVector, typeMgr.getTypes(null, domain));
		}
		return resolveType(name, typesVector);
	}
}
