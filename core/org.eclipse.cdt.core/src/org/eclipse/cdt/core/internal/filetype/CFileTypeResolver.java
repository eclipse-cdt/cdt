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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeAssociation;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.core.resources.IContainer;

public abstract class CFileTypeResolver implements ICFileTypeResolver {

	// the container of the resolver
	protected IContainer fContainer;

	// The association list holds a list of known file associations.
	protected List fAssocList;

	public CFileTypeResolver(IContainer container) {
		fContainer = container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.filetype.ICFileTypeResolver#getContainer()
	 */
	public IContainer getContainer() {
		return fContainer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.filetype.ICFileTypeResolver#addAssociations(org.eclipse.cdt.core.filetype.ICFileTypeAssociation[])
	 */
	public boolean addAssociations(ICFileTypeAssociation[] assocs) {
		return adjustAssociations(assocs, null, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.filetype.ICFileTypeResolver#getFileType(java.lang.String)
	 */
	public ICFileType getFileType(String fileName) {
		ICFileTypeAssociation[] assocs = getFileTypeAssociations();
		for (int i = 0; i < assocs.length; ++i) {
			ICFileTypeAssociation element = assocs[i];
			if (element.matches(fileName)) {
				return element.getType();
			}
		}
		return ResolverModel.DEFAULT_FILE_TYPE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.filetype.ICFileTypeResolver#removeAssociations(org.eclipse.cdt.core.filetype.ICFileTypeAssociation[])
	 */
	public boolean removeAssociations(ICFileTypeAssociation[] assocs) {
		return adjustAssociations(null, assocs, false);
	}


	public synchronized ICFileTypeAssociation[] getFileTypeAssociations() {
		if (fAssocList == null) {
			loadAssociationList();
		}
		return (ICFileTypeAssociation[]) fAssocList.toArray(new ICFileTypeAssociation[fAssocList.size()]);
	}

	public boolean adjustAssociations(ICFileTypeAssociation[] addAssocs, ICFileTypeAssociation[] delAssocs) {
		return adjustAssociations(addAssocs, delAssocs, true);
	}

	protected synchronized boolean adjustAssociations(ICFileTypeAssociation[] addAssocs,
			ICFileTypeAssociation[] delAssocs, boolean triggerEvent) {
		List addList = new ArrayList();
		List delList = new ArrayList();

		loadAssociationList();

		// check the adds
		if (null != addAssocs) {
			for (int i = 0; i < addAssocs.length; i++) {
				if (!fAssocList.contains(addAssocs[i])) {
					fAssocList.add(addAssocs[i]);
					addList.add(addAssocs[i]);
				}
			}
		}

		// check the removes
		if (null != delAssocs) {
			for (int i = 0; i < delAssocs.length; i++) {
				if (fAssocList.remove(delAssocs[i])) {
					delList.add(delAssocs[i]);
				}
			}
		}

		// Anything change ?
		boolean changed =  !addList.isEmpty() || !delList.isEmpty();
		
		if (changed) {
			Collections.sort(fAssocList, ICFileTypeAssociation.Comparator);
			addAssocs = (ICFileTypeAssociation[]) addList.toArray(new ICFileTypeAssociation[addList.size()]);
			delAssocs = (ICFileTypeAssociation[]) delList.toArray(new ICFileTypeAssociation[delList.size()]);
			doAdjustAssociations(addAssocs, delAssocs, triggerEvent);
		}

		return changed;
	}

	public ICFileTypeResolver createWorkingCopy() {
		final ICFileTypeAssociation[] associations = getFileTypeAssociations();
		CFileTypeResolver copy = new CFileTypeResolver(fContainer) {
			public void doAdjustAssociations(ICFileTypeAssociation[] add, ICFileTypeAssociation[] del,
					boolean triggerEvent) {
				//
			}
			protected ICFileTypeAssociation[] loadAssociations() {
				return associations;
			}
		};
		return copy;
	}

	protected abstract void doAdjustAssociations(ICFileTypeAssociation[] addAssocs, ICFileTypeAssociation[] delAssocs,
			boolean triggerEvent);
	protected abstract ICFileTypeAssociation[] loadAssociations();


	private synchronized List loadAssociationList() {
		if (fAssocList == null) {
			fAssocList = new ArrayList();
			ICFileTypeAssociation[] assocs = loadAssociations();
			if (assocs != null) {
				fAssocList.addAll(Arrays.asList(assocs));
				Collections.sort(fAssocList, ICFileTypeAssociation.Comparator);
			}
		}
		return fAssocList;
	}

}
