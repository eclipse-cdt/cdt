/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 *	A sorter to sort the file and the folders in the C viewer in the following order:
 * 	1 Project
 * 	2 BinaryContainer
 *  3 ArchiveContainer
 *  4 LibraryContainer
 *  5 IncludeContainer
 *  6 Source roots
 *  5 C Elements
 *  6 non C Elements
 */
public class TypeInfoSorter extends ViewerSorter { 

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ITypeInfo) {
			return compare((ITypeInfo)e1, e2);
		} else if (e1 instanceof ICElement) {
			return compare((ICElement)e1, e2);
		}
		return 0;
//		return getCollator().compare(name1, name2);		
	}
	
	int compare(ITypeInfo t1, Object o2) {
		if (o2 instanceof ITypeInfo) {
			ITypeInfo t2 = (ITypeInfo)o2;
			return t1.compareTo(t2);
		} else if (o2 instanceof ICElement) {
			ICElement e2 = (ICElement)o2;
			return getCollator().compare(t1.getName(), e2.getElementName());		
		}
		return 0;
	}
	
	int compare(ICElement e1, Object o2) {
		if (o2 instanceof ITypeInfo) {
			ITypeInfo t2 = (ITypeInfo)o2;
			return getCollator().compare(e1.getElementName(), t2.getName());		
		} else if (o2 instanceof ICElement) {
			ICElement e2 = (ICElement)o2;
			return getCollator().compare(e1.getElementName(), e2.getElementName());		
		}
		return 0;
	}
}
