/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;

public class CFileTypeResolver implements ICFileTypeResolver {
	/**
	 * The association list holds a list of known file associations.
	 */
	protected List fAssocList = new ArrayList();
	
	/**
	 * Create a new resolver.
	 */
	public CFileTypeResolver() {
	}

	/**
	 * @return the file type associations known to this resolver
	 */
	public ICFileTypeAssociation[] getFileTypeAssociations() {
		return (ICFileTypeAssociation[]) fAssocList.toArray(new ICFileTypeAssociation[fAssocList.size()]);
	}

	/**
	 * Get the file type assocated with the specified file name.
	 * Returns ResolverModel.DEFAULT_FILE_TYPE if the file name
	 * could not be resolved to a particular file type.
	 * 
	 * @param fileName name of the file to resolve
	 * 
	 * @return associated file type, or ResolverModel.DEFAULT_FILE_TYPE
	 */
	public ICFileType getFileType(String fileName) {
		for (Iterator iter = fAssocList.iterator(); iter.hasNext();) {
			ICFileTypeAssociation element = (ICFileTypeAssociation) iter.next();
			if (element.matches(fileName)) {
				return element.getType();
			}
		}
		return ResolverModel.DEFAULT_FILE_TYPE;
	}
	
	/**
	 * Add an instance of a file type association to the associations
	 * known to the resolver.
	 * 
	 * Returns true if the instance is added; returns false if the
	 * instance is not added, or if it is already present in the list.
	 * 
	 * @param assoc association to add
	 * 
	 * @return true if the association is added, false otherwise
	 */
	public boolean addAssociation(String pattern, ICFileType type) {
		boolean	added = false;
		ICFileTypeAssociation assoc = new CFileTypeAssociation(pattern, type);
		if (!fAssocList.contains(assoc)) {
			added = fAssocList.add(assoc);
		}
		return added;
	}

	public boolean removeAssociation(ICFileTypeAssociation assoc) {
		return fAssocList.remove(assoc);
	}

}
