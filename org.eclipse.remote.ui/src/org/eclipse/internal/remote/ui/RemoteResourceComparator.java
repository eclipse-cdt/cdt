/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.internal.remote.ui;


import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class RemoteResourceComparator extends ViewerComparator {
	
	private boolean ascending = true;
	
	public RemoteResourceComparator() {
		super();
	}
	
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	
	public boolean isAscending() {
		return ascending;
	}

	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 instanceof IFileInfo && o2 instanceof IFileInfo) {
			int compareResult = ((IFileInfo)o1).getName().compareToIgnoreCase(((IFileInfo)o2).getName());;
			return ascending ? compareResult : -compareResult;
		}
		
		return super.compare(viewer, o1, o2);
	}
	
}

