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
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.jface.viewers.Viewer;

public class TypeInfoSorter extends CBrowsingViewerSorter {

    public TypeInfoSorter() {
        super();
    }
    
	public int category (Object element) {
	    if (element instanceof ITypeInfo) {
	        ITypeInfo info = (ITypeInfo)element;
			String name = info.getName();
	        if (info.getCElementType() == ICElement.C_NAMESPACE) {
				if (name.startsWith("__")) { //$NON-NLS-1$
					return NAMESPACES_SYSTEM;
				}
				if (name.charAt(0) == '_') {
					return NAMESPACES_RESERVED;
				}
	            return NAMESPACES;
	        } else {
				if (name.startsWith("__")) { //$NON-NLS-1$
					return CELEMENTS_SYSTEM;
				}
				if (name.charAt(0) == '_') {
					return CELEMENTS_RESERVED;
				}
	        }
			return CELEMENTS;
	    }
	    return super.category(element);
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
	    if (e1 instanceof ITypeInfo || e2 instanceof ITypeInfo) {
			int cat1 = category(e1);
			int cat2 = category(e2);
	
			if (cat1 != cat2)
				return cat1 - cat2;
			
			// cat1 == cat2
	
			if (cat1 == NAMESPACES || cat1 == CELEMENTS || cat1 == CELEMENTS_SYSTEM || cat1 == CELEMENTS_RESERVED) {
				String name1;
				String name2;
				if (e1 instanceof ICElement) {
					name1 = ((ICElement)e1).getElementName();
				} else if (e1 instanceof ITypeInfo) {
				    name1 = ((ITypeInfo)e1).getName();
				} else {
					name1 = e1.toString();
				}
				if (e2 instanceof ICElement) {
					name2 = ((ICElement)e2).getElementName();
				} else if (e2 instanceof ITypeInfo) {
				    name2 = ((ITypeInfo)e2).getName();
				} else {
					name2 = e2.toString();
				}
				return getCollator().compare(name1, name2);
			}
	    }
		return super.compare(viewer, e1, e2);
	}
}
