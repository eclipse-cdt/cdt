/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.internal.filetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;

public class CFileTypeResolver implements ICFileTypeResolver {
	// The association list holds a list of known file associations.
	protected List fAssocList = new ArrayList();
	
	public CFileTypeResolver() {
	}

	public ICFileTypeAssociation[] getFileTypeAssociations() {
		return (ICFileTypeAssociation[]) fAssocList.toArray(new ICFileTypeAssociation[fAssocList.size()]);
	}

	public ICFileType getFileType(String fileName) {
		for (Iterator iter = fAssocList.iterator(); iter.hasNext();) {
			ICFileTypeAssociation element = (ICFileTypeAssociation) iter.next();
			if (element.matches(fileName)) {
				return element.getType();
			}
		}
		return ResolverModel.DEFAULT_FILE_TYPE;
	}
	
	//TODO: the add/remove methods do not generate change notifications...
	// They really should be part of an IFileTypeResolverWorkingCopy interface
	// For now, just be careful with them...
	
	public boolean addAssociation(ICFileTypeAssociation assoc) {
		return addAssociations(new ICFileTypeAssociation[] { assoc } );
	}
	
	public boolean addAssociations(ICFileTypeAssociation[] assocs) {
		return doAddAssociations(assocs);
	}
	
	public boolean removeAssociation(ICFileTypeAssociation assoc) {
		return removeAssociations(new ICFileTypeAssociation[] { assoc } );
	}
	
	public boolean removeAssociations(ICFileTypeAssociation[] assocs) {
		return doRemoveAssociations(assocs);
	}

	public boolean adjustAssociations(ICFileTypeAssociation[] add, ICFileTypeAssociation[] remove) {
		boolean added	= doAddAssociations(add);
		boolean removed	= doRemoveAssociations(remove);
		return (added || removed);
	}
	
	public ICFileTypeResolver createWorkingCopy() {
		CFileTypeResolver copy = new CFileTypeResolver();
		copy.fAssocList.addAll(fAssocList);
		return copy;
	}
	
	protected boolean doAddAssociations(ICFileTypeAssociation[] assocs) {
		boolean	added = false;
		if (null != assocs) {
			for (int i = 0; i < assocs.length; i++) {
				if (!fAssocList.contains(assocs[i])) {
					if (fAssocList.add(assocs[i])) {
						added = true;
					}
				}
			}
		}
		if (added) {
			Collections.sort(fAssocList, ICFileTypeAssociation.Comparator);
		}
		return added;
	}
	
	public boolean doRemoveAssociations(ICFileTypeAssociation[] assocs) {
		boolean removed = false;
		if (null != assocs) {
			for (int i = 0; i < assocs.length; i++) {
				if (fAssocList.remove(assocs[i])) {
					removed = true;
				}
			}
		}
		if (removed) {
			Collections.sort(fAssocList, ICFileTypeAssociation.Comparator);
		}
		return removed;
	}

	private static boolean isDebugging() {
		return ResolverModel.VERBOSE;
	}
	
	private static void debugLog(String message) {
		System.out.println("CDT Resolver: " + message);
	}
}
